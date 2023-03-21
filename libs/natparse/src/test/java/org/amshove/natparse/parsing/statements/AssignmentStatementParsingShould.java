package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AssignmentStatementParsingShould extends StatementParseTest
{
	@Test
	void parseSimpleAssigns()
	{
		var assign = assertParsesSingleStatement("""
			#VAR := 5
			""", IAssignmentStatementNode.class);

		assertThat(assertNodeType(assign.target(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
		assertThat(assertNodeType(assign.operand(), ILiteralNode.class).token().intValue()).isEqualTo(5);
	}

	@Test
	void allowToAssignSystemVars()
	{
		var assign = assertParsesSingleStatement("""
			*ERROR-NR := 5
			""", IAssignmentStatementNode.class);

		assertThat(assertNodeType(assign.target(), ISystemVariableNode.class).systemVariable()).isEqualTo(SyntaxKind.ERROR_NR);
	}

	@Test
	void parseAssignSubstring()
	{
		var assign = assertParsesSingleStatement("""
			#TAR := SUBSTRING(#VAR, 5)
			""", IAssignmentStatementNode.class);

		assertNodeType(assign.operand(), ISubstringOperandNode.class);
	}

	@Test
	void parseAssignControlDefinition()
	{
		var assign = assertParsesSingleStatement("""
			#TAR := (AD=IO)
			""", IAssignmentStatementNode.class);

		assertNodeType(assign.operand(), IAttributeNode.class);
	}

	@Test
	void parseAssignWithSingleFunctionCall()
	{
		ignoreModuleProvider();
		var assign = assertParsesSingleStatement("""
			#IVAR := INDEX(<#ARR(*), QUAL.IFIED>)
			""", IAssignmentStatementNode.class);

		var call = assertNodeType(assign.operand(), IFunctionCallNode.class);
		assertThat(call.referencingToken().symbolName()).isEqualTo("INDEX");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"#ARR(#I)", "#ARR (#I)", "#ARR (#I)    ", "#ARR(1,5)", "#ARR (1, 5, #I)"
	})
	void parseAssigningArrayIndices()
	{
		var assign = assertParsesSingleStatement("#ARR(#I) := 5", IAssignmentStatementNode.class);
		var target = assertNodeType(assign.target(), IVariableReferenceNode.class);
		assertThat(target.token().symbolName()).isEqualTo("#ARR");
		assertThat(target.dimensions()).isNotEmpty();
	}
}
