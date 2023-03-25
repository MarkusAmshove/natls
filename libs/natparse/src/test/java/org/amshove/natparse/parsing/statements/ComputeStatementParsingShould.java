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
	@ValueSource(strings =
	{
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
	@ValueSource(strings =
	{
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

	@ParameterizedTest
	@ValueSource(strings =
	{
		"=", ":="
	})
	void parseMultiAssignments(String operator)
	{
		var statements = assertParsesWithoutDiagnostics("COMPUTE #VAR1 %s #VAR2 %s 5 + 2".formatted(operator, operator));
		assertThat(statements).hasSize(2);

		var firstCompute = assertNodeType(statements.statements().first(), IComputeStatementNode.class);
		assertThat(firstCompute.isRounded()).isFalse();
		assertIsVariableReference(firstCompute.target(), "#VAR1");
		assertIsVariableReference(firstCompute.operand(), "#VAR2");

		var secondCompute = assertNodeType(statements.statements().get(1), IComputeStatementNode.class);
		assertThat(secondCompute.isRounded()).isFalse();
		assertIsVariableReference(secondCompute.target(), "#VAR2");
		var arithmetic = assertNodeType(secondCompute.operand(), IArithmeticExpressionNode.class);
		assertThat(assertNodeType(arithmetic.left(), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(arithmetic.operator()).isEqualTo(SyntaxKind.PLUS);
		assertThat(assertNodeType(arithmetic.right(), ILiteralNode.class).token().intValue()).isEqualTo(2);
	}
}
