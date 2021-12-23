package org.amshove.natparse.parsing;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ResourceHelper;
import org.amshove.natparse.lexing.Lexer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class DefineDataParserDiagnosticTest
{
	@TestFactory
	Iterable<DynamicTest> diagnosticTests()
	{
		var diagnosticTests = ResourceHelper.findRelativeResourceFiles("definedatadiagnostics", getClass());
		return diagnosticTests.stream()
			.flatMap(path -> {
				var name = Path.of(path).getFileName().toString();
				var source = ResourceHelper.readResourceFile(path);
				var tests = new ArrayList<DynamicTest>();
				var expectedDiagnostics = findExpectedDiagnostics(source);

				var lexer = new Lexer();
				var tokens = lexer.lex(source, Path.of(path));
				var parser = new DefineDataParser();
				var parseResult = parser.parse(tokens);

				for (var diagnostic : parseResult.diagnostics())
				{
					tests.add(dynamicTest(name + ": Unexpected diagnostic in line " + (diagnostic.line() + 1), () -> {
						if(expectedDiagnostics.stream().noneMatch(d -> d.matches(diagnostic)))
						{
							fail("Diagnostic [%s] not expected but found".formatted(diagnostic));
						}
					}));
				}

				for (var expectedDiagnostic : expectedDiagnostics)
				{
					tests.add(dynamicTest(name + ": Expected diagnostic in line " + (expectedDiagnostic.line + 1) + " not found", () -> {
						if(parseResult.diagnostics().stream().noneMatch(d -> ExpectedDiagnostic.doMatch(expectedDiagnostic, d)))
						{
							fail("Diagnostic [%s] expected but not found".formatted(expectedDiagnostic));
						}
					}));
				}

				return tests.stream();
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
			for(var diagnosticIndex = 1; diagnosticIndex < split.length; diagnosticIndex++)
			{
				var severityAndId = split[diagnosticIndex].split(":");
				var severity = DiagnosticSeverity.valueOf(severityAndId[0]);
				var id = severityAndId[1].split("}")[0];

				expectedDiagnostics.add(new ExpectedDiagnostic(i, id, severity));
			}
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
