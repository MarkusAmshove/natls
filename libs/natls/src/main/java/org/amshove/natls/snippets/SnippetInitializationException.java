package org.amshove.natls.snippets;

public class SnippetInitializationException extends RuntimeException
{
	public SnippetInitializationException(String cause)
	{
		super("Can't initialize snippet: %s".formatted(cause));
	}
}
