package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

public class SubroutineNameMismatchAnalyzerShould extends AbstractAnalyzerTest
{
	protected SubroutineNameMismatchAnalyzerShould()
	{
		super(new SubroutineNameMismatchAnalyzer());
	}

	@Test
	void reportADiagnosticWhenNamesMismatchAfter32Characters()
	{
		testDiagnostics("""
			define data local
			end-define
			define subroutine this-is-a-very-long-name-no-one-should-ever-do-this
			ignore
			end-subroutine
			perform this-is-a-very-long-name-no-one-does-not-matter-whats-here
			end
			""",
			expectDiagnostic(5, SubroutineNameMismatchAnalyzer.SUBROUTINE_NAME_MISMATCH));
	}

	@Test
	void notReportADiagnosticIfNamesMatchEvenAfter32Characters()
	{
		testDiagnostics("""
			define data local
			end-define
			define subroutine this-is-a-very-long-name-no-one-should-ever-do-this
			ignore
			end-subroutine
			perform this-is-a-very-long-name-no-one-should-ever-do-this
			end
			""",
			expectNoDiagnosticOfType(SubroutineNameMismatchAnalyzer.SUBROUTINE_NAME_MISMATCH));
	}

	@Test
	void notReportADiagnosticIfNamesMatchOnShorterSubroutines()
	{
		testDiagnostics("""
			define data local
			end-define
			define subroutine this-is-okay
			ignore
			end-subroutine
			perform this-is-okay
			end
			""",
			expectNoDiagnosticOfType(SubroutineNameMismatchAnalyzer.SUBROUTINE_NAME_MISMATCH));
	}

	@Test
	void reportADiagnosticOnTheCallingSideOfThePerformForLongExternalSubroutines(@ProjectName("long_subroutine_names") NaturalProject project)
	{
		testDiagnostics(
			project.findModule("SUB").orElseThrow(),
			expectDiagnostic(2, SubroutineNameMismatchAnalyzer.SUBROUTINE_NAME_MISMATCH)
		);
	}

	@Test
	void reportADiagnosticOnTheCallingSideOfThePerformForLongExternalSubroutinesNamesWhereThePerformIsExactly32(@ProjectName("long_subroutine_names") NaturalProject project)
	{
		testDiagnostics(
			project.findModule("SUB2").orElseThrow(),
			expectDiagnostic(2, SubroutineNameMismatchAnalyzer.SUBROUTINE_NAME_MISMATCH)
		);
	}
}
