package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.conditionals.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@DisplayName("ConditionParser should")
class ConditionalParsingTests extends AbstractParserTest<IStatementListNode>
{
	protected ConditionalParsingTests()
	{
		super(StatementListParser::new);
	}

	@Test
	void parseTrueLiteral()
	{
		var criteria = assertParsesCriteria("TRUE", IUnaryLogicalCriteriaNode.class);
		var literal = assertNodeType(criteria.node(), ILiteralNode.class);
		assertThat(literal.token().kind()).isEqualTo(SyntaxKind.TRUE);
	}

	@Test
	void parseFalseLiteral()
	{
		var criteria = assertParsesCriteria("FALSE", IUnaryLogicalCriteriaNode.class);
		var literal = assertNodeType(criteria.node(), ILiteralNode.class);
		assertThat(literal.token().kind()).isEqualTo(SyntaxKind.FALSE);
	}

	@Test
	void parseVariableReferencesAsUnaryCondition()
	{
		var criteria = assertParsesCriteria("#CONDITION", IUnaryLogicalCriteriaNode.class);
		var reference = assertNodeType(criteria.node(), IVariableReferenceNode.class);
		assertThat(reference.referencingToken().symbolName()).isEqualTo("#CONDITION");
	}

	@Test
	void parseFunctionCallsAsUnaryCondition()
	{
		var calledFunction = new NaturalModule(null);
		moduleProvider.addModule("ISSTH", calledFunction);

		var criteria = assertParsesCriteria("ISSTH(<'1', '2'>)", IUnaryLogicalCriteriaNode.class);
		var call = assertNodeType(criteria.node(), IFunctionCallNode.class);
		assertThat(call.referencingToken().symbolName()).isEqualTo("ISSTH");
		// TODO: Check parameter
	}

	@TestFactory
	Stream<DynamicTest> parseSimpleRelationalExpressionForAllOperatorsWithCorrectMapping()
	{
		var operatorMappings = Map.ofEntries(
			Map.entry("=", ComparisonOperator.EQUAL),
			Map.entry("EQ", ComparisonOperator.EQUAL),
			Map.entry("EQUAL", ComparisonOperator.EQUAL),
			Map.entry("EQUAL TO", ComparisonOperator.EQUAL),
			Map.entry("<>", ComparisonOperator.NOT_EQUAL),
			Map.entry("NE", ComparisonOperator.NOT_EQUAL),
			Map.entry("NOT =", ComparisonOperator.NOT_EQUAL),
			Map.entry("NOT EQ ", ComparisonOperator.NOT_EQUAL),
			Map.entry("NOT EQUAL", ComparisonOperator.NOT_EQUAL),
			Map.entry("NOT EQUAL TO", ComparisonOperator.NOT_EQUAL),
			Map.entry("<", ComparisonOperator.LESS_THAN),
			Map.entry("LT", ComparisonOperator.LESS_THAN),
			Map.entry("LESS THAN", ComparisonOperator.LESS_THAN),
			Map.entry("<=", ComparisonOperator.LESS_OR_EQUAL),
			Map.entry("LE", ComparisonOperator.LESS_OR_EQUAL),
			Map.entry("LESS EQUAL", ComparisonOperator.LESS_OR_EQUAL),
			Map.entry(">", ComparisonOperator.GREATER_THAN),
			Map.entry("GT", ComparisonOperator.GREATER_THAN),
			Map.entry("GREATER THAN", ComparisonOperator.GREATER_THAN),
			Map.entry(">=", ComparisonOperator.GREATER_OR_EQUAL),
			Map.entry("GE", ComparisonOperator.GREATER_OR_EQUAL),
			Map.entry("GREATER EQUAL", ComparisonOperator.GREATER_OR_EQUAL)
		);
		return operatorMappings.entrySet().stream()
			.map(e -> dynamicTest("%s should be operator %s".formatted(e.getKey(), e.getValue()), () -> {
				var criteria = assertParsesCriteria("1 %s 2".formatted(e.getKey()), IRelationalCriteriaNode.class);
				var left = assertNodeType(criteria.left(), ILiteralNode.class);
				var right = assertNodeType(criteria.right(), ILiteralNode.class);
				assertThat(left.token().intValue()).isEqualTo(1);
				assertThat(right.token().intValue()).isEqualTo(2);
				assertThat(criteria.operator()).isEqualTo(e.getValue());
			}));
	}

	@Test
	void parseRelationalExpressionsWithVariables()
	{
		var criteria = assertParsesCriteria("#NUM1 > #NUM2", IRelationalCriteriaNode.class);
		var left = assertNodeType(criteria.left(), IVariableReferenceNode.class);
		var right = assertNodeType(criteria.right(), IVariableReferenceNode.class);
		assertThat(left.token().symbolName()).isEqualTo("#NUM1");
		assertThat(right.token().symbolName()).isEqualTo("#NUM2");
		assertThat(criteria.operator()).isEqualTo(ComparisonOperator.GREATER_THAN);
	}

