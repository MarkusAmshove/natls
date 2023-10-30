package org.amshove.natls.progress;

import org.amshove.natls.languageserver.ClientMessage;
import org.eclipse.lsp4j.services.LanguageClient;

public class MessageProgressMonitor implements IProgressMonitor
{
	private final LanguageClient client;

	private int lastTenthPercentage = 0;
	private int previousPercentage = 0;

	public MessageProgressMonitor(LanguageClient client)
	{
		this.client = client;
	}

	@Override
	public void progress(String message)
	{
		progress(message, previousPercentage);
	}

	@Override
	public void progress(String message, int percentage)
	{
		previousPercentage = percentage;
		if (percentage / 10 > lastTenthPercentage)
		{
			// for client messages, only show every tenth entry to reduce spam
			client.showMessage(ClientMessage.log("%d%% %s".formatted(percentage, message)));
			lastTenthPercentage = percentage / 10;
		}
	}

	@Override
	public boolean isCancellationRequested()
	{
		return false;
	}
}
