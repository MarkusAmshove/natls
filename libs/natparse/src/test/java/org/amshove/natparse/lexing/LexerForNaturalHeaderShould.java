package org.amshove.natparse.lexing;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

public class LexerForNaturalHeaderShould extends AbstractLexerTest
{
	@Test
	void lexNaturalHeaderStructuredModeLineIncr10()
	{
		var source = """
		* >Natural Source Header 000000
		* :Mode S
		* :CP
		* :LineIncrement 10
		* <Natural Source Header
		""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isStructuredMode()).isEqualTo(true);
		assertThat(tokenList.sourceHeader().getLineIncrement()).isEqualTo(10);
	}

	@Test
	void lexNaturalHeaderStructuredModeLineIncr5()
	{
		var source = """
		* >Natural Source Header 000000
		* :Mode S
		* :CP
		* :LineIncrement 5
		* <Natural Source Header
		""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isStructuredMode()).isEqualTo(true);
		assertThat(tokenList.sourceHeader().getLineIncrement()).isEqualTo(5);
	}
	void lexNaturalHeaderReportingModeLineIncr10()
	{
		var source = """
		* >Natural Source Header 000000
		* :Mode R
		* :CP
		* :LineIncrement 10
		* <Natural Source Header
		""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isReportingMode()).isEqualTo(true);
		assertThat(tokenList.sourceHeader().getLineIncrement()).isEqualTo(10);
	}

	@Test
	void lexNaturalHeaderReportingModeLineIncr5()
	{
		var source = """
		* >Natural Source Header 000000
		* :Mode R
		* :CP
		* :LineIncrement 5
		* <Natural Source Header
		""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isReportingMode()).isEqualTo(true);
		assertThat(tokenList.sourceHeader().getLineIncrement()).isEqualTo(5);
	}
	@Test
	void lexNaturalHeaderInlineStructuredModeLineIncr10()
	{
		var source = """
		/* >Natural Source Header 000000
		/* :Mode S
		/* :CP
		/* :LineIncrement 10
		/* <Natural Source Header
		""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isStructuredMode()).isEqualTo(true);
		assertThat(tokenList.sourceHeader().getLineIncrement()).isEqualTo(10);
	}

	@Test
	void lexNaturalHeaderInlineStructuredModeLineIncr5()
	{
		var source = """
		/* >Natural Source Header 000000
		/* :Mode S
		/* :CP
		/* :LineIncrement 5
		/* <Natural Source Header
		""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isStructuredMode()).isEqualTo(true);
		assertThat(tokenList.sourceHeader().getLineIncrement()).isEqualTo(5);
	}
	void lexNaturalHeaderInlineReportingModeLineIncr10()
	{
		var source = """
		/* >Natural Source Header 000000
		/* :Mode R
		/* :CP
		/* :LineIncrement 10
		/* <Natural Source Header
		""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isReportingMode()).isEqualTo(true);
		assertThat(tokenList.sourceHeader().getLineIncrement()).isEqualTo(10);
	}

	@Test
	void lexNaturalHeaderInlineReportingModeLineIncr5()
	{
		var source = """
		/* >Natural Source Header 000000
		/* :Mode R
		/* :CP
		/* :LineIncrement 5
		/* <Natural Source Header
		""";

		var tokenList = lexSource(source);
		assertThat(tokenList.sourceHeader().isReportingMode()).isEqualTo(true);
		assertThat(tokenList.sourceHeader().getLineIncrement()).isEqualTo(5);
	}

}
