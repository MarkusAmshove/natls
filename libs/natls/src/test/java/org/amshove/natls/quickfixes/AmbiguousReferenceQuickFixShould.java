package org.amshove.natls.quickfixes;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natparse.parsing.ParserError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@LspTest
public class AmbiguousReferenceQuickFixShould extends CodeActionTest
{
	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}

	@Override
	protected ICodeActionProvider getCodeActionUnderTest()
	{
		return new AmbiguousReferenceQuickFix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Test
	void recognizeTheQuickfix()
	{
		var actions = receiveCodeActions("LIBONE", "MEINS.NSN", """
			   DEFINE DATA
			   LOCAL
			   1 #AGROUP
			   2 #THEVAR (A5)
			   1 #TWOGROUP
			   2 #THEVAR (A5)
			   END-DEFINE

			   WRITE #THEV${}$AR
			   END
			""");

		assertContainsCodeAction("Use #AGROUP.#THEVAR", actions);
		assertContainsCodeAction("Use #TWOGROUP.#THEVAR", actions);
	}

	@Test
	void applyTheQuickfixes()
	{
		var actions = receiveCodeActions("LIBONE", "MEINS.NSN", """
			   DEFINE DATA
			   LOCAL
			   1 #AGROUP
			   2 #THEVAR (A5)
			   1 #TWOGROUP
			   2 #THEVAR (A5)
			   END-DEFINE

			   WRITE #THEV${}$AR
			   END
			""");

		assertContainsCodeAction("Use #AGROUP.#THEVAR", actions);
		assertContainsCodeAction("Use #TWOGROUP.#THEVAR", actions);

		assertCodeAction(actions.get(0))
			.fixes(ParserError.AMBIGUOUS_VARIABLE_REFERENCE.id())
			.insertsText(7, 9, "#AGROUP.#THEVAR");
		assertCodeAction(actions.get(1))
			.fixes(ParserError.AMBIGUOUS_VARIABLE_REFERENCE.id())
			.insertsText(7, 9, "#TWOGROUP.#THEVAR");
	}
}
