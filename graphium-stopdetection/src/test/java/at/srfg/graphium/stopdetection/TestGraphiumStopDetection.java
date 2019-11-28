package at.srfg.graphium.stopdetection;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;

import at.srfg.graphium.core.exception.GraphNotExistsException;
import at.srfg.graphium.geomutils.GeometryUtils;
import at.srfg.graphium.mapmatching.model.IMatchedBranch;
import at.srfg.graphium.mapmatching.model.IMatchedWaySegment;
import at.srfg.graphium.mapmatching.model.ITrack;
import at.srfg.graphium.mapmatching.model.ITrackPoint;
import at.srfg.graphium.mapmatching.model.impl.TrackImpl;
import at.srfg.graphium.mapmatching.model.impl.TrackMetadataImpl;
import at.srfg.graphium.mapmatching.model.impl.TrackPointImpl;
import at.srfg.graphium.stopdetection.cluster.IAnalysisMethod;
import at.srfg.graphium.stopdetection.model.impl.DetectedPlace;
import at.srfg.graphium.stopdetection.model.impl.Stay;
import at.srfg.graphium.stopdetection.service.MapMatchingService;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/application-context-graphium-stopdetection-test.xml",
		"classpath:/application-context-graphium-neo4j-test.xml",
		"classpath:/application-context-graphium-core.xml",
		"classpath:/application-context-graphium-neo4j-persistence.xml",
		"classpath:/application-context-graphium-neo4j-aliasing.xml",
		"classpath:/application-context-graphium-mapmatching.xml",
		"classpath:/application-context-graphium-mapmatching-neo4j.xml",
		"classpath:/application-context-graphium-routing-neo4j.xml",
})
public class TestGraphiumStopDetection {
	protected Logger log = Logger.getLogger(this.getClass().getName());

	@Resource(name="analysisYe")
	private IAnalysisMethod analysisMethod;

	@Resource(name="mapMatchingService")
	private MapMatchingService mapMatchingService;
	private String graphNameHighLevel = "gip_at_frc_0_4";
	private String graphNameLowLevel = "gip_at_frc_0_8";
	
	private String outputDir = "D:\\tmp\\";

	@Test
	public void testStopDetection() throws IOException, GraphNotExistsException {
		
		graphNameHighLevel = "gip_at_frc_0_4"; //TODO input parameter
		graphNameLowLevel = "gip_at_frc_0_8"; //TODO input parameter

		List<DetectedPlace> places = new ArrayList<DetectedPlace>();
		
		File gpxFile = new File("D:\\daten\\fcd\\2019-02_DelayCalc\\tracks\\20190508\\38261768withouttimezone.gpx"); //TODO input parameter

		log.info("Processing tracks ...");
		log.info("TrackID: " + gpxFile);
		doBeforeProcessing(null);
		for (Track gpxTrack : readGpxTracks(gpxFile)) {
			ITrack track = convertTrackToGraphium(gpxTrack);
			processTrack(track, true);
			doAfterProcessing(track);
			
			//Map matching
			List<IMatchedBranch> matchedTrackHighLevel = mapMatchingService.matchTrack(track, graphNameHighLevel);
			List<IMatchedBranch> matchedTrackLowLevel = mapMatchingService.matchTrack(track, graphNameLowLevel);
			
			for (DetectedPlace place : analysisMethod.getDetectedPlaces()) {
				for (Stay stay : place.getStays()) {
					int index = (stay.getTrackPointIndexExit() + stay.getTrackPointIndexEntry()) / 2;
					for (IMatchedBranch branch : matchedTrackHighLevel) {
						for (IMatchedWaySegment segment : branch.getMatchedWaySegments()) {
							if (segment.getStartPointIndex() <= index && index < segment.getEndPointIndex()) {
								stay.setDistanceToHighLevelRoad(segment.getDistance(index));
								stay.setFrcOfNextHighLevelRoad(segment.getSegment().getFrc());
								stay.setFowOfNextHighLevelRoad(segment.getSegment().getFormOfWay());
								break;
								
							} else if (index < segment.getStartPointIndex()) {
								// Could not find segment -> cancel search
								break;
							}
						}
					}
					for (IMatchedBranch branch : matchedTrackLowLevel) {
						for (IMatchedWaySegment segment : branch.getMatchedWaySegments()) {
							if (segment.getStartPointIndex() <= index && index < segment.getEndPointIndex()) {
								stay.setDistanceToLowLevelRoad(segment.getDistance(index));
								stay.setFrcOfNextLowLevelRoad(segment.getSegment().getFrc());
								stay.setFowOfNextLowLevelRoad(segment.getSegment().getFormOfWay());
								break;
								
							} else if (index < segment.getStartPointIndex()) {
								// Could not find segment -> cancel search
								break;
							}
						}
					}
				}
				
				places.add(place);
			}
		}
		
		writeResults(null, places, "20171114" + analysisMethod.getClass().getSimpleName());
		
		log.info("Finished!");
	}

