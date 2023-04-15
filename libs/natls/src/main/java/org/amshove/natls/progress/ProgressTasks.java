package org.amshove.natls.progress;

import org.amshove.natls.languageserver.ClientMessage;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class ProgressTasks
{
	private static final ConcurrentMap<String, IProgressMonitor> runningTasks = new ConcurrentHashMap<>();
	private static ClientProgressType progressType = ClientProgressType.MESSAGE;

	public static void setClientProgressType(ClientProgressType type)
	{
		progressType = type;
	}

	public static CompletableFuture<Void> startNewVoid(String title, LanguageClient client, Consumer<IProgressMonitor> task)
	{
		if (progressType == ClientProgressType.WORK_DONE)
		{
			return startNewWorkDone(title, client, wrapVoid(task));
		}
		else
		{
			return startNewMessageBased(title, client, wrapVoid(task));
		}
	}

	public static <T> CompletableFuture<T> startNew(String title, LanguageClient client, Function<IProgressMonitor, T> task)
	{
		if (progressType == ClientProgressType.WORK_DONE)
		{
			return startNewWorkDone(title, client, task);
		}
		else
		{
			return startNewMessageBased(title, client, task);
		}
	}

	private static Function<IProgressMonitor, Void> wrapVoid(Consumer<IProgressMonitor> task)
	{
		return m ->
		{
			task.accept(m);
			return null;
		};
	}

	private static <T> CompletableFuture<T> startNewMessageBased(String title, LanguageClient client, Function<IProgressMonitor, T> task)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			var taskId = UUID.randomUUID().toString();
			var progressMonitor = new MessageProgressMonitor(client);
			runningTasks.put(taskId, progressMonitor);
			try
			{
				client.showMessage(ClientMessage.log(title));
				return task.apply(progressMonitor);
			}
			catch (Exception e)
			{
				System.err.printf("Error in task %s: %s%n", taskId, e.getMessage());
				return null;
			}
			finally
			{
				client.showMessage(ClientMessage.log("%s done".formatted(title)));
			}
		});
	}

	private static <T> CompletableFuture<T> startNewWorkDone(String title, LanguageClient client, Function<IProgressMonitor, T> task)
	{
		var taskId = UUID.randomUUID().toString();
		var params = new WorkDoneProgressCreateParams();
		params.setToken(taskId);
		var progressMonitor = new WorkDoneProgressMonitor(taskId, client);
		runningTasks.put(taskId, progressMonitor);
		return client.createProgress(params).thenApply((ignored) ->
		{
			try
			{
				var begin = new WorkDoneProgressBegin();
				begin.setTitle(title);
				begin.setCancellable(true);
				begin.setMessage(title);
				client.notifyProgress(new ProgressParams(Either.forLeft(taskId), Either.forLeft(begin)));
				return task.apply(progressMonitor);
			}
			catch (Exception e)
			{
				System.err.printf("Error in task %s: %s%n", taskId, e.getMessage());
				return null;
			}
			finally
			{
				runningTasks.remove(taskId);
				var end = new WorkDoneProgressEnd();
				end.setMessage("Done");
				client.notifyProgress(new ProgressParams(Either.forLeft(taskId), Either.forLeft(end)));
			}
		});
	}
}
