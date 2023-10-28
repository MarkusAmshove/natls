package org.amshove.natls.progress;

public interface IProgressMonitor
{
	/**
	 * Sends a progress notification with the given percentage.
	 */
	void progress(String message, int percentage);

	/**
	 * Increments the previous percentage (if below 100%) and sends a progress message.
	 */
	void progress(String message);

	boolean isCancellationRequested();
}
