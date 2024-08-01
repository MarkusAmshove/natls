package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class DefineDataIndependentAnalyzerShould extends AbstractAnalyzerTest
{
	protected DefineDataIndependentAnalyzerShould()
	{
		super(new DefineDataIndependentAnalyzer());
	}

	@Test
	void reportIndividualDiagnosticsForEach()
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_independent=true
			""");

		testDiagnostics(
			"""
			DEFINE DATA
			INDEPENDENT
            1 +VAR (N1/1:10)
			END-DEFINE
			END
			""",
			expectDiagnostic(1, DefineDataIndependentAnalyzer.USE_OF_INDEPENDENT_IS_DISCOURAGED)
		);
	}
}
