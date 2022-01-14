package org.amshove.natls.progress;

public class NullProgressMonitor implements IProgressMonitor
{
	@Override
	public void progress(String message, int percentage)
	{

	}

	@Override
	public boolean isCancellationRequested()
	{
		return false;
	}
}
