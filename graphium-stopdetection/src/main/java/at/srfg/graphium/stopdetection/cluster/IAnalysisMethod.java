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
package at.srfg.graphium.stopdetection.cluster;

import java.util.List;

import at.srfg.graphium.mapmatching.model.ITrack;
import at.srfg.graphium.stopdetection.model.impl.DetectedPlace;
import at.srfg.graphium.stopdetection.model.impl.Transition;

public interface IAnalysisMethod {
	void initialize();
	void initialize(Object params[]);
	void processTrack(ITrack track);
	List<DetectedPlace> processSingleTrack(ITrack track);
	void doAfterProcessing();
	
	List<DetectedPlace> getDetectedPlaces();
	List<Transition> getDetectedTransitions();
	
	@Deprecated
	AnalysisMethod getAnalysisMethod();
	@Deprecated
	String getAnalysisSubject();
	
	/**
	 * Returns name for the generated files to indicate the parameter combination
	 * @return file identifier
	 */
	@Deprecated
	String getFileIdentifier();
	
	long getRuntime();
	int getTrackPoints();
	@Deprecated
	void setTrackPoints(int trackPoints);
	int getDays();
	@Deprecated
	void setDays(int days);
}
