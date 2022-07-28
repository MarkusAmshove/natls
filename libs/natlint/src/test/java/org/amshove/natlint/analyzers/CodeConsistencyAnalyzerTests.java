package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.lexing.SyntaxKind;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class CodeConsistencyAnalyzerTests extends AbstractAnalyzerTest
{
	protected CodeConsistencyAnalyzerTests()
	{
		super(new CodeConsistencyAnalyzer());
	}

	@TestFactory
	Stream<DynamicTest> testTokenPreferences()
	{
		var preferences = List.of(
			new TokenPreferenceConfig(SyntaxKind.OCCURRENCE, SyntaxKind.OCC, 5, """
				DEFINE DATA
				LOCAL
				1 #ARR (A10/*)
				1 #I (I4)
				END-DEFINE
				#I := *%s(1)
				END
				""")
		);

		return preferences.stream()
			.flatMap(config ->
				Stream.of(
					dynamicTest("%s should report a diagnostic to prefer %s".formatted(config.unwantedToken, config.preferredToken),
						() -> testDiagnostics(config.createSadPathCode(), expectDiagnostic(config.line, CodeConsistencyAnalyzer.PREFER_DIFFERENT_TOKEN))),

					dynamicTest("%s should not report a diagnostic to prefer anything else".formatted(config.preferredToken),
						() -> testDiagnostics(config.createHappyPathCode(), expectNoDiagnosticOfType(CodeConsistencyAnalyzer.PREFER_DIFFERENT_TOKEN)))
				)
			);
	}

	private record TokenPreferenceConfig(SyntaxKind unwantedToken, SyntaxKind preferredToken, int line, String sourceToReplicate)
	{
		String createHappyPathCode()
		{
			return sourceToReplicate.formatted(preferredToken.toString());
		}

		String createSadPathCode()
		{
			return sourceToReplicate.formatted(unwantedToken.toString());
		}
	}
}
