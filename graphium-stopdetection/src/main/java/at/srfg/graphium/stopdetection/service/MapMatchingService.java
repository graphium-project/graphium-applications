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
package at.srfg.graphium.stopdetection.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import at.srfg.graphium.core.exception.GraphNotExistsException;
import at.srfg.graphium.mapmatching.matcher.IMapMatcherTask;
import at.srfg.graphium.mapmatching.model.IMatchedBranch;
import at.srfg.graphium.mapmatching.model.ITrack;
import at.srfg.graphium.mapmatching.neo4j.matcher.impl.Neo4jMapMatcher;

public class MapMatchingService {
	protected Logger log = Logger.getLogger(this.getClass().getName());
	
	private Neo4jMapMatcher mapMatcher;
	private String routingMode = "car";
	
	/**
	 * @param track
	 * @return
	 * @throws GraphNotExistsException 
	 */
	public List<IMatchedBranch> matchTrack(ITrack mapMatchingTrack, String graphName) throws GraphNotExistsException {
		log.info("Matching Track " + mapMatchingTrack.getId());
		long startTime = System.nanoTime();
		try {
			IMapMatcherTask task = mapMatcher.getTask(graphName, mapMatchingTrack, routingMode);
			List<IMatchedBranch> branches = task.matchTrack();
			log.info("Map matching took " +  + (System.nanoTime() - startTime) + "ns = " + ((System.nanoTime() - startTime) / 1000000) + "ms");
			return branches;
		} catch (Exception e) {
			log.error("Map matching error: " + e.getMessage());
			return new ArrayList<IMatchedBranch>();
		}
	}

	public Neo4jMapMatcher getMapMatcher() {
		return mapMatcher;
	}

	public void setMapMatcher(Neo4jMapMatcher mapMatcher) {
		this.mapMatcher = mapMatcher;
	}

	public String getRoutingMode() {
		return routingMode;
	}

	public void setRoutingMode(String routingMode) {
		this.routingMode = routingMode;
	}

}