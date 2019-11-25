package at.srfg.graphium.stopdetection.cluster.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import org.apache.commons.math3.ml.clustering.Cluster;
//import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import at.srfg.graphium.geomutils.GeometryUtils;
import at.srfg.graphium.mapmatching.model.ITrack;
import at.srfg.graphium.mapmatching.model.ITrackPoint;
import at.srfg.graphium.stopdetection.cluster.AbstractAnalysisMethod;
import at.srfg.graphium.stopdetection.cluster.IAnalysisMethod;
import at.srfg.graphium.stopdetection.model.impl.DetectedPlace;
import at.srfg.graphium.stopdetection.model.impl.Stay;
import at.srfg.graphium.stopdetection.model.impl.Transition;
import at.srfg.graphium.stopdetection.service.AnalysisUtils;


/**
 * YE's algorithm implementation<br>
 * <br>
 * Yang Ye, Yu Zheng, Yukun Chen, Jianhua Feng, Xing Xie - 2009
 * "Mining Individual Life Pattern Based on Location History" [Tsinghua
 * University // Microsoft Research Asia]<br>
 * <br>
 * A Stay point represents a region in which user stays for a while. They
 * consider 2 types of stay points: 1) User maintains stationary in the point
 * for over a time threshold 2) User wanders around within a region for over a
 * time threshold They use mean long and lat to construct a stay point.<br>
 * <br>
 * They iteratively seek the spatial region in which the user stays for a period
 * over a threshold. Each extracted point retains arrival and leaving time,
 * respectively equals timestamp of first and last GPS point constructing this
 * stay point.<br>
 * <br>
 * In this paper, output of this incremental algorithm is clustered with DBSCAN
 * to obtain the final clusters. In our implementation, DBSCAN is substituted
 * with the grouping and convexHull included in this process.
 */
public class YeImpl extends AbstractAnalysisMethod implements IAnalysisMethod {
//	private static Logger log = Logger.getLogger(YeImpl.class);
	
	private boolean initialized = false;
	
	private List<DetectedPlace> tmpDetectedPlaces = null; // First temporal list of detectedPlaces
//	private List<DetectedPlace> detectedGroups = null; // Second temporal list to contain grouped detectedPlaces (as a new single dP)
	private List<ITrackPoint> tempCluster = null; // Temporal cluster structure before converted in StayPoint
	private ITrack track = null;
	
	// Parameters
	protected double clusterDistance = 53; // Distance parameter
	protected int minStayDuration = 900; // Time parameter
	// Threshold for cluster grouping before getting final clusters
//	private double mergeBuffer = 40; // Fixed range as imitation of DBSCAN Eps search radius
    /* private double groupRange = 0.75 * disThre; // In paper they calculate Eps as 75% of the disThre for OPTICS/DBSCAN clustering */

	private int transitionId = 1;
	private int dTempCounter = 1; // To add ID to the complete DetectedPlaces for Quality evaluation
	private Date lastTimeExit = null; // Stay object to store the last timeElement added so as to extract transition times.
	private Date lastTimeEnd = null;
	private DetectedPlace lastDetectedPlace = null; // Point object to store the last stay centroid added as visit to the corresponding detectedPlace
	
//	private final DBSCANClusterer<DoublePointWithPlace> pointClusterer;
//    private List<DoublePointWithPlace> pointList;
	
	public YeImpl() {
//		this.analysisMethod = AnalysisMethod.YE;
//        this.pointClusterer = new DBSCANClusterer<DoublePointWithPlace>(clusterDistance, 0, new AndoyerDistanceMeasure());
//        this.pointList = new ArrayList<DoublePointWithPlace>();
	}
	
	/**
	 * Clustering of the dataset using our implementation of the Incremental Ye's
	 * algorithm
	 * 
	 * @param clusterDistance
	 *            Distance threshold that determine the minimum distance between
	 *            first and current considered point so that a potential cluster has
	 *            been detected
	 * @param minStayDuration
	 *            Time threshold that determine the minimum time the object has to
	 *            be wandering around a disThre distance
	 */
	public YeImpl(double clusterDistance, int minStayDuration) {
		this();
		this.clusterDistance = clusterDistance;
		this.minStayDuration = minStayDuration;
	}
	
//	public YeImpl(double clusterDistance, int minStayDuration, double mergeBuffer) {
//		this();
//		this.clusterDistance = clusterDistance;
//		this.minStayDuration = minStayDuration;
//		this.mergeBuffer = mergeBuffer;
//	}
	
