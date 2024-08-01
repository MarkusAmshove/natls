package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.VariableReferenceAnalyzer;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@LspTest
class RemoveUnusedVariableQuickfixShould extends CodeActionTest
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
		assertCodeActionWithTitle("Remove unused variable", "LIBONE", "MEINS.NSN", """
			   DEFINE DATA
			   LOCAL
			   01 #U${}$NUSED (A10)
			   END-DEFINE
			   END
			""")
			.deletesLine(2)
			.fixes(VariableReferenceAnalyzer.UNUSED_VARIABLE.getId())
			.resultsApplied("""
			   DEFINE DATA
			   LOCAL
			   END-DEFINE
			   END
			""");
	}

	@Test
	void deleteTheLineWithTheUnusedVariable()
	{
		assertCodeActionWithTitle("Remove unused variable", "LIBONE", "DELETE.NSN", """
			   DEFINE DATA
			   LOCAL
			   01 #U${NUS}$ED (A10)
			   END-DEFINE
			   END
			""")
			.deletesLine(2)
			.fixes(VariableReferenceAnalyzer.UNUSED_VARIABLE.getId());
	}

	@Test
	void removeAllUnusedImportsAndVariables()
	{
		assertCodeActionWithTitle("Remove all unused symbols in DEFINE DATA", "LIBONE", "SUB.NSN", """
			DEFINE DATA
			LOCAL
			1 #VAR (A1)
			1 #V${}$AR1 (A1)
			1 #VAR2 (A1)
			1 #VAR3 (A1)
			END-DEFINE
			END
			""")
			.resultsApplied("""
				DEFINE DATA
				LOCAL
				END-DEFINE
				END
				""");
	}
}
