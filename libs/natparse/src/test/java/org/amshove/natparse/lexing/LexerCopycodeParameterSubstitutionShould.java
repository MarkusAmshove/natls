package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

class LexerCopycodeParameterSubstitutionShould
{

	@Test
	void substituteAParameter()
	{
		var result = lex("&1&", "#VAR");
		assertThat(result.diagnostics()).isEmpty();
		var firstToken = result.advance();
		assertThat(firstToken.kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(firstToken.source()).isEqualTo("#VAR");
	}

	@Test
	void substituteParameterToCreateAQualifiedName()
	{
		var result = lex("&1&.#VAR", "#GRP");
		assertThat(result.diagnostics()).isEmpty();
		var firstToken = result.advance();
		assertThat(firstToken.kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(firstToken.source()).isEqualTo("#GRP.#VAR");
	}

	@Test
	void substituteParameterIncludingArrayAccess()
	{
		var result = lex("&1&", "#VAR (#I)");
		assertThat(result.diagnostics()).isEmpty();

		var token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(token.source()).isEqualTo("#VAR");

		token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.LPAREN);

		token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(token.source()).isEqualTo("#I");

		token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.RPAREN);
	}

	@Test
	void substituteParameterToCreateAQualifiedNameIncludingArrayAccessIfNestedIdentifierIsAKeywordThatCanBeIdentifier()
	{
		var result = lex("&1&.ISN(*)", "#GRP");
		assertThat(result.diagnostics()).isEmpty();

		var token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(token.source()).isEqualTo("#GRP.ISN");

		token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.LPAREN);

		token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.ASTERISK);

		token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.RPAREN);
	}

	@Test
	void raiseADiagnosticIfParameterAreMissing()
	{
		var result = lex("&2&", "'Hi'");
		assertThat(result.diagnostics()).hasSize(1);
		assertThat(result.diagnostics().first().id()).isEqualTo(LexerError.MISSING_COPYCODE_PARAMETER.id());
		assertThat(result.diagnostics().first().message()).isEqualTo("Copy code parameter with position 2 not provided");
	}

	private TokenList lex(String source, String... substitutions)
	{
		var substitutionList = Arrays.stream(substitutions).toList();
		return new Lexer(substitutionList).lex(source, Path.of("original.nsn"));
	}
}
