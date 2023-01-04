package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ForLoopAnalyzerShould extends AbstractAnalyzerTest
{
	protected ForLoopAnalyzerShould()
	{
		super(new ForLoopAnalyzer());
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"OCC", "OCCURRENCE"
	})
	void recognizeTheDiagnostic(String systemVar)
	{
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				1 #ARR (A10/*)
				1 #I-ARR (I4)
				END-DEFINE
								
				FOR #I-ARR = 1 TO *%s(#ARR)
				IGNORE
				END-FOR
								
				END
				""".formatted(systemVar),
			expectDiagnostic(5, ForLoopAnalyzer.UPPER_BOUND_OCC)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"#S-ARR", "10", "5"
	})
	void notReportADiagnosticIfUpperBoundIsNotOcc(String systemVar)
	{
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				1 #ARR (A10/*)
				1 #I-ARR (I4)
				1 #S-ARR (I4)
				END-DEFINE
								
				FOR #I-ARR = 1 TO %s
				IGNORE
				END-FOR
								
				END
				""".formatted(systemVar),
			expectNoDiagnosticOfType(ForLoopAnalyzer.UPPER_BOUND_OCC)
		);
	}
}
