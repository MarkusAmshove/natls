package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.BooleanOperatorAnalyzer;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@LspTest
public class BooleanOperatorQuickfixShould extends CodeActionTest
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
		return new BooleanOperatorQuickfix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"GT,>", "LT,<", "EQ,=", "NE,<>", "GE,>=", "LE,<="
	})
	void recognizeTheQuickfixAndReplacementForEveryOperator(String operators)
	{
		var discouragedOperator = operators.split(",")[0];
		var preferredOperator = operators.split(",")[1];

		configureEditorConfig("""
			[*]
			natls.style.comparisons=sign
			""");

		assertCodeActionWithTitle("Change operator to %s".formatted(preferredOperator), "LIBONE", "MEINS.NSN", """
			   DEFINE DATA LOCAL
			   END-DEFINE
			   IF 5 ${}$%s 2
			   IGNORE
			   END-IF
			   END
			""".formatted(discouragedOperator))
			.insertsText(2, 8, preferredOperator)
			.fixes(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR.getId())
			.resultsApplied("""
			   DEFINE DATA LOCAL
			   END-DEFINE
			   IF 5 %s 2
			   IGNORE
			   END-IF
			   END
			""".formatted(preferredOperator));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		">,GT", "<,LT", "=,EQ", "<>,NE", ">=,GE", "<=,LE"
	})
	void recognizeTheQuickfixAndReplacementForEverySignOperator(String operators)
	{
		var discouragedOperator = operators.split(",")[0];
		var preferredOperator = operators.split(",")[1];

		configureEditorConfig("""
			[*]
			natls.style.comparisons=short
			""");

		assertCodeActionWithTitle("Change operator to %s".formatted(preferredOperator), "LIBONE", "MEINS.NSN", """
			   DEFINE DATA LOCAL
			   END-DEFINE
			   IF 5 ${}$%s 2
			   IGNORE
			   END-IF
			   END
			""".formatted(discouragedOperator))
			.insertsText(2, 8, preferredOperator)
			.fixes(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR.getId())
			.resultsApplied("""
			   DEFINE DATA LOCAL
			   END-DEFINE
			   IF 5 %s 2
			   IGNORE
			   END-IF
			   END
			""".formatted(preferredOperator));
	}

	@Test
	void recognizeTheQuickFixForInvalidNatUnitTestComparison()
	{
		configureEditorConfig("""
			[*]
			natls.style.comparisons=sign
			""");

		assertCodeActionWithTitle("Change operator to EQ", "LIBONE", "TCTEST.NSN", """
			DEFINE DATA
			LOCAL USING NUTESTP
			END-DEFINE
			DEFINE SUBROUTINE TEST
			IF NUTESTP.TEST ${}$= 'My test'
			IGNORE
			END-IF
			END-SUBROUTINE
			END
			""")
			.insertsText(4, 16, "EQ")
			.resultsApplied("""
			DEFINE DATA
			LOCAL USING NUTESTP
			END-DEFINE
			DEFINE SUBROUTINE TEST
			IF NUTESTP.TEST EQ 'My test'
			IGNORE
			END-IF
			END-SUBROUTINE
			END
			""");
	}

	@Test
	void fixAllIssuesInFile()
	{

		configureEditorConfig("""
			[*]
			natls.style.comparisons=sign
			""");

		assertCodeActionWithTitle("Fix all operators in SUBMOD", "LIBONE", "SUBMOD.NSN", """
			DEFINE DATA
			LOCAL
			END-DEFINE

			IF 5 GT 2
			IGNORE
			END-IF

			IF 10 L${}$T 10
			IGNORE
			END-IF

			DECIDE FOR FIRST CONDITION
			WHEN 10 EQ 10
			IGNORE
			WHEN 10 = 10
			IGNORE
			END-DECIDE

			END
			""")
			.resultsApplied("""
			DEFINE DATA
			LOCAL
			END-DEFINE

			IF 5 > 2
			IGNORE
			END-IF

			IF 10 < 10
			IGNORE
			END-IF

			DECIDE FOR FIRST CONDITION
			WHEN 10 = 10
			IGNORE
			WHEN 10 = 10
			IGNORE
			END-DECIDE

			END
			""");
	}
}
