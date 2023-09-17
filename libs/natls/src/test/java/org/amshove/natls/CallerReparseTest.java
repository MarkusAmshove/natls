package org.amshove.natls;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natparse.lexing.LexerError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CallerReparseTest extends LanguageServerTest
{
	private static LspTestContext testContext;

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Test
	void savingACopyCodeShouldTriggerReparseOnCaller(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;

		createOrSaveFile("LIBONE", "MYCC.NSC", """
			WRITE ''
		""");

		var subprog = createOrSaveFile("LIBONE", "MYSUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			INCLUDE MYCC
			END
		""");

		var subprogramFile = findLanguageServerFile(subprog);

		// Diagnostic from copy code is propagated to the subprogram
		assertThat(subprogramFile.allDiagnostics())
			.as("Expected diagnostic not found")
			.anyMatch(d -> d.getCode().getLeft().equals(LexerError.INVALID_STRING_LENGTH.id()));

		// Fix the diagnostic in the copy code
		createOrSaveFile("LIBONE", "MYCC.NSC", """
			WRITE 'Hi'
		""");

		// Diagnostic from copy code should not be present on subprogram anymore, because it was reparsed
		assertThat(subprogramFile.allDiagnostics())
			.as("Diagnostic shouldn't be present anymore")
			.noneMatch(d -> d.getCode().getLeft().equals(LexerError.INVALID_STRING_LENGTH.id()));
	}
}
