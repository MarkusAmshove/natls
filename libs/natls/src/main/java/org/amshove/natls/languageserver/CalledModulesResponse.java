package org.amshove.natls.languageserver;

import java.util.List;

@SuppressWarnings("all")
public class CalledModulesResponse
{
	private List<String> uris;

	public CalledModulesResponse(List<String> uris)
	{
		this.uris = uris;
	}

	public List<String> getUris()
	{
		return uris;
	}

	public void setUris(List<String> uris)
	{
		this.uris = uris;
	}
}
