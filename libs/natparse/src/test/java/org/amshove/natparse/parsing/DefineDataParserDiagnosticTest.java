package org.amshove.natparse.parsing;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ResourceHelper;
import org.amshove.natparse.lexing.Lexer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class DefineDataParserDiagnosticTest
{
	@TestFactory
	Iterable<DynamicTest> diagnosticTests()
	{
		var diagnosticTests = ResourceHelper.findRelativeResourceFiles("definedatadiagnostics", getClass());
		return diagnosticTests.stream()
			.map(path -> {
				var name = Path.of(path).getFileName().toString();
				var source = ResourceHelper.readResourceFile(path);
				return dynamicTest(name, () -> {
					var expectedDiagnostics = findExpectedDiagnostics(source);

					var lexer = new Lexer();
					var tokens = lexer.lex(source);
					var parser = new DefineDataParser();
					var parseResult = parser.parse(tokens);

					var tests = new ArrayList<Executable>();

					for (var diagnostic : parseResult.diagnostics())
					{
						tests.add(() ->
							assertThat(expectedDiagnostics)
								.as("Diagnostic [%s] not expected, but found".formatted(diagnostic))
								.anyMatch(d -> d.matches(diagnostic))
						);
					}

					for (var expectedDiagnostic : expectedDiagnostics)
					{
						tests.add(() ->
							assertThat(parseResult.diagnostics())
								.as("Diagnostic [%s] expected but not found".formatted(expectedDiagnostic))
								.anyMatch(d -> ExpectedDiagnostic.doMatch(expectedDiagnostic, d))
						);
					}

					var expectedDiagnosticsHeading = expectedDiagnostics.stream().map(ExpectedDiagnostic::toString).collect(Collectors.joining("\n"));
					var actualDiagnosticsHeading = parseResult.diagnostics().stream().map(IDiagnostic::toVerboseString).collect(Collectors.joining("\n"));

					var heading = """
      					File: %s
      					
						Expected Diagnostics:
						%s

						Actual Diagnostics:
						%s
						""".formatted(name, expectedDiagnosticsHeading, actualDiagnosticsHeading).stripIndent();

					assertAll(heading, tests);
				});
			})
			.toList();

	}

	private static List<ExpectedDiagnostic> findExpectedDiagnostics(String source)
	{
		var lines = source.split("\n");
		var expectedDiagnostics = new ArrayList<ExpectedDiagnostic>();

		for (var i = 0; i < lines.length; i++)
		{
			var line = lines[i];
			if (!line.contains("!{D:"))
			{
				continue;
			}

			var split = line.split("!\\{D:");
			var severityAndId = split[1].split(":");
			var severity = DiagnosticSeverity.valueOf(severityAndId[0]);
			var id = severityAndId[1].split("}")[0];

			expectedDiagnostics.add(new ExpectedDiagnostic(i, id, severity));
		}

		return expectedDiagnostics;
	}

	private static class ExpectedDiagnostic
	{
		int line;
		String id;
		DiagnosticSeverity severity;

		public ExpectedDiagnostic(int line, String id, DiagnosticSeverity severity)
		{
			this.line = line;
			this.id = id;
			this.severity = severity;
		}

		public boolean matches(IDiagnostic diagnostic)
		{
			return doMatch(this, diagnostic);
		}

		public static boolean doMatch(ExpectedDiagnostic expectedDiagnostic, IDiagnostic diagnostic)
		{
			return diagnostic.id().equals(expectedDiagnostic.id)
				&& diagnostic.line() == expectedDiagnostic.line
				&& diagnostic.severity() == expectedDiagnostic.severity;
		}

		@Override public String toString()
		{
			return "ExpectedDiagnostic{line=" + line + ", id='" + id + '\'' + ", severity=" + severity + '}';
		}
	}
}
