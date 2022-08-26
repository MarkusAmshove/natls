package org.amshove.natls.testlifecycle;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natls.project.LanguageServerProject;
import org.eclipse.lsp4j.services.TextDocumentService;

public record LspTestContext(
	LanguageServerProject project,
	StubClient client,
	NaturalLanguageServer server,
	NaturalLanguageService languageService)
{
	public TextDocumentService documentService()
	{
		return server.getTextDocumentService();
	}
}
