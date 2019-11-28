package at.srfg.graphium.stopdetection.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import at.srfg.graphium.core.exception.GraphNotExistsException;
import at.srfg.graphium.mapmatching.matcher.IMapMatcherTask;
import at.srfg.graphium.mapmatching.model.IMatchedBranch;
import at.srfg.graphium.mapmatching.model.ITrack;
import at.srfg.graphium.mapmatching.neo4j.matcher.impl.Neo4jMapMatcher;

public class MapMatchingService {
	protected Logger log = Logger.getLogger(this.getClass().getName());
	
	@Autowired
	private Neo4jMapMatcher mapMatcher;
	private String routingMode = "";
	
	public MapMatchingService () {
		this("car");
	}
	
	public MapMatchingService (String routingMode) {
		this.routingMode = routingMode;
	}

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

}
