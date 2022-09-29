package org.amshove.natls.signaturehelp;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.testhelpers.IntegrationTest;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.amshove.natls.testlifecycle.SourceWithCursor.fromSourceWithCursor;

@IntegrationTest
abstract class SignatureHelpTest extends LanguageServerTest
{
	private static LspTestContext testContext;

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@BeforeAll
	static void beforeAll(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}

	protected abstract String getCalledModuleFilename();

	protected abstract String getCalledModuleSource();

	@BeforeEach
	void setupCalledModule()
	{
		createOrSaveFile("LIBONE", getCalledModuleFilename(), getCalledModuleSource());
	}

	protected SignatureHelp getSignatureHelpForParameterList(String call) throws ExecutionException, InterruptedException, TimeoutException
	{
		var sourceWithCursor = fromSourceWithCursor("""
			DEFINE DATA LOCAL
			END-DEFINE
						
			%s
			END
			""".formatted(call));

		var caller = createOrSaveFile("LIBONE", "CALLER.NSN", sourceWithCursor);

		return testContext
			.documentService()
			.signatureHelp(new SignatureHelpParams(
				caller,
				sourceWithCursor.toSinglePosition())
			)
			.get(5, TimeUnit.SECONDS);
	}
}
