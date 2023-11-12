package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class LexerCopycodeParameterSubstitutionShould
{
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