	/**
	 * 1. Defines the structure of the files names
	 * 2. Creates needed ArrayLists
	 */
	@Override
	public void initialize() {
		super.initialize();
		
		/**
		 * Temporal structure, not mentioned in paper, to store points in the
		 * growing cluster so as to calculate centroid of new DetectedPlace
		 */
		tempCluster = new ArrayList<ITrackPoint>();
		track = null;
		
		tmpDetectedPlaces = new ArrayList<DetectedPlace>(); // Temporal list to firstly store dPs
//		detectedGroups = new ArrayList<DetectedPlace>(); // Definitive list to store final dPs

		transitionId = 1;
		dTempCounter = 1;
		lastTimeExit = null;
		lastTimeEnd = null;
		lastDetectedPlace = null;
//		pointList = new ArrayList<DoublePointWithPlace>();
		
		initialized = true;
	}
	
	@Override
	public void initialize(Object params[]) {
		this.initialize();
		
		if (params != null) {
			this.clusterDistance = (double)params[0];
//			this.groupRange = (double)params[1];
		}
	}
	
	@Override
	public List<DetectedPlace> processSingleTrack(ITrack track) {
		this.processTrack(track);
		this.doAfterProcessing();
		return this.getDetectedPlaces();
	}
	
	@Override
	public void processTrack(ITrack track) {
		if (!initialized) {
			this.initialize();
		}
		
		long runtimeStart = System.currentTimeMillis();
		
		int i = 0;
		int j = 0;
		int pointNum = track.getTrackPoints().size();
		this.track = track;

		while (i < pointNum && j < pointNum) {
			j = i + 1;
			tempCluster.add(this.track.getTrackPoints().get(i));
//			checkTempCluster();
			
			while (j < pointNum) {
				// Distance calculation between previous and current point
				Coordinate coord1 = tempCluster.get(0).getPoint().getCoordinate();
				Coordinate coord2 = this.track.getTrackPoints().get(j).getPoint().getCoordinate();
				double distance = AnalysisUtils.calculateDistance(coord1, coord2);
				
				if (distance >= clusterDistance) {
					long duration = AnalysisUtils.calculateDurationOfTrackPoints(tempCluster);
					if (duration >= minStayDuration) {
						createStay();
					}
					tempCluster.clear();
					
					// Update iterator i to j or k if low speed
//					boolean lowSpeedFound = false;
//					for (int k=i; k<j; k++) {
//						double km = (track.getTrackPoints().get(k).getDist_calc() / 1000);
//						double h = (((double)track.getTrackPoints().get(k).getTimeDiff()) / (1000 * 60 * 60));
//						double speed = km / h;
//						if (speed < 5) {
//							i = k;
//							lowSpeedFound = true;
//							break;
//						}
//					}
//					if (!lowSpeedFound) {
					i = j;
//					}
					
					break;
				}
				tempCluster.add(this.track.getTrackPoints().get(j));
//				checkTempCluster();
				j++;
			}
//			// Not present in algorithm pseudo code; but apparently necessary
//			i = j;
		}

		runtime += System.currentTimeMillis() - runtimeStart;
	}
	
//	private void checkTempCluster() {
//		for (int i=1; i<tempCluster.size(); i++) {
//			if (!tempCluster.get(i - 1).getTimestamp().before(tempCluster.get(i).getTimestamp())) {
//				tempCluster.get(i);
//			}
//		}
//	}
	
