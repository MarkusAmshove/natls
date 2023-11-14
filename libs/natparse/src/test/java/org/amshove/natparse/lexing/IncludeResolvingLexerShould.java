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
	void setTheDiagnosticPositionOfParameterTokensToTheCopyCodeName()
	{
		givenAnExternalCopyCodeWithSource("MYCC", "WRITE &1& #NOPARM");
		var result = lexAndResolve("WRITE #OUTER INCLUDE MYCC '#INNER'");

		var copyCodeNameToken = result.hiddenTokens().get(1);
		assertThat(copyCodeNameToken.kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(copyCodeNameToken.symbolName()).isEqualTo("MYCC");

		var noParm = result.allTokens().get(result.allTokens().size() - 1);
		assertThat(noParm.diagnosticPosition()).isEqualTo(copyCodeNameToken);
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
	void setTheDiagnosticPositionOfNestedIncludesToTheOuterMostIncludeCopyCodeName()
	{
		givenAnExternalCopyCodeWithSource("MYCC", "WRITE 'MYCC' INCLUDE MYCC2");
		givenAnExternalCopyCodeWithSource("MYCC2", "WRITE 'INMYCC2'");
		var result = lexAndResolve("WRITE 'OUTER' INCLUDE MYCC");

		var lastToken = result.allTokens().get(result.allTokens().size() - 1);
		assertThat(lastToken.source()).isEqualTo("'INMYCC2'");

		var copyCodeNameToken = result.hiddenTokens().get(1);
		assertThat(copyCodeNameToken.kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(copyCodeNameToken.symbolName()).isEqualTo("MYCC");

		assertThat(lastToken.diagnosticPosition()).isEqualTo(copyCodeNameToken);
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
	void addParameterUsagesOfUnprovidedParameterAsAdditionalPositions()
	{
		givenAnExternalCopyCodeWithSource("PARCC", "&1& := 'Hello'\nWRITE &1&");
		var result = lexAndResolve("INCLUDE PARCC");

		assertThat(result.diagnostics()).hasSize(1);
		var diagnostic = result.diagnostics().first();
		assertThat(diagnostic.fileType()).isEqualTo(NaturalFileType.SUBPROGRAM);

		assertThat(diagnostic.additionalInfo()).hasSize(2);
		assertThat(diagnostic.additionalInfo().first().position().fileType()).isEqualTo(NaturalFileType.COPYCODE);
		assertThat(diagnostic.additionalInfo().first().position().offset()).isEqualTo(0);
		assertThat(diagnostic.additionalInfo().last().position().fileType()).isEqualTo(NaturalFileType.COPYCODE);
		assertThat(diagnostic.additionalInfo().last().position().offset()).isEqualTo(21);
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

	@Test
	void correctlyBuildTokensWhenACopyCodeParameterIsUsedAfterIncludeButNotAsIncludeParameter()
	{
		givenAnExternalCopyCodeWithSource("CC1", "INCLUDE CC2 &1&\n&2& := 10");
		givenAnExternalCopyCodeWithSource("CC2", "WRITE &1&");
		var result = lexAndResolve("INCLUDE CC1 '''#VAR''' '#VAR2'");
		assertTokensInOrder(
			result,
			token(SyntaxKind.WRITE),
			token(SyntaxKind.IDENTIFIER, "#VAR"),
			token(SyntaxKind.IDENTIFIER, "#VAR2"),
			token(SyntaxKind.COLON_EQUALS_SIGN),
			token(SyntaxKind.NUMBER_LITERAL, "10")
		);
	}

	@Test
	void raiseADiagnosticIfACopyCodeCantBeResolved()
	{
		var result = lexAndResolve("INCLUDE UNRES");
		assertThat(result.diagnostics()).hasSize(1);
		assertThat(result.diagnostics().first().id()).isEqualTo(LexerError.UNRESOLVED_COPYCODE.id());
		assertThat(result.diagnostics().first().message()).isEqualTo("Unresolved copy code UNRES");
	}

	@Test
	void raiseADiagnosticIfAnInvalidModuleTypeIsUsedForInclude()
	{
		givenAnExternalSubprogramExists("SUBPROG");
		var result = lexAndResolve("INCLUDE SUBPROG");
		assertThat(result.diagnostics()).hasSize(1);
		assertThat(result.diagnostics().first().id()).isEqualTo(LexerError.INVALID_INCLUDE_TYPE.id());
		assertThat(result.diagnostics().first().message()).isEqualTo("Module type SUBPROGRAM can't be used with INCLUDE");
	}

	@Test
	void addPartsAfterCopyCodeParameterThatAreNotSeparatedWithWhitespaceToTheParameterToken()
	{
		givenAnExternalCopyCodeWithSource("CC", "WRITE &1&_S &1&");
		var result = lexAndResolve("INCLUDE CC '#VAR'");
		assertTokensInOrder(
			result,
			token(SyntaxKind.WRITE),
			token(SyntaxKind.IDENTIFIER, "#VAR_S"),
			token(SyntaxKind.IDENTIFIER, "#VAR")
		);
	}

	@Test
	void convertAnIdentifierEndingInDotToLabelIdentifier()
	{
		givenAnExternalCopyCodeWithSource("CC", "&1&.");
		var result = lexAndResolve("INCLUDE CC 'VAR'");
		assertTokensInOrder(
			result,
			token(SyntaxKind.LABEL_IDENTIFIER, "VAR.")
		);
	}

	@Test
	void combineIdentifiersWithKeywordsThatCanBeIdentifier()
	{
		givenAnExternalCopyCodeWithSource("MYCC", "ASSIGN &1&.COUNT(#I) = #COUNT");
		var result = lexAndResolve("INCLUDE MYCC 'VAR'");
		assertTokensInOrder(
			result,
			token(SyntaxKind.ASSIGN),
			token(SyntaxKind.IDENTIFIER, "VAR.COUNT"),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.IDENTIFIER, "#I"),
			token(SyntaxKind.RPAREN),
			token(SyntaxKind.EQUALS_SIGN),
			token(SyntaxKind.IDENTIFIER, "#COUNT")
		);
	}

	@Test
	void raiseADiagnosticForCyclomaticIncludes()
	{
		givenAnExternalCopyCodeWithSource("CC1", "INCLUDE CC2");
		givenAnExternalCopyCodeWithSource("CC2", "INCLUDE CC1");
		var result = lexAndResolve("INCLUDE CC1");
		assertThat(result.diagnostics()).hasSize(1);
		assertThat(result.diagnostics().first().id()).isEqualTo(LexerError.CYCLOMATIC_INCLUDE.id());
		assertThat(result.diagnostics().first().message()).isEqualTo("Cyclomatic include found. CC1 is recursively included multiple times");
	}

	@Test
	void correctlyPassIncludeStringConcatParameter()
	{
		givenAnExternalCopyCodeWithSource("CC", "WRITE &1&");
		var result = lexAndResolve("INCLUDE CC '#VAR1'\n- ' #VAR2'");
		assertTokensInOrder(
			result,
			token(SyntaxKind.WRITE),
			token(SyntaxKind.IDENTIFIER, "#VAR1"),
			token(SyntaxKind.IDENTIFIER, "#VAR2")
		);
	}

	@Test
	void correctlyPassIncludeStringConcatParameterWithMultipleConcats()
	{
		givenAnExternalCopyCodeWithSource("CC", "WRITE &1& &2&");
		var result = lexAndResolve("INCLUDE CC '#VAR1'\n- ' #VAR2'\n- ' #VAR3' '#VAR4'");
		assertThat(result.diagnostics()).isEmpty();
		assertTokensInOrder(
			result,
			token(SyntaxKind.WRITE),
			token(SyntaxKind.IDENTIFIER, "#VAR1"),
			token(SyntaxKind.IDENTIFIER, "#VAR2"),
			token(SyntaxKind.IDENTIFIER, "#VAR3"),
			token(SyntaxKind.IDENTIFIER, "#VAR4")
		);
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

	private void givenAnExternalSubprogramExists(String name)
	{
		var path = Path.of(name + ".NSN");
		var module = mock(INaturalModule.class);
		when(module.file()).thenReturn(new NaturalFile(name, path, NaturalFileType.SUBPROGRAM));
		when(moduleProvider.findNaturalModule(name)).thenReturn(module);
	}

}
