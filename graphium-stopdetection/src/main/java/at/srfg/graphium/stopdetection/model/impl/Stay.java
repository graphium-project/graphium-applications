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
package at.srfg.graphium.stopdetection.model.impl;

import java.util.Date;

import com.vividsolutions.jts.geom.Point;

import at.srfg.graphium.model.IWaySegment;

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
	
	private double distanceToHighLevelRoad = -1.0;
	private IWaySegment nextHighLevelRoadSegment = null;
	private double distanceToLowLevelRoad = -1.0;
	private IWaySegment nextLowLevelRoadSegment = null;

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
	 * nextHighLevelRoadSegment
	 */
	public IWaySegment getNextHighLevelRoadSegment() {
		return nextHighLevelRoadSegment;
	}
	public void setNextHighLevelRoadSegment(IWaySegment nextHighLevelRoadSegment) {
		this.nextHighLevelRoadSegment = nextHighLevelRoadSegment;
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
	 * nextLowLevelRoadSegment
	 */
	public IWaySegment getNextLowLevelRoadSegment() {
		return nextLowLevelRoadSegment;
	}
	public void setNextLowLevelRoadSegment(IWaySegment nextLowLevelRoadSegment) {
		this.nextLowLevelRoadSegment = nextLowLevelRoadSegment;
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