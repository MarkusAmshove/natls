package org.amshove.natparse.lexing;

import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.IModuleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IncludeResolvingLexerShould extends AbstractLexerTest
{
	private IModuleProvider moduleProvider;
	private IFilesystem filesystem;
	private IncludeResolvingLexer sut;

	@BeforeEach
	void setup()
	{
		moduleProvider = mock(IModuleProvider.class);
		filesystem = mock(IFilesystem.class);
		sut = new IncludeResolvingLexer(filesystem);
	}

	@Test
	void lexASourceWithoutInclude()
	{
		var source = "WRITE VAR 'Literal' #VAR2";
		var tokens = lexAndResolve(source);

		assertTokensInOrder(
			tokens,
			SyntaxKind.WRITE,
			SyntaxKind.IDENTIFIER,
			SyntaxKind.STRING_LITERAL,
			SyntaxKind.IDENTIFIER
		);
	}

	@Test
	void replaceIncludeStatementWithNestedCopyCode()
	{
		givenAnExternalCopyCodeWithSource("MYCC", "WRITE #VAR");
		var result = lexAndResolve("WRITE #OUTER INCLUDE MYCC");
		assertTokensInOrder(result, SyntaxKind.WRITE, SyntaxKind.IDENTIFIER, SyntaxKind.WRITE, SyntaxKind.IDENTIFIER);
	}

	@Test
	void substituteParameterToCreateAQualifiedName()
	{
		givenAnExternalCopyCodeWithSource("THECC", "&1&.#VAR");
		var result = lexAndResolve("INCLUDE THECC '#GRP'");
		assertTokensInOrder(result, token(SyntaxKind.IDENTIFIER, "#GRP.#VAR"));
	}

	@Test
	void substituteParameterIncludingArrayAccess()
	{
		givenAnExternalCopyCodeWithSource("ARRACC", "&1&");
		var result = lexAndResolve("INCLUDE ARRACC '#VAR (#I)'");
		assertTokensInOrder(
			result,
			token(SyntaxKind.IDENTIFIER, "#VAR"),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.IDENTIFIER, "#I"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void passParameterToNestedCopyCodes()
	{
		givenAnExternalCopyCodeWithSource("MYCC", "WRITE &1& #NOPARM");
		var result = lexAndResolve("WRITE #OUTER INCLUDE MYCC '#INNER'");
		assertTokensInOrder(
			result,
			token(SyntaxKind.WRITE),
			token(SyntaxKind.IDENTIFIER, "#OUTER"),
			token(SyntaxKind.WRITE),
			token(SyntaxKind.IDENTIFIER, "#INNER"),
			token(SyntaxKind.IDENTIFIER, "#NOPARM")
		);
	}

	@Test
	void setThePositionOfParametersToTheInnerPositionOfTheLiteralPassed()
	{
		givenAnExternalCopyCodeWithSource("MYCC", "WRITE &1& #NOPARM");
		var result = lexAndResolve("WRITE #OUTER INCLUDE MYCC '#INNER'");

		/*
		Given the code
		WRITE #OUTER INCLUDE MYCC '#INNER'
		the token within the copy code should span here:
		WRITE #OUTER INCLUDE MYCC '#INNER'
								------
		that should make it sure that e.g. refactoring could also rename within the literal
		and that passing multiple tokens like '5 > 5' can actually point at the part in the literal
		that causes a diagnostic
		 */

		var includeParameter = result.hiddenTokens().get(2);
		assertThat(includeParameter.kind()).isEqualTo(SyntaxKind.STRING_LITERAL);

		var resolvedParameter = result.allTokens().get(3);
		assertThat(resolvedParameter.source()).isEqualTo("#INNER");
		assertThat(resolvedParameter.offsetInLine()).isEqualTo(27);
		assertThat(resolvedParameter.offset()).isEqualTo(27);
		assertThat(resolvedParameter.length()).isEqualTo("#INNER".length());
		assertThat(resolvedParameter.endOffset()).isEqualTo(27 + "#INNER".length());
		assertThat(resolvedParameter.totalEndOffset()).isEqualTo(27 + "#INNER".length());
	}

	@Test
	void setTheDiagnosticPositionOfNonParameterTokensToTheCopyCodeName()
	{
		givenAnExternalCopyCodeWithSource("MYCC", "WRITE &1& #NOPARM");
		var result = lexAndResolve("WRITE #OUTER INCLUDE MYCC '#INNER'");

		var copyCodeNameToken = result.hiddenTokens().get(1);
		assertThat(copyCodeNameToken.kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(copyCodeNameToken.symbolName()).isEqualTo("MYCC");

		var writeToken = result.allTokens().get(2);
		assertThat(writeToken.diagnosticPosition()).isEqualTo(copyCodeNameToken);
	}

	@Test
	void correctlyHandleMultipleNestedIncludes()
	{
		givenAnExternalCopyCodeWithSource("MYCC", "WRITE 'MYCC' INCLUDE MYCC2");
		givenAnExternalCopyCodeWithSource("MYCC2", "WRITE 'MYCC2'");
		var result = lexAndResolve("WRITE 'OUTER' INCLUDE MYCC");
		assertTokensInOrder(
			result,
			token(SyntaxKind.WRITE),
			token(SyntaxKind.STRING_LITERAL, "'OUTER'"),
			token(SyntaxKind.WRITE),
			token(SyntaxKind.STRING_LITERAL, "'MYCC'"),
			token(SyntaxKind.WRITE),
			token(SyntaxKind.STRING_LITERAL, "'MYCC2'")
		);
	}

	@Test
	void substituteParameterToCreateAQualifiedNameIncludingArrayAccessIfNestedIdentifierIsAKeywordThatCanBeIdentifier()
	{
		givenAnExternalCopyCodeWithSource("MACC", "&1&.ISN(*)");
		var result = lexAndResolve("INCLUDE MACC '#GRP'");
		assertTokensInOrder(
			result,
			token(SyntaxKind.IDENTIFIER, "#GRP.ISN"),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.ASTERISK),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void raiseADiagnosticIfParametersAreMissing()
	{
		givenAnExternalCopyCodeWithSource("PARCC", "&1& := &2&");
		var result = lexAndResolve("INCLUDE PARCC '#VAR'");

		var ccNameToken = result.hiddenTokens().get(1);
		assertThat(ccNameToken.symbolName()).isEqualTo("PARCC");

		assertThat(result.diagnostics()).hasSize(1);

		var diagnostic = result.diagnostics().first();
		assertThat(diagnostic.id()).isEqualTo(LexerError.MISSING_COPYCODE_PARAMETER.id());
		assertThat(diagnostic.message()).isEqualTo("Copy code parameter with position 2 not provided");
		assertThat(diagnostic.isSamePositionAs(ccNameToken))
			.as("Expected the diagnostic to be raised on the CC name")
			.isTrue();

		assertThat(diagnostic.additionalInfo()).hasSize(1);
		var danglingParameterInCC = result.peek(2);
		assertThat(danglingParameterInCC.kind()).isEqualTo(SyntaxKind.COPYCODE_PARAMETER);
		assertThat(danglingParameterInCC.source()).isEqualTo("&2&");
		var additionalDiagnosticInCC = diagnostic.additionalInfo().first();
		assertThat(additionalDiagnosticInCC.position()).isEqualTo(danglingParameterInCC);
		assertThat(additionalDiagnosticInCC.message()).isEqualTo("Parameter is used here");
	}

	@Test
	void substituteSingleQuotedStringLiterals()
	{
		givenAnExternalCopyCodeWithSource("STRLIT", "&1&");
		var result = lexAndResolve("INCLUDE STRLIT '''SOME TEXT'''");
		assertThat(result.diagnostics).isEmpty();
		var token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.STRING_LITERAL);
		assertThat(token.source()).isEqualTo("'SOME TEXT'");
	}

	@Test
	void substituteDoubleQuotedStringLiterals()
	{
		givenAnExternalCopyCodeWithSource("STRLIT", "&1&");
		var result = lexAndResolve("INCLUDE STRLIT \"\"\"SOME TEXT\"\"\"");
		assertThat(result.diagnostics).isEmpty();
		var token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.STRING_LITERAL);
		assertThat(token.source()).isEqualTo("\"SOME TEXT\"");
	}

	@Test
	@Disabled("This doesn't compile (at least on open systems). The nested include is not allowed to have '&1&', it needs to be &1& and be passed with multiple quotes")
	void substituteStringLiteralsForMultipleNestedLevelsWithoutPassingQuotes()
	{
		givenAnExternalCopyCodeWithSource("CC1", "INCLUDE CC2 '&1&'");
		givenAnExternalCopyCodeWithSource("CC2", "&1&");
		var result = lexAndResolve("INCLUDE CC1 '#VAR'");
		assertThat(result.diagnostics).isEmpty();
		var token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(token.symbolName()).isEqualTo("#VAR");
	}

	@Test
	void substituteStringLiteralsForMultipleNestedLevelsWithPassingQuotes()
	{
		givenAnExternalCopyCodeWithSource("CC1", "INCLUDE CC2 &1&");
		givenAnExternalCopyCodeWithSource("CC2", "&1&");
		var result = lexAndResolve("INCLUDE CC1 '''#VAR'''");
		assertThat(result.diagnostics).isEmpty();
		var token = result.advance();
		assertThat(token.kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(token.symbolName()).isEqualTo("#VAR");
	}

	private TokenList lexAndResolve(String source)
	{
		return sut.lex(source, Path.of("OUTER.NSN"), moduleProvider);
	}

	private void givenAnExternalCopyCodeWithSource(String name, String source)
	{
		var path = Path.of(name + ".NSC");
		when(filesystem.readFile(path)).thenReturn(source);
		var module = mock(INaturalModule.class);
		when(module.file()).thenReturn(new NaturalFile(name, path, NaturalFileType.COPYCODE));
		when(moduleProvider.findNaturalModule(name)).thenReturn(module);
	}

}
