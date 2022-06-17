package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexerForStringsShould extends AbstractLexerTest
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

		assertTokensInOrder(tokens, SyntaxKind.IDENTIFIER, SyntaxKind.COLON_EQUALS, SyntaxKind.STRING_LITERAL, SyntaxKind.WRITE, SyntaxKind.IDENTIFIER);
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
			SyntaxKind.EDITOR_MASK,
			SyntaxKind.RPAREN,
			SyntaxKind.TO,
			SyntaxKind.IDENTIFIER
		);
	}

	@Test
	void lexConcatenatedString()
	{
		assertTokens("""
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
}
