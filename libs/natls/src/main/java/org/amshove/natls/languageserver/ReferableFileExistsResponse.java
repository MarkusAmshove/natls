package org.amshove.natls.languageserver;

@SuppressWarnings("all")
public class ReferableFileExistsResponse
{
	private boolean fileAlreadyExists;

	public ReferableFileExistsResponse(boolean exists)
	{
		fileAlreadyExists = exists;
	}

	public boolean getFileAlreadyExists()
	{
		return fileAlreadyExists;
	}
}
