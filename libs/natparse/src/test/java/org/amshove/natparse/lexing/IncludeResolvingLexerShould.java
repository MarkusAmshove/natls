package org.amshove.natparse.lexing;

import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.IModuleProvider;
import org.junit.jupiter.api.BeforeEach;
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
	void setTheDiagnosticPositionOfParameterToTheParameterOnTheIncludeSide()
	{
		givenAnExternalCopyCodeWithSource("MYCC", "WRITE &1& #NOPARM");
		var result = lexAndResolve("WRITE #OUTER INCLUDE MYCC '#INNER'");

		var includeParameter = result.hiddenTokens().get(2);
		assertThat(includeParameter.kind()).isEqualTo(SyntaxKind.STRING_LITERAL);

		var resolvedParameter = result.allTokens().get(3);
		assertThat(resolvedParameter.diagnosticPosition()).isEqualTo(includeParameter);
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
