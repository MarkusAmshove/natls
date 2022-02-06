package org.amshove.natls.testlifecycle;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natls.project.LanguageServerProject;

public record LspTestContext(
	LanguageServerProject project,
	StubClient client,
	NaturalLanguageServer server,
	NaturalLanguageService languageService)
{
}
