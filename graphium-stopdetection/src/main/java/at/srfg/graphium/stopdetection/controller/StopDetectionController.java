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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import at.srfg.graphium.core.exception.GraphNotExistsException;
import at.srfg.graphium.stopdetection.service.StopDetectionService;

/**
 * @author mwimmer
 *
 */
@Controller
public class StopDetectionController {
	
	private static Logger log = LoggerFactory.getLogger(StopDetectionController.class);
	
	private StopDetectionService stopDetectionService;
	
	@RequestMapping(value="/graphs/{graph}/detectstops", method=RequestMethod.POST)
    public ResponseEntity<String> detectStops(
    		@RequestParam(value = "graphHighLevel") String graphNameHighLevel,
    		@RequestParam(value = "graphLowLevel", required = false) String graphNameLowLevel,
    		@RequestParam(value = "file") MultipartFile file) 
    		throws GraphNotExistsException, IOException {

		if (!file.isEmpty()) {
			InputStream fileIn = getInputStream(file);
			
			return ResponseEntity.ok()
//	                .header("Content-Disposition", "attachment; filename=" + reportName + ".csv")
					.header("Content-Disposition")
//	                .contentLength(file.length())
	                .contentType(MediaType.parseMediaType("text/csv"))
	                .body(stopDetectionService.detectStops(fileIn, graphNameHighLevel, graphNameLowLevel));
			
		}
		
		return null;
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
    
	public StopDetectionService getStopDetectionService() {
		return stopDetectionService;
	}

	public void setStopDetectionService(StopDetectionService stopDetectionService) {
		this.stopDetectionService = stopDetectionService;
	}

}
