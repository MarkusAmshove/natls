package org.amshove.natls.progress;

public class StdOutProgressMonitor implements IProgressMonitor
{
	private int previousMessageLength;

	@Override
	public void progress(String message, int percentage)
	{
		var theMessage = "\r[%3d%%] %s".formatted(percentage, message);

		System.out.print("\r" + " ".repeat(previousMessageLength + 1));
		System.out.print(theMessage);

		previousMessageLength = theMessage.length();

		if(percentage == 100)
		{
			System.out.println();
		}
	}

	@Override
	public boolean isCancellationRequested()
	{
		return false;
	}
}
