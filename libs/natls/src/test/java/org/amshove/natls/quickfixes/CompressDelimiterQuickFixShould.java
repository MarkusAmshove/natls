package org.amshove.natls.quickfixes;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CompressDelimiterQuickFixShould extends CodeActionTest
{
	@Test
	void addAllToACompress()
	{
		var result = receiveCodeActions("LIBONE", "SUBMOD.NSN", """
			DEFINE DATA
			LOCAL
			1 #VAR (N12,7)
			1 #TEXT (A) DYNAMIC
			END-DEFINE
			COM${}$PRESS #VAR INTO #TEXT WITH DELIMITER ';'
			END
			""");

		var actions = result.codeActions();

		assertContainsCodeAction("Add ALL to COMPRESS", actions);

		assertSingleCodeAction(actions)
			.resultsApplied(result.savedSource(), """
			DEFINE DATA
			LOCAL
			1 #VAR (N12,7)
			1 #TEXT (A) DYNAMIC
			END-DEFINE
			COMPRESS #VAR INTO #TEXT WITH ALL DELIMITER ';'
			END
			""");
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
		return new CompressDelimiterQuickFix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}
}
