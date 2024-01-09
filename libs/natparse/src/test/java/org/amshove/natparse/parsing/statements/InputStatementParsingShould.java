package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.output.*;
import org.amshove.natparse.parsing.ParserError;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("DataFlowIssue")
class InputStatementParsingShould extends StatementParseTest
{
	@Test
	void parseASimpleInput()
	{
		var input = assertParsesSingleStatement("INPUT #VAR", IInputStatementNode.class);
		assertNodeOperand(input, 0, IVariableReferenceNode.class, "#VAR");
	}

	@Test
	void parseASimpleInputWithMultipleOperands()
	{
		var input = assertParsesSingleStatement("INPUT #VAR 'Hi' #VAR2", IInputStatementNode.class);
		assertNodeOperand(input, 0, IVariableReferenceNode.class, "#VAR");
		assertNodeOperand(input, 1, ILiteralNode.class, "'Hi'");
		assertNodeOperand(input, 2, IVariableReferenceNode.class, "#VAR2");
	}

	@Test
	void parseAInputWithWindow()
	{
		assertParsesSingleStatement("INPUT WINDOW='window' 'Hi'", IInputStatementNode.class);
	}

	@Test
	void parseInputWithNoErase()
	{
		assertParsesSingleStatement("INPUT WINDOW='window' NO ERASE 'Hi'", IInputStatementNode.class);
	}

	@Test
	void consumeStatementAttributes()
	{
		var input = assertParsesSingleStatement("INPUT (AD=IO) 'Hi'", IInputStatementNode.class);
		var attribute = assertNodeType(input.statementAttributes().first(), IValueAttributeNode.class);

		assertThat(attribute.kind()).isEqualTo(SyntaxKind.AD);
		assertThat(attribute.value()).isEqualTo("IO");
	}

