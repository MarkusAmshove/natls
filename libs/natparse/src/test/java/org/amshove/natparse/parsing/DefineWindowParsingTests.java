package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IDefineWindowNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DefineWindowParsingTests extends StatementParseTest
{
	@Test
	void parseSimpleDefineWindow()
	{
		var window = assertParsesSingleStatement("DEFINE WINDOW MAIN", IDefineWindowNode.class);
		assertThat(window.name().symbolName()).isEqualTo("MAIN");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SIZE AUTO",
		"SIZE QUARTER",
		"SIZE 10 * 10",
		"SIZE #VAR * 5",
		"SIZE 5 * #VAR",
		"SIZE #VAR * #VAR"
	})
	void parseDefineWindowWithSize(String size)
	{
		assertParsesSingleStatement("""
			DEFINE WINDOW MAWINDOW
			%s
			""".formatted(size), IDefineWindowNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"BASE CURSOR",
		"BASE TOP LEFT",
		"BASE TOP RIGHT",
		"BASE BOTTOM LEFT",
		"BASE BOTTOM RIGHT",
		"BASE 10 / 10",
		"BASE #VAR / #VAR"
	})
	void parseDefineWindowWithBase(String base)
	{
		assertParsesSingleStatement("""
			DEFINE WINDOW MAWINDOW
			%s
			""".formatted(base), IDefineWindowNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"REVERSED",
		"REVERSED (CD=RE)"
	})
	void parseDefineWindowWithReversed(String reversed)
	{
		assertParsesSingleStatement("""
			DEFINE WINDOW MAWINDOW
			%s
			""".formatted(reversed), IDefineWindowNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"TITLE 'Hi'",
		"TITLE #VAR"
	})
	void parseDefineWindowWithTitle(String title)
	{
		assertParsesSingleStatement("""
			DEFINE WINDOW MAWINDOW
			%s
			""".formatted(title), IDefineWindowNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"CONTROL WINDOW",
		"CONTROL SCREEN"
	})
	void parseDefineWindowWithControl(String control)
	{
		assertParsesSingleStatement("""
			DEFINE WINDOW MAWINDOW
			%s
			""".formatted(control), IDefineWindowNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"FRAMED",
		"FRAMED ON",
		"FRAMED OFF",
		"FRAMED ON (CD=RE)",
		"FRAMED ON (CD=RE) POSITION SYMBOL",
		"FRAMED ON (CD=RE) POSITION SYMBOL TOP",
		"FRAMED ON (CD=RE) POSITION SYMBOL TOP AUTO",
		"FRAMED ON (CD=RE) POSITION SYMBOL TOP AUTO SHORT",
		"FRAMED ON (CD=RE) POSITION SYMBOL TOP AUTO SHORT RIGHT",
		"FRAMED ON (CD=RE) POSITION SYMBOL TOP LEFT",
		"FRAMED ON (CD=RE) POSITION SYMBOL TOP RIGHT",
		"FRAMED ON POSITION SYMBOL TOP AUTO SHORT LEFT",
		"FRAMED ON (CD=RE) POSITION SYMBOL BOTTOM",
		"FRAMED ON (CD=RE) POSITION SYMBOL BOTTOM AUTO",
		"FRAMED ON (CD=RE) POSITION SYMBOL BOTTOM AUTO SHORT",
		"FRAMED ON (CD=RE) POSITION SYMBOL BOTTOM AUTO SHORT RIGHT",
		"FRAMED ON (CD=RE) POSITION SYMBOL BOTTOM LEFT",
		"FRAMED ON (CD=RE) POSITION SYMBOL BOTTOM RIGHT",
		"FRAMED ON POSITION TEXT OFF",
		"FRAMED ON POSITION TEXT MORE",
		"FRAMED ON POSITION TEXT MORE RIGHT",
		"FRAMED ON POSITION TEXT MORE RIGHT",
		"FRAMED OFF",
	})
	void parseDefineWindowWithFramed(String framed)
	{
		assertParsesSingleStatement("""
			DEFINE WINDOW MAWINDOW
			%s
			""".formatted(framed), IDefineWindowNode.class);
	}
}
