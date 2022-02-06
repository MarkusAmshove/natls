package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

public class UnusedVariableAnalyzerShould extends AbstractAnalyzerTest
{
	protected UnusedVariableAnalyzerShould()
	{
		super(new UnusedVariableAnalyzer());
	}

	@Test
	void reportNoDiagnosticIfAVariableIsUsed()
	{
		testDiagnostics("""
				define data
				local
				1 #myvar (a10)
				end-define
				write #myvar
				end
				""",
			expectNoDiagnostic(2, UnusedVariableAnalyzer.UNUSED_VARIABLE));
	}

	@Test
	void reportADiagnosticIfAVariableIsUnused()
	{
		testDiagnostics("""
				define data
				local
				1 #myvar (a10)
				end-define
				end
				""",
			expectDiagnostic(2, UnusedVariableAnalyzer.UNUSED_VARIABLE, "Variable #MYVAR is unused")
		);
	}

	@Test
	void notReportADiagnosticForTheGroupIfAVariableWithinIsUsed()
	{
		testDiagnostics("""
				define data
				local
				1 #group
				  2 #used (n1)
				end-define
				write #used
				end
				""",
			expectNoDiagnosticOfType(UnusedVariableAnalyzer.UNUSED_VARIABLE)
		);
	}

	@Test
	void notReportADiagnosticForNestedGroupsIfAVariableWithinIsUsed()
	{
		testDiagnostics("""
			define data
			local
			1 #group
			  2 #group2
				3 #used (n2)
			end-define
			write #used
			end
			""",
			expectNoDiagnostic(2, UnusedVariableAnalyzer.UNUSED_VARIABLE),
			expectNoDiagnostic(3, UnusedVariableAnalyzer.UNUSED_VARIABLE),
			expectNoDiagnostic(4, UnusedVariableAnalyzer.UNUSED_VARIABLE)
		);
	}

	@Test
	void notReportADiagnosticIfTheGroupNameIsReferenced()
	{
		testDiagnostics("""
               define data
               local
               1 #group
                 2 #inside (a10) /* this is not referenced directly, but it should not have a diagnostic because its group is used
               end-define
               write #group
               end
            """,
			expectNoDiagnostic(3, UnusedVariableAnalyzer.UNUSED_VARIABLE)
		);
	}
}
