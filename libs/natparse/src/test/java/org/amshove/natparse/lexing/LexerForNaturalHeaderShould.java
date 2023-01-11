package org.amshove.natparse.lexing;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.amshove.natparse.natural.project.NaturalProgrammingMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LexerForNaturalHeaderShould extends AbstractLexerTest
{
	@ParameterizedTest
	@CsvSource(
		{
			"S,10", "S,5", "R,10", "R,5"
		}
	)

	void lexNaturalHeader(String mode, Integer increment)
	{
		var source = """
		* >Natural Source Header 000000
		* :Mode %s
		* :CP
		* :LineIncrement %d
		* <Natural Source Header
		""".formatted(mode, increment);

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().getProgrammingMode().getMode().equals(mode));
		assertThat(tokenList.sourceHeader().getLineIncrement()).isEqualTo(increment);
	}

	@Test
	void lexForStructuredMode()
	{
		var source = """
			* >Natural Source Header 000000
			* :Mode S
			* :LineIncrement 10
			* <Natural Source Header
			""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isStructuredMode()).isTrue();
		assertThat(tokenList.sourceHeader().isReportingMode()).isFalse();
	}

	@Test
	void lexForReportingMode()
	{
		var source = """
			* >Natural Source Header 000000
			* :Mode R
			* :LineIncrement 10
			* <Natural Source Header
			""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isStructuredMode()).isFalse();
		assertThat(tokenList.sourceHeader().isReportingMode()).isTrue();
	}

	@Test
	void lexForUnknownMode()
	{
		var source = """
			* >Natural Source Header 000000
			* :Mode Unknown
			* :LineIncrement 10
			* <Natural Source Header
			""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isStructuredMode()).isFalse();
		assertThat(tokenList.sourceHeader().isReportingMode()).isFalse();
		assertThat(tokenList.sourceHeader().getProgrammingMode()).isEqualTo(NaturalProgrammingMode.UNKNOWN);
	}

}
