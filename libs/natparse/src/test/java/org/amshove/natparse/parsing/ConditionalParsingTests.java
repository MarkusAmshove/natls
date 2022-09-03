package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.conditionals.IConditionNode;
import org.amshove.natparse.natural.conditionals.IUnaryLogicalCriteriaNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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
		var condition = assertParsesCondition("TRUE");
		var criteria = assertNodeType(condition.criteria(), IUnaryLogicalCriteriaNode.class);
		var literal = assertNodeType(criteria.node(), ILiteralNode.class);
		assertThat(literal.token().kind()).isEqualTo(SyntaxKind.TRUE);
	}

	@Test
	void parseFalseLiteral()
	{
		var condition = assertParsesCondition("FALSE");
		var criteria = assertNodeType(condition.criteria(), IUnaryLogicalCriteriaNode.class);
		var literal = assertNodeType(criteria.node(), ILiteralNode.class);
		assertThat(literal.token().kind()).isEqualTo(SyntaxKind.FALSE);
	}

	@Test
	void parseVariableReferencesAsUnaryCondition()
	{
		var condition = assertParsesCondition("#CONDITION");
		var criteria = assertNodeType(condition.criteria(), IUnaryLogicalCriteriaNode.class);
		var reference = assertNodeType(criteria.node(), IVariableReferenceNode.class);
		assertThat(reference.referencingToken().symbolName()).isEqualTo("#CONDITION");
	}

	@Test
	void parseFunctionCallsAsUnaryCondition()
	{
		var calledFunction = new NaturalModule(null);
		moduleProvider.addModule("ISSTH", calledFunction);

		var condition = assertParsesCondition("ISSTH(<'1', '2'>)");
		var criteria = assertNodeType(condition.criteria(), IUnaryLogicalCriteriaNode.class);
		var call = assertNodeType(criteria.node(), IFunctionCallNode.class);
		assertThat(call.referencingToken().symbolName()).isEqualTo("ISSTH");
		// TODO: Check parameter
	}

	protected IConditionNode assertParsesCondition(String source)
	{
		var list = assertParsesWithoutDiagnostics("IF %s\nIGNORE\nEND-IF".formatted(source));
		var ifNode = assertNodeType(list.statements().first(), IIfStatementNode.class);
		return ifNode.condition();
	}

	/*
	condition: logical_expression ((AND|OR) logical_expression)?;

	logical_expression:  LPAREN? TRUE | FALSE | function_call | IS (Type) | relational_expression RPAREN?;

	relational_expression: operand COMPARISON_OPERATOR operand;
	 */
}
