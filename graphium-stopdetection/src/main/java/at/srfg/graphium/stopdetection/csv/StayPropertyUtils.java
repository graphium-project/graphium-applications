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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

import at.srfg.graphium.geomutils.GeometryUtils;
import at.srfg.graphium.mapmatching.model.ITrack;
import at.srfg.graphium.mapmatching.model.ITrackPoint;
import at.srfg.graphium.stopdetection.model.impl.Stay;
import at.srfg.graphium.stopdetection.utils.AnalysisUtils;

public class StayPropertyUtils {

	
	/**
	 * 
	 * @param track
	 */
	public static Map<String, Object> calculateProperties(Stay stay, ITrack track) {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("min_speed", getMinimumSpeed(stay, track));
		return properties;
	}

	/**
	 * 
	 * @param track
	 */
	public static double getMinimumSpeed(Stay stay, ITrack track) {
		List<? extends ITrackPoint> trackpoints = track.getTrackPoints();
		double minSpeed = Double.MAX_VALUE;
		for (int i = stay.getTrackPointIndexEntry() + 1; i <= stay.getTrackPointIndexExit(); i++) {
			double speed = AnalysisUtils.calculateSpeed(trackpoints.get(i-1), trackpoints.get(i));
			if (speed < minSpeed) {
				
				minSpeed = speed;
			}
		}
		return minSpeed;
	}

	/**
	 * 
	 * @param track
	 */
	public static double getSummedAngles(Stay stay, ITrack track) {
		Coordinate[] simplified = getSimplifiedTrackGeom(stay, track).getCoordinates();
		double angleSum = 0.0;
		for (int i = 1; i < simplified.length - 1; i++) {
			if (simplified[i-1].x == simplified[i].x && simplified[i-1].y == simplified[i].y) {
				continue; //equal coordinates
			} else if (simplified[i].x == simplified[i+1].x && simplified[i].y == simplified[i+1].y) {
				continue; //equal coordinates
			}
			angleSum += 180 - AnalysisUtils.calculateDirectionChangeAngle(simplified[i-1], simplified[i], simplified[i+1]);
		}
		return angleSum;
	}

	/**
	 * 
	 * @param track
	 */
	public static double getNumberOfSignificantDirectionChanges(Stay stay, ITrack track) {
		Coordinate[] simplified = getSimplifiedTrackGeom(stay, track).getCoordinates();
		int significantDirectionChanges = 0;
		for (int i = 1; i < simplified.length - 1; i++) {
			if (AnalysisUtils.calculateDirectionChangeAngle(simplified[i-1], simplified[i], simplified[i+1]) > 30) {
				significantDirectionChanges++;
			}
		}
		return significantDirectionChanges;
	}

	/**
	 * 
	 * @param track
	 */
	public static double getDistanceSlow(Stay stay, ITrack track) {
		List<? extends ITrackPoint> trackpoints = track.getTrackPoints();
		double distance = 0.0;
		for (int i = stay.getTrackPointIndexEntry() + 1; i <= stay.getTrackPointIndexExit(); i++) {
			double speed = AnalysisUtils.calculateSpeed(trackpoints.get(i-1), trackpoints.get(i));
			if (speed < 5.0) {
				distance += AnalysisUtils.calculateDistance(trackpoints.get(i-1).getPoint().getCoordinate(), trackpoints.get(i).getPoint().getCoordinate());
			}
		}
		return distance;
	}

	/**
	 * 
	 * @param track
	 */
	public static int getNumberOfStops(Stay stay, ITrack track) {
		List<? extends ITrackPoint> trackpoints = track.getTrackPoints();
		int numberOfStops = 0;
		boolean currentlyInStop = false;
		for (int i = stay.getTrackPointIndexEntry() + 1; i <= stay.getTrackPointIndexExit(); i++) {
			double speed = AnalysisUtils.calculateSpeed(trackpoints.get(i-1), trackpoints.get(i));
			if (speed < 5.0) {
				if (!currentlyInStop) {
					currentlyInStop = true;
					numberOfStops++;
				}
			} else if (speed > 10) {
				currentlyInStop = false;
			}
		}
		if (numberOfStops == 0) {
			numberOfStops = 1;
		}
		return numberOfStops;
	}
	
	public static double getSamplingInterval(Stay stay) {
		double duration = (stay.getTimeExit().getTime() - stay.getTimeEntry().getTime()) / 1000;
		int numberOfPoints = stay.getTrackPointIndexExit() - stay.getTrackPointIndexEntry() + 1;
		return duration / numberOfPoints;
	}

	public static LineString getSimplifiedTrackGeom(Stay stay, ITrack track) {
		List<? extends ITrackPoint> trackpoints = track.getTrackPoints().subList(stay.getTrackPointIndexEntry(), stay.getTrackPointIndexExit() + 1);
		Coordinate[] coords = new Coordinate[trackpoints.size()];
		for (int i=0; i<trackpoints.size(); i++) {
			coords[i] = trackpoints.get(i).getPoint().getCoordinate();
		}
		LineString line = GeometryUtils.createLineString(coords, 4326);
//		TopologyPreservingSimplifier.simplify(line, 0.0001);
		return (LineString)TopologyPreservingSimplifier.simplify(line, 0.0001);
	}
}
