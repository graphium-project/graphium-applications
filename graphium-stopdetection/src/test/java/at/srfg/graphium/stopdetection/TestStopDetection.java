package at.srfg.graphium.stopdetection;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
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

import at.srfg.graphium.geomutils.GeometryUtils;
import at.srfg.graphium.mapmatching.model.ITrack;
import at.srfg.graphium.mapmatching.model.ITrackPoint;
import at.srfg.graphium.mapmatching.model.impl.TrackImpl;
import at.srfg.graphium.mapmatching.model.impl.TrackMetadataImpl;
import at.srfg.graphium.mapmatching.model.impl.TrackPointImpl;
import at.srfg.graphium.stopdetection.cluster.IAnalysisMethod;
import at.srfg.graphium.stopdetection.model.impl.DetectedPlace;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/application-context-fcd-stopdetection-test.xml" })
public class TestStopDetection {
	protected Logger log = Logger.getLogger(this.getClass().getName());

	@Resource(name="analysisYe")
	private IAnalysisMethod analysisMethod;
	
	private String outputDir = "D:\\tmp\\";

    private boolean writeResultToCSVs = true;

	@Test
	public void testStopDetection() throws IOException {
		
		log.info("Detecting stops...");
		
		initialize();

		List<DetectedPlace> places = new ArrayList<DetectedPlace>();
		
		File gpxFile = new File("D:\\daten\\digibus\\accuracy\\tracks\\770_without_timezone.gpx");
		
		log.info("TrackID: " + gpxFile);
		doBeforeProcessing(null);
		for (Track gpxTrack : readGpxTracks(gpxFile)) {
			ITrack track = convertTrackToGraphium(gpxTrack);
			processTrack(track, true);
			doAfterProcessing(track);
			places.addAll(analysisMethod.getDetectedPlaces());
		}
		
		writeResults(null, places, "20171114" + analysisMethod.getClass().getName());
		
		log.info("Finished!");
	}

	private List<Track> readGpxTracks(File gpxFile) throws IOException {
		List<Track> tracks = new ArrayList<Track>();
		
		Iterator<Track> it = GPX.read(new FileInputStream(gpxFile)).tracks().iterator();
		while (it.hasNext()) {
			Track track = it.next();
			if (track != null) {
				tracks.add(track);
			}
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
				graphiumTrackPoint.setTimestamp(new Date(p.getTime().get().toEpochSecond()));
				track.getTrackPoints().add(graphiumTrackPoint);
			});
		});
		track.calculateTrackPointValues();
		track.calculateMetaData(true);
		return track;
	}

    private void writeResults(List<ITrack> tracks, List<DetectedPlace> places, String filename) throws IOException {
		log.info("Test done, got " + places.size() + " places calculated results");

        if (writeResultToCSVs) {
        	File stopFileCsv = new File(outputDir + "/stops_" + filename + ".csv");
            writeCSV(stopFileCsv, places);
        }
	}
	
	private void writeCSV(File stopFile, List<DetectedPlace> places) throws IOException {
		log.info("Write CSV ...");
		
		StopCsvWriter writerCsv = new StopCsvWriter();
		try (FileWriter travelFileWriter = new FileWriter(stopFile)) {
			writerCsv.write(travelFileWriter, places);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void initialize() {
//		dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
