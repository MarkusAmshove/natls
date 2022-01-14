package org.amshove.natls.progress;

import org.amshove.natls.languageserver.ClientMessage;
import org.eclipse.lsp4j.services.LanguageClient;

public class MessageProgressMonitor implements IProgressMonitor
{
	private final LanguageClient client;

	private int lasteTenthPercentage = 0;

	public MessageProgressMonitor(LanguageClient client)
	{
		this.client = client;
	}

	@Override
	public void progress(String message, int percentage)
	{
		if(percentage / 10 > lasteTenthPercentage)
		{
			// for client messages, only show every tenth entry to reduce spam
			lasteTenthPercentage = percentage / 10;
			client.showMessage(ClientMessage.log("%d%% %s".formatted(percentage, message)));
		}
	}

	@Override
	public boolean isCancellationRequested()
	{
		return false;
	}
}
