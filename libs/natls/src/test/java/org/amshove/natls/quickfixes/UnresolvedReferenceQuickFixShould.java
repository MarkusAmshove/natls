package org.amshove.natls.quickfixes;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natparse.parsing.ParserError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UnresolvedReferenceQuickFixShould extends CodeActionTest
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
		return new UnresolvedReferenceQuickFix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Test
	void addAVariableToDefineDataWhenNoVariablesArePresent()
	{
		var result = receiveCodeActions("LIBONE", "MEINS.NSN", """
			DEFINE DATA
			END-DEFINE

			WRITE #N${}$AME

			END
			""");

		var actions = result.codeActions();

		assertContainsCodeAction("Declare variable #NAME", actions);

		assertSingleCodeAction(actions)
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied(result.savedSource(), """
				DEFINE DATA
				LOCAL
				1 #NAME (A) DYNAMIC
				END-DEFINE

				WRITE #NAME

				END
				""");
	}

	@Test
	void addAVariableToDefineDataWhenNoVariablesButAScopeArePresent()
	{
		var result = receiveCodeActions("LIBONE", "MEINS.NSN", """
			DEFINE DATA
			LOCAL
			END-DEFINE

			WRITE #N${}$AME

			END
			""");

		var actions = result.codeActions();

		assertContainsCodeAction("Declare variable #NAME", actions);

		assertSingleCodeAction(actions)
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied(result.savedSource(), """
				DEFINE DATA
				LOCAL
				1 #NAME (A) DYNAMIC
				END-DEFINE

				WRITE #NAME

				END
				""");
	}

	@Test
	void addAVariableToDefineDataWhenAnotherVariableIsAlreadyPresent()
	{
		var result = receiveCodeActions("LIBONE", "MEINS.NSN", """
			DEFINE DATA
			LOCAL
			1 #ANOTHERVAR (A10)
			END-DEFINE

			WRITE #N${}$AME

			END
			""");

		var actions = result.codeActions();

		assertContainsCodeAction("Declare variable #NAME", actions);

		assertSingleCodeAction(actions)
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied(result.savedSource(), """
				DEFINE DATA
				LOCAL
				1 #NAME (A) DYNAMIC
				1 #ANOTHERVAR (A10)
				END-DEFINE

				WRITE #NAME

				END
				""");
	}

	@Test
	void addAUsingIfAnUnresolvedVariableCanBeFoundInADataAreaAndAScopeIsAlreadyPresent()
	{
		createOrSaveFile("LIBONE", "DATAAREA.NSA", """
			DEFINE DATA
			LOCAL
			1 #IN-LDA (A5)
			END-DEFINE
			""");

		var result = receiveCodeActions("LIBONE", "MEINS.NSN", """
			DEFINE DATA
			LOCAL
			END-DEFINE

			WRITE #IN-L${}$DA

			END
			""");

		var actions = result.codeActions();

		assertContainsCodeAction("Add USING to DATAAREA (from LIBONE)", actions);

		assertCodeAction(actions.get(0))
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied(result.savedSource(), """
				DEFINE DATA
				LOCAL USING DATAAREA
				LOCAL
				END-DEFINE

				WRITE #IN-LDA

				END
				""");
	}

	@Test
	void addAUsingIfAnUnresolvedVariableCanBeFoundInADataAreaAndNoScopeIsPresent()
	{
		createOrSaveFile("LIBONE", "DATAAREA.NSA", """
			DEFINE DATA
			LOCAL
			1 #IN-LDA (A5)
			END-DEFINE
			""");

		var result = receiveCodeActions("LIBONE", "MEINS.NSN", """
			DEFINE DATA
			END-DEFINE

			WRITE #IN-L${}$DA

			END
			""");

		var actions = result.codeActions();

		assertContainsCodeAction("Add USING to DATAAREA (from LIBONE)", actions);

		assertCodeAction(actions.get(0))
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied(result.savedSource(), """
				DEFINE DATA
				LOCAL USING DATAAREA
				END-DEFINE

				WRITE #IN-LDA

				END
				""");
	}
}
