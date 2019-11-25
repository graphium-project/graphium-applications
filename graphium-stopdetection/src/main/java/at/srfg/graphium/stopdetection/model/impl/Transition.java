package at.srfg.graphium.stopdetection.model.impl;

import java.util.Date;

import at.srfg.graphium.stopdetection.model.AbstractPlace;

/**
 * This class is a structure created to store the transitions between detectedPlaces as well as their corresponding associated info.
*/
public class Transition {
	
	private int transitionId = 0; // ID of the Transition
	private AbstractPlace originPlace = null; // origin dP
	private AbstractPlace destinationPlace = null; // destination dP
	private Date timeStart = null; // 
	private Date timeEnd = null; //
	private long duration = 0; //
	private double speed = 0; // Calculated speed of such transition
	
	public Transition() {
		
	}
	
	public Transition(Date timeStart, Date timeEnd) {
		super();
		this.transitionId = transitionId++;
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
	}
	
	public Transition(int transitionId, AbstractPlace originPlace, AbstractPlace destinationPlace, Date timeStart, Date timeEnd) {
		super();
		this.transitionId = transitionId;
		this.originPlace = originPlace;
		this.destinationPlace = destinationPlace;
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
	}
	
	public int getTransitionId() {
		return transitionId;
	}
	
	public AbstractPlace getOriginPlace() {
		return originPlace;
	}
	public void setOriginPlace(DetectedPlace originPlace) {
		this.originPlace = originPlace;
	}
	
	public AbstractPlace getDestinationPlace() {
		return destinationPlace;
	}
	public void setDestinationPlace(DetectedPlace destinationPlace) { 
		this.destinationPlace = destinationPlace;
	}

	public Date getTimeStart() {
		return timeStart;
	}
	public void setTimeStart(Date timeStart) {
		this.timeStart = timeStart;
	}
	
	public Date getTimeEnd() {
		return timeEnd;
	}
	public void setTimeEnd(Date timeEnd) {
		this.timeEnd = timeEnd;
	}
	
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
}