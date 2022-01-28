package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.LinterTest;
import org.junit.jupiter.api.Test;

public class UnusedVariableAnalyzerShould extends LinterTest
{
	protected UnusedVariableAnalyzerShould()
	{
		super(new UnusedVariableAnalyzer());
	}

	@Test
	void reportNoDiagnosticIfAVariableIsUsed()
	{
		assertNoDiagnostic(UnusedVariableAnalyzer.UNUSED_VARIABLE, """
			define data
			local
			1 #myvar (a10)
			end-define
			write #myvar
			end
			""");
	}

	@Test
	void reportADiagnosticIfAVariableIsUnused()
	{
		assertDiagnostic(2, UnusedVariableAnalyzer.UNUSED_VARIABLE, """
			define data
			local
			1 #myvar (a10)
			end-define
			end
			""");
	}

	@Test
	void notReportADiagnosticForTheGroupIfAVariableWithinIsUsed()
	{
		assertNoDiagnostic(UnusedVariableAnalyzer.UNUSED_VARIABLE, """
			define data
			local
			1 #group
			  2 #used (n1)
			end-define
			write #used
			end
			""");
	}

	@Test
	void notReportADiagnosticForNestedGroupsIfAVariableWithinIsUsed()
	{
		assertNoDiagnostic(UnusedVariableAnalyzer.UNUSED_VARIABLE, """
			define data
			local
			1 #group
			  2 #group2
				3 #used (n2)
			end-define
			write #used
			end
			""");
	}
}