	@Test
	void raiseADiagnosticForInvalidStatementAttributes()
	{
		assertDiagnostic("INPUT (ES=ON) 'Hi'", ParserError.INVALID_INPUT_STATEMENT_ATTRIBUTE);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"WITH TEXT", "TEXT"
	})
	void consumeWithText(String permutation)
	{
		var input = assertParsesSingleStatement("INPUT %s *MSG-INFO.##MSG-NR,#VAR2 'HI'".formatted(permutation), IInputStatementNode.class);
		assertThat(input.operands()).hasSize(1);
		assertNodeOperand(input, 0, ILiteralNode.class, "'HI'");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"MARK POSITION 3 IN FIELD #NUMBER",
		"MARK 3",
		"MARK #NUMBER",
		"MARK *#NUMBER",
		"MARK **COM"
	})
	void consumeMark(String permutation)
	{
		var input = assertParsesSingleStatement("INPUT %s 'HI'".formatted(permutation), IInputStatementNode.class);
		assertThat(input.operands()).hasSize(1);
		assertNodeOperand(input, 0, ILiteralNode.class, "'HI'");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"AND SOUND ALARM", "AND ALARM", "SOUND ALARM", "ALARM"
	})
	void consumeAlarm(String permutation)
	{
		var input = assertParsesSingleStatement("INPUT %s 'HI'".formatted(permutation), IInputStatementNode.class);
		assertThat(input.operands()).hasSize(1);
		assertNodeOperand(input, 0, ILiteralNode.class, "'HI'");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"USING MAP", "MAP"
	})
	void consumeUsingMap(String permutation)
	{
		var input = assertParsesSingleStatement("INPUT %s 'Map' 'HI'".formatted(permutation), IInputStatementNode.class);
		assertThat(input.operands()).hasSize(1);
		assertNodeOperand(input, 0, ILiteralNode.class, "'HI'");
	}

	@Test
	void consumeUsingMapWithoutParameter()
	{
		var input = assertParsesSingleStatement("INPUT USING MAP 'Map' NO PARAMETER", IInputStatementNode.class);
		assertThat(input.operands()).isEmpty();
	}

	@Test
	void consumeInputsWithPositions()
	{
		var input = assertParsesSingleStatement("INPUT 'Hi' 10/15 'Ho' 20/20 #VAR", IInputStatementNode.class);
		assertThat(input.operands()).hasSize(5);
	}

	@Test
	void raiseADiagnosticIfPositioningToColumnZero()
	{
		assertDiagnostic("INPUT 'Hi' 10/0", ParserError.INVALID_LITERAL_VALUE);
	}

	@Test
	void parseSpaceElement()
	{
		var input = assertParsesSingleStatement("INPUT 'Hi' 5X 'Ho'", IInputStatementNode.class);
		var operand = assertNodeType(input.operands().get(1), ISpaceElementNode.class);
		assertThat(operand.spaces()).isEqualTo(5);
	}

	@Test
	void parseTabulatorElement()
	{
		var input = assertParsesSingleStatement("INPUT 'Hi' 5T 'Ho'", IInputStatementNode.class);
		var operand = assertNodeType(input.operands().get(1), ITabulatorElementNode.class);
		assertThat(operand.tabs()).isEqualTo(5);
	}

	@Test
	void consumeNewLines()
	{
		var input = assertParsesSingleStatement("INPUT 'Hi' / 'Ho'", IInputStatementNode.class);
		assertNodeType(input.operands().get(1), IOutputNewLineNode.class);
	}

	@Test
	void consumeTabsAndSkips()
	{
		var input = assertParsesSingleStatement("INPUT 'Hi' / 'Ho' 5T #VAR", IInputStatementNode.class);
		assertThat(input.operands()).hasSize(5);
	}

	@Test
	void consumeOperandAttributes()
	{
		var input = assertParsesSingleStatement("INPUT #VAR (AD=IO)", IInputStatementNode.class);
		var inputOperand = assertNodeType(input.operands().first(), IOutputOperandNode.class);
		assertIsVariableReference(inputOperand.operand(), "#VAR");
		assertThat(inputOperand.attributeNode()).as("Attribute List for operand should not be null").isNotNull();
		var valueAttribute = assertNodeType(inputOperand.attributeNode().attributes().first(), IValueAttributeNode.class);
		assertThat(valueAttribute.kind()).isEqualTo(SyntaxKind.AD);
		assertThat(valueAttribute.value()).isEqualTo("IO");
	}

	@ParameterizedTest
	@CsvSource(
		{
			"B,AD",
			"C,AD",
			"D,AD",
			"I,AD",
			"N,AD",
			"U,AD",
			"V,AD",
			"BL,CD",
			"GR,CD",
			"NE,CD",
			"PI,CD",
			"RE,CD",
			"TU,CD",
			"YE,CD"
		}
	)
	void parseImplicitAttributes(String value, SyntaxKind expectedKind)
	{
		var input = assertParsesSingleStatement("INPUT 'Lit' (%s)".formatted(value), IInputStatementNode.class);
		var inputOperand = assertNodeType(input.operands().first(), IOutputOperandNode.class);
		var attribute = inputOperand.attributes().first();
		assertValueAttribute(attribute, expectedKind, value);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"UYE", "YEU"
	})
	void parseCombinationsOfImplicitAttributes(String combination)
	{
		var input = assertParsesSingleStatement("INPUT 'Lit' (%s)".formatted(combination), IInputStatementNode.class);
		var inputOperand = assertNodeType(input.operands().first(), IOutputOperandNode.class);
		assertThat(inputOperand.attributes()).hasSize(2);

		assertThat(inputOperand.attributes())
			.as("AD=U not found")
			.anySatisfy(a -> assertValueAttribute(a, SyntaxKind.AD, "U"));
		assertThat(inputOperand.attributes())
			.as("CD=YE not found")
			.anySatisfy(a -> assertValueAttribute(a, SyntaxKind.CD, "YE"));
	}

	@Test
	void raiseAnIssueIfTwoImplicitAttributesCantBeCreated()
	{
		assertDiagnostic("INPUT 'Lit' (RYY)", ParserError.INTERNAL);
	}

	@Test
	void parseOperandsWithNumericAttributes()
	{
		assertParsesSingleStatement("INPUT #NUM (EM=99.9999)", IInputStatementNode.class);
	}

	@Test
	void parseCharacterRepetition()
	{
		var input = assertParsesSingleStatement("INPUT '*' (70)", IInputStatementNode.class);
		var operand = input.operands().first();
		var repetition = assertNodeType(operand, ICharacterRepetitionOperandNode.class);

		var literal = assertLiteral(repetition.operand(), SyntaxKind.STRING_LITERAL);
		assertThat(literal.token().stringValue()).isEqualTo("*");

		assertThat(repetition.repetition()).isEqualTo(70);
	}

	@Test
	void parseCharacterRepetitionWithAttributes()
	{
		var input = assertParsesSingleStatement("INPUT '*' (70) (AD=I)", IInputStatementNode.class);

		var inputOperand = assertNodeType(input.operands().first(), IOutputOperandNode.class);
		assertValueAttribute(inputOperand.attributes().first(), SyntaxKind.AD, "I");
	}

	@Test
	void parseCoordinateOperands()
	{
		var input = assertParsesSingleStatement("INPUT 'A' 2/5 'B'", IInputStatementNode.class);

		var operand = assertNodeType(input.operands().get(1), IOutputPositioningNode.class);;
		assertThat(operand.row()).isEqualTo(2);
		assertThat(operand.column()).isEqualTo(5);
	}

	@Test
	void raiseADiagnosticForInvalidElementAttributes()
	{
		assertDiagnostic("INPUT 'Hi' (LS=20)", ParserError.INVALID_INPUT_ELEMENT_ATTRIBUTE);
	}

	private void assertNodeOperand(IInputStatementNode input, int index, Class<? extends ITokenNode> operandType, String source)
	{
		var inputOperand = assertNodeType(input.operands().get(index), IOutputOperandNode.class);
		assertThat(
			assertNodeType(inputOperand.operand(), operandType)
				.token().source()
		).isEqualTo(source);
	}
}