	/**
	 * Create a new stay. Merges with previous stay if both are close to each
	 * other (spatially and temporally)
	 */
	private void createStay() {
		
		optimizeStayFromBounds();
		
		if (!tempCluster.isEmpty()) {
			
			Date timeStart = tempCluster.get(0).getTimestamp();
			Date timeEnd = tempCluster.get(tempCluster.size()-1).getTimestamp();
			int indexEntry = (tempCluster.get(0).getNumber() != null) ? (tempCluster.get(0).getNumber() - 1) : -1; //getNumber() -1 because number starts with 1
			int indexExit = (tempCluster.get(0).getNumber() != null) ? (tempCluster.get(tempCluster.size()-1).getNumber() - 1) : -1; //getNumber() -1 because number starts with 1
			
			Point clusterCentroid = null;
			if (AbstractAnalysisMethod.getSamplingInterval(timeStart, timeEnd, indexEntry, indexExit) < 30) {
				clusterCentroid = AnalysisUtils.calculateWeightedCentroid(tempCluster);
			} else {
				int index = getHalfDurationIndex();
				clusterCentroid = GeometryUtils.createPoint2D(tempCluster.get(index).getPoint().getCoordinate().x, tempCluster.get(index).getPoint().getCoordinate().y, 4326);;
			}
			
			DetectedPlace detectedPlace = new DetectedPlace(clusterCentroid, dTempCounter++, this.track);
//			if (indexExit >= this.track.getTrackPoints().size()) {
//				System.out.println("indexExit >= this.track.getTrackPoints().size()");
//			}
			Stay stay = new Stay(clusterCentroid, timeStart, timeEnd, indexEntry, indexExit);
			stay.setTrackLength(AnalysisUtils.calculateTrackLength(tempCluster));
			detectedPlace.addStay(stay);
			
			if (tmpDetectedPlaces.size() == 0) {
				tmpDetectedPlaces.add(detectedPlace);
				lastDetectedPlace = detectedPlace;
			} else {
				/* Checks if the last place/stay is located nearby and the time between last
				 * visit and current is less than minStayDuration. Then both visits are merged,
				 * else a new place/stay is created
				 */
				double distanceInBetween = AnalysisUtils.calculateDistance(clusterCentroid.getCoordinate(),
						lastDetectedPlace.getLocation().getCoordinate());
				if ((timeStart.getTime() - lastTimeEnd.getTime()) <= (minStayDuration * 1000) && (distanceInBetween <= clusterDistance)) {
					// Merge stays
					int indexLastPlace = tmpDetectedPlaces.size() - 1;
					int indexLastStay = tmpDetectedPlaces.get(indexLastPlace).getStays().size() - 1;
					Stay lastStay = tmpDetectedPlaces.get(indexLastPlace).getStays().get(indexLastStay); 
					lastStay.setTimeExit(timeEnd);
					lastStay.setTrackPointIndexExit(indexExit);
					lastStay.setTrackLength(lastStay.getTrackLength() + stay.getTrackLength());
					
				} else {
					// A new detectedPlace is stored and a new transition created
					tmpDetectedPlaces.add(detectedPlace);
					detectedTransitions.add(new Transition(transitionId++, lastDetectedPlace, detectedPlace, lastTimeExit, timeStart));
					lastDetectedPlace = detectedPlace;
				}
			}

//			tempCluster.clear();
			lastTimeEnd = timeEnd;
			lastTimeExit = timeEnd;
		}
	}
	
//	/**
//	 * Remove segments between track points at the beginning and end of the cluster
//	 * where speed is more than 10 km/h
//	 */
//	private void optimizeStay() {
//		if (tempCluster.size() < 3) {
//			return;
//		}
//		// Determine track point index at half duration
//		int initialIndex = getHalfDurationIndex();
//		//From initial index backwards
//		double speed = 0.0;
//		int indexStart = initialIndex + 1;
//		do {
//			indexStart--;
//			if (indexStart > 0) {
//				speed = AnalysisUtils.calculateSpeed(tempCluster.get(indexStart), tempCluster.get(indexStart - 1));
//			}
//		} while (speed < 10 && indexStart > 0);
//		
//		//From initial index upwards
//		speed = 0.0;
//		int indexEnd = initialIndex;
//		do {
//			indexEnd++;
//			if (indexEnd < tempCluster.size() - 1) {
//				speed = AnalysisUtils.calculateSpeed(tempCluster.get(indexEnd + 1), tempCluster.get(indexEnd));
//			}
//		} while (speed < 10 && indexEnd < tempCluster.size() - 1);
//		tempCluster = tempCluster.subList(indexStart, indexEnd + 1);
//	}


	private int getHalfDurationIndex() {
		int initialIndex = 0;
		long duration = tempCluster.get(tempCluster.size() - 1).getTimestamp().getTime() - tempCluster.get(0).getTimestamp().getTime();
		long cumulatedDuration = 0;
		for (int i=1; i<tempCluster.size(); i++) {
			cumulatedDuration += tempCluster.get(i).getTimestamp().getTime() - tempCluster.get(i - 1).getTimestamp().getTime();
			if (cumulatedDuration > duration / 2) {
				initialIndex = i - 1;
				break;
			}
		}
		return initialIndex;
	}
	
