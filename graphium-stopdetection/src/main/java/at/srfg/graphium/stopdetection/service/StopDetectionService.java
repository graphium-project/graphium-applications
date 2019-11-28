/**
 * Copyright © 2019 Salzburg Research Forschungsgesellschaft (graphium@salzburgresearch.at)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.srfg.graphium.stopdetection.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

import at.srfg.graphium.geomutils.GeometryUtils;
import at.srfg.graphium.mapmatching.model.ITrack;
import at.srfg.graphium.mapmatching.model.ITrackPoint;
import at.srfg.graphium.mapmatching.model.impl.TrackImpl;
import at.srfg.graphium.mapmatching.model.impl.TrackMetadataImpl;
import at.srfg.graphium.mapmatching.model.impl.TrackPointImpl;
import at.srfg.graphium.stopdetection.cluster.IAnalysisMethod;
import at.srfg.graphium.stopdetection.csv.StopCsvWriter;
import at.srfg.graphium.stopdetection.model.impl.DetectedPlace;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;

/**
 * @author mwimmer
 *
 */
public class StopDetectionService {
	
	private static Logger log = LoggerFactory.getLogger(StopDetectionService.class);
	
	private IAnalysisMethod analysisMethod;
	
	public String detectStops(InputStream gpxFile) throws IOException {
		log.info("Detecting stops...");
		
		List<DetectedPlace> places = new ArrayList<DetectedPlace>();
		
		doBeforeProcessing(null);
		for (Track gpxTrack : readGpxTracks(gpxFile)) {
			ITrack track = convertTrackToGraphium(gpxTrack);
			processTrack(track, true);
			doAfterProcessing(track);
			places.addAll(analysisMethod.getDetectedPlaces());
		}

		StringWriter csvWriter = new StringWriter();
		StopCsvWriter stopCsvWriter = new StopCsvWriter();
		stopCsvWriter.write(csvWriter, places);
//		writeResults(null, places, "20171114" + analysisMethod.getClass().getName());
		
		log.info("Detecting stops finished!");
		
		return csvWriter.toString();

	}

	private List<Track> readGpxTracks(InputStream gpxFile) throws IOException {
		List<Track> tracks = new ArrayList<Track>();
		
		Iterator<Track> it = GPX.read(gpxFile).tracks().iterator();
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
				graphiumTrackPoint.setTimestamp(new Date(p.getTime().get().toEpochSecond() * 1000 + (long)(p.getTime().get().getNano() / 1000000)));
				track.getTrackPoints().add(graphiumTrackPoint);
			});
		});
		track.calculateTrackPointValues();
		track.calculateMetaData(true);
		return track;
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

	public IAnalysisMethod getAnalysisMethod() {
		return analysisMethod;
	}

	public void setAnalysisMethod(IAnalysisMethod analysisMethod) {
		this.analysisMethod = analysisMethod;
	}

}
