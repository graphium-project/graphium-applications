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
package at.srfg.graphium.stopdetection.model;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Point;

import at.srfg.graphium.mapmatching.model.ITrack;
import at.srfg.graphium.stopdetection.model.impl.Stay;

public abstract class AbstractPlace {
	private Point location = null;
	private long placeId;
	private List<Stay> stays = null;
	private ITrack track = null;
	
	public AbstractPlace() {
		placeId = -1;
		this.stays = new ArrayList<Stay>();
	}
	
	public AbstractPlace(Point location, long numberId, ITrack track) {
		this();
		this.location = location;
		this.placeId = numberId;
		this.track = track;
	}
	
	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	/**
	 * @return place id
	 */
	public long getPlaceId() {
		return placeId;
	}
	
	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}
	
	/**
	 * Adds a Stay to the list of Stays
	 * @param stay
	 */
	public void addStay(Stay stay) {
		this.stays.add(stay);		
	}
	
	/**
	 * @return list of stays
	 */
	public List<Stay> getStays() {
		return stays;
	}
	
	/**
	 * @return track
	 */
	public ITrack getTrack() {
		return track;
	}
	public void setTrack(ITrack track) {
		this.track = track;
	}
}
