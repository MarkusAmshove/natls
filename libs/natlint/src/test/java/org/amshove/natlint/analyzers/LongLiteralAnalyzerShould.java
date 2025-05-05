package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LongLiteralAnalyzerShould extends AbstractAnalyzerTest
{
	protected LongLiteralAnalyzerShould()
	{
		super(new LongLiteralAnalyzer());
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"This is a long literal with Danish special characters øæå as well as German ä, ö, ü, ß",
		"A MiXeD CaSe LiNe..",
		"Limit",
		"åøæäöüß",
	})
	void reportDiagnosticIfLongLineAndLowercase(String literal)
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_long_literals=true
			""");

		var source = """
            DEFINE DATA LOCAL
            1 #VAR (A20)
            END-DEFINE
			#VAR := '%s'
            END
            """.formatted(literal);

		testDiagnostics(source, expectDiagnostic(3, LongLiteralAnalyzer.LONG_LITERAL_DETECTED));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"Yes",
		"æøå",
		"äöüß",
	})
	void reportNoDiagnosticIfShortLine(String literal)
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_long_literals=true
			""");

		var source = """
            DEFINE DATA LOCAL
            1 #VAR (A20)
            END-DEFINE
			#VAR := '%s'
            END
            """.formatted(literal);

		testDiagnostics(source, expectNoDiagnosticOfType(LongLiteralAnalyzer.LONG_LITERAL_DETECTED));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"DEFINE WORK FILE 1",
		"ABCDEFGHIJKLM",
		"ÆØÅÄÖÜ:",
		"STOP",
		"SPECIAL CHARS:!#\"¤(%=(=?\\/<>))",
		"Y/N"
	})
	void reportNoDiagnosticIfLineIsUppercase(String literal)
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_long_literals=true
			""");

		var source = """
            DEFINE DATA LOCAL
            1 #VAR (A20)
            END-DEFINE
			#VAR := '%s'
            END
            """.formatted(literal);

		testDiagnostics(source, expectNoDiagnosticOfType(LongLiteralAnalyzer.LONG_LITERAL_DETECTED));
	}
}
