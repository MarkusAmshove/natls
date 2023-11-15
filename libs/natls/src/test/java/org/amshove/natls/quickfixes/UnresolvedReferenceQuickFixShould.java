package org.amshove.natls.quickfixes;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natparse.parsing.ParserError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
	void addAParameterToDefineDataWhenNoVariablesArePresent()
	{
		assertCodeActionWithTitle("Declare parameter #NAME", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			END-DEFINE

			WRITE #N${}$AME

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
				DEFINE DATA
				PARAMETER
				1 #NAME (A) DYNAMIC
				END-DEFINE

				WRITE #NAME

				END
				""");
	}

	@Test
	void addAParameterToDefineDataWhenOtherVariablesArePresent()
	{
		assertCodeActionWithTitle("Declare parameter #NAME", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			LOCAL
			1 #VAR1 (A10)
			END-DEFINE

			WRITE #N${}$AME

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
				DEFINE DATA
				PARAMETER
				1 #NAME (A) DYNAMIC
				LOCAL
				1 #VAR1 (A10)
				END-DEFINE

				WRITE #NAME

				END
				""");
	}

	@Test
	void addAParameterToDefineDataWhenOtherParametersArePresent()
	{
		assertCodeActionWithTitle("Declare parameter #NAME", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			PARAMETER
			1 #PARM1 (A10)
			END-DEFINE

			WRITE #N${}$AME

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
				DEFINE DATA
				PARAMETER
				1 #PARM1 (A10)
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

		assertCodeActionWithTitle("Add LOCAL USING to DATAAREA (from LIBONE)", "LIBONE", "MEINS.NSN", """
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

		assertCodeActionWithTitle("Add LOCAL USING to DATAAREA (from LIBONE)", "LIBONE", "MEINS.NSN", """
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
	void notRecommendAddingAUsingToAPdaIfDataAreaIsNotAPda()
	{
		createOrSaveFile("LIBONE", "PDAAA.NSL", """
			DEFINE DATA
			LOCAL
			1 #IN-LDA (A5)
			END-DEFINE
			""");

		assertNoCodeActionWithTitle("Add PARAMETER USING to PDAAA (from LIBONE)", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			END-DEFINE

			WRITE #IN-${}$LDA

			END
			""");
	}

	@Test
	void addAParameterUsingIfAnUnresolvedVariableCanBeFoundInAParameterDataAreaAndAScopeIsNotAlreadyPresent()
	{
		createOrSaveFile("LIBONE", "PDAAA.NSA", """
			DEFINE DATA
			PARAMETER
			1 #IN-PDA (A5)
			END-DEFINE
			""");

		assertCodeActionWithTitle("Add PARAMETER USING to PDAAA (from LIBONE)", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			END-DEFINE

			WRITE #IN-${}$PDA

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
				DEFINE DATA
				PARAMETER USING PDAAA
				END-DEFINE

				WRITE #IN-PDA

				END
				""");
	}

	@Test
	void addAParameterUsingIfAnUnresolvedVariableCanBeFoundInAParameterDataAreaAndAScopeIsAlreadyPresent()
	{
		createOrSaveFile("LIBONE", "PDAAA.NSA", """
			DEFINE DATA
			PARAMETER
			1 #IN-PDA (A5)
			END-DEFINE
			""");

		assertCodeActionWithTitle("Add PARAMETER USING to PDAAA (from LIBONE)", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			PARAMETER
			1 #P-PARM (A1)
			END-DEFINE

			WRITE #IN-${}$PDA

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
				DEFINE DATA
				PARAMETER USING PDAAA
				PARAMETER
				1 #P-PARM (A1)
				END-DEFINE

				WRITE #IN-PDA

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

	@Test
	void addAVariableWithSpecificTypeBasedOnInferredTypeForAssignmentsLiterals()
	{
		assertCodeActionWithTitle("Declare local variable #NAME", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			END-DEFINE

			#N${}$AME := 'Peter'

			END
			""")
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
				DEFINE DATA
				LOCAL
				1 #NAME (A5)
				END-DEFINE

				#NAME := 'Peter'

				END
				""");
	}

	@ParameterizedTest
	@CsvSource(
		{
			"*OCC(#ARR),I4",
			"*ISN,P10",
			"*LENGTH(#STR),I4",
			"*TIMX,T",
			"*TIMESTMP,B8",
			"*TIMN,N7",
			"*DAT4J,A7",
			"*NET-USER,A253"
		}
	)
	void addAVariableWithSpecificTypeBasedOnInferredTypeForAssignmentsWithSystemVariables(String rhs, String expectedType)
	{
		assertCodeActionWithTitle("Declare local variable #NAME", "LIBONE", "MEINS.NSN", """
			DEFINE DATA
			END-DEFINE

			#N${}$AME := %s

			END
			""".formatted(rhs))
			.fixes(ParserError.UNRESOLVED_REFERENCE.id())
			.resultsApplied("""
				DEFINE DATA
				LOCAL
				1 #NAME (%s)
				END-DEFINE

				#NAME := %s

				END
				""".formatted(expectedType, rhs));
	}
}
