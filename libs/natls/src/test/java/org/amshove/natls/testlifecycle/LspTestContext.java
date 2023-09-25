package org.amshove.natls.testlifecycle;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natls.project.LanguageServerProject;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

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
}
