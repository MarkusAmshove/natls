package org.amshove.natls.codelens;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.testhelpers.IntegrationTest;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
abstract class CodeLensTest extends LanguageServerTest
{
	protected void testCodeLens(TextDocumentIdentifier identifier, Consumer<List<? extends CodeLens>> test)
	{
		var params = new CodeLensParams(identifier);
		assertThat(getContext().documentService().codeLens(params))
			.succeedsWithin(5, TimeUnit.SECONDS)
			.satisfies(test);
	}

	private LspTestContext testContext;

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@BeforeEach
	void beforeEach(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}
}
