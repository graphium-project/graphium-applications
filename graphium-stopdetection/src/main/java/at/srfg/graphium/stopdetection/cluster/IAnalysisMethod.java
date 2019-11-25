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
