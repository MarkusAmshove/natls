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

import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class LinterTest
{
	private final AbstractAnalyzer analyzerUnderTest;

	protected LinterTest(AbstractAnalyzer analyzerUnderTest)
	{
		this.analyzerUnderTest = analyzerUnderTest;
	}

	protected void assertDiagnostic(int line, DiagnosticDescription diagnosticDescription, String source)
	{
		assertDescriptionIsExported(diagnosticDescription);
		var diagnostics = lint(source);
		assertThat(diagnostics)
			.anyMatch(d ->
				d.line() == line
					&& d.id().equals(diagnosticDescription.getId())
			);
	}

	protected void assertNoDiagnostic(DiagnosticDescription diagnosticDescription, String source)
	{
		assertDescriptionIsExported(diagnosticDescription);
		var diagnostics = lint(source);
		assertThat(diagnostics)
			.noneMatch(d -> d.id().equals(diagnosticDescription.getId()));
	}

	protected void assertDescriptionIsExported(DiagnosticDescription diagnosticDescription)
	{
		assertThat(analyzerUnderTest.getDiagnosticDescriptions())
			.as("Test aborted, DiagnosticDescription %s is not exported by %s", diagnosticDescription.getId(), analyzerUnderTest.getClass().getSimpleName())
			.contains(diagnosticDescription);
	}

	private ReadOnlyList<LinterDiagnostic> lint(String source)
	{
		var module = parse(source);
		LinterContext.INSTANCE.reset();
		LinterContext.INSTANCE.registerAnalyzer(analyzerUnderTest);
		var linter = new NaturalLinter();
		return linter.lint(module);
	}

	private INaturalModule parse(String source)
	{
		var testpath = Paths.get("TESTPATH.NSN");
		var tokenList = new Lexer().lex(source, testpath);
		assertThat(tokenList.diagnostics())
			.as("No Lexer errors should be present to test an analyzer")
			.isEmpty();

		var parser = new NaturalParser();
		var parseResult = parser.parse(new NaturalFile("TESTPATH", testpath, NaturalFileType.SUBPROGRAM), tokenList);
		assertThat(parseResult.diagnostics())
			.as("No Parser errors should be present to test an analyzer, but found\n%s", parseResult.diagnostics().stream().map(IDiagnostic::toString).collect(Collectors.joining("\n")))
			.isEmpty();

		return parseResult;
	}
}
