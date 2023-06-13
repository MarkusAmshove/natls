package org.amshove.natls.config;

public class LSConfiguration
{

	private CompletionConfiguration completion;

	public CompletionConfiguration getCompletion()
	{
		return completion;
	}

	public void setCompletion(CompletionConfiguration completion)
	{
		this.completion = completion;
	}

	public static LSConfiguration createDefault()
	{
		var config = new LSConfiguration();

		var completion = new CompletionConfiguration();
		completion.setQualify(false);

		config.setCompletion(completion);
		return config;
	}
}
