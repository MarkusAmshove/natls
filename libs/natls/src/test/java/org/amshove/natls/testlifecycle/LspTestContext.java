package org.amshove.natls.testlifecycle;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natls.progress.ProgressTasks;
import org.amshove.natls.project.LanguageServerProject;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public record LspTestContext(
	LanguageServerProject project,
	StubClient client,
	NaturalLanguageServer server,
	NaturalLanguageService languageService
)
{
	public TextDocumentService documentService()
	{
		return server.getTextDocumentService();
	}

	public WorkspaceService workspaceService()
	{
		return server.getWorkspaceService();
	}

	@Override
	public LanguageServerProject project()
	{
		return languageService.getProject();
	}

	public StubClient getClient()
	{
		return client;
	}

	public void waitForRunningTasksToFinish()
	{
		try
		{
			CompletableFuture.allOf(ProgressTasks.getRunningTasks().toArray(new CompletableFuture[0]))
				.get(30, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Tasks did not finish in time", e);
		}
	}
}
