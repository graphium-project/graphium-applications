package at.srfg.graphium.stopdetection.model.impl;

import java.util.Date;

import com.vividsolutions.jts.geom.Point;

import at.srfg.graphium.model.FormOfWay;
import at.srfg.graphium.model.FuncRoadClass;

/**
 * Class created to store the stays at a place in form of timeEntry and timeExit.
 */
public class Stay {
	
	private Point point;
	private Date timeEntry;
	private Date timeExit;
	private int trackPointIndexEntry;
	private int trackPointIndexExit;
	private int matchedStays = 0;
	private double trackLength = 0.0;
	
	private double distanceToHighLevelRoad = Double.MAX_VALUE;
	private FuncRoadClass frcOfNextHighLevelRoad = null;
	private FormOfWay fowOfNextHighLevelRoad = null;
	private double distanceToLowLevelRoad = Double.MAX_VALUE;
	private FuncRoadClass frcOfNextLowLevelRoad = null;
	private FormOfWay fowOfNextLowLevelRoad = null;

	public Stay() {
		this.point = null;
	}
	public Stay(Date timeEntry, Date timeExit, int trackPointIndexEntry, int trackPointIndexExit) { 
		this();
		this.timeEntry = timeEntry;
		this.timeExit = timeExit;
		this.trackPointIndexEntry = trackPointIndexEntry;
		this.trackPointIndexExit = trackPointIndexExit;
	}
	public Stay(Point point, Date timeEntry, Date timeExit, int trackPointIndexEntry, int trackPointIndexExit) { 
		this(timeEntry, timeExit, trackPointIndexEntry, trackPointIndexExit);
		this.point = point;
	}
	
	/**
	 * point
	 */
	public Point getPoint() {
		return point;
	}
	public void setPoint(Point point) {
		this.point = point;
	}

	/**
	 * time entry
	 */
	public Date getTimeEntry() {
		return timeEntry;
	}
	public void setTimeEntry(Date timeEntry) {
		this.timeEntry = timeEntry;
	}
	
	/**
	 * time exit
	 */
	public Date getTimeExit() {
		return timeExit;
	}
	public void setTimeExit(Date timeExit) {
		this.timeExit = timeExit;
	}
	
	/**
	 * trackPointIndexEntry
	 */
	public int getTrackPointIndexEntry() {
		return trackPointIndexEntry;
	}
	public void setTrackPointIndexEntry(int trackPointIndexEntry) {
		this.trackPointIndexEntry = trackPointIndexEntry;
	}
	
	/**
	 * trackPointIndexExit
	 */
	public int getTrackPointIndexExit() {
		return trackPointIndexExit;
	}
	public void setTrackPointIndexExit(int trackPointIndexExit) {
		this.trackPointIndexExit = trackPointIndexExit;
	}

	/**
	 * track length
	 */
	public double getTrackLength() {
		return trackLength;
	}
	public void setTrackLength(double trackLength) {
		this.trackLength = trackLength;
	}
	
	/**
	 * Returns the duration in seconds
	 */
	public double getDurationInSec() {
		double duration = (timeExit.getTime() - timeEntry.getTime()) / 1000;
		return duration;
	}

	/**
	 * distanceToHighLevelRoad
	 */
	public double getDistanceToHighLevelRoad() {
		return distanceToHighLevelRoad;
	}
	public void setDistanceToHighLevelRoad(double distanceToHighLevelRoad) {
		this.distanceToHighLevelRoad = distanceToHighLevelRoad;
	}

	/**
	 * frcOfNextHighLevelRoad
	 */
	public FuncRoadClass getFrcOfNextHighLevelRoad() {
		return frcOfNextHighLevelRoad;
	}
	public void setFrcOfNextHighLevelRoad(FuncRoadClass frcOfNextHighLevelRoad) {
		this.frcOfNextHighLevelRoad = frcOfNextHighLevelRoad;
	}

	/**
	 * fowOfNextHighLevelRoad
	 */
	public FormOfWay getFowOfNextHighLevelRoad() {
		return fowOfNextHighLevelRoad;
	}
	public void setFowOfNextHighLevelRoad(FormOfWay fowOfNextHighLevelRoad) {
		this.fowOfNextHighLevelRoad = fowOfNextHighLevelRoad;
	}

	/**
	 * distanceToLowLevelRoad
	 */
	public double getDistanceToLowLevelRoad() {
		return distanceToLowLevelRoad;
	}
	public void setDistanceToLowLevelRoad(double distanceToLowLevelRoad) {
		this.distanceToLowLevelRoad = distanceToLowLevelRoad;
	}

	/**
	 * frcOfNextLowLevelRoad
	 */
	public FuncRoadClass getFrcOfNextLowLevelRoad() {
		return frcOfNextLowLevelRoad;
	}
	public void setFrcOfNextLowLevelRoad(FuncRoadClass frcOfNextLowLevelRoad) {
		this.frcOfNextLowLevelRoad = frcOfNextLowLevelRoad;
	}

	/**
	 * fowOfNextLowLevelRoad
	 */
	public FormOfWay getFowOfNextLowLevelRoad() {
		return fowOfNextLowLevelRoad;
	}
	public void setFowOfNextLowLevelRoad(FormOfWay fowOfNextLowLevelRoad) {
		this.fowOfNextLowLevelRoad = fowOfNextLowLevelRoad;
	}
	
	@Override
	public String toString() {
		return "{point: " + point.toString() + ", timeEntry: " + timeEntry + ", timeExit: " + timeExit + ", trackPointIndexEntry: " +
				trackPointIndexEntry + ", trackPointIndexExit: " + trackPointIndexExit + ", matchedStays: " + matchedStays +
				", trackLenght: " + trackLength + "}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Stay)) return false;

		Stay that = (Stay) o;
		if (point != that.point) return false;
		if (timeEntry != that.timeEntry) return false;
		if (timeExit != that.timeExit) return false;
		if (trackPointIndexEntry != that.trackPointIndexEntry) return false;
		if (trackPointIndexExit != that.trackPointIndexExit) return false;
		if (matchedStays != that.matchedStays) return false;
		if (trackLength != that.trackLength) return false;
		return true;
	}
}