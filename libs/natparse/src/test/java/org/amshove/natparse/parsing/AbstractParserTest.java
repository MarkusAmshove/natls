package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class AbstractParserTest
{
	private final DefineDataParser sut = new DefineDataParser();

	void assertDiagnostic(String source, ParserError expectedError)
	{
		var lexer = new Lexer();
		var tokens = lexer.lex(source);
		var result = sut.parseDefineData(tokens);
		assertThat(result.diagnostics().size())
			.as("Expected to get at least one diagnostic, but found none")
			.isGreaterThan(0);
		assertThat(result.diagnostics())
			.anyMatch(d -> d.id().equals(expectedError.id()));
	}
}
