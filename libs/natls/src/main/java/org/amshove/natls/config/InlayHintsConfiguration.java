package org.amshove.natls.config;

public class InlayHintsConfiguration
{
	private boolean showAssignmentTargetType = false;
	private boolean showSkippedParameter = true;

	public boolean isShowAssignmentTargetType()
	{
		return showAssignmentTargetType;
	}

	public void setShowAssignmentTargetType(boolean showAssignmentTargetType)
	{
		this.showAssignmentTargetType = showAssignmentTargetType;
	}

	public boolean isShowSkippedParameter()
	{
		return showSkippedParameter;
	}

	public void setShowSkippedParameter(boolean showSkippedParameter)
	{
		this.showSkippedParameter = showSkippedParameter;
	}
}
