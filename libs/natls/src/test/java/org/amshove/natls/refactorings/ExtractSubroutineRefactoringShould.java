package org.amshove.natls.refactorings;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ExtractSubroutineRefactoringShould extends CodeActionTest
{
	@Test
	void extractASubroutineFromASingleStatement()
	{
		assertCodeActionWithTitle(
			"Extract inline subroutine", "LIBONE", "SUBN.NSN",
			"""
				DEFINE DATA LOCAL
				END-DEFINE
				${WRITE 'HI'}$
				END
				"""
		)
			.resultsApplied("""
				DEFINE DATA LOCAL
				END-DEFINE
				PERFORM EXTRACTED
				/***********************************************************************
				DEFINE SUBROUTINE EXTRACTED
				/***********************************************************************

				WRITE 'HI'


				END-SUBROUTINE

				END
				""");
	}

	@Test
	void extractASubroutineFromASingleStatementWhenCursorIsNotOnWholeStatement()
	{
		assertCodeActionWithTitle(
			"Extract inline subroutine", "LIBONE", "SUBN.NSN",
			"""
				DEFINE DATA LOCAL
				END-DEFINE
				WRI${TE 'HI'}$
				END
				"""
		)
			.resultsApplied("""
				DEFINE DATA LOCAL
				END-DEFINE
				PERFORM EXTRACTED
				/***********************************************************************
				DEFINE SUBROUTINE EXTRACTED
				/***********************************************************************

				WRITE 'HI'


				END-SUBROUTINE

				END
				""");
	}

	@Test
	void extractASubroutineFromMultipleStatements()
	{
		assertCodeActionWithTitle(
			"Extract inline subroutine", "LIBONE", "SUBN.NSN",
			"""
				DEFINE DATA LOCAL
				END-DEFINE
				${WRITE 'Hello'
				WRITE 'World'}$
				END
				"""
		)
			.resultsApplied("""
				DEFINE DATA LOCAL
				END-DEFINE
				PERFORM EXTRACTED
				/***********************************************************************
				DEFINE SUBROUTINE EXTRACTED
				/***********************************************************************

				WRITE 'Hello'
				WRITE 'World'


				END-SUBROUTINE

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
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Override
	protected ICodeActionProvider getCodeActionUnderTest()
	{
		return new ExtractSubroutineRefactoring();
	}
}
