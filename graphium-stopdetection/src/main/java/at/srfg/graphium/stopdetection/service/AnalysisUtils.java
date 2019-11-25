package at.srfg.graphium.stopdetection.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import at.srfg.graphium.geomutils.GeometryUtils;
import at.srfg.graphium.mapmatching.model.ITrackPoint;

public abstract class AnalysisUtils {
	private static Logger log = Logger.getLogger(AnalysisUtils.class);
	
	/**
	 * Calculation of the centroid of a list of points
	 * @param trackPointList List of trackPoints
	 * @return Centroid of the list
	 */
	public static Point calculateCentroid(List<ITrackPoint> trackPointList){
//		long runtimeStart = System.currentTimeMillis();
		double sumX = 0.0;
		double sumY = 0.0;
		for (ITrackPoint trackPoint : trackPointList) {
			sumX = sumX + trackPoint.getPoint().getX();
			sumY = sumY + trackPoint.getPoint().getY();	
		}
		double averageLon = sumX / trackPointList.size();
		double averageLat = sumY / trackPointList.size();
		
//		log.info("runtime " + (System.currentTimeMillis() - runtimeStart));
		return GeometryUtils.createPoint2D(averageLon, averageLat, 4326);
	}
	
	/**
	 * Calculates the centroid of the segments between the track points and uses the
	 * segment duration as weight
	 * 
	 * @param trackPointList
	 * @return centroid
	 */
	public static Point calculateWeightedCentroid(List<ITrackPoint> trackPointList) {
		double sumX = 0.0;
		double sumY = 0.0;
		double sumWeight = 0.0;
		
		double averageLon = 0.0;
		double averageLat = 0.0;
		
		if (trackPointList.size() > 1) {
			ITrackPoint previousTrackPoint = null;
			
			for (ITrackPoint trackPoint : trackPointList) {
				if (previousTrackPoint != null) {
					long duration = trackPoint.getTimestamp().getTime() - previousTrackPoint.getTimestamp().getTime();
					double avgX = (previousTrackPoint.getPoint().getX() + trackPoint.getPoint().getX()) / 2;
					double avgY = (previousTrackPoint.getPoint().getY() + trackPoint.getPoint().getY()) / 2;
					sumX = sumX + avgX * duration;
					sumY = sumY + avgY * duration;
					sumWeight += duration;
				}
				previousTrackPoint = trackPoint;
			}
			
			averageLon = sumX / sumWeight;
			averageLat = sumY / sumWeight;
			
		} else {
			averageLon = trackPointList.get(0).getPoint().getX();
			averageLat = trackPointList.get(0).getPoint().getY();
		}
		
		return GeometryUtils.createPoint2D(averageLon, averageLat, 4326);
	}

	public static Point calculateCentroid(double currentClusterXSum, double currentClusterYSum, int size) {
		double averageLon = currentClusterXSum/size;
		double averageLat = currentClusterYSum/size;
		
		return GeometryUtils.createPoint2D(averageLon, averageLat, 4326);
	}
	
	public static Point calculateMedianCentroid(List<ITrackPoint> trackPointList){
		List<Double> longitudes = new ArrayList<Double>();
		List<Double> latitudes = new ArrayList<Double>();
		for (ITrackPoint trackPoint : trackPointList) {
			longitudes.add(trackPoint.getPoint().getX());
			latitudes.add(trackPoint.getPoint().getY());	
		}
//		long runtimeStart = System.currentTimeMillis();
		Collections.sort(longitudes);
		Collections.sort(latitudes);
//		long runtime = System.currentTimeMillis() - runtimeStart;
//		if (runtime > 0) {
//			log.info("runtime " + runtime + " " + trackPointList.size());
//		}
		int medianIndex = longitudes.size()/2;
		double medianLongitude=0.0, medianLatitude=0.0;
		if (longitudes.size()%2 == 1) {
			medianLongitude = longitudes.get(medianIndex);
			medianLatitude = latitudes.get(medianIndex);
		} else {
			medianLongitude = (longitudes.get(medianIndex-1) + longitudes.get(medianIndex)) / 2;
			medianLatitude = (latitudes.get(medianIndex-1) + latitudes.get(medianIndex)) / 2;
		}
		return GeometryUtils.createPoint2D(medianLongitude, medianLatitude, 4326);
	}
	