	@Test
	void parseRelationalExpressionsWithMultipleEquals()
	{
		var criteria = assertParsesCriteria("#NUM1 = #NUM2 OR = 5 OR EQUAL 10 OR EQUAL TO 20", IExtendedRelationalCriteriaNode.class);
		assertThat(criteria.descendants().size()).isEqualTo(13);

		var left = assertNodeType(criteria.left(), IVariableReferenceNode.class);
		assertThat(left.token().symbolName()).isEqualTo("#NUM1");
		var rights = criteria.rights();
		assertThat(assertNodeType(rights.first(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#NUM2");
		assertThat(assertNodeType(rights.get(1), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(assertNodeType(rights.get(2), ILiteralNode.class).token().intValue()).isEqualTo(10);
		assertThat(assertNodeType(rights.get(3), ILiteralNode.class).token().intValue()).isEqualTo(20);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"GT", "LT", "<", ">", ">=", "<=", "NE", "<>"
	})
	void reportADiagnosticIfExtendedRelationalExpressionIsNotUsedWithEqualComparison(String operator)
	{
		assertDiagnostic("""
			IF #NUM1 %s #NUM2 OR = 5 OR EQUAL 10 OR EQUAL TO 20
			IGNORE
			END-IF
			""".formatted(operator), ParserError.EXTENDED_RELATIONAL_EXPRESSION_NEEDS_EQUAL);
	}

	@Test
	void parseASimpleThruExtendedRelationalExpression()
	{
		var criteria = assertParsesCriteria("#VAR = 1 THRU 10", IRangedExtendedRelationalCriteriaNode.class);
		assertThat(assertNodeType(criteria.left(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
		assertThat(criteria.operator()).isEqualTo(ComparisonOperator.EQUAL);
		assertThat(assertNodeType(criteria.lowerBound(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(criteria.upperBound(), ILiteralNode.class).token().intValue()).isEqualTo(10);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"GT", "LT", "<", ">", ">=", "<=", "NE", "<>"
	})
	void reportADiagnosticIfRangedExtendedRelationalExpressionIsNotUsedWithEqualComparison(String operator)
	{
		assertDiagnostic("""
			IF #NUM1 %s #NUM2 THRU 10
			IGNORE
			END-IF
			""".formatted(operator), ParserError.EXTENDED_RELATIONAL_EXPRESSION_NEEDS_EQUAL);
	}

	@Test
	void parseARangedExtendedRelationalCriteriaWithASingleExcluding()
	{
		var criteria = assertParsesCriteria("#VAR = 1 THRU 10 BUT NOT 5", IRangedExtendedRelationalCriteriaNode.class);
		assertThat(criteria.excludedLowerBound()).isNotEmpty();
		assertThat(criteria.excludedUpperBound()).isEmpty();
		assertThat(assertNodeType(criteria.excludedLowerBound().get(), ILiteralNode.class).token().intValue()).isEqualTo(5);
	}

	@Test
	void parseARangedExtendedRelationalCriteriaWithAnExclusionRange()
	{
		var criteria = assertParsesCriteria("#VAR = 1 THRU 10 BUT NOT 5 THRU #VAR2", IRangedExtendedRelationalCriteriaNode.class);
		assertThat(criteria.excludedLowerBound()).isNotEmpty();
		assertThat(criteria.excludedUpperBound()).isNotEmpty();
		assertThat(assertNodeType(criteria.excludedLowerBound().get(), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(assertNodeType(criteria.excludedUpperBound().get(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR2");
	}

	@Test
	void parseARelationalExpressionWithSubstring()
	{
		var criteria = assertParsesCriteria("SUBSTR(#VAR, 1, #MAX) = SUBSTRING(#VAR, #MIN, #MAX)", IRelationalCriteriaNode.class);
		var firstSubstring = assertNodeType(criteria.left(), ISubstringOperandNode.class);
		assertThat(assertNodeType(firstSubstring.operand(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
		assertThat(assertNodeType(firstSubstring.startPosition(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(firstSubstring.length(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#MAX");
		var secondSubstring = assertNodeType(criteria.right(), ISubstringOperandNode.class);
		assertThat(assertNodeType(secondSubstring.operand(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
		assertThat(assertNodeType(secondSubstring.startPosition(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#MIN");
		assertThat(assertNodeType(secondSubstring.length(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#MAX");
	}

	@Test
	void parseConditionCriteriaWithParens()
	{
		var criteria = assertParsesCriteria("( 5 > 2)", IGroupedConditionCriteria.class);
		assertThat(criteria.descendants()).hasSize(3);
		var nestedRelationalCriteria = assertNodeType(criteria.criteria(), IRelationalCriteriaNode.class);
		assertThat(assertNodeType(nestedRelationalCriteria.left(), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(nestedRelationalCriteria.operator()).isEqualTo(ComparisonOperator.GREATER_THAN);
		assertThat(assertNodeType(nestedRelationalCriteria.right(), ILiteralNode.class).token().intValue()).isEqualTo(2);
		// IF NOT #TRUE
		// IF (5 < 2) AND 5 = 2
		// IF 5 = 5 AND TRUE
		// IF 5 = 5 OR TRUE
		// ....
	}

	@Test
	void parseConditionCriteriaWithMultipleParens()
	{
		var criteria = assertParsesCriteria("((( 5 > 2 )))", IGroupedConditionCriteria.class);
		var secondNest = assertNodeType(criteria.criteria(), IGroupedConditionCriteria.class);
		var thirdNest = assertNodeType(secondNest.criteria(), IGroupedConditionCriteria.class);

		var relational = assertNodeType(thirdNest.criteria(), IRelationalCriteriaNode.class);
		assertThat(relational.operator()).isEqualTo(ComparisonOperator.GREATER_THAN);
	}

	@TestFactory
	Stream<DynamicTest> parseChainedCriteria()
	{
		return Map.of(
				"AND", ChainedCriteriaOperator.AND,
				"OR", ChainedCriteriaOperator.OR
			).entrySet().stream()
			.map(e -> dynamicTest(e.getKey(), () -> {
				var chained = assertParsesCriteria("#VAR = #VAR2 %s #VAR3 <> #VAR4".formatted(e.getKey()), IChainedCriteriaNode.class);
				assertThat(chained.operator()).isEqualTo(e.getValue());
				var left = assertNodeType(chained.left(), IRelationalCriteriaNode.class);
				assertThat(left.operator()).isEqualTo(ComparisonOperator.EQUAL);
				var right = assertNodeType(chained.right(), IRelationalCriteriaNode.class);
				assertThat(right.operator()).isEqualTo(ComparisonOperator.NOT_EQUAL);
			}));
	}

	@Test
	void parseMultipleChainedCriteria()
	{
		/*"""
	            OR
			   /   \
			 AND   1 = 1
		   /    \
		 5 = 5  2 = 2
		 """*/
		var firstChained = assertParsesCriteria("5 = 5 AND 2 = 2 OR (1 = 1)", IChainedCriteriaNode.class);
		assertThat(firstChained.operator()).isEqualTo(ChainedCriteriaOperator.OR);

		var firstLeft = assertNodeType(firstChained.left(), IChainedCriteriaNode.class);
		assertThat(firstLeft.operator()).isEqualTo(ChainedCriteriaOperator.AND);
		var nestedLeft = assertNodeType(firstLeft.left(), IRelationalCriteriaNode.class);
		assertThat(assertNodeType(nestedLeft.left(), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(assertNodeType(nestedLeft.right(), ILiteralNode.class).token().intValue()).isEqualTo(5);
		var nestedRight = assertNodeType(firstLeft.right(), IRelationalCriteriaNode.class);
		assertThat(assertNodeType(nestedRight.left(), ILiteralNode.class).token().intValue()).isEqualTo(2);
		assertThat(assertNodeType(nestedRight.right(), ILiteralNode.class).token().intValue()).isEqualTo(2);

		var firstRightGrouped = assertNodeType(firstChained.right(), IGroupedConditionCriteria.class).criteria();
		var firstRight = assertNodeType(firstRightGrouped, IRelationalCriteriaNode.class);
		assertThat(assertNodeType(firstRight.left(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(firstRight.right(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseChainedCriteriaWithinGroupedCriteria()
	{
		var grouped = assertParsesCriteria("(5 > 2 AND 3 < 10)", IGroupedConditionCriteria.class);
		var chained = assertNodeType(grouped.criteria(), IChainedCriteriaNode.class);
		assertThat(chained.operator()).isEqualTo(ChainedCriteriaOperator.AND);
	}

	protected <T extends ILogicalConditionCriteriaNode> T assertParsesCriteria(String source, Class<T> criteriaType)
	{
		var list = assertParsesWithoutDiagnostics("IF %s\nIGNORE\nEND-IF".formatted(source));
		var ifNode = assertNodeType(list.statements().first(), IIfStatementNode.class);
		return assertNodeType(ifNode.condition().criteria(), criteriaType);
	}

	/*
	condition: logical_expression ((AND|OR) logical_expression)?;

	logical_expression:  LPAREN? TRUE | FALSE | function_call | IS (Type) | relational_expression RPAREN?;

	relational_expression: operand COMPARISON_OPERATOR operand;
	 */
}
