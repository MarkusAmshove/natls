package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.natural.IInputStatementNode;
import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.ITokenNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class InputStatementParsingShould extends StatementParseTest
{
	@Test
	void parseASimpleInput()
	{
		var input = assertParsesSingleStatement("INPUT #VAR", IInputStatementNode.class);
		assertNodeOperand(input, 0, IVariableReferenceNode.class, "#VAR");
		assertIsVariableReference(input.mutations().first(), "#VAR");
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
		assertParsesSingleStatement("INPUT (AD=IO) 'Hi'", IInputStatementNode.class);
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
		"MARK POSITION 3 IN FIELD #NUMBER", "MARK 3", "MARK #NUMBER", "MARK *#NUMBER"
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
		assertThat(input.operands()).hasSize(3);
		assertNodeOperand(input, 0, ILiteralNode.class, "'Hi'");
		assertNodeOperand(input, 1, ILiteralNode.class, "'Ho'");
		assertNodeOperand(input, 2, IVariableReferenceNode.class, "#VAR");
	}

	@Test
	void consumeTabsAndSkips()
	{
		var input = assertParsesSingleStatement("INPUT 'Hi' / 'Ho' 5T #VAR", IInputStatementNode.class);
		assertThat(input.operands()).hasSize(3);
		assertNodeOperand(input, 0, ILiteralNode.class, "'Hi'");
		assertNodeOperand(input, 1, ILiteralNode.class, "'Ho'");
		assertNodeOperand(input, 2, IVariableReferenceNode.class, "#VAR");
	}

	private void assertNodeOperand(IInputStatementNode input, int index, Class<? extends ITokenNode> operandType, String source)
	{
		assertThat(
			assertNodeType(input.operands().get(index), operandType)
				.token().source()
		).isEqualTo(source);
	}
}
