package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.NaturalParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

public class SuperdescriptorChildParserShould
{
	private final SuperdescriptorChildParser sut = new SuperdescriptorChildParser();

	@Test
	void parseTheName()
	{
		assertThat(sut.parse("* A-FIELD(1-10)").name()).isEqualTo("A-FIELD");
	}

	@Test
	void parseTheRangeFrom()
	{
		assertThat(sut.parse("* A-FIELD(1-10)").rangeFrom()).isEqualTo(1);
	}

	@Test
	void parseTheRangeTo()
	{
		assertThat(sut.parse("* A-FIELD(1-10)").rangeTo()).isEqualTo(10);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"*NO-SPACE(1-10)", "* ONE-SPACE(1-10)", "*      #MUCHO-SPACO(5-7)"
	})
	void accountForSpacesBetweenCommentStartAndName(String line)
	{
		assertThatCode(() -> sut.parse(line)).doesNotThrowAnyException();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"* NO-SPACE(1-10)", "* ONE-SPACE (1-10)", "* #MUCHO-SPACO        (5-7)"
	})
	void accountForSpacesBetweenNameAndRangeDefinition(String line)
	{
		assertThatCode(() -> sut.parse(line)).doesNotThrowAnyException();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"NO-COMMENT(1-2)", "* INVALID-RANGE-FROM(a-10)", "* INVALID-RANGE-TO(1-a)"
	})
	void throwAnExceptionWhenParsingAnInvalidLine(String line)
	{
		assertThatThrownBy(() -> sut.parse(line))
			.isInstanceOf(NaturalParseException.class)
			.hasMessage("Can't parse Superdescriptorchild from \"" + line + "\"");
	}
}
