package org.amshove.natls.config;

public class LSConfiguration
{

	private CompletionConfiguration completion;
	private InlayHintsConfiguration inlayhints;

	public static LSConfiguration createDefault()
	{
		var config = new LSConfiguration();

		var completion = new CompletionConfiguration();
		completion.setQualify(false);
		config.setCompletion(completion);

		var inlay = new InlayHintsConfiguration();
		inlay.setShowAssignmentTargetType(false);
		config.setInlayhints(inlay);

		return config;
	}

	public CompletionConfiguration getCompletion()
	{
		return completion;
	}

	public void setCompletion(CompletionConfiguration completion)
	{
		this.completion = completion;
	}

	public InlayHintsConfiguration getInlayhints()
	{
		return inlayhints;
	}

	public void setInlayhints(InlayHintsConfiguration inlayhints)
	{
		this.inlayhints = inlayhints;
	}
}
