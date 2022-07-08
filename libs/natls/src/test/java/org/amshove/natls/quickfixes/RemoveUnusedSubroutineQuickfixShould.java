package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.UnusedLocalSubroutineAnalyzer;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@LspTest
public class RemoveUnusedSubroutineQuickfixShould extends CodeActionTest
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
		return new RemoveUnusedSubroutineQuickfix();
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
		END-DEFINE

		DEFINE SUBROUTINE M${}$Y-SUB
		IGNORE
		WRITE 'A'
		IGNORE
		END-SUBROUTINE

		END
		""");

		var actions = result.codeActions();

		assertContainsCodeAction("Remove unused subroutine", actions);

		assertSingleCodeAction(actions)
			.deletesLines(4, 8)
			.fixes(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE.getId())
			.resultsApplied(result.savedSource(), """
				DEFINE DATA
				LOCAL
				END-DEFINE



				END
				""");
	}
}
