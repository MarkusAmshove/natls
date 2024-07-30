package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

public class BetweenSubroutinesAnalyzerShould extends AbstractAnalyzerTest
{
	protected BetweenSubroutinesAnalyzerShould()
	{
		super(new BetweenSubroutinesAnalyzer());
	}

	@Test
	void reportNoDiagnosticIfNoSubroutine()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE

			NEWPAGE

			END
			""", expectNoDiagnosticOfType(BetweenSubroutinesAnalyzer.DISCOURAGED_CODE_BETWEEN_SUBROUTINES));
	}

	@Test
	void reportNoDiagnosticIfOnlyOneSubroutine()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE

			PERFORM MY-SUB

			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE
			NEWPAGE

			END
			""", expectNoDiagnosticOfType(BetweenSubroutinesAnalyzer.DISCOURAGED_CODE_BETWEEN_SUBROUTINES));
	}

	@Test
	void reportNoDiagnosticForCodeBeforeAfterSubroutines()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE

			NEWPAGE
			PERFORM MY-SUB

			DEFINE SUBROUTINE MY-SUB
			PERFORM MY-SECOND-SUB
			END-SUBROUTINE

			/* Description is fine
			DEFINE SUBROUTINE MY-SECOND-SUB
			IGNORE
			END-SUBROUTINE

			NEWPAGE

			END
			""", expectNoDiagnosticOfType(BetweenSubroutinesAnalyzer.DISCOURAGED_CODE_BETWEEN_SUBROUTINES));
	}

	@Test
	void reportDiagnosticIfCodeBetweenSubroutines()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE

			PERFORM MY-SUB

			DEFINE SUBROUTINE MY-SUB
			PERFORM MY-SECOND-SUB
			END-SUBROUTINE

			NEWPAGE

			DEFINE SUBROUTINE MY-SECOND-SUB
			IGNORE
			END-SUBROUTINE

			END
			""", expectDiagnostic(9, BetweenSubroutinesAnalyzer.DISCOURAGED_CODE_BETWEEN_SUBROUTINES));
	}

	@Test
	void reportDiagnosticIfPerformBetweenSubroutines()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE

			PERFORM MY-SUB

			DEFINE SUBROUTINE MY-SUB
			PERFORM MY-SECOND-SUB
			END-SUBROUTINE

			PERFORM MY-SECOND-SUB

			DEFINE SUBROUTINE MY-SECOND-SUB
			IGNORE
			END-SUBROUTINE

			END
			""", expectDiagnostic(9, BetweenSubroutinesAnalyzer.DISCOURAGED_CODE_BETWEEN_SUBROUTINES));
	}
}
