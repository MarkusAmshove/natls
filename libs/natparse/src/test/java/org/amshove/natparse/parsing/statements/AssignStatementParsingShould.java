package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AssignStatementParsingShould extends StatementParseTest
{
	@ParameterizedTest
	@ValueSource(strings =
	{
		"=", ":="
	})
	void parseSimpleAssigns(String operator)
	{
		var assign = assertParsesSingleStatement("""
			ASSIGN #VAR %s 5
			""".formatted(operator), IAssignStatementNode.class);

		assertIsVariableReference(assign.target(), "#VAR");
		assertThat(assertNodeType(assign.operand(), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(assign.isRounded()).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"=", ":="
	})
	void parseAssignRounded(String operator)
	{
		var assign = assertParsesSingleStatement("""
			ASSIGN ROUNDED #VAR %s 5
			""".formatted(operator), IAssignStatementNode.class);

		assertThat(assign.isRounded()).isTrue();
	}

	@Test
	void allowToAssignSystemVars()
	{
		var assign = assertParsesSingleStatement("""
			ASSIGN *ERROR-NR := 5
			""", IAssignStatementNode.class);

		assertThat(assertNodeType(assign.target(), ISystemVariableNode.class).systemVariable()).isEqualTo(SyntaxKind.ERROR_NR);
	}

	@Test
	void parseAssignSubstring()
	{
		var assign = assertParsesSingleStatement("""
			ASSIGN #TAR := SUBSTRING(#VAR, 5)
			""", IAssignStatementNode.class);

		assertNodeType(assign.operand(), ISubstringOperandNode.class);
	}

	@Test
	void parseAssignControlDefinition()
	{
		var assign = assertParsesSingleStatement("""
			ASSIGN #TAR := (AD=IO)
			""", IAssignStatementNode.class);

		assertNodeType(assign.operand(), IAttributeListNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"=", ":="
	})
	void parseMultiAssignments(String operator)
	{
		var statements = assertParsesWithoutDiagnostics("ASSIGN ROUNDED #VAR1 %s #VAR2 %s 5".formatted(operator, operator));
		assertThat(statements).hasSize(2);

		var firstAssign = assertNodeType(statements.statements().first(), IAssignStatementNode.class);
		assertThat(firstAssign.isRounded()).isTrue();
		assertIsVariableReference(firstAssign.target(), "#VAR1");
		assertIsVariableReference(firstAssign.operand(), "#VAR2");

		var secondAssign = assertNodeType(statements.statements().get(1), IAssignStatementNode.class);
		assertThat(secondAssign.isRounded()).isTrue();
		assertIsVariableReference(secondAssign.target(), "#VAR2");
		assertThat(assertNodeType(secondAssign.operand(), ILiteralNode.class).token().intValue()).isEqualTo(5);
	}
}
