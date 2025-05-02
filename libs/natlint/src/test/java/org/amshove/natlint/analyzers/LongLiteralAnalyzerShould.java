package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class LongLiteralAnalyzerShould extends AbstractAnalyzerTest
{
	protected LongLiteralAnalyzerShould()
	{
		super(new LongLiteralAnalyzer());
	}

	@Test
	void reportDiagnosticIfLongLineAndLowercase()
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_long_literals=true
			""");

		var source = """
            DEFINE DATA LOCAL
            1 #VAR (A20)
            END-DEFINE
			#VAR := 'This is a long literal.'
            END
            """;
		testDiagnostics(source, expectDiagnostic(3, LongLiteralAnalyzer.LONG_LITERAL_DETECTED));
	}

	@Test
	void reportNoDiagnosticIfShortLine()
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_long_literals=true
			""");

		var source = """
            DEFINE DATA LOCAL
            1 #VAR (A20)
            END-DEFINE
			#VAR := 'Yes'
            END
            """;
		testDiagnostics(source, expectNoDiagnosticOfType(LongLiteralAnalyzer.LONG_LITERAL_DETECTED));
	}

	@Test
	void reportNoDiagnosticIfLongLineAndUppercase()
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_long_literals=true
			""");

		var source = """
            DEFINE DATA LOCAL
            1 #VAR (A20)
            END-DEFINE
			#VAR := 'ABCDEFGHIJKLM'
            END
            """;
		testDiagnostics(source, expectNoDiagnosticOfType(LongLiteralAnalyzer.LONG_LITERAL_DETECTED));
	}
}
