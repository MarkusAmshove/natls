package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class OperandParsingTests extends AbstractParserTest<IStatementListNode>
{
	protected OperandParsingTests()
	{
		super(StatementListParser::new);
	}

	private ReadOnlyList<IOperandNode> parseOperands(String source)
	{
		var statement = assertParsesWithoutDiagnostics("RESET %s".formatted(source)).statements().first();
		return assertNodeType(statement, IResetStatementNode.class).operands();
	}

	@Test
	void parseSystemVariables()
	{
		var operand = parseOperands("*LINE").first();
		var variable = assertNodeType(operand, ISystemVariableNode.class);
		assertThat(variable.systemVariable()).isEqualTo(SyntaxKind.LINE);
	}

	@Test
	void parseSystemFunctions()
	{
		var operand = parseOperands("*TRIM(' Hello ')").first();
		var function = assertNodeType(operand, ISystemFunctionNode.class);
		assertThat(function.systemFunction()).isEqualTo(SyntaxKind.TRIM);
		var parameter = assertNodeType(function.parameter(), ILiteralNode.class);
		assertThat(parameter.token().source()).isEqualTo("' Hello '");
	}

	@Test
	void parsesVariables()
	{
		var operand = parseOperands("#THEVAR").first();
		var reference = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(reference.referencingToken()).isNotNull();
	}

	@Test
	void parseVariablesWithArrayAccess()
	{
		var operand = parseOperands("#THEVAR(5)").first();
		var reference = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(reference.referencingToken()).isNotNull();
		assertThat(reference.dimensions()).hasSize(1);
		var firstDimension = assertNodeType(reference.dimensions().first(), ILiteralNode.class);
		assertThat(firstDimension.token().intValue()).isEqualTo(5);
	}

	@Test
	void parseVariablesWithMultiArrayAccess()
	{
		var operand = parseOperands("#THEVAR(5,#OTHER-VAR)").first();
		var reference = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(reference.referencingToken()).isNotNull();
		assertThat(reference.dimensions()).hasSize(2);
		var firstDimension = assertNodeType(reference.dimensions().first(), ILiteralNode.class);
		assertThat(firstDimension.token().intValue()).isEqualTo(5);
		var secondDimension = assertNodeType(reference.dimensions().get(1), IVariableReferenceNode.class);
		assertThat(secondDimension.token().symbolName()).isEqualTo("#OTHER-VAR");
	}
}
