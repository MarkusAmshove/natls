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
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

@IntegrationTest
public abstract class AbstractAnalyzerTest
{
	private final AbstractAnalyzer analyzerUnderTest;
	@TempDir Path directoryForSyntheticFiles;

	@Test
	void analyzerShouldExportRules()
	{
		assertThat(analyzerUnderTest.getDiagnosticDescriptions()).isNotEmpty();
	}

	protected AbstractAnalyzerTest(AbstractAnalyzer analyzerUnderTest)
	{
		this.analyzerUnderTest = analyzerUnderTest;
	}

	protected void assertDiagnostics(@Nullable NaturalFile file, ExpectedDiagnostic... expectedDiagnostics)
	{
		if(expectedDiagnostics.length == 0)
		{
			fail("At least one diagnostic has to be expected");
		}

		var diagnostics = lint(file);
		for (var expectedDiagnostic : expectedDiagnostics)
		{
			assertDescriptionIsExported(expectedDiagnostic.description);
		}
		assertAll("Expected and unexpected diagnostics",
			() -> assertAll("Expected Diagostics",
				Arrays.stream(expectedDiagnostics)
					.map(e -> () -> assertThat(diagnostics)
						.as("Expected diagnostic %s to be present but was not", e)
						.anyMatch(d -> d.id().equals(e.description.getId()) && d.line() == e.line))
			),
			() -> assertAll("Unexpected Diagnostics",
				diagnostics.stream()
					.map(d -> () -> assertThat(expectedDiagnostics)
						.as("Expected diagnostic %s to not be present, but was", d)
						.anyMatch(e -> e.line == d.line() && e.description.getId().equals(d.id()))))
		);
	}

	protected void assertDiagnostics(String source, ExpectedDiagnostic... expectedDiagnostics)
	{
		assertDiagnostics(testFile(source), expectedDiagnostics);
	}

	protected void assertNoDiagnostics(@Nullable NaturalFile file, DiagnosticDescription diagnosticDescription)
	{
		assertDescriptionIsExported(diagnosticDescription);
		var diagnostics = lint(file);
		assertThat(diagnostics)
			.as("Expected no diagnostic with id %s", diagnosticDescription.getId())
			.noneMatch(d -> d.id().equals(diagnosticDescription.getId()));
	}

	protected void assertNoDiagnostics(String source, DiagnosticDescription diagnosticDescription)
	{
		assertNoDiagnostics(testFile(source), diagnosticDescription);
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

	protected ExpectedDiagnostic expectDiagnostic(int line, DiagnosticDescription description)
	{
		return new ExpectedDiagnostic(line, description);
	}

	protected static record ExpectedDiagnostic(int line, DiagnosticDescription description)
	{
	}
}