	/**
	 * Remove segments between track points at the beginning and end of the cluster
	 * where speed is more than 10 km/h
	 */
	private void optimizeStayFromBounds() {
		if (tempCluster.size() < 3) {
			return;
		}
		//Iterate from beginning
		double speed = Double.MAX_VALUE;
		int indexStart = -1;
		do {
			indexStart++;
			speed = AnalysisUtils.calculateSpeed(tempCluster.get(indexStart), tempCluster.get(indexStart + 1));
		} while (speed > 10 && indexStart < tempCluster.size() - 2);
		
		//Iterate from end
		speed = Double.MAX_VALUE;
		int indexEnd = tempCluster.size();
		do {
			indexEnd--;
			speed = AnalysisUtils.calculateSpeed(tempCluster.get(indexEnd), tempCluster.get(indexEnd - 1));
		} while (speed > 10 && indexEnd > 2 && indexStart < indexEnd);
		if (indexStart < indexEnd) {
			tempCluster = tempCluster.subList(indexStart, indexEnd + 1);
		} else {
			tempCluster.clear();
		}
	}
	
	/**
	 * Second part of the Incremental + Density-based approach. This processing is equivalent to a density-based clustering
	 * 1. Initial dPs detected are compared and grouped if close together
	 * 2. Groups are compared and if at least one point inside is close to another point in another group, both groups are merged
	 * 3. A convex hull is created with the points in each group. Then a centroid is calculated and these are the final coordinates of the dP
	 * 4. Transitions are then post-processed to correct the final IDs and coordinates of origin and destination of each one
	 */
	@Override
	public void doAfterProcessing() {
		long runtimeStart = System.currentTimeMillis();
		long timeDuration = AnalysisUtils.calculateDurationOfTrackPoints(tempCluster);
		if (tempCluster.size() > 0 && timeDuration >= minStayDuration) {
			createStay();
		}

//		clusterWithDbScan();
//		clusterDetectedPlaces();

		runtime += System.currentTimeMillis() - runtimeStart;
		
		super.doAfterProcessing();
	}
	
//	private void clusterWithDbScan() {
//		for (DetectedPlace dP : tmpDetectedPlaces) {
//			pointList.add(new DoublePointWithPlace(new double[] { dP.getLocation().getX(), dP.getLocation().getY() }, dP));
//		}
//		List<Cluster<DoublePointWithPlace>> clusterList = pointClusterer.cluster(pointList);
//		int detectedPlaceID = 1;
//		
//		for (Cluster<DoublePointWithPlace> cluster : clusterList) {
//			List<ITrackPoint> trackPointList = new ArrayList<ITrackPoint>();
//			DetectedPlace detectedPlace = new DetectedPlace();
//			for (DoublePointWithPlace place : cluster.getPoints()) {
//				ITrackPoint point = new TrackPointImpl();
//				point.setGeometry(GeometryUtils.createPoint2D(place.getPoint()[0], place.getPoint()[1], 4326));
//				trackPointList.add(point);
////				place.getTimestamp().getStays().forEach(stay -> detectedPlace.addStay(stay));
//				for (Stay stay : place.getPlace().getStays()) {
//					detectedPlace.addStay(stay);
//				}
//			}
//			detectedPlace.setLocation(AnalysisUtils.calculateCentroid(trackPointList));
//			detectedPlace.setPlaceId(detectedPlaceID++);
//			detectedPlace.setTrack(cluster.getPoints().get(0).getPlace().getTrack());
//			super.getDetectedPlaces().add(detectedPlace);
//		}
//	}
//
//	/**
//	 * Alternative to clustering with DB scan
//	 */
//	@SuppressWarnings("unused")
//	private void clusterDetectedPlaces() {
//		/**
//		 * DP POST-PROCESSING: "CONVEXHULL SOLUTION"<br>
//		 * NOTE: the stay used for the spatio-temporal approaches can have its
//		 * own coordinates used to store the specific location at which such
//		 * stay was detected
//		 */
//		
//		
//		/**
//		 * Creating groups of close points. Initial dPs only have one stay. If
//		 * two initial dPs are close, their associated stays are merged to a
//		 * unique dP in the detectedGroups list
//		 */
//		int dTempCounter = 1;
//		for (DetectedPlace dTemp : tmpDetectedPlaces) {
//			
//			if (detectedGroups.size() == 0) {
//				dTemp.setPlaceId(dTempCounter++);
//				detectedGroups.add(dTemp);
//				continue;
//			}
//			
//			DetectedPlace closestDp = null;
//			double closestDpDistance = mergeBuffer;
//			
//			// Iterate detectedPlaces
//			for (DetectedPlace detectedGroup : detectedGroups) {
//				// Iterate stays of detected group
//				for (Stay stay : detectedGroup.getStays()) {
//					
//					Coordinate teC = stay.getPoint().getCoordinate();
//					Coordinate dpC = dTemp.getLocation().getCoordinate();
//					
//					double distanceRangeLength = AnalysisUtils.calculateDistance(dpC, teC);
//					
////					// Check if dTemp is too far away
////					if (distanceRangeLength > groupRange * 3) break;
//					
//					if (distanceRangeLength < closestDpDistance) {
//						closestDpDistance = distanceRangeLength;
//						closestDp = detectedGroup;
//					}
//				}
//			}
//			if (closestDp != null) {
//				closestDp.addStay(dTemp.getStays().get(0));
//			} else {
//				dTemp.setPlaceId(dTempCounter++);
//				detectedGroups.add(dTemp);
//			}
//		}
//		
//		/**
//		 * Merging close groups
//		 */
//		int dGroupCounter = 1;
//		for (DetectedPlace dGroup : detectedGroups) { 
//			DetectedPlace closestDetectedPlace = null;
//			
//			if (detectedPlaces.size() == 0) {
//				// Set first detected place
//				dGroup.setPlaceId(dGroupCounter++);
//				detectedPlaces.add(dGroup);
//				continue;
//			}
//			
//			// Iterate final detected places
//			for (DetectedPlace detectedPlace : detectedPlaces) {
//				// If the detectedGroup contains at least two stays, it is compared for merging with other group
//				//TODO [SGroe] i don't understand, why groups with 1 stay are directly added to detected places
//				if (dGroup.getStays().size() < 2) break;
//				
//				// Iterate stays inside each final detectedPlace
//				for (Stay detectedPlaceStay : detectedPlace.getStays()) {
//					// Compare every stay inside the final detectedPlace with every stay inside the considered dGroup
//					for (Stay detectedGroupStay : dGroup.getStays()) {
//						Coordinate teC = detectedPlaceStay.getPoint().getCoordinate();
//						Coordinate geC = detectedGroupStay.getPoint().getCoordinate();
//						
//						double distanceRangeLength = AnalysisUtils.calculateDistance(geC, teC);
//						
////						// First, checks if the point is too far so as to avoid more tests
////						if (distanceRangeLength > groupRange * 10) break;
//						
//						// If any of the points in the group is within the groupRange distance, both groups are merged
//						if (distanceRangeLength <= mergeBuffer) {
//							closestDetectedPlace = detectedPlace;
//							break;
//						}
//					}
//					if (closestDetectedPlace != null) break;
//				}
//			}
//			
//			if (closestDetectedPlace != null) {
//				closestDetectedPlace.getStays().addAll(dGroup.getStays());
//			} else {
//				dGroup.setPlaceId(dGroupCounter++);
//				detectedPlaces.add(dGroup);
//			}
//		}
//		
//		/*
//		 * Set a new centroid for each detected place
//		 */
//		for (DetectedPlace dPlace : detectedPlaces) {
//			if (dPlace.getStays().size() < 2) continue;
//			
//			Coordinate[] tePointsArray = new Coordinate[dPlace.getStays().size()];
//			for (int t = 0; t < dPlace.getStays().size(); t++) {
//				tePointsArray[t] = dPlace.getStays().get(t).getPoint().getCoordinate();
//			}
//			
//			// creates a convexHull for the points and calculates the centroid of the convex hull
//			dPlace.setLocation(AnalysisUtils.calculateConvexHullCentroid(tePointsArray));	
//		}
//	}
	
	@Override
	public String getFileIdentifier() {
		return "_" + clusterDistance + "m_" + minStayDuration + "s";
	}
	
	@Override
	public String toString() {
		return "YeImpl_" + this.clusterDistance + "_" + this.minStayDuration;
	}
}
