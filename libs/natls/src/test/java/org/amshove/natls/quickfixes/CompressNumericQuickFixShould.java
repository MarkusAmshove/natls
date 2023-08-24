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
		assertCodeActionWithTitle("Add NUMERIC to COMPRESS", "LIBONE", "SUBMOD.NSN", """
			DEFINE DATA
			LOCAL
			1 #VAR (N12,7)
			1 #TEXT (A) DYNAMIC
			END-DEFINE
			COM${}$PRESS #VAR INTO #TEXT
			END
			""")
			.resultsApplied("""
			DEFINE DATA
			LOCAL
			1 #VAR (N12,7)
			1 #TEXT (A) DYNAMIC
			END-DEFINE
			COMPRESS NUMERIC #VAR INTO #TEXT
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
		return new CompressNumericQuickFix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}
}
