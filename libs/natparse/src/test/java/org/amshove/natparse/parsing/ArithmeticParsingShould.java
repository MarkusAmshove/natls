package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

class ArithmeticParsingShould extends AbstractParserTest<IStatementListNode>
{

	@Test
	void parseSimpleArithmetic()
	{
		var arithmetic = parseArithmetic("5 + 10");
		assertThat(arithmetic.operator()).isEqualTo(SyntaxKind.PLUS);
		assertNumericOperand(arithmetic.left(), 5);
		assertNumericOperand(arithmetic.right(), 10);
	}

	@Test
	void parseExponentArithmetic()
	{
		var arithmetic = parseArithmetic("5 ** 10");
		assertThat(arithmetic.operator()).isEqualTo(SyntaxKind.EXPONENT_OPERATOR);
		assertNumericOperand(arithmetic.left(), 5);
		assertNumericOperand(arithmetic.right(), 10);
	}

	@Test
	void parseArithmeticThatMightLookLikeAIV()
	{
		var arithmetic = parseArithmetic("5+NUM");
		assertThat(arithmetic.operator()).isEqualTo(SyntaxKind.PLUS);
		assertNumericOperand(arithmetic.left(), 5);
		assertThat(assertNodeType(arithmetic.right(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("NUM");
	}

	@Test
	void parseSimpleArithmeticInParens()
	{
		var arithmetic = parseArithmetic("(5 + 10)");
		assertThat(arithmetic.operator()).isEqualTo(SyntaxKind.PLUS);
		assertNumericOperand(arithmetic.left(), 5);
		assertNumericOperand(arithmetic.right(), 10);
	}

	@Test
	void parseASimpleChainedArithmetic()
	{
		var arithmetic = parseArithmetic("5 + 10 + 7");
		assertThat(arithmetic.operator()).isEqualTo(SyntaxKind.PLUS);
		assertNumericOperand(arithmetic.left(), 5);
		var chained = assertNestedArithmetic(arithmetic.right());
		assertNumericOperand(chained.left(), 10);
		assertNumericOperand(chained.right(), 7);
	}

	@Test
	void parseArithmeticWithMultipleChainedNesting()
	{
		var arithmetic = parseArithmetic("2 / ( 4 + (12 - 3) - 5)");

		assertNumericOperand(arithmetic.left(), 2);
		assertThat(arithmetic.operator()).isEqualTo(SyntaxKind.SLASH);
		var firstParens = assertNestedArithmetic(arithmetic.right()); // ( 4 + (12 - 3) - 5)
		assertNumericOperand(firstParens.left(), 4);
		assertThat(firstParens.operator()).isEqualTo(SyntaxKind.PLUS);

		var rhsInFirstParens = assertNestedArithmetic(firstParens.right()); // (12 - 3) - 5
		assertThat(rhsInFirstParens.operator()).isEqualTo(SyntaxKind.MINUS);
		assertNumericOperand(rhsInFirstParens.right(), 5);

		var lhsOfLastExpressionInFirstParens = assertNestedArithmetic(rhsInFirstParens.left()); // (12 - 3)
		assertNumericOperand(lhsOfLastExpressionInFirstParens.left(), 12);
		assertThat(lhsOfLastExpressionInFirstParens.operator()).isEqualTo(SyntaxKind.MINUS);
		assertNumericOperand(lhsOfLastExpressionInFirstParens.right(), 3);
	}

	@Test
	void parseArithmeticWithNamesOfAttributesWithoutDiagnostics()
	{
		// This was an error because it's (CV - a common check if the next operand is a attribute is "LPAREN and isAttribute() after LPAREN"
		// However, attributes always look like CV= so this was a false positive
		parseArithmetic("(CV + 5) + (CV * 10)");
		parseArithmetic("(SB(1) + 5) / SB(2)");
	}

	@Test
	void parseArithmeticWithUnnecessaryParens()
	{
		parseArithmetic("((0 + 5))");
	}

	protected ArithmeticParsingShould()
	{
		super(StatementListParser::new);
	}

	private IArithmeticExpressionNode parseArithmetic(String source)
	{
		var statement = assertParsesWithoutDiagnostics("COMPUTE #VAR := %s".formatted(source)).statements().first();
		return assertNodeType(
			assertNodeType(statement, IComputeStatementNode.class).operand(),
			IArithmeticExpressionNode.class
		);
	}

	private void assertNumericOperand(IOperandNode node, int value)
	{
		var literal = assertOperand(node, ILiteralNode.class);
		assertThat(literal.token().kind()).isEqualTo(SyntaxKind.NUMBER_LITERAL);
		assertThat(literal.token().intValue())
			.as("Literal value should match")
			.isEqualTo(value);
	}

	private <T extends IOperandNode> T assertOperand(IOperandNode node, Class<T> type)
	{
		return assertNodeType(node, type);
	}

	private IArithmeticExpressionNode assertNestedArithmetic(IOperandNode node)
	{
		return assertNodeType(node, IArithmeticExpressionNode.class);
	}
}
