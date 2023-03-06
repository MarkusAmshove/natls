package org.amshove.natls.quickfixes;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CompressNumericQuickFixShould extends CodeActionTest
{
	@Test
	void addsNumericToACompressWhichUsesFloatingNumberTypes()
	{
		var result = receiveCodeActions("LIBONE", "SUBMOD.NSN", """
			DEFINE DATA
			LOCAL
			1 #VAR (N12,7)
			1 #TEXT (A) DYNAMIC
			END-DEFINE
			COM${}$PRESS #VAR INTO #TEXT
			END
			"""); // 1

		var actions = result.codeActions(); // 2

		assertContainsCodeAction("Add NUMERIC to COMPRESS", actions); // 3

		assertSingleCodeAction(actions) // 4
			.resultsApplied(result.savedSource(), """
			DEFINE DATA
			LOCAL
			1 #VAR (N12,7)
			1 #TEXT (A) DYNAMIC
			END-DEFINE
			COMPRESS NUMERIC #VAR INTO #TEXT
			END
			"""); // 5
	}

	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}

	@Override
	protected ICodeActionProvider getCodeActionUnderTest()
	{
		return new CompressNumericQuickFix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}
}
