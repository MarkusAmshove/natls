package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

public class LongLinesAnalyzerShould extends AbstractAnalyzerTest
{
	protected LongLinesAnalyzerShould()
	{
		super(new LongLinesAnalyzer());
	}

	@Test
	void reportDiagnosticIfLongLine()
	{
		configureEditorConfig("""
			[*]
			natls.style.mark_mainframelongline=true
			""");

		var source = """
            DEFINE DATA LOCAL
            END-DEFINE
			* This is a very long line. The fact that it is a comment does not matter, it should still be split in two lines because of the good, old Mainframe.
            END
            """;
		testDiagnostics(source, expectDiagnostic(2, LongLinesAnalyzer.MAINFRAME_LONG_LINE));
	}

	@Test
	void reportNoDiagnosticIfNoLongLine()
	{
		configureEditorConfig("""
			[*]
			natls.style.mark_mainframelongline=true
			""");

		var source = """
			DEFINE DATA LOCAL
			END-DEFINE

			PERFORM MY-SUB

			DEFINE SUBROUTINE MY-SUB
			PRINT 'HELLO'
			END-SUBROUTINE
            END
                """;
		testDiagnostics(source, expectNoDiagnosticOfType(LongLinesAnalyzer.MAINFRAME_LONG_LINE));
	}
}
