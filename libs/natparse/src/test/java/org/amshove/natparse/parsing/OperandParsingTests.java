package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class OperandParsingTests extends AbstractParserTest<IStatementListNode>
{
	protected OperandParsingTests()
	{
		super(StatementListParser::new);
	}

	private IOperandNode parseOperand(String source)
	{
		var statement = assertParsesWithoutDiagnostics("#I := %s".formatted(source)).statements().first();
		return assertNodeType(statement, IAssignmentStatementNode.class).operand();
	}

	private ReadOnlyList<IOperandNode> parseOperands(String source)
	{
		var statement = assertParsesWithoutDiagnostics("RESET %s".formatted(source)).statements().first();
		return assertNodeType(statement, IResetStatementNode.class).operands();
	}

	@Test
	void parseSystemVariables()
	{
		var operand = parseOperand("*LINE");
		var variable = assertNodeType(operand, ISystemVariableNode.class);
		assertThat(variable.systemVariable()).isEqualTo(SyntaxKind.LINE);
	}

	@Test
	void parseANumberAsLiteral()
	{
		var operand = parseOperand("1");
		var variable = assertNodeType(operand, ILiteralNode.class);
		assertThat(variable.token().intValue()).isEqualTo(1);
	}

	@Test
	void parseANegativeNumberAsPostfixUnary()
	{
		var operand = parseOperand("-1");
		var postfix = assertNodeType(operand, PrefixUnaryArithmeticExpressionNode.class);
		assertThat(postfix.postfixOperator()).isEqualTo(SyntaxKind.MINUS);
		assertThat(assertNodeType(postfix.operand(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseSystemFunctions()
	{
		var operand = parseOperand("*TRIM(' Hello ')");
		var function = assertNodeType(operand, ISystemFunctionNode.class);
		assertThat(function.systemFunction()).isEqualTo(SyntaxKind.TRIM);
		var parameter = assertNodeType(function.parameter().first(), ILiteralNode.class);
		assertThat(parameter.token().source()).isEqualTo("' Hello '");
	}

	@Test
	void parseSystemFunctionsWithMultipleParameter()
	{
		var operand = parseOperand("*OCC(#THE-ARR, 1, 5)");
		var function = assertNodeType(operand, ISystemFunctionNode.class);
		assertThat(function.systemFunction()).isEqualTo(SyntaxKind.OCC);
		assertNodeType(function.parameter().first(), IVariableReferenceNode.class);
		assertNodeType(function.parameter().get(1), ILiteralNode.class);
		assertNodeType(function.parameter().get(2), ILiteralNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"MINVAL", "MAXVAL"
	})
	void parseMinAndMaxValWithExplicitReturnType(String function)
	{
		var node = parseOperand("""
			*%s((IR=N8,2)5, 7)
			""".formatted(function));

		var functionCall = assertNodeType(node, ISystemFunctionNode.class);
		// Currently the IR is just consumed and not inspected any further
		assertNodeType(functionCall.parameter().first(), ILiteralNode.class);
		assertNodeType(functionCall.parameter().get(1), ILiteralNode.class);
	}

	@Test
	void parsesVariables()
	{
		var operand = parseOperand("#THEVAR");
		var reference = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(reference.referencingToken()).isNotNull();
	}

	@Test
	void parseVariablesWithArrayAccess()
	{
		var operand = parseOperand("#THEVAR(5)");
		var reference = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(reference.referencingToken()).isNotNull();
		assertThat(reference.dimensions()).hasSize(1);
		var firstDimension = assertNodeType(reference.dimensions().first(), ILiteralNode.class);
		assertThat(firstDimension.token().intValue()).isEqualTo(5);
	}

	@Test
	void parseVariablesWithMultiArrayAccess()
	{
		var operand = parseOperand("#THEVAR(5,#OTHER-VAR)");
		var reference = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(reference.referencingToken()).isNotNull();
		assertThat(reference.dimensions()).hasSize(2);
		var firstDimension = assertNodeType(reference.dimensions().first(), ILiteralNode.class);
		assertThat(firstDimension.token().intValue()).isEqualTo(5);
		var secondDimension = assertNodeType(reference.dimensions().get(1), IVariableReferenceNode.class);
		assertThat(secondDimension.token().symbolName()).isEqualTo("#OTHER-VAR");
	}

	@Test
	void parseMultilineOperands()
	{
		var operand = parseOperands("""
			#THEVAR
			OTHERVAR
			""");
		var firstReference = assertNodeType(operand.get(0), IVariableReferenceNode.class);
		assertThat(firstReference.referencingToken().symbolName()).isEqualTo("#THEVAR");

		var secondReference = assertNodeType(operand.get(1), IVariableReferenceNode.class);
		assertThat(secondReference.referencingToken().symbolName()).isEqualTo("OTHERVAR");
	}

	@Test
	void parseVal()
	{
		var operand = parseOperand("VAL(#THEVAR(1))");
		var valNode = assertNodeType(operand, IValOperandNode.class);
		var ref = assertNodeType(valNode.operand(), IVariableReferenceNode.class);
		assertThat(ref.referencingToken().symbolName()).isEqualTo("#THEVAR");
		assertThat(assertNodeType(ref.dimensions().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseValWithNestedOperand()
	{
		var operand = parseOperand("VAL(OLD(#VAR))");
		var valNode = assertNodeType(operand, IValOperandNode.class);
		var old = assertNodeType(valNode.operand(), IOldOperandNode.class);
		assertThat(old.variable().referencingToken().symbolName()).isEqualTo("#VAR");
	}

	@Test
	void parseInt()
	{
		var operand = parseOperand("INT(#THEVAR(1))");
		var valNode = assertNodeType(operand, IIntOperandNode.class);
		assertThat(valNode.variable().referencingToken().symbolName()).isEqualTo("#THEVAR");
		assertThat(assertNodeType(valNode.variable().dimensions().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseSum()
	{
		var operand = parseOperand("SUM(#THEVAR)");
		var sumNode = assertNodeType(operand, ISumOperandNode.class);
		assertThat(sumNode.variable().referencingToken().symbolName()).isEqualTo("#THEVAR");
	}

	@Test
	void parseLog()
	{
		var operand = parseOperand("LOG(#THEVAR(1))");
		var logNode = assertNodeType(operand, ILogOperandNode.class);
		var reference = assertNodeType(logNode.parameter(), IVariableReferenceNode.class);
		assertThat(reference.referencingToken().symbolName()).isEqualTo("#THEVAR");
		assertThat(assertNodeType(reference.dimensions().first(), ILiteralNode.class).token().intValue()).isEqualTo(1);
	}

	@Test
	void parseOld()
	{
		var operand = parseOperand("OLD(#THEVAR)");
		var oldNode = assertNodeType(operand, IOldOperandNode.class);
		assertThat(oldNode.variable().referencingToken().symbolName()).isEqualTo("#THEVAR");
	}

	@Test
	void parseAbs()
	{
		var operand = parseOperand("ABS(#THEVAR)");
		var absNode = assertNodeType(operand, IAbsOperandNode.class);
		var parameter = assertNodeType(absNode.parameter(), IVariableReferenceNode.class);
		assertThat(parameter.referencingToken().symbolName()).isEqualTo("#THEVAR");
	}

	@Test
	void parseFunctionsAsAbsParameter()
	{
		moduleProvider.addModule("FUNC", new NaturalModule(null));
		var operand = parseOperand("ABS(FUNC(<'A', 5>))");
		var abs = assertNodeType(operand, IAbsOperandNode.class);
		var functionAsParameter = assertNodeType(abs.parameter(), IFunctionCallNode.class);
		assertThat(functionAsParameter.referencingToken().symbolName()).isEqualTo("FUNC");
	}

	@Test
	void parseFrac()
	{
		var operand = parseOperand("FRAC(#THEVAR)");
		var fracNode = assertNodeType(operand, IFracOperandNode.class);
		var parameter = assertNodeType(fracNode.parameter(), IVariableReferenceNode.class);
		assertThat(parameter.referencingToken().symbolName()).isEqualTo("#THEVAR");
	}

	@Test
	void parseNumberWithParam()
	{
		var operand = parseOperand("*NUMBER(R1.)");
		var number = assertNodeType(operand, ISystemFunctionNode.class);
		assertThat(number.parameter()).hasSize(1);
	}

	@Test
	void parseNumberWithoutParam()
	{
		var operand = parseOperand("*NUMBER");
		assertNodeType(operand, ISystemVariableNode.class);
	}

	@Test
	void parseTranslateUpper()
	{
		var operand = parseOperand("*TRANSLATE(#VAR, UPPER)");
		var translate = assertNodeType(operand, ITranslateSystemFunctionNode.class);
		assertThat(assertNodeType(translate.parameter().first(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#VAR");
		assertThat(assertNodeType(translate.toTranslate(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#VAR");
		assertThat(translate.isToUpper()).isTrue();
	}

	@Test
	void parseTranslateWithLiterals()
	{
		var operand = parseOperand("*TRANSLATE('dr.', UPPER)");
		var translate = assertNodeType(operand, ITranslateSystemFunctionNode.class);
		assertThat(assertNodeType(translate.parameter().first(), ILiteralNode.class).token().stringValue()).isEqualTo("dr.");
		assertThat(assertNodeType(translate.toTranslate(), ILiteralNode.class).token().stringValue()).isEqualTo("dr.");
		assertThat(translate.isToUpper()).isTrue();
	}

	@Test
	void parseTranslateLower()
	{
		var operand = parseOperand("*TRANSLATE(#VAR, LOWER)");
		var translate = assertNodeType(operand, ITranslateSystemFunctionNode.class);
		assertThat(assertNodeType(translate.parameter().first(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#VAR");
		assertThat(assertNodeType(translate.toTranslate(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#VAR");
		assertThat(translate.isToUpper()).isFalse();
	}

	@Test
	void parseArithmeticInArrayAccess()
	{
		var operand = parseOperand("#THEVAR(#I + 5)");
		var reference = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(reference.referencingToken()).isNotNull();
		assertThat(reference.dimensions()).hasSize(1);
		var arithmetic = assertNodeType(reference.dimensions().first(), IArithmeticExpressionNode.class);
		assertThat(assertNodeType(arithmetic.left(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#I");
		assertThat(arithmetic.operator()).isEqualTo(SyntaxKind.PLUS);
		assertThat(assertNodeType(arithmetic.right(), ILiteralNode.class).token().intValue()).isEqualTo(5);
	}

	@Test
	void parsePosOperand()
	{
		var operand = parseOperand("POS(#VAR.#VAR2)");
		assertThat(assertNodeType(operand, IPosNode.class).positionOf().token().symbolName()).isEqualTo("#VAR.#VAR2");
	}

	@Test
	void parsePageNumberWithoutRep()
	{
		var operand = parseOperand("*PAGE-NUMBER");
		assertThat(assertNodeType(operand, ISystemVariableNode.class).systemVariable()).isEqualTo(SyntaxKind.PAGE_NUMBER);
	}

	@Test
	void parsePageNumberWithRep()
	{
		var operand = parseOperand("*PAGE-NUMBER(SV1)");
		var function = assertNodeType(operand, ISystemFunctionNode.class);
		assertThat(function.systemFunction()).isEqualTo(SyntaxKind.PAGE_NUMBER);
		assertThat(function.parameter()).hasSize(1);
		var parameter = assertNodeType(function.parameter().get(0), IReportSpecificationOperandNode.class);
		assertThat(parameter.reportSpecification().symbolName()).isEqualTo("SV1");
	}

	@Test
	void parseLineCountWithoutRep()
	{
		var operand = parseOperand("*LINE-COUNT");
		assertThat(assertNodeType(operand, ISystemVariableNode.class).systemVariable()).isEqualTo(SyntaxKind.LINE_COUNT);
	}

	@Test
	void parseLineCountWithRep()
	{
		var operand = parseOperand("*LINE-COUNT(SV1)");
		var function = assertNodeType(operand, ISystemFunctionNode.class);
		assertThat(function.systemFunction()).isEqualTo(SyntaxKind.LINE_COUNT);
		assertThat(function.parameter()).hasSize(1);
		var parameter = assertNodeType(function.parameter().get(0), IReportSpecificationOperandNode.class);
		assertThat(parameter.reportSpecification().symbolName()).isEqualTo("SV1");
	}

	@Test
	void parseArrayAccessRanges()
	{
		var operand = parseOperand("#VAR(1:10)");
		var access = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(access.dimensions()).hasSize(1);
		var rangedAccess = assertNodeType(access.dimensions().first(), IRangedArrayAccessNode.class);
		assertThat(assertNodeType(rangedAccess.lowerBound(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(rangedAccess.upperBound(), ILiteralNode.class).token().intValue()).isEqualTo(10);
	}

	@Test
	void parseArrayAccessRangesWithSingleAsterisk()
	{
		var operand = parseOperand("#VAR(*)");
		var access = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(access.dimensions()).hasSize(1);
		var rangedAccess = assertNodeType(access.dimensions().first(), IRangedArrayAccessNode.class);
		assertThat(assertNodeType(rangedAccess.lowerBound(), ITokenNode.class).token().kind()).isEqualTo(SyntaxKind.ASTERISK);
		assertThat(assertNodeType(rangedAccess.upperBound(), ITokenNode.class).token().kind()).isEqualTo(SyntaxKind.ASTERISK);
		assertThat(rangedAccess.lowerBound().position().isSamePositionAs(rangedAccess.upperBound().position())).isTrue();
	}

	@Test
	void parseArrayAccessRangesWithDoubleAsterisk()
	{
		var operand = parseOperand("#VAR(*:*)");
		var access = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(access.dimensions()).hasSize(1);
		var rangedAccess = assertNodeType(access.dimensions().first(), IRangedArrayAccessNode.class);
		assertThat(assertNodeType(rangedAccess.lowerBound(), ITokenNode.class).token().kind()).isEqualTo(SyntaxKind.ASTERISK);
		assertThat(assertNodeType(rangedAccess.upperBound(), ITokenNode.class).token().kind()).isEqualTo(SyntaxKind.ASTERISK);
		assertThat(rangedAccess.lowerBound().position().isSamePositionAs(rangedAccess.upperBound().position())).isFalse();
	}

	@Test
	void parseArrayAccessWithVariableRanges()
	{
		var operand = parseOperand("#VAR(#LOW:50)");
		var access = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(access.dimensions()).hasSize(1);
		var rangedAccess = assertNodeType(access.dimensions().first(), IRangedArrayAccessNode.class);
		assertThat(assertNodeType(rangedAccess.lowerBound(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#LOW");
		assertThat(assertNodeType(rangedAccess.upperBound(), ILiteralNode.class).token().intValue()).isEqualTo(50);
	}

	@Test
	void parseArrayAccessWithVariableRangesInUpperBound()
	{
		var operand = parseOperand("#VAR(5:#UP)");
		var access = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(access.dimensions()).hasSize(1);
		var rangedAccess = assertNodeType(access.dimensions().first(), IRangedArrayAccessNode.class);
		assertThat(assertNodeType(rangedAccess.lowerBound(), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(assertNodeType(rangedAccess.upperBound(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#UP");
	}

	@Test
	void parseArrayAccessWithVariableArithmeticRanges()
	{
		var operand = parseOperand("#VAR(#DOWN(2) -5:#UP + #DOWN(3))");
		var access = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(access.dimensions()).hasSize(1);
		var rangedAccess = assertNodeType(access.dimensions().first(), IRangedArrayAccessNode.class);

		var lower = assertNodeType(rangedAccess.lowerBound(), IArithmeticExpressionNode.class);
		assertThat(assertNodeType(lower.left(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#DOWN");
		assertThat(lower.operator()).isEqualTo(SyntaxKind.MINUS);
		assertThat(assertNodeType(lower.right(), ILiteralNode.class).token().intValue()).isEqualTo(5);

		var upper = assertNodeType(rangedAccess.upperBound(), IArithmeticExpressionNode.class);
		assertThat(assertNodeType(upper.left(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#UP");
		assertThat(upper.operator()).isEqualTo(SyntaxKind.PLUS);
		assertThat(assertNodeType(upper.right(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#DOWN");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"#VAR(2, 1:10)", "#VAR(1:10, 2)"
	})
	void parseArrayAccessWithMultipleDimensionsAndRanges(String operandSource)
	{
		var operand = parseOperand(operandSource);
		var access = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(access.dimensions()).hasSize(2);
		assertThat(access.descendants()).hasSize(6); // #VAR ( IOperand , IRangedArrayAccess )
	}

	@Test
	void parseRetOperand()
	{
		ignoreModuleProvider();
		var operand = parseOperand("RET('MODULE')");
		var retOperand = assertNodeType(operand, IRetOperandNode.class);
		assertThat(retOperand.reference().referencingToken().stringValue()).isEqualTo("MODULE");
	}

	@Test
	void raiseADiagnosticForInvalidRetLiterals()
	{
		assertDiagnostic("#I := RET(5)", ParserError.INVALID_LITERAL_VALUE);
	}

	@Test
	void parsePostfixMinusOperands()
	{
		var operand = parseOperand("-#VAR");
		var postfix = assertNodeType(operand, IPrefixUnaryArithmeticExpressionNode.class);
		assertThat(postfix.postfixOperator()).isEqualTo(SyntaxKind.MINUS);
		assertThat(assertNodeType(postfix.operand(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
	}

	@Test
	void parsePostfixPlusOperands()
	{
		var operand = parseOperand("+ #VAR");
		var postfix = assertNodeType(operand, IPrefixUnaryArithmeticExpressionNode.class);
		assertThat(postfix.postfixOperator()).isEqualTo(SyntaxKind.PLUS);
		assertThat(assertNodeType(postfix.operand(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
	}

	@Test
	void parsePostfixOperandsWithFollowupArithmetic()
	{
		var operand = parseOperand("-#VAR+5");
		var arithmetic = assertNodeType(operand, IArithmeticExpressionNode.class);
		assertNodeType(arithmetic.left(), IPrefixUnaryArithmeticExpressionNode.class);
		assertThat(arithmetic.operator()).isEqualTo(SyntaxKind.PLUS);
		assertNodeType(arithmetic.right(), ILiteralNode.class);
	}
}
