package org.amshove.natls.progress;

import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

public class WorkDoneProgressMonitor implements IProgressMonitor
{
	private final String taskId;
	private final LanguageClient client;

	public WorkDoneProgressMonitor(String taskId, LanguageClient client)
	{
		this.taskId = taskId;
		this.client = client;
	}

	@Override
	public void progress(String message, int percentage)
	{
		var report = new WorkDoneProgressReport();
		report.setPercentage(percentage);
		report.setMessage(message);
		report.setCancellable(true);

		client.notifyProgress(new ProgressParams(Either.forLeft(taskId), Either.forLeft(report)));
	}

	@Override
	public boolean isCancellationRequested()
	{
		return false;
	}
}
