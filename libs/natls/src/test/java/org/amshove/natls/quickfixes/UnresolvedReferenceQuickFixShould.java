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
		assertCodeActionWithTitle("Declare local variable #NAME", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			END-DEFINE

			WRITE #N${}$AME

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
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
		assertCodeActionWithTitle("Declare local variable #NAME", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			LOCAL
			END-DEFINE

			WRITE #N${}$AME

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
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
		assertCodeActionWithTitle("Declare local variable #NAME", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			LOCAL
			1 #ANOTHERVAR (A10)
			END-DEFINE

			WRITE #N${}$AME

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
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

		assertCodeActionWithTitle("Add USING to DATAAREA (from LIBONE)", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			LOCAL
			END-DEFINE

			WRITE #IN-L${}$DA

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
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

		assertCodeActionWithTitle("Add USING to DATAAREA (from LIBONE)", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			END-DEFINE

			WRITE #IN-L${}$DA

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
				DEFINE DATA
				LOCAL USING DATAAREA
				END-DEFINE

				WRITE #IN-LDA

				END
				""");
	}

	@Test
	void addAVariableNeededByACopyCodeWhenTheCursorIsOnTheInclude()
	{
		createOrSaveFile("LIBONE", "THECC.NSC", """
			WRITE #THE-VAR-I-NEED
			""");

		assertCodeActionWithTitle("Declare local variable #THE-VAR-I-NEED", "LIBONE", "SUB.NSN", """
			DEFINE DATA
			LOCAL
			END-DEFINE
			   
			INCLUDE TH${}$ECC
			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.hasTitle("Declare local variable #THE-VAR-I-NEED")
			.resultsApplied("""
				DEFINE DATA
				LOCAL
				1 #THE-VAR-I-NEED (A) DYNAMIC
				END-DEFINE
				   
				INCLUDE THECC
				END
					""");
	}
}
