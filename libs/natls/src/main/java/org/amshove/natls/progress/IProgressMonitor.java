package org.amshove.natls.progress;

public interface IProgressMonitor
{
	void progress(String message, int percentage);
	boolean isCancellationRequested();
}
