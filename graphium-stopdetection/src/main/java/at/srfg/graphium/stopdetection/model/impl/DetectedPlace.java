package at.srfg.graphium.stopdetection.model.impl;

import com.vividsolutions.jts.geom.Point;

import at.srfg.graphium.mapmatching.model.ITrack;
import at.srfg.graphium.stopdetection.model.AbstractPlace;

/**
 * This class is a hybrid structure created to standardize an object to contain
 * points and additional information needed for the QualityEvaluation (common
 * class). It also contains information needed for the implementation of the
 * KANG's and YE's algorithms. They use a structure to store the clusters
 * detected = the centroid of these clusters = point coordinates<br>
 * <br>
 * Now TaggedPlaces and DetectedPlaces store the timeStamps associated to them.
 * For TaggedPlaces these are reported stay durations in wegeprotokoll. For
 * DetectedPlaces it depends on the resulting clusters obtained with your
 * approach; with the incremental algorithm same locations are detected in
 * different days or moments of the day. Each visit to this location has
 * specific coordinates but the final clusters are calculated as the centroid of
 * a convex hull (Ye new) including them or as the mean coordinate (Kang).<br>
 * <br>
 * This is the reason for storing also the coordinates specifically detected for
 * each visit (point in timeElement object). If you only need storing timeEnter
 * and timeExit in the TimeElement, you will see the associated point is
 * predefined as null.
 * 
 * @author sgroeche
 *
 */
public class DetectedPlace extends AbstractPlace {
	
//	private List<TaggedPlace> taggedPlaceList = new ArrayList<TaggedPlace>(); // It was necessary creating here the new ArrayList
//	
////	private int pointsInCluster = 0; // Number of original trackPoints included in final DetectedPlace determined
//	private TaggedPlace nearestTaggedPlace = null; //closest taggedPlace
//	private double distanceToNextTaggedPlace = Double.MAX_VALUE; // To be set = to bufferRadius variable from QualityEvaluation // Distance from SignificantLocation to closer TaggedPlace // Equal to bufferRadius when creating object so as to allow for first condition checking
////	private int numTpPtsClose = 0; // Number of TaggedPlaces within the selected buffer
//
//	private double sumTtp = 0;
//	private int matchedStayCount = 0;

	public DetectedPlace() { }
	
	public DetectedPlace(Point location, long placeId, ITrack track) {
		super(location, placeId, track);
	}
	
//	/**
//	 * Adds TaggedPlace to list of associated tagged places
//	 * @param taggedPlace TaggedPlace which is associated to detected location
//	 */
//	public void addTaggedPlace(TaggedPlace taggedPlace){
//		this.taggedPlaceList.add(taggedPlace);
//	}
//	
//	public List<TaggedPlace> getTaggedPlaceList(){
//		return taggedPlaceList;
//	}
//	
//	/**
//	 * Get closest TaggedPlace i.e. the TaggedPlace to which this DetectedPlace was finally and uniquely assigned
//	 * @return closed tagged place
//	 */
//	public TaggedPlace getNearestTaggedPlace() {
//		return nearestTaggedPlace;
//	}
//
//	public void setNearestTaggedPlace(TaggedPlace nearestTaggedPlace) {
//		this.nearestTaggedPlace = nearestTaggedPlace;
//	}
//	
//	public double getDistanceToNextTaggedPlace() {
//		return distanceToNextTaggedPlace;
//	}
//
//	public void setDistanceToNearestTaggedPlace(double distanceToNextTaggedPlace) {
//		this.distanceToNextTaggedPlace = distanceToNextTaggedPlace;
//	}
//	
//	/**
//	 *  Get stored sum of time deviations for this dP
//	 * @return
//	 */
//	public double getSumTtp() {
//		return sumTtp;
//	}
//
//	public void setSumTtp(double sumTtp) {
//		this.sumTtp = sumTtp;
//	}
//	
//	/**
//	 * Get number of correctly detected times for this dP (after comparison of dP with the stored times in the related TaggedPlace)
//	 * @return
//	 */
//	public int getMatchedStayCount() {
//		return matchedStayCount;
//	}
//	
//	public void setMatchedStayCount(int matchedStayCount) {
//		this.matchedStayCount = matchedStayCount;
//	}
}