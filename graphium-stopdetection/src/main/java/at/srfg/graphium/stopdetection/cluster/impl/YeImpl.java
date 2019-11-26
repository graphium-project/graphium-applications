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
	private double clusterDistance = 53; // Distance parameter
	private int minStayDuration = 900; // Time parameter
	private float maxStopSpeed = 10;

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
	public YeImpl(double clusterDistance, int minStayDuration, float maxStopSpeed) {
		this();
		this.clusterDistance = clusterDistance;
		this.minStayDuration = minStayDuration;
		this.maxStopSpeed = maxStopSpeed;
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
					
					i = j;
					
					break;
				}
				tempCluster.add(this.track.getTrackPoints().get(j));
				j++;
			}
		}

		runtime += System.currentTimeMillis() - runtimeStart;
	}
	
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

			lastTimeEnd = timeEnd;
			lastTimeExit = timeEnd;
		}
	}

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
		} while (speed > maxStopSpeed && indexStart < tempCluster.size() - 2);
		
		//Iterate from end
		speed = Double.MAX_VALUE;
		int indexEnd = tempCluster.size();
		do {
			indexEnd--;
			speed = AnalysisUtils.calculateSpeed(tempCluster.get(indexEnd), tempCluster.get(indexEnd - 1));
		} while (speed > maxStopSpeed && indexEnd > 2 && indexStart < indexEnd);
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

		runtime += System.currentTimeMillis() - runtimeStart;
		
		super.detectedPlaces.addAll(this.tmpDetectedPlaces);
		
		super.doAfterProcessing();
	}
	
	@Override
	public String getFileIdentifier() {
		return "_" + clusterDistance + "m_" + minStayDuration + "s";
	}
	
	@Override
	public String toString() {
		return "YeImpl_" + this.clusterDistance + "_" + this.minStayDuration;
	}
}
