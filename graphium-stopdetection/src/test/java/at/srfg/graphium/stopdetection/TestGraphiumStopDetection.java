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
package at.srfg.graphium.stopdetection;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.srfg.graphium.core.exception.GraphNotExistsException;
import at.srfg.graphium.stopdetection.cluster.IAnalysisMethod;
import at.srfg.graphium.stopdetection.service.StopDetectionService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/application-context-graphium-stopdetection.xml",
		"classpath:/application-context-graphium-neo4j-test.xml",
		"classpath:/application-context-graphium-core.xml",
		"classpath:/application-context-graphium-neo4j-persistence.xml",
		"classpath:/application-context-graphium-neo4j-aliasing.xml",
		"classpath:/application-context-graphium-mapmatching.xml",
		"classpath:/application-context-graphium-mapmatching-neo4j.xml",
		"classpath:/application-context-graphium-routing-neo4j.xml"
})
public class TestGraphiumStopDetection {
	protected Logger log = Logger.getLogger(this.getClass().getName());

	@Resource(name="analysisYe")
	private IAnalysisMethod analysisMethod;

	@Resource(name="stopDetectionService")
	private StopDetectionService stopDetectionService;
	private String graphNameHighLevel = "gip_at_frc_0_4";
	private String graphNameLowLevel = "gip_at_frc_0_8";

	@Test
	public void testStopDetection() throws IOException, GraphNotExistsException {
		
		graphNameHighLevel = "gip_at_frc_0_4";
		graphNameLowLevel = "gip_at_frc_0_8";
		
		File gpxFile = new File("D:\\daten\\fcd\\2019-02_DelayCalc\\tracks\\20190508\\38261768withouttimezone.gpx");
		String csv = stopDetectionService.detectStops(new FileInputStream(gpxFile), graphNameHighLevel, graphNameLowLevel);
		System.out.print(csv);

		log.info("Finished!");
	}
}
