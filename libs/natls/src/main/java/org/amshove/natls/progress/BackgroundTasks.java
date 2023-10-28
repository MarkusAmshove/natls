package org.amshove.natls.progress;

import org.amshove.natls.languageserver.ClientMessage;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackgroundTasks
{
	private static final Logger log = Logger.getAnonymousLogger();
	private static final ExecutorService workpool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private static LanguageClient client;

	private BackgroundTasks()
	{}

	public static CompletableFuture<Void> enqueue(Runnable runnable, String description)
	{
		var future = new CompletableFuture<Void>();
		workpool.submit(() ->
		{
			try
			{
				runnable.run();
				future.complete(null);
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "Background task <%s> threw an exception".formatted(description), e);
				client.showMessage(ClientMessage.error("Background task <%s> failed".formatted(description)));
				future.completeExceptionally(e);
			}
		});
		return future;
	}

	public static void initialize(LanguageClient client)
	{
		BackgroundTasks.client = client;
	}
}
