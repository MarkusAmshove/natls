package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.natural.*;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class MathStatementParsingShould extends StatementParseTest
{
	@Test
	void parseASimpleAddStatement()
	{
		var add = assertParsesSingleStatement("""
			ADD 1 TO #VAR
			""", IAddStatementNode.class);

		assertThat(add.isGiving()).isFalse();
		assertThat(add.isRounded()).isFalse();
		assertIsVariableReference(add.target(), "#VAR");
		assertThat(assertNodeType(add.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseASimpleAddRoundedStatement()
	{
		var add = assertParsesSingleStatement("""
			ADD ROUNDED 1 TO #VAR
			""", IAddStatementNode.class);

		assertThat(add.isRounded()).isTrue();
	}

	@Test
	void parseAnAddStatementWithMultipleOperands()
	{
		var add = assertParsesSingleStatement("""
			ADD 1 5 #VAR2(#I) TO #VAR
			""", IAddStatementNode.class);

		assertThat(add.isGiving()).isFalse();
		assertThat(assertNodeType(add.target(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
		assertThat(assertNodeType(add.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(add.operands().get(1), ILiteralNode.class).token().intValue()).isEqualTo(5);
		var varOperand = assertIsVariableReference(add.operands().get(2), "#VAR2");
		assertThat(varOperand.dimensions()).hasSize(1);
	}

	@Test
	void parseASimpleAddGivingStatement()
	{
		var add = assertParsesSingleStatement("""
			ADD 1 GIVING #VAR
			""", IAddStatementNode.class);

		assertThat(add.isGiving()).isTrue();
		assertIsVariableReference(add.target(), "#VAR");
		assertThat(assertNodeType(add.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseAnAddGivingStatementWithMultipleOperands()
	{
		var add = assertParsesSingleStatement("""
			ADD 1 5 #VAR2(#I) GIVING #VAR
			""", IAddStatementNode.class);

		assertThat(add.isGiving()).isTrue();
		assertIsVariableReference(add.target(), "#VAR");
		assertThat(assertNodeType(add.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(add.operands().get(1), ILiteralNode.class).token().intValue()).isEqualTo(5);
		var varOperand = assertIsVariableReference(add.operands().get(2), "#VAR2");
		assertThat(varOperand.dimensions()).hasSize(1);
	}

	@Test
	void parseASimpleSubtractStatement()
	{
		var subtract = assertParsesSingleStatement("""
			SUBTRACT 1 FROM #VAR
			""", ISubtractStatementNode.class);

		assertThat(subtract.isGiving()).isFalse();
		assertThat(subtract.isRounded()).isFalse();
		assertIsVariableReference(subtract.target(), "#VAR");
		assertThat(assertNodeType(subtract.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseASimpleSubtractRoundedStatement()
	{
		var subtract = assertParsesSingleStatement("""
			SUBTRACT ROUNDED 1 FROM #VAR
			""", ISubtractStatementNode.class);

		assertThat(subtract.isRounded()).isTrue();
	}

	@Test
	void parseASubtractStatementWithMultipleOperands()
	{
		var subtract = assertParsesSingleStatement("""
			SUBTRACT 1 5 #VAR2(#I) FROM #VAR
			""", ISubtractStatementNode.class);

		assertThat(subtract.isGiving()).isFalse();
		assertIsVariableReference(subtract.target(), "#VAR");
		assertThat(assertNodeType(subtract.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(subtract.operands().get(1), ILiteralNode.class).token().intValue()).isEqualTo(5);
		var varOperand = assertIsVariableReference(subtract.operands().get(2), "#VAR2");
		assertThat(varOperand.dimensions()).hasSize(1);
	}

	@Test
	void parseASimpleSubtractGivingStatement()
	{
		var subtract = assertParsesSingleStatement("""
			SUBTRACT 1 FROM #VAR GIVING #VAR2
			""", ISubtractGivingStatementNode.class);

		assertThat(subtract.isGiving()).isTrue();
		assertIsVariableReference(subtract.target(), "#VAR");
		assertIsVariableReference(subtract.giving(), "#VAR2");
		assertThat(assertNodeType(subtract.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseASubtractGivingStatementWithMultipleOperands()
	{
		var subtract = assertParsesSingleStatement("""
			SUBTRACT 1 5 #VAR2(#I) FROM #VAR GIVING #VAR3
			""", ISubtractGivingStatementNode.class);

		assertThat(subtract.isGiving()).isTrue();
		assertIsVariableReference(subtract.target(), "#VAR");
		assertThat(assertNodeType(subtract.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(subtract.operands().get(1), ILiteralNode.class).token().intValue()).isEqualTo(5);
		var varOperand = assertIsVariableReference(subtract.operands().get(2), "#VAR2");
		assertThat(varOperand.dimensions()).hasSize(1);
		assertIsVariableReference(subtract.giving(), "#VAR3");
	}

	@Test
	void parseASimpleMultiplyStatement()
	{
		var subtract = assertParsesSingleStatement("""
			MULTIPLY #VAR BY -1
			""", IMultiplyStatementNode.class);

		assertThat(subtract.isGiving()).isFalse();
		assertThat(subtract.isRounded()).isFalse();
		assertIsVariableReference(subtract.target(), "#VAR");
		assertNodeType(subtract.operands().first(), IPrefixUnaryArithmeticExpressionNode.class);
	}

	@Test
	void parseASimpleMultiplyRoundedStatement()
	{
		var multiply = assertParsesSingleStatement("""
			MULTIPLY ROUNDED #VAR BY 2
			""", IMultiplyStatementNode.class);

		assertThat(multiply.isRounded()).isTrue();
	}

	@Test
	void parseAMultiplyStatementWithMultipleOperands()
	{
		var multiply = assertParsesSingleStatement("""
			MULTIPLY #VAR BY 1 5 #VAR2(#I)
			""", IMultiplyStatementNode.class);

		assertThat(multiply.isGiving()).isFalse();
		assertIsVariableReference(multiply.target(), "#VAR");
		assertThat(assertNodeType(multiply.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(multiply.operands().get(1), ILiteralNode.class).token().intValue()).isEqualTo(5);
		var varOperand = assertIsVariableReference(multiply.operands().get(2), "#VAR2");
		assertThat(varOperand.dimensions()).hasSize(1);
	}

	@Test
	void parseASimpleMultiplyGivingStatement()
	{
		var multiply = assertParsesSingleStatement("""
			MULTIPLY #VAR BY 1 GIVING #VAR2
			""", IMultiplyGivingStatementNode.class);

		assertThat(multiply.isGiving()).isTrue();
		assertIsVariableReference(multiply.target(), "#VAR");
		assertIsVariableReference(multiply.giving(), "#VAR2");
		assertThat(assertNodeType(multiply.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseAMultiplyGivingStatementWithMultipleOperands()
	{
		var multiply = assertParsesSingleStatement("""
			MULTIPLY #VAR BY 1 5 #VAR2(#I) GIVING #VAR3
			""", IMultiplyGivingStatementNode.class);

		assertThat(multiply.isGiving()).isTrue();
		assertIsVariableReference(multiply.target(), "#VAR");
		assertThat(assertNodeType(multiply.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(multiply.operands().get(1), ILiteralNode.class).token().intValue()).isEqualTo(5);
		var varOperand = assertIsVariableReference(multiply.operands().get(2), "#VAR2");
		assertThat(varOperand.dimensions()).hasSize(1);
		assertIsVariableReference(multiply.giving(), "#VAR3");
	}

	@Test
	void parseASimpleDivideStatement()
	{
		var divide = assertParsesSingleStatement("""
			DIVIDE -1 INTO #VAR
			""", IDivideStatementNode.class);

		assertThat(divide.isGiving()).isFalse();
		assertThat(divide.isRounded()).isFalse();
		assertIsVariableReference(divide.target(), "#VAR");
		assertNodeType(divide.operands().first(), IPrefixUnaryArithmeticExpressionNode.class);
	}

	@Test
	void parseASimpleDivideRoundedStatement()
	{
		var divide = assertParsesSingleStatement("""
			DIVIDE ROUNDED 2 INTO #VAR
			""", IDivideStatementNode.class);

		assertThat(divide.isRounded()).isTrue();
	}

	@Test
	void parseADivideStatementWithMultipleOperands()
	{
		var divide = assertParsesSingleStatement("""
			DIVIDE 1 5 #VAR2(#I) INTO #VAR
			""", IDivideStatementNode.class);

		assertThat(divide.isGiving()).isFalse();
		assertIsVariableReference(divide.target(), "#VAR");
		assertThat(assertNodeType(divide.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(divide.operands().get(1), ILiteralNode.class).token().intValue()).isEqualTo(5);
		var varOperand = assertIsVariableReference(divide.operands().get(2), "#VAR2");
		assertThat(varOperand.dimensions()).hasSize(1);
	}

	@Test
	void parseASimpleDivideGivingStatement()
	{
		var divide = assertParsesSingleStatement("""
			DIVIDE 1 INTO #VAR GIVING #VAR2
			""", IDivideStatementNode.class);

		assertThat(divide.isGiving()).isTrue();
		assertIsVariableReference(divide.target(), "#VAR");
		assertIsVariableReference(divide.giving(), "#VAR2");
		assertThat(assertNodeType(divide.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseADivideGivingStatementWithMultipleOperands()
	{
		var divide = assertParsesSingleStatement("""
			DIVIDE 1 5 #VAR2(#I) INTO #VAR GIVING #VAR3
			""", IDivideStatementNode.class);

		assertThat(divide.isGiving()).isTrue();
		assertIsVariableReference(divide.target(), "#VAR");
		assertThat(assertNodeType(divide.operands().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(divide.operands().get(1), ILiteralNode.class).token().intValue()).isEqualTo(5);
		var varOperand = assertIsVariableReference(divide.operands().get(2), "#VAR2");
		assertThat(varOperand.dimensions()).hasSize(1);
		assertIsVariableReference(divide.giving(), "#VAR3");
	}

	@Test
	void allowMathExpressionsInInto()
	{
		var divide = assertParsesSingleStatement("""
			DIVIDE 2 INTO (#QUOTIENT + #REST) GIVING #QUOTIENT REMAINDER #REST
			""", IDivideStatementNode.class);

		assertNodeType(divide.target(), IArithmeticExpressionNode.class);
	}
}
