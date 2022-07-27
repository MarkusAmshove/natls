package org.amshove.natls.quickfixes;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CodeConsistencyQuickfixShould extends CodeActionTest
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
		return new CodeConsistencyQuickfix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Test
	void changeOccurrenceToOcc()
	{
		var result = receiveCodeActions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #ARR (A2/*)
			1 #I-ARR (I4)
			END-DEFINE
						
			#I-ARR := *OCCURRE${}$NCE(#ARR)
						
			END
			""");

		assertContainsCodeAction("Replace *OCCURRENCE with *OCC", result.codeActions());

		assertSingleCodeAction(result.codeActions())
			.resultsApplied(result.savedSource(), """
				DEFINE DATA LOCAL
				1 #ARR (A2/*)
				1 #I-ARR (I4)
				END-DEFINE
							
				#I-ARR := *OCC(#ARR)
							
				END
					""");
	}
}