	/**
	 * Calculates the distance between two WGS84 coordinates
	 * @param coordinate1
	 * @param coordinate2
	 * @return distance in meter
	 */
	public static double calculateDistance(Coordinate coordinate1, Coordinate coordinate2) {
		return GeometryUtils.distanceAndoyer(coordinate1, coordinate2);
	}
	
	public static double calculateTrackLength(List<ITrackPoint> trackpoints) {
		double length = 0.0;
		
		ITrackPoint previousTP = null;
		for (ITrackPoint tp : trackpoints) {
			if (previousTP != null) {
				length += GeometryUtils.distanceAndoyer(previousTP.getPoint(), tp.getPoint());
			}
			previousTP = tp;
		};
		
		return length;
	}
	
	/**
	 * Calculates the time span between first and last point of a list of track
	 * points
	 * 
	 * @param trackPointList
	 *            List of trackPoints
	 * @return Time span between the first and last track points in seconds. 0
	 *         if track point list is empty
	 */
	public static long calculateDurationOfTrackPoints(List<ITrackPoint> trackPointList) {
		if (trackPointList.size() == 0) {
			return 0l;
		}
		long listStart = trackPointList.get(0).getTimestamp().getTime();
		long listEnd = trackPointList.get(trackPointList.size() - 1).getTimestamp().getTime();
		return (listEnd - listStart) / 1000;
	}
	
	public static double calculateSpeed(ITrackPoint tp1, ITrackPoint tp2) {
		Coordinate coord1 = tp1.getPoint().getCoordinate();
		Coordinate coord2 = tp2.getPoint().getCoordinate();
		double distance = AnalysisUtils.calculateDistance(coord1, coord2);
		
		long listStart = tp1.getTimestamp().getTime();
		long listEnd = tp2.getTimestamp().getTime();
		double duration = Math.abs((double)(listEnd - listStart)) / 1000;
		
		return (distance / 1000) / (duration / (60 * 60));
	}
	
	/**
	 * Checks if the two dates refer the same day.
	 * @param date1 first date
	 * @param date2 second date 
	 */
	public static boolean isSameDay(Date date1, Date date2) {
	    Calendar calendar1 = Calendar.getInstance();
	    calendar1.setTime(date1);
	    Calendar calendar2 = Calendar.getInstance();
	    calendar2.setTime(date2);
	    boolean sameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
	    boolean sameMonth = calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
	    boolean sameDay = calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
	    return (sameDay && sameMonth && sameYear);
	}
	
	/**
	 * Calculates the centroid of the convex hull around all coordinates
	 * @param pointsArray list of coordinates
	 * @return centroid of the convex hull around all coordinates in pointsArray
	 */
	public static Point calculateConvexHullCentroid(Coordinate[] pointsArray) {
		ConvexHull convexHull = new ConvexHull(pointsArray, new GeometryFactory());
//		Point chCentroid = dPlace.getStays().get(0).getPoint();
		Point centroid = GeometryUtils.createPoint(pointsArray[0], 4326);
		if (pointsArray.length == 2) {
			centroid = GeometryUtils.createPoint2D((pointsArray[0].x + pointsArray[1].x) / 2,
					(pointsArray[0].y + pointsArray[1].y) / 2, 4326);
		} else if (pointsArray.length > 2 || (pointsArray.length > 2 && pointsArray[0].x != pointsArray[1].x)) {
			try {
				/** convex hull does not work if array contains only two identical coordinates */
				centroid = convexHull.getConvexHull().getCentroid();
			} catch (Exception e) {
				log.warn("convex hull problem");
			}
		}
		return centroid;
	}
	
	public static double calculateDirectionChangeAngle(Coordinate c1, Coordinate c2, Coordinate c3) {
		double[] vector1 = {c1.x - c2.x, c1.y - c2.y};
		double[] vector2 = {c3.x - c2.x, c3.y - c2.y};
		
		double distance1 = Math.sqrt(Math.pow(vector1[0], 2) + Math.pow(vector1[1], 2));
		double distance2 = Math.sqrt(Math.pow(vector2[0], 2) + Math.pow(vector2[1], 2));
		double distance = distance1 * distance2;
		
		double scalar = vector1[0] * vector2[0] + vector1[1] * vector2[1];
		
		double d = Math.max(-1.0, Math.min(1.0, scalar / distance));
		
		double arccos = Math.acos(d);
		
		return arccos / Math.PI * 180;
	}
}
