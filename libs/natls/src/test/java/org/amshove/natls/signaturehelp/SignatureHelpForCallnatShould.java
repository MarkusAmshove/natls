package org.amshove.natls.signaturehelp;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.testhelpers.IntegrationTest;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.amshove.natls.testlifecycle.SourceWithCursor.fromSourceWithCursor;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
class SignatureHelpForCallnatShould extends LanguageServerTest
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

	@BeforeEach
	void setupCalledModule()
	{
		createOrSaveFile("LIBONE", "CALLED.NSN", """
			DEFINE DATA
			PARAMETER USING APDA
			PARAMETER
			1 P-OPTIONAL (A10) OPTIONAL
			1 P-NUMBER (N12)
			END-DEFINE
			
			END
			""");
	}

	@Test
	void haveTheCompleteSignatureAsLabel() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForParameterList("${}$");
		var signature = help.getSignatures().get(0);
		assertThat(signature.getLabel()).isEqualTo("CALLED (USING APDA, P-OPTIONAL :(A10) OPTIONAL, P-NUMBER :(N12))");
	}

	@Test
	void haveTheFirstParameterActiveWhenCursorIsAfterModuleName() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForParameterList("${}$");
		var signature = help.getSignatures().get(0);

		assertThat(help.getActiveParameter()).isEqualTo(1);
		assertThat(signature.getParameters().get(0).getLabel().getLeft()).isEqualTo("USING APDA");
	}

	@Test
	void haveTheSecondParameterActiveWhenCursorIsAfterFirstParameter() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForParameterList("APDA${}$");

		assertThat(help.getActiveParameter()).isEqualTo(1);
	}

	@Test
	void haveTheSecondParameterActiveWhenCursorIsInTheSecondParameter() throws ExecutionException, InterruptedException, TimeoutException
	{
		var help = getSignatureHelpForParameterList("APDA 'Lit${}$eral'");

		assertThat(help.getActiveParameter()).isEqualTo(1);
	}

	private SignatureHelp getSignatureHelpForParameterList(String parameterList) throws ExecutionException, InterruptedException, TimeoutException
	{
		var sourceWithCursor = fromSourceWithCursor("""
			DEFINE DATA LOCAL
			END-DEFINE
						
			CALLNAT 'CALLED' %s
			""".formatted(parameterList));

		var caller = createOrSaveFile("LIBONE", "CALLER.NSN", sourceWithCursor);

		return testContext
			.documentService()
			.signatureHelp(new SignatureHelpParams(
				caller,
				sourceWithCursor.toSinglePosition())
			)
			.get();
	}
}
