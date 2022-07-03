package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.UnusedVariableAnalyzer;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@LspTest
public class RemoveUnusedVariableQuickfixShould extends CodeActionTest
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
		return new RemoveUnusedVariableQuickfix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Test
	void recognizeTheQuickfix()
	{
		var result = receiveCodeActions("LIBONE", "MEINS.NSN", """
			   DEFINE DATA
			   LOCAL
			   01 #U${}$NUSED (A10)
			   END-DEFINE
			   END
			""");

		var actions = result.codeActions();

		assertContainsCodeAction("Remove unused variable", actions);

		assertSingleCodeAction(actions)
			.deletesLine(2)
			.fixes(UnusedVariableAnalyzer.UNUSED_VARIABLE.getId());
	}

	@Test
	void deleteTheLineWithTheUnusedVariable()
	{
		var result = receiveCodeActions("LIBONE", "DELETE.NSN", """
			   DEFINE DATA
			   LOCAL
			   01 #U${NUS}$ED (A10)
			   END-DEFINE
			   END
			""");

		var actions = result.codeActions();

		assertSingleCodeAction(actions)
			.deletesLine(2)
			.fixes(UnusedVariableAnalyzer.UNUSED_VARIABLE.getId());
	}
}
