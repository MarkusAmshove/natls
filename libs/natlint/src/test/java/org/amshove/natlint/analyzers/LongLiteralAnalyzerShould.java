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
	void reportDiagnosticIfLongLine()
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
	void reportNoDiagnosticIfNoLongLine()
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_long_literals=true
			""");

		var source = """
            DEFINE DATA LOCAL
            1 #VAR (A20)
            END-DEFINE
			#VAR := 'Short'
            END
            """;
		testDiagnostics(source, expectNoDiagnosticOfType(LongLiteralAnalyzer.LONG_LITERAL_DETECTED));
	}
}
