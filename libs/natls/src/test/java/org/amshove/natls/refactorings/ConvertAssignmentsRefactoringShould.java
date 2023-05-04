package org.amshove.natls.refactorings;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConvertAssignmentsRefactoringShould extends CodeActionTest
{

	@ParameterizedTest
	@ValueSource(
		strings =
		{
			"COM${}$PUTE", "AS${}$SIGN"
		}
	)
	void refactorSimpleAssignAndComputeToAssignment(String keyword)
	{
		assertCodeActionWithTitle(
			"Convert to assignment",
			"LIBONE",
			"SUB.NSN",
			"""
				DEFINE DATA LOCAL
				1 #VAR (N12)
				END-DEFINE
											
				%s #VAR = 5
				END
				""".formatted(keyword)
		)
			.resultsApplied("""
				DEFINE DATA LOCAL
				1 #VAR (N12)
				END-DEFINE
								
				#VAR := 5
				END
				""");
	}

	@ParameterizedTest
	@ValueSource(
		strings =
		{
			"COM${}$PUTE", "AS${}$SIGN"
		}
	)
	void notBeApplicableWhenRoundedIsPresent(String keyword)
	{
		assertNoCodeActionWithTitle(
			"Convert to assignment", "LIBONE", "SUB.NSN",
			"""
				DEFINE DATA LOCAL
				1 #VAR (N12)
				END-DEFINE
											
				%s ROUNDED #VAR = 5
				END
				""".formatted(keyword)
		);
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
		return new ConvertAssignmentsRefactoring();
	}

}
