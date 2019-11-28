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
package at.srfg.graphium.stopdetection.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import at.srfg.graphium.core.exception.GraphNotExistsException;
import at.srfg.graphium.stopdetection.service.StopDetectionService;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;

/**
 * @author mwimmer
 *
 */
@Controller
public class StopDetectionController {
	
	private static Logger log = LoggerFactory.getLogger(StopDetectionController.class);
	
	private StopDetectionService stopDetectionService;
	
	@RequestMapping(value="/test", method=RequestMethod.GET)
    public ResponseEntity<String> test() 
    		throws GraphNotExistsException {

		return ResponseEntity.ok()
//	                .header("Content-Disposition", "attachment; filename=" + reportName + ".csv")
				.header("Content-Disposition")
//	                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body("A;B;C\na;b;c");

	}
	
	@RequestMapping(value="/graphs/{graph}/detectstops", method=RequestMethod.POST)
    public ResponseEntity<String> detectStops(
    		@PathVariable(value = "graph") String graphName,
    		@RequestParam(value = "file") MultipartFile file) 
    		throws GraphNotExistsException, IOException {

		if (!file.isEmpty()) {
//			try {
				InputStream fileIn = getInputStream(file);
				
				return ResponseEntity.ok()
	//	                .header("Content-Disposition", "attachment; filename=" + reportName + ".csv")
						.header("Content-Disposition")
	//	                .contentLength(file.length())
		                .contentType(MediaType.parseMediaType("text/csv"))
		                .body(stopDetectionService.detectStops(fileIn));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
		}
		
		return null;
//			
//			
//			
//
//			response.setContentType("text/plain; charset=utf-8");
//		    response.getWriter().print("a,b,c\n1,2,3\n3,4,5");
//
//		
//		ITrack track = trackAdapter.adapt(trackDto);
//		
//		String graphVersion = null;
//		List<IWayGraphVersionMetadata> metadataList = metadataDao.getWayGraphVersionMetadataList(graphName, State.ACTIVE, 
//														track.getMetadata().getStartDate(), track.getMetadata().getStartDate(), null);
//		if (metadataList == null || metadataList.isEmpty()) {
//			String msg = "No valid graph version for graph " + graphName + " found for track " + track.getId() + " and timestamp " + track.getMetadata().getStartDate();
//			log.error(msg);
//			throw new GraphNotExistsException(msg, graphName);
//		} else {
//			if (metadataList.size() > 1) {
//				log.warn("More than one graph versions found for track " + track.getId() + " and timestamp " + track.getMetadata().getStartDate());
//			}
//			graphVersion = metadataList.get(0).getVersion();
//		}
//		
//		return match(graphName, graphVersion, track, startSegmentId, timeout, outputVerbose, routingMode);
	}
	
    private InputStream getInputStream(MultipartFile file) throws IOException{
        //Compressed inputStream
	    if (file.getOriginalFilename().endsWith(".zip")) {
            ZipInputStream zip = new ZipInputStream(file.getInputStream());
            ZipEntry entry = zip.getNextEntry();
            return zip;
        } else {
            return file.getInputStream();
        }
    }
    
	private List<Track> readGpxTracks(File gpxFile) throws IOException {
		List<Track> tracks = new ArrayList<Track>();
		
		Iterator<Track> it = GPX.read(new FileInputStream(gpxFile)).tracks().iterator();
		while (it.hasNext()) {
			Track track = it.next();
			if (track != null) {
				tracks.add(track);
			}
		}
		return tracks;
	}

	public StopDetectionService getStopDetectionService() {
		return stopDetectionService;
	}

	public void setStopDetectionService(StopDetectionService stopDetectionService) {
		this.stopDetectionService = stopDetectionService;
	}

}
