package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ComputeStatementParsingShould extends StatementParseTest
{
	@ParameterizedTest
	@ValueSource(strings = {
		"=", ":="
	})
	void parseSimpleComputes(String operator)
	{
		var assign = assertParsesSingleStatement("""
			COMPUTE #VAR %s 5
			""".formatted(operator), IComputeStatementNode.class);

		assertThat(assertNodeType(assign.target(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
		assertThat(assertNodeType(assign.operand(), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(assign.isRounded()).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"=", ":="
	})
	void parseComputeRounded(String operator)
	{
		var assign = assertParsesSingleStatement("""
			COMPUTE ROUNDED #VAR %s 5
			""".formatted(operator), IComputeStatementNode.class);

		assertThat(assign.isRounded()).isTrue();
	}

	@Test
	void allowToComputeSystemVars()
	{
		var assign = assertParsesSingleStatement("""
			COMPUTE *ERROR-NR := 5
			""", IComputeStatementNode.class);

		assertThat(assertNodeType(assign.target(), ISystemVariableNode.class).systemVariable()).isEqualTo(SyntaxKind.ERROR_NR);
	}

	@Test
	void parseComputeSubstring()
	{
		var assign = assertParsesSingleStatement("""
			COMPUTE #TAR := SUBSTRING(#VAR, 5)
			""", IComputeStatementNode.class);

		assertNodeType(assign.operand(), ISubstringOperandNode.class);
	}
}
