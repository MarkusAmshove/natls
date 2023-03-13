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
		var firstParens = assertNestedArithmetic(arithmetic.right());

		var lhsOfFirstParens = assertNestedArithmetic(firstParens.left());
		assertThat(firstParens.operator()).isEqualTo(SyntaxKind.MINUS);
		assertNumericOperand(firstParens.right(), 5);

		assertNumericOperand(lhsOfFirstParens.left(), 4);
		assertThat(lhsOfFirstParens.operator()).isEqualTo(SyntaxKind.PLUS);
		var rhsOfFourPlus = assertNestedArithmetic(lhsOfFirstParens.right());

		assertNumericOperand(rhsOfFourPlus.left(), 12);
		assertThat(rhsOfFourPlus.operator()).isEqualTo(SyntaxKind.MINUS);
		assertNumericOperand(rhsOfFourPlus.right(), 3);
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
			IArithmeticExpressionNode.class);
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
