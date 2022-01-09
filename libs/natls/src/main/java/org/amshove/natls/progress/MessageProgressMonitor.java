package org.amshove.natls.progress;

import org.amshove.natls.languageserver.ClientMessage;
import org.eclipse.lsp4j.services.LanguageClient;

public class MessageProgressMonitor implements IProgressMonitor
{
	private final LanguageClient client;

	public MessageProgressMonitor(LanguageClient client)
	{
		this.client = client;
	}

	@Override
	public void progress(String message, int percentage)
	{
		client.showMessage(ClientMessage.log("%d%% %s".formatted(percentage, message)));
	}

	@Override
	public boolean isCancellationRequested()
	{
		return false;
	}
}
