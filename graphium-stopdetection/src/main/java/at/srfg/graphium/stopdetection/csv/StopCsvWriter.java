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
package at.srfg.graphium.stopdetection.csv;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;

import at.srfg.graphium.stopdetection.model.AbstractPlace;
import at.srfg.graphium.stopdetection.model.impl.Stay;
import at.srfg.graphium.stopdetection.model.impl.StopCategory;

public class StopCsvWriter<T extends AbstractPlace> {
	protected Logger log = Logger.getLogger(this.getClass().getName());

	private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void write(Writer travelFileWriter, List<T> places) throws IOException {
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator("\n");
		CSVPrinter travelPrinter = new CSVPrinter(travelFileWriter, csvFileFormat);
		writeHeader(travelPrinter);
		
		writeStays(travelPrinter, places);
	}

	private void writeHeader(CSVPrinter stopPrinter) throws IOException {
		
		final Object[] FILE_HEADER = { "track_id", "placeId", "placeGeom", "stayId", "stayGeom", "startTime",
				"endTime", "duration", "startIndex", "endIndex", "stopDistance", "avgSpeed", "minimumSpeed",
				"avgSamplingInterval", "summedAngles", "significantDirectionChanges", "simpleGeom", "distanceSlow",
				"numberOfStops", "distanceToNextHighLevelRoad", "frcOfNextHighLevelRoad", "fowOfNextHighLevelRoad",
				"idOfNextHighLevelRoad", "distanceToNextLowLevelRoad", "frcOfNextLowLevelRoad", "fowOfNextLowLevelRoad",
				"idOfNextLowLevelRoad", "categoryTarget", };
		stopPrinter.printRecord(FILE_HEADER);
	}
	
	/**
	 * 1. Writes the stays to each place with their corresponding coordinates and stats as well as the dPs coordinates.<br />
	 * 2. Write the places with each of the stay time information and related stats. Not the coordinates of each visit.
	 * 
	 * @param places List containing the places
	 * @param file name prefix
	 * @return number of written stays
	 * @throws IOException 
	 */
	private void writeStays(CSVPrinter csvFilePrinter, List<T> places) throws IOException {
//		int fid = 0;

		for (T place : places) {
			for (Stay stay : place.getStays()) {
				int iStay = 0;
			    
				List<Object> record = new ArrayList<Object>();
//				record.add(fid++);
				record.add(place.getTrack().getId());
				record.add(place.getPlaceId());
				record.add(place.getLocation().toText());
				record.add(iStay++);
				record.add(stay.getPoint().toText());
				record.add(dateTimeFormat.format(stay.getTimeEntry()));
				record.add(dateTimeFormat.format(stay.getTimeExit()));
				record.add(((float)(stay.getTimeExit().getTime() - stay.getTimeEntry().getTime())) / (1000 * 60)); // duration
				record.add(stay.getTrackPointIndexEntry());
				record.add(stay.getTrackPointIndexExit());
				record.add(stay.getTrackLength()); // stop distance
				record.add((stay.getTrackLength() / 1000)
						/ (((float)(stay.getTimeExit().getTime() - stay.getTimeEntry().getTime())) / (1000 * 60 * 60))); // avgSpeed
				record.add(StayPropertyUtils.getMinimumSpeed(stay, place.getTrack()));
				record.add(((stay.getTimeExit().getTime() - stay.getTimeEntry().getTime()) / (1000))
						/ (stay.getTrackPointIndexExit() - stay.getTrackPointIndexEntry())); // avgSamplingInterval
				record.add(StayPropertyUtils.getSummedAngles(stay, place.getTrack()));
				record.add(StayPropertyUtils.getNumberOfSignificantDirectionChanges(stay, place.getTrack()));
				record.add(StayPropertyUtils.getSimplifiedTrackGeom(stay, place.getTrack()).toText());
				record.add(StayPropertyUtils.getDistanceSlow(stay, place.getTrack()));
				record.add(StayPropertyUtils.getNumberOfStops(stay, place.getTrack()));
				
				record.add((stay.getDistanceToHighLevelRoad() != -1.0) ? stay.getDistanceToHighLevelRoad() : "");
				record.add((stay.getNextHighLevelRoadSegment() != null) ? stay.getNextHighLevelRoadSegment().getFrc().getValue() : "");
				record.add((stay.getNextHighLevelRoadSegment() != null) ? stay.getNextHighLevelRoadSegment().getFormOfWay().getValue() : "");
				record.add((stay.getNextHighLevelRoadSegment() != null) ? stay.getNextHighLevelRoadSegment().getId() : "");
				record.add((stay.getDistanceToLowLevelRoad() != -1.0) ? stay.getDistanceToLowLevelRoad() : "");
				record.add((stay.getNextLowLevelRoadSegment() != null) ? stay.getNextLowLevelRoadSegment().getFrc().getValue() : "");
				record.add((stay.getNextLowLevelRoadSegment() != null) ? stay.getNextLowLevelRoadSegment().getFormOfWay().getValue() : "");
				record.add((stay.getNextLowLevelRoadSegment() != null) ? stay.getNextLowLevelRoadSegment().getId() : "");
				record.add((stay.getDistanceToHighLevelRoad() > 10) ? StopCategory.NON_TRAFFIC_RELATED_STOP : StopCategory.TRAFFIC_RELATED_STOP);
				
				csvFilePrinter.printRecord(record);
			}
		}
	}
}
