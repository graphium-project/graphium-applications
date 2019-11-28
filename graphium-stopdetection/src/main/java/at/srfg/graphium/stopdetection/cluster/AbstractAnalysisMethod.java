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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.srfg.graphium.stopdetection.model.impl.DetectedPlace;
import at.srfg.graphium.stopdetection.model.impl.Transition;

public abstract class AbstractAnalysisMethod {
	
	@Deprecated
	protected String analysisSubject = "";
	
	@Deprecated
	protected AnalysisMethod analysisMethod = AnalysisMethod.UNKNOWN;
	
	/** Stores the detected places identified by the analysis methods */
	protected List<DetectedPlace> detectedPlaces = null;
	/** Stores the transitions between detected places identified by the analysis methods */
	protected List<Transition> detectedTransitions = null;
	
	protected long runtime = 0l;
	protected int trackPoints = 0;
	protected int days = 0;
	
	public AbstractAnalysisMethod() { }
	
	/**
	 * Initializes analysis and quality evaluation
	 * @param homeDirectory
	 * @param analysisSubject
	 */
	public void initialize() {
		detectedPlaces = new ArrayList<DetectedPlace>();
		detectedTransitions = new ArrayList<Transition>();
		
		runtime = 0l;
	}
	
	public static double getSamplingInterval(Date timeStart, Date timeEnd, int indexEntry, int indexExit) {
		double duration = (timeEnd.getTime() - timeStart.getTime()) / 1000;
		int numberOfPoints = indexExit - indexEntry + 1;
		return duration / numberOfPoints;
	}
	
	@Deprecated
	public AnalysisMethod getAnalysisMethod() {
		return analysisMethod;
	}
	
	public String getAnalysisSubject() {
		return analysisSubject;
	}

	public void doAfterProcessing() { }
	
	/**
	 * @return runtime in ms
	 */
	public long getRuntime() {
		return runtime;
	}
	
	/**
	 * @return trackPoints
	 */
	public int getTrackPoints() {
		return trackPoints;
	}
	public void setTrackPoints(int trackPoints) {
		this.trackPoints = trackPoints;
	}
	
	/**
	 * @return days
	 */
	public int getDays() {
		return days;
	}
	public void setDays(int days) {
		this.days = days;
	}

	/**
	 * @return detectedPlaces
	 */
	public List<DetectedPlace> getDetectedPlaces() {
		return detectedPlaces;
	}

	public List<Transition> getDetectedTransitions() {
		return detectedTransitions;
	}
}
