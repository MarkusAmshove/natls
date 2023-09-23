package org.amshove.natlint.linter;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.LinterDiagnostic;
import org.amshove.natlint.editorconfig.EditorConfig;
import org.amshove.natlint.editorconfig.EditorConfigParser;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.testhelpers.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

@IntegrationTest
public abstract class AbstractAnalyzerTest
{
	private final AbstractAnalyzer analyzerUnderTest;
	private final List<String> allowedParserErrors = new ArrayList<>();
	private EditorConfig editorConfigToUse;
	@TempDir
	Path directoryForSyntheticFiles;

	@Test
	void analyzerShouldExportRules()
	{
		assertThat(analyzerUnderTest.getDiagnosticDescriptions()).isNotEmpty();
	}

	protected AbstractAnalyzerTest(AbstractAnalyzer analyzerUnderTest)
	{
		this.analyzerUnderTest = analyzerUnderTest;
	}

	protected void configureEditorConfig(String editorConfig)
	{
		var parser = new EditorConfigParser();
		editorConfigToUse = parser.parse(editorConfig);
	}

	@AfterEach
	void afterEach()
	{
		editorConfigToUse = null;
	}

	protected void testDiagnostics(@Nullable NaturalFile file, DiagnosticAssertion... diagnosticAssertions)
	{
		if (file == null)
		{
			throw new RuntimeException("Natural file could not be found");
		}

		if (diagnosticAssertions.length == 0)
		{
			fail("At least one diagnostic has to be asserted. Use expectDiagnostic() or expectNoDiagnostic()");
		}

		var diagnostics = lint(file);
		for (var expectedDiagnostic : diagnosticAssertions)
		{
			assertDescriptionIsExported(expectedDiagnostic.description());
		}
		assertAll(
			"Expected and unexpected diagnostics",
			() -> assertAll(
				"Expected Diagostics",
				Arrays.stream(diagnosticAssertions)
					.map(e -> e.checkAssertion(diagnostics.toList()))
			),
			() -> assertAll(
				"Unexpected Diagnostics",
				diagnostics.stream()
					.map(
						d -> () -> assertThat(diagnosticAssertions)
							.as("Expected diagnostic %s to not be present, but was", d)
							.anyMatch(e -> e.matches(d))
					)
			)
		);
	}

	protected void testDiagnostics(String source, DiagnosticAssertion... expectedDiagnostics)
	{
		testDiagnostics("TESTFILE.NSN", source, expectedDiagnostics);
	}

	protected void testDiagnostics(String filename, String source, DiagnosticAssertion... expectedDiagnostics)
	{
		testDiagnostics(testFile(filename, source), expectedDiagnostics);
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
		LinterContext.INSTANCE.initializeAnalyzers();
		LinterContext.INSTANCE.updateEditorConfig(editorConfigToUse);
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
			assertThat(parseResult.diagnostics().stream().filter(d -> !allowedParserErrors.contains(d.id())))
				.as("No Parser errors should be present to test an analyzer, but found\n%s", parseResult.diagnostics().stream().map(IDiagnostic::toString).collect(Collectors.joining("\n")))
				.isEmpty();

			return parseResult;
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private NaturalFile testFile(String name, String source)
	{
		var syntheticFilePath = directoryForSyntheticFiles.resolve(name);
		var withoutExtension = name.split("\\.")[0];
		var extension = name.split("\\.")[1];
		var syntheticFile = new SyntheticNaturalFile(withoutExtension, syntheticFilePath, NaturalFileType.fromExtension(extension));
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

	protected void allowParserError(String id)
	{
		allowedParserErrors.add(id);
	}

	protected DiagnosticAssertion expectDiagnostic(int line, DiagnosticDescription description)
	{
		return new ExpectedDiagnostic(line, description);
	}

	protected DiagnosticAssertion expectDiagnostic(int line, DiagnosticDescription description, String expectedMessage)
	{
		return new ExpectedDiagnosticWithMessage(line, description, expectedMessage);
	}

	protected DiagnosticAssertion expectNoDiagnostic(int line, DiagnosticDescription description)
	{
		return new ExpectedNoDiagnostic(line, description);
	}

	protected DiagnosticAssertion expectNoDiagnosticOfType(DiagnosticDescription description)
	{
		return new ExpectedNoDiagnosticOfType(description);
	}

	protected sealed interface DiagnosticAssertion
	{
		int line();

		DiagnosticDescription description();

		Executable checkAssertion(List<LinterDiagnostic> actualDiagnostics);

		default boolean matches(IDiagnostic diagnostic)
		{
			return diagnostic.line() == line() && diagnostic.id().equals(description().getId());
		}
	}

	protected record ExpectedDiagnostic(int line, DiagnosticDescription description) implements DiagnosticAssertion
	{
		@Override
		public Executable checkAssertion(List<LinterDiagnostic> actualDiagnostics)
		{
			return () -> assertThat(actualDiagnostics)
				.as("Expected diagnostic %s to be present but was not", this)
				.anyMatch(this::matches);
		}

		@Override
		public String toString()
		{
			return "ExpectedDiagnostic{" +
				"line=" + line +
				", description=" + description.getId() +
				'}';
		}
	}

	protected record ExpectedDiagnosticWithMessage(int line, DiagnosticDescription description, String expectedMessage) implements DiagnosticAssertion
	{
		@Override
		public Executable checkAssertion(List<LinterDiagnostic> actualDiagnostics)
		{
			return () -> assertThat(actualDiagnostics)
				.as("Expected diagnostic %s to be present but was not", this)
				.anyMatch(this::matches);
		}

		@Override
		public boolean matches(IDiagnostic diagnostic)
		{
			return DiagnosticAssertion.super.matches(diagnostic) && diagnostic.message().equals(expectedMessage);
		}

		@Override
		public String toString()
		{
			return "ExpectedDiagnosticWithMessage{line=" + line + ", description=" + description.getId() + ", expectedMessage=" + expectedMessage + "}";
		}
	}

	protected record ExpectedNoDiagnostic(int line, DiagnosticDescription description) implements DiagnosticAssertion
	{
		@Override
		public Executable checkAssertion(List<LinterDiagnostic> actualDiagnostics)
		{
			return () -> assertThat(actualDiagnostics)
				.as("Expected diagnostic %s to not be present", this)
				.noneMatch(this::matches);
		}

		@Override
		public String toString()
		{
			return "ExpectedNoDiagnostic{" +
				"line=" + line +
				", description=" + description.getId() +
				'}';
		}
	}

	protected record ExpectedNoDiagnosticOfType(DiagnosticDescription description) implements DiagnosticAssertion
	{
		@Override
		public int line()
		{
			return 0;
		}

		@Override
		public Executable checkAssertion(List<LinterDiagnostic> actualDiagnostics)
		{
			return () -> assertThat(actualDiagnostics)
				.as("Expected no diagnostic with id %s", description.getId())
				.noneMatch(d -> d.id().equals(description.getId()));
		}

		@Override
		public String toString()
		{
			return "ExpectedNoDiagnosticOfType{" +
				"description=" + description.getId() +
				'}';
		}
	}
}