	private List<Track> readGpxTracks(File gpxFile) throws IOException {
		List<Track> tracks = new ArrayList<Track>();
		
		try {
			Iterator<Track> it = GPX.read(new FileInputStream(gpxFile)).tracks().iterator();
			while (it.hasNext()) {
				Track track = it.next();
				if (track != null) {
					tracks.add(track);
				}
			}
		} catch (InvalidObjectException e) {
			log.error(e.getLocalizedMessage());
			Assert.isTrue(false);
		}
		return tracks;
	}
	
	private ITrack convertTrackToGraphium(Track gpxTrack) {
		ITrack track = new TrackImpl();
		track.setId(gpxTrack.getNumber().get().longValue());
		track.setTrackPoints(new ArrayList<ITrackPoint>());
		track.setMetadata(new TrackMetadataImpl());
		gpxTrack.getSegments().forEach(sg -> {
			sg.getPoints().forEach(p -> {
				ITrackPoint graphiumTrackPoint = new TrackPointImpl();
				graphiumTrackPoint.setPoint(GeometryUtils.createPoint(new Coordinate(p.getLongitude().doubleValue(), p.getLatitude().doubleValue()), 4326));
				graphiumTrackPoint.setTimestamp(new Date(p.getTime().get().toEpochSecond() * 1000 + (long)(p.getTime().get().getNano() / 1000000)));
				track.getTrackPoints().add(graphiumTrackPoint);
			});
		});
		track.calculateTrackPointValues();
		track.calculateMetaData(true);
		return track;
	}

    private void writeResults(List<ITrack> tracks, List<DetectedPlace> places, String filename) throws IOException {
		log.info("Test done, got " + places.size() + " places calculated results");

    	File stopFileCsv = new File(outputDir + "/stops_" + filename + ".csv");
		
    	StopCsvWriter<DetectedPlace> writerCsv = new StopCsvWriter<DetectedPlace>();
		try (FileWriter travelFileWriter = new FileWriter(stopFileCsv)) {
			writerCsv.write(travelFileWriter, places);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initializes analysis methods
	 * @param params
	 */
	protected void doBeforeProcessing(Object params[]) {
		this.analysisMethod.initialize(params);
	}

	@SuppressWarnings(value = { "unused" })
	private void processTracks(List<ITrack> parsedTracks, boolean preprocessTrackPoints) {
		for (ITrack track : parsedTracks) {
			processTrack(track, preprocessTrackPoints);
		}
	}
	
	private void processTrack(ITrack track, boolean preprocessTrackPoints) {
		if (track.getTrackPoints().size() == 0) {
			return;
		}
		
//		/** Pre-process track */
//		if (preprocessTrackPoints) {
//			TrackPreprocessingFilter.preprocessTrack(track, true);
//		}
		
		/** Process track... */
		log.info("Processing ... " + track.getMetadata().getStartDate() + " (" + track.getTrackPoints().size() + " Trackpoints) > " + analysisMethod);
		track.calculateTrackPointValues();
		track.calculateMetaData(false);
		analysisMethod.processTrack(track);
	}
	
	protected void doAfterProcessing(ITrack track) {
		log.info("After-processing ... " + analysisMethod);
		analysisMethod.doAfterProcessing();
	}

	public static Comparator<at.srfg.graphium.mapmatching.model.ITrack> getTrackStartDateComparator() {
		return new Comparator<ITrack>() {
			public int compare(ITrack t1, ITrack t2) {
				if (t1.getMetadata().getStartDate().equals(t2.getMetadata().getStartDate())) {
					return 0;
				} else if (t1.getMetadata().getStartDate().before(t2.getMetadata().getStartDate())) {
					return -1;
				} else {
					return 1;
				}
			}
		};
	}
}
