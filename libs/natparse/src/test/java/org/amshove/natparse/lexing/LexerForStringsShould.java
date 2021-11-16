package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexerForStringsShould extends AbstractLexerTest
{
	@Test
	void lexSingleQuoteStrings()
	{
		assertTokens("'Hello World!'", token(SyntaxKind.STRING, "'Hello World!'"));
	}

	@Test
	void lexDoubleQuoteStrings()
	{
		assertTokens("\"Hello World!\"", token(SyntaxKind.STRING, "\"Hello World!\""));
	}

	@Test
	void reportADiagnosticForUnterminatedSingleQuotedStrings()
	{
		assertDiagnostic(
			"#VAR := 'This is unterminated",
			LexerDiagnostic.create(8, 8, 0, 21, LexerError.UNTERMINATED_STRING)
		);
	}

	@Test
	void reportADiagnosticForUnterminatedDoubleQuotedStrings()
	{
		assertDiagnostic(
			"#VAR := \"This is unterminated",
			LexerDiagnostic.create(8, 8, 0, 21, LexerError.UNTERMINATED_STRING)
		);
	}

	@Test
	void reportADiagnosticForUnterminatedStringsWithLineBreak()
	{
		assertDiagnostic(
			"#VAR := \"This is unterminated\nWRITE #VAR",
			LexerDiagnostic.create(8, 8, 0, 21, LexerError.UNTERMINATED_STRING)
		);
	}

	@Test
	void recoverAfterUnterminatedStrings()
	{
		var tokens = assertDiagnostic(
			"#VAR := \"This is unterminated\nWRITE #VAR",
			LexerDiagnostic.create(8, 8, 0, 21, LexerError.UNTERMINATED_STRING)
		);

		assertTokensInOrder(tokens, SyntaxKind.IDENTIFIER, SyntaxKind.COLON_EQUALS, SyntaxKind.STRING, SyntaxKind.WRITE, SyntaxKind.IDENTIFIER);
	}

	@Test
	void correctlyParseTimeFormats()
	{
		assertTokensIgnoreWhitespace(
			"MOVE EDITED *TIMX(EM=' 'HH':'II':'SS) TO #RIGHT-PROMPT ",
			SyntaxKind.MOVE,
			SyntaxKind.IDENTIFIER_OR_KEYWORD,
			SyntaxKind.TIMX,
			SyntaxKind.LPAREN,
			SyntaxKind.IDENTIFIER_OR_KEYWORD, // EM
			SyntaxKind.EQUALS,
			SyntaxKind.STRING, // ' '
			SyntaxKind.IDENTIFIER_OR_KEYWORD, // HH
			SyntaxKind.STRING, // ':'
			SyntaxKind.IDENTIFIER_OR_KEYWORD, // II
			SyntaxKind.STRING, // ':'
			SyntaxKind.IDENTIFIER_OR_KEYWORD, // SS
			SyntaxKind.RPAREN,
			SyntaxKind.IDENTIFIER_OR_KEYWORD, // TO
			SyntaxKind.IDENTIFIER
		);
	}
}
