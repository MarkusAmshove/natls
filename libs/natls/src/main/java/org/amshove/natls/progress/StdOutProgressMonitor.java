package org.amshove.natls.progress;

public class StdOutProgressMonitor implements IProgressMonitor
{
	@Override
	public void progress(String message, int percentage)
	{
		System.out.printf("\r[%s%%] %s", String.format("%3d", percentage), message);
	}

	@Override
	public boolean isCancellationRequested()
	{
		return false;
	}
}
