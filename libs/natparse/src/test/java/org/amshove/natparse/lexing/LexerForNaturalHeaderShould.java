package org.amshove.natparse.lexing;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class LexerForNaturalHeaderShould extends AbstractLexerTest
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

}