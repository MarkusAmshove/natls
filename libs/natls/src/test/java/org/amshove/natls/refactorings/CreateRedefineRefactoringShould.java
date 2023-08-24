package org.amshove.natls.refactorings;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CreateRedefineRefactoringShould extends CodeActionTest
{
	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Override
	protected ICodeActionProvider getCodeActionUnderTest()
	{
		return new CreateRedefineRefactoring();
	}

	@Test
	void beApplicableWhenHoveringALocalVariable()
	{
		assertCodeActionWithTitle(
			"Redefine #MY-VAR",
			"LIBONE",
			"SUBN.NSN",
			"""
				    DEFINE DATA
				    LOCAL
				    1 #MY${}$-VAR (A10)
				    END-DEFINE
				    WRITE #MY-VAR
				    END
				"""
		);
	}

	@Test
	void notBeApplicableWhenHoveringOverAParameter()
	{
		assertNoCodeAction(
			"LIBONE",
			"SUBN.NSN",
			""" 
				DEFINE DATA
				PARAMETER 
				1 #MY${}$-VAR (A10) 
				END-DEFINE 
				WRITE #MY-VAR 
				END 
				"""
		);
	}

	@Test
	void notBeApplicableWhenHoveringOverAGlobalVariable()
	{
		assertNoCodeAction(
			"LIBONE",
			"SUBN.NSN",
			""" 
				DEFINE DATA
				GLOBAL 
				1 #MY${}$-VAR (A10) 
				END-DEFINE 
				END 
				"""
		);
	}

	@Test
	void notBeApplicableWhenHoveringOverAnIndependentVariable()
	{
		assertNoCodeAction(
			"LIBONE",
			"SUBN.NSN",
			""" 
				DEFINE DATA
				INDEPENDENT 
				1 +MY${}$-VAR (A10) 
				END-DEFINE 
				END 
				"""
		);
	}

	@Test
	void addARedefinitionWithOneVariableOfSameType()
	{
		assertCodeActionWithTitle(
			"Redefine #MY-VAR",
			"LIBONE",
			"SUBN.NSN",
			"""
				    DEFINE DATA
				    LOCAL
				    1 #MY${}$-VAR (A10)
				    END-DEFINE
				    WRITE #MY-VAR
				    END
				"""
		)
			.insertsText(3, 0, "1 REDEFINE #MY-VAR\n2 #R-#MY-VAR (A10)\n");
	}
}
