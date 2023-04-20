package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

class LexerForStringsShould extends AbstractLexerTest
{
	@Test
	void lexSingleQuoteStrings()
	{
		assertTokens("'Hello World!'", token(SyntaxKind.STRING_LITERAL, "'Hello World!'"));
	}

	@Test
	void lexDoubleQuoteStrings()
	{
		assertTokens("\"Hello World!\"", token(SyntaxKind.STRING_LITERAL, "\"Hello World!\""));
	}

	@Test
	void reportADiagnosticForUnterminatedSingleQuotedStrings()
	{
		assertDiagnostic(
			"#VAR := 'This is unterminated",
			assertedDiagnostic(8, 8, 0, 21, LexerError.UNTERMINATED_STRING)
		);
	}

	@Test
	void reportADiagnosticForUnterminatedDoubleQuotedStrings()
	{
		assertDiagnostic(
			"#VAR := \"This is unterminated",
			assertedDiagnostic(8, 8, 0, 21, LexerError.UNTERMINATED_STRING)
		);
	}

	@Test
	void reportADiagnosticForUnterminatedStringsWithLineBreak()
	{
		assertDiagnostic(
			"#VAR := \"This is unterminated\nWRITE #VAR",
			assertedDiagnostic(8, 8, 0, 21, LexerError.UNTERMINATED_STRING)
		);
	}

	@Test
	void recoverAfterUnterminatedStrings()
	{
		var tokens = assertDiagnostic(
			"#VAR := \"This is unterminated\nWRITE #VAR",
			assertedDiagnostic(8, 8, 0, 21, LexerError.UNTERMINATED_STRING)
		);

		assertTokensInOrder(tokens, SyntaxKind.IDENTIFIER, SyntaxKind.COLON_EQUALS_SIGN, SyntaxKind.STRING_LITERAL, SyntaxKind.WRITE, SyntaxKind.IDENTIFIER);
	}

	@Test
	void lexHexStrings()
	{
		assertTokens("H'00'", token(SyntaxKind.STRING_LITERAL, "H'00'"));
	}

	@Test
	void correctlyParseTimeFormats()
	{
		assertTokens(
			"MOVE EDITED *TIMX(EM=' 'HH':'II':'SS) TO #RIGHT-PROMPT ",
			SyntaxKind.MOVE,
			SyntaxKind.EDITED,
			SyntaxKind.TIMX,
			SyntaxKind.LPAREN,
			SyntaxKind.EM,
			SyntaxKind.RPAREN,
			SyntaxKind.TO,
			SyntaxKind.IDENTIFIER
		);
	}

	@Test
	void lexConcatenatedString()
	{
		assertTokens(
			"""
				INPUT
				'Hello'
				 - ' World'
						-'!'
				 WRITE
				""",
			token(SyntaxKind.INPUT),
			token(SyntaxKind.STRING_LITERAL, "'Hello World!'"),
			token(SyntaxKind.WRITE)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"(YE)", "(TU)", "(BL)", "(PI)", "(NE)", "(RE)", "(GR)",
	})
	void lexColorAttribute(String attribute)
	{
		assertTokens(
			"""
				WRITE (REP) 'Hello World!' %s
			""".formatted(attribute),
			token(SyntaxKind.WRITE),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.IDENTIFIER),
			token(SyntaxKind.RPAREN),
			token(SyntaxKind.STRING_LITERAL, "'Hello World!'"),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.COLOR_ATTRIBUTE),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void lexEscapedStrings()
	{
		var tokens = lexSource("''''");
		assertThat(tokens.size()).isEqualTo(1);
		assertThat(tokens.peek().stringValue()).isEqualTo("'");
	}

	@Test
	void lexEscapedStringsWithContent()
	{
		var tokens = lexSource("'''Hello'''");
		assertThat(tokens.size()).isEqualTo(1);
		assertThat(tokens.peek().stringValue()).isEqualTo("'Hello'");
	}

	@Test
	void containTheHexLiteralAsString()
	{
		var token = lexSingle("H'5B'");
		assertThat(token.stringValue()).isEqualTo("[");
	}
}
