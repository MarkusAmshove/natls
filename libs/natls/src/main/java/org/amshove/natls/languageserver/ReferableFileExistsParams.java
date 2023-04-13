package org.amshove.natls.languageserver;

@SuppressWarnings("all")
public class ReferableFileExistsParams
{
	private String library;
	private String referableName;

	public ReferableFileExistsParams(String library, String referableName)
	{
		this.library = library;
		this.referableName = referableName;
	}

	public String getLibrary()
	{
		return library;
	}

	public String getReferableName()
	{
		return referableName;
	}
}
