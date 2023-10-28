package org.amshove.natls.config;

public class LSConfiguration
{

	private CompletionConfiguration completion;
	private InlayHintsConfiguration inlayhints;
	private InitilizationConfiguration initialization;

	public static LSConfiguration createDefault()
	{
		var config = new LSConfiguration();

		var completion = new CompletionConfiguration();
		completion.setQualify(false);
		config.setCompletion(completion);

		var inlay = new InlayHintsConfiguration();
		inlay.setShowAssignmentTargetType(false);
		config.setInlayhints(inlay);

		var init = new InitilizationConfiguration();
		init.setAsync(false);
		config.setInitialization(init);

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

	public InitilizationConfiguration getInitialization()
	{
		return initialization;
	}

	public void setInitialization(InitilizationConfiguration initialization)
	{
		this.initialization = initialization;
	}
}
