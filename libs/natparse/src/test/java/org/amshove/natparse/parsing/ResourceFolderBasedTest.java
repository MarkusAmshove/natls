package org.amshove.natparse.parsing;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.testhelpers.ResourceHelper;
import org.junit.jupiter.api.DynamicTest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class ResourceFolderBasedTest
{
	protected Iterable<DynamicTest> testFolder(String relativeFolderPath)
	{
		var testFiles = ResourceHelper.findRelativeResourceFiles(relativeFolderPath, getClass());

		if (testFiles.isEmpty())
		{
			throw new RuntimeException("No Testfiles found in %s".formatted(relativeFolderPath));
		}

		return testFiles.stream()
			.flatMap(testFile ->
			{
				var testFilePath = Path.of(testFile);
				var testFileName = testFilePath.getFileName().toString();
				var source = ResourceHelper.readResourceFile(testFile, getClass());
				if (source.isEmpty())
				{
					throw new RuntimeException("Empty source read or no source found for %s".formatted(testFilePath));
				}

				var testsInFile = new ArrayList<DynamicTest>();
				var expectedDiagnostics = findExpectedDiagnostics(source);

				var lexer = new Lexer();
				var tokens = lexer.lex(source, testFilePath);
				var diagnostics = new ArrayList<>(tokens.diagnostics().toList());
				var parser = new NaturalParser();
				var parseResult = parser.parse(new NaturalFile(testFileName, testFilePath, NaturalFileType.SUBPROGRAM), tokens);
				parseResult.diagnostics().stream()
					.filter(d -> !d.id().equals(ParserError.UNRESOLVED_IMPORT.id()))
					.forEach(diagnostics::add);

				for (var diagnostic : diagnostics)
				{
					testsInFile.add(dynamicTest(testFileName + ": Actual diagnostic in line " + (diagnostic.line() + 1), () ->
					{
						if (expectedDiagnostics.stream().noneMatch(d -> d.matches(diagnostic)))
						{
							fail("Diagnostic [%s] not expected but found".formatted(diagnostic));
						}
					}));
				}

				if (expectedDiagnostics.isEmpty())
				{
					testsInFile.add(dynamicTest(testFileName + ": Expected no diagnostics", () ->
					{
						assertThat(diagnostics).isEmpty();
					}));
				}

				for (var expectedDiagnostic : expectedDiagnostics)
				{
					testsInFile.add(dynamicTest(testFileName + ": Expected diagnostic in line " + (expectedDiagnostic.line + 1) + " not found", () ->
					{
						if (diagnostics.stream().noneMatch(d -> ExpectedDiagnostic.doMatch(expectedDiagnostic, d)))
						{
							fail("Diagnostic [%s] expected but not found".formatted(expectedDiagnostic));
						}
					}));
				}

				return testsInFile.stream();
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
			for (var diagnosticIndex = 1; diagnosticIndex < split.length; diagnosticIndex++)
			{
				var severityAndId = split[diagnosticIndex].split(":");
				var severity = DiagnosticSeverity.valueOf(severityAndId[0]);
				var id = severityAndId[1].split("}")[0];

				expectedDiagnostics.add(new ExpectedDiagnostic(i, id, severity));
			}
		}

		return expectedDiagnostics;
	}

	public record ResouceFileBasedTest(Path filepath, List<ExpectedDiagnostic> expectedDiagnostics)
	{}

	public record ExpectedDiagnostic(int line, String id, DiagnosticSeverity severity)
	{
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

		@Override
		public String toString()
		{
			return "ExpectedDiagnostic{line=" + line + ", id='" + id + '\'' + ", severity=" + severity + '}';
		}
	}

	private static class NullModuleProvider implements IModuleProvider
	{

		@Override
		public INaturalModule findNaturalModule(String referableName)
		{
			return null;
		}

		@Override
		public IDataDefinitionModule findDdm(String referableName)
		{
			return null;
		}
	}
}
