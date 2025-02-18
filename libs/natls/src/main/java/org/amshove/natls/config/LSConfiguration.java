package org.amshove.natls.config;

public class LSConfiguration
{

	private CompletionConfiguration completion = new CompletionConfiguration();
	private InlayHintsConfiguration inlayhints = new InlayHintsConfiguration();
	private InitilizationConfiguration initialization = new InitilizationConfiguration();
	private MapsConfiguration maps = new MapsConfiguration();

	public static LSConfiguration createDefault()
	{
		return new LSConfiguration();
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

	public MapsConfiguration getMaps()
	{
		return maps;
	}

	public void setMaps(MapsConfiguration maps)
	{
		this.maps = maps;
	}
}
