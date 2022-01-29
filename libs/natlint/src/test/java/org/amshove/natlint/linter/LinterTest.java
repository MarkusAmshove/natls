package org.amshove.natlint.linter;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.LinterDiagnostic;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.testhelpers.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
public abstract class LinterTest
{
	private final AbstractAnalyzer analyzerUnderTest;
	@TempDir Path directoryForSyntheticFiles;

	@Test
	void analyzerShouldExportRules()
	{
		assertThat(analyzerUnderTest.getDiagnosticDescriptions()).isNotEmpty();
	}

	protected LinterTest(AbstractAnalyzer analyzerUnderTest)
	{
		this.analyzerUnderTest = analyzerUnderTest;
	}

	protected void assertDiagnostic(int line, DiagnosticDescription diagnosticDescription, @Nullable NaturalFile file)
	{
		assertDescriptionIsExported(diagnosticDescription);
		var diagnostics = lint(file);
		assertThat(diagnostics)
			.anyMatch(d ->
				d.line() == line
					&& d.id().equals(diagnosticDescription.getId())
			);
	}

	protected void assertDiagnostic(int line, DiagnosticDescription diagnosticDescription, String source)
	{
		assertDiagnostic(line, diagnosticDescription, testFile(source));
	}

	protected void assertNoDiagnostic(DiagnosticDescription diagnosticDescription, @Nullable NaturalFile file)
	{
		assertDescriptionIsExported(diagnosticDescription);
		var diagnostics = lint(file);
		assertThat(diagnostics)
			.noneMatch(d -> d.id().equals(diagnosticDescription.getId()));
	}

	protected void assertNoDiagnostic(DiagnosticDescription diagnosticDescription, String source)
	{
		assertNoDiagnostic(diagnosticDescription, testFile(source));
	}

	private void assertDescriptionIsExported(DiagnosticDescription diagnosticDescription)
	{
		assertThat(analyzerUnderTest.getDiagnosticDescriptions())
			.as("Test aborted, DiagnosticDescription %s is not exported by %s", diagnosticDescription.getId(), analyzerUnderTest.getClass().getSimpleName())
			.contains(diagnosticDescription);
	}

	private ReadOnlyList<LinterDiagnostic> lint(NaturalFile file)
	{
		var module = parse(file);
		LinterContext.INSTANCE.reset();
		LinterContext.INSTANCE.registerAnalyzer(analyzerUnderTest);
		var linter = new NaturalLinter();
		return linter.lint(module);
	}

	private INaturalModule parse(NaturalFile file)
	{
		try
		{
			var source = Files.readString(file.getPath());
			var tokenList = new Lexer().lex(source, file.getPath());
			assertThat(tokenList.diagnostics())
				.as("No Lexer errors should be present to test an analyzer")
				.isEmpty();

			var parser = new NaturalParser();
			var parseResult = parser.parse(file, tokenList);
			assertThat(parseResult.diagnostics())
				.as("No Parser errors should be present to test an analyzer, but found\n%s", parseResult.diagnostics().stream().map(IDiagnostic::toString).collect(Collectors.joining("\n")))
				.isEmpty();

			return parseResult;
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private NaturalFile testFile(String source)
	{
		var syntheticFilePath = directoryForSyntheticFiles.resolve("TESTFILE.NSN");
		var syntheticFile = new NaturalFile("TESTFILE", syntheticFilePath, NaturalFileType.SUBPROGRAM);
		try
		{
			Files.writeString(syntheticFilePath, source);
			return syntheticFile;
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
