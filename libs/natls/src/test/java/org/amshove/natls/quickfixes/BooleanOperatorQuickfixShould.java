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
	@ValueSource(strings = {
		"GT,>",
		"LT,<",
		"EQ,=",
		"NE,<>",
		"GE,>=",
		"LE,<="
	})
	void recognizeTheQuickfixAndReplacementForEveryOperator(String operators)
	{
		var discouragedOperator = operators.split(",")[0];
		var preferredOperator = operators.split(",")[1];

		var result = receiveCodeActions("LIBONE", "MEINS.NSN", """
			   DEFINE DATA LOCAL
			   END-DEFINE
			   IF 5 ${}$%s 2
			   IGNORE
			   END-IF
			   END
			""".formatted(discouragedOperator));

		var actions = result.codeActions();

		assertContainsCodeAction("Change operator to %s".formatted(preferredOperator), actions);

		assertSingleCodeAction(actions)
			.insertsText(2, 8, preferredOperator)
			.fixes(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR.getId());
	}

	@Test
	void recognizeTheQuickFixForInvalidNatUnitTestComparison()
	{
		var result = receiveCodeActions("LIBONE", "TCTEST.NSN", """
			DEFINE DATA
			LOCAL USING NUTESTP
			END-DEFINE
			DEFINE SUBROUTINE TEST
			IF NUTESTP.TEST ${}$= 'My test'
			IGNORE
			END-IF
			END-SUBROUTINE
			END
			""");

		var actions = result.codeActions();

		assertContainsCodeAction("Change operator to EQ", actions);

		assertSingleCodeAction(actions)
			.insertsText(4, 16, "EQ");
	}
}
