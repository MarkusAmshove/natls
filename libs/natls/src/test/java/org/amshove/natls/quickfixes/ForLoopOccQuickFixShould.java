package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.ForLoopAnalyzer;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ForLoopOccQuickFixShould extends CodeActionTest
{
	@Test
	void createAVariableForTheUpperBound()
	{
		assertCodeActionWithTitle("Use a variable for the upper bound", "LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #I (I4)
			1 #ARR (A8/*)
			END-DEFINE
			
			FOR #I := 1 TO *OC${}$C(#ARR)
			IGNORE
			END-FOR
			END
			""")
			.fixes(ForLoopAnalyzer.UPPER_BOUND_OCC.getId())
			.resultsApplied("""
				DEFINE DATA LOCAL
				1 #S-#ARR (I4)
				1 #I (I4)
				1 #ARR (A8/*)
				END-DEFINE
				
				#S-#ARR := *OCC(#ARR)
				FOR #I := 1 TO #S-#ARR
				IGNORE
				END-FOR
				END
				""");
	}

	@Test
	void notAddAnotherIterationVariableIfItAlreadyExists()
	{
		assertCodeActionWithTitle("Use a variable for the upper bound", "LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #I (I4)
			1 #ARR (A8/*)
			1 #S-#ARR (I4)
			END-DEFINE
			
			FOR #I := 1 TO *OC${}$C(#ARR)
			IGNORE
			END-FOR
			END
			""")
			.fixes(ForLoopAnalyzer.UPPER_BOUND_OCC.getId())
			.resultsApplied("""
				DEFINE DATA LOCAL
				1 #I (I4)
				1 #ARR (A8/*)
				1 #S-#ARR (I4)
				END-DEFINE
				
				#S-#ARR := *OCC(#ARR)
				FOR #I := 1 TO #S-#ARR
				IGNORE
				END-FOR
				END
				""");
	}

	@Test
	void keepTheQualificationOfTheArrayAsBefore()
	{
		assertCodeActionWithTitle("Use a variable for the upper bound", "LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #I (I4)
			1 #GRP
			  2 #ARR (A8/*)
			END-DEFINE
			
			FOR #I := 1 TO *OC${}$C(#GRP.#ARR)
			IGNORE
			END-FOR
			END
			""")
			.fixes(ForLoopAnalyzer.UPPER_BOUND_OCC.getId())
			.resultsApplied("""
				DEFINE DATA LOCAL
				1 #S-#GRP-#ARR (I4)
				1 #I (I4)
				1 #GRP
				  2 #ARR (A8/*)
				END-DEFINE
				
				#S-#GRP-#ARR := *OCC(#GRP.#ARR)
				FOR #I := 1 TO #S-#GRP-#ARR
				IGNORE
				END-FOR
				END
				""");

	}

	@Override
	protected ICodeActionProvider getCodeActionUnderTest()
	{
		return new ForLoopOccQuickFix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}
}
