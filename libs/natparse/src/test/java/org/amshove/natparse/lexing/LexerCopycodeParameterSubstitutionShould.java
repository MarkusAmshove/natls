package org.amshove.natparse.lexing;

import org.amshove.natparse.IDiagnostic;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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
		var lexer = new Lexer(List.of("'Hi'"));
		lexer.relocateDiagnosticPosition(new PlainPosition(0, 0, 0, 0, Path.of("")));

		var result = lexer.lex("&2&", Path.of("original.nsn"));
		assertThat(result.diagnostics()).hasSize(1);
		assertThat(result.diagnostics().first().id()).isEqualTo(LexerError.MISSING_COPYCODE_PARAMETER.id());
		assertThat(result.diagnostics().first().message()).isEqualTo("Copy code parameter with position 2 not provided");
	}

	@Test
	void substituteStringLiterals()
	{
		var result = lex("&1&", "\"\"\"SOME TEXT\"\"\"");
		assertThat(result.diagnostics()).isEmpty();
		var token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.STRING_LITERAL);
		assertThat(token.source()).isEqualTo("\"\"\"SOME TEXT\"\"\"");
	}

	@Test
	void addADiagnosticToIncludePositionWithPositionInCopyCodeAsAdditionalInfo()
	{
		var lexer = new Lexer(List.of("''"));
		var includePosition = new PlainPosition(0, 0, 0, 0, Path.of(""));
		lexer.relocateDiagnosticPosition(includePosition);

		var copyCodePath = Path.of("original.nsn");
		var result = lexer.lex("WRITE &1&", copyCodePath);

		assertThat(result.diagnostics()).hasSize(1);
		var diagnostic = result.diagnostics().first();
		assertThat(diagnostic.id()).isEqualTo(LexerError.INVALID_STRING_LENGTH.id());

		assertThat(diagnostic.isSamePositionAs(includePosition)).as("Diagnostic main position should be INCLUDE position").isTrue();

		assertThat(diagnostic.additionalInfo().first().position().filePath()).isEqualTo(copyCodePath);
	}

	private TokenList lex(String source, String... substitutions)
	{
		var substitutionList = Arrays.stream(substitutions).toList();
		return new Lexer(substitutionList).lex(source, Path.of("original.nsn"));
	}
}
