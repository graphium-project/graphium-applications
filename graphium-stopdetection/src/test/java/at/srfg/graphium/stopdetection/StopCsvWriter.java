package at.srfg.graphium.stopdetection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import at.srfg.graphium.stopdetection.model.AbstractPlace;
import at.srfg.graphium.stopdetection.model.impl.DetectedPlace;
import at.srfg.graphium.stopdetection.model.impl.Stay;

public class StopCsvWriter {
	protected Logger log = Logger.getLogger(this.getClass().getName());
	
	Map<Long, List<GroundTruthStop>> groundTruthStops = null;


	private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void write(FileWriter travelFileWriter, List<DetectedPlace> places) throws IOException {
		readGroundTruthStops();
		
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter travelPrinter = new CSVPrinter(travelFileWriter, csvFileFormat);
		writeHeader(travelPrinter);
		
		writeStays(travelPrinter, places);
	}

	private void writeHeader(CSVPrinter stopPrinter) throws IOException {
		
		final Object[] FILE_HEADER = { "fid", "track_id", "placeId", "placeGeom", "stayId", "stayGeom", "startTime", "endTime",
				"startIndex", "endIndex", "stopDistance", "minimumSpeed", "summedAngles",
				"significantDirectionChanges", "simpleGeom", "distanceSlow", "numberOfStops",
				"category", "categoryRating"
		};
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
	private <T extends AbstractPlace> void writeStays(CSVPrinter csvFilePrinter, List<T> places) throws IOException {
		int fid = 0;

		for (T place : places) {
			for (Stay stay : place.getStays()) {
				int iStay = 0;
			    
				List<Object> record = new ArrayList<Object>();
				record.add(fid++);
				record.add(place.getTrack().getId());
				record.add(place.getPlaceId());
				record.add(place.getLocation().toText());
				record.add(iStay++);
				record.add(stay.getPoint().toText());
				record.add(dateTimeFormat.format(stay.getTimeEntry()));
				record.add(dateTimeFormat.format(stay.getTimeExit()));
				record.add(stay.getTrackPointIndexEntry());
				record.add(stay.getTrackPointIndexExit());
				record.add(stay.getTrackLength());
				record.add(StayPropertyUtils.getMinimumSpeed(stay, place.getTrack()));
				record.add(StayPropertyUtils.getSummedAngles(stay, place.getTrack()));
				record.add(StayPropertyUtils.getNumberOfSignificantDirectionChanges(stay, place.getTrack()));
				record.add(StayPropertyUtils.getSimplifiedTrackGeom(stay, place.getTrack()).toText());
				record.add(StayPropertyUtils.getDistanceSlow(stay, place.getTrack()));
				record.add(StayPropertyUtils.getNumberOfStops(stay, place.getTrack()));
				
				GroundTruthStop stop = findGroundTruthStop(place, stay);
				if (stop != null) {
					record.add(stop.category);
					record.add(stop.rating);
				} else {
					record.add("Nicht klassifiziert");
					record.add(0);
				}
				csvFilePrinter.printRecord(record);
			}
		}
	}
	
	private GroundTruthStop findGroundTruthStop(AbstractPlace place, Stay stay) {
		List<GroundTruthStop> stops = groundTruthStops.get(place.getTrack().getId());
		if (stops != null) {
			for (GroundTruthStop stop : stops) {
				if (overlaps(stop, stay)) {
					return stop;
				}
			}
		}
		return null;
	}
	
	private boolean overlaps(GroundTruthStop stop, Stay stay) {
		if (!stop.startTime.before(stay.getTimeEntry()) && !stop.startTime.after(stay.getTimeExit()) ||
				!stop.endTime.before(stay.getTimeEntry()) && !stop.endTime.after(stay.getTimeExit()) ||
				!stay.getTimeEntry().before(stop.startTime) && !stay.getTimeEntry().after(stop.endTime) ||
				!stay.getTimeExit().before(stop.startTime) && !stay.getTimeExit().after(stop.endTime)
		) {
			return true;
		}
		return false;
	}
	
	private void readGroundTruthStops() {
		groundTruthStops = new HashMap<Long, List<GroundTruthStop>>();
		try (Reader in = new FileReader(new File("D:\\projects\\gitl-patterninterpret\\stopdetection\\ground_truth_stops.csv"))) {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : records) {
				GroundTruthStop stop = new GroundTruthStop();
				stop.trackId = Long.valueOf(record.get("track_id"));
				stop.startTime = dateTimeFormat.parse(record.get("startTime"));
				stop.endTime = dateTimeFormat.parse(record.get("endTime"));
				stop.category = record.get("category");
				stop.rating = Integer.valueOf(record.get("categoryRating"));
				
				List<GroundTruthStop> stops = groundTruthStops.get(stop.trackId);
				if (stops == null) {
					stops = new ArrayList<GroundTruthStop>();
					groundTruthStops.put(stop.trackId, stops);
				}
				stops.add(stop);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			log.warn("Skip remaining track IDs");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private class GroundTruthStop {
		long trackId;
		Date startTime;
		Date endTime;
		String category;
		int rating;
	}
}
