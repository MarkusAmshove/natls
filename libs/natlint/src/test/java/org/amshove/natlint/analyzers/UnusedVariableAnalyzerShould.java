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
		assertNoDiagnostics("""
				define data
				local
				1 #myvar (a10)
				end-define
				write #myvar
				end
				""",
			UnusedVariableAnalyzer.UNUSED_VARIABLE);
	}

	@Test
	void reportADiagnosticIfAVariableIsUnused()
	{
		assertDiagnostics("""
				define data
				local
				1 #myvar (a10)
				end-define
				end
				""",
			expectDiagnostic(2, UnusedVariableAnalyzer.UNUSED_VARIABLE)
		);
	}

	@Test
	void notReportADiagnosticForTheGroupIfAVariableWithinIsUsed()
	{
		assertNoDiagnostics("""
				define data
				local
				1 #group
				  2 #used (n1)
				end-define
				write #used
				end
				""",
			UnusedVariableAnalyzer.UNUSED_VARIABLE
		);
	}

	@Test
	void notReportADiagnosticForNestedGroupsIfAVariableWithinIsUsed()
	{
		assertNoDiagnostics("""
			define data
			local
			1 #group
			  2 #group2
				3 #used (n2)
			end-define
			write #used
			end
			""",
			UnusedVariableAnalyzer.UNUSED_VARIABLE
		);
	}

	@Test
	void notReportADiagnosticIfTheGroupNameIsReferenced()
	{
		assertNoDiagnostics("""
               define data
               local
               1 #group
                 2 #inside (a10) /* this is not referenced directly, but it should not have a diagnostic because its group is used
               end-define
               write #group
               end
            """,
			UnusedVariableAnalyzer.UNUSED_VARIABLE
		);
	}
}
