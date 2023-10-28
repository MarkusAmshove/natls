package org.amshove.natls.progress;

public class NullProgressMonitor implements IProgressMonitor
{
	@Override
	public void progress(String message, int percentage)
	{
		// intentionally empty
	}

	@Override
	public void progress(String message)
	{
		// intentionally empty
	}

	@Override
	public boolean isCancellationRequested()
	{
		return false;
	}
}
