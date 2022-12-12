package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

public class UnusedLocalSubroutineAnalyzerShould extends AbstractAnalyzerTest
{
	protected UnusedLocalSubroutineAnalyzerShould()
	{
		super(new UnusedLocalSubroutineAnalyzer());
	}

	@Test
	void reportNoDiagnosticIfASubroutineIsUsedBeforeDeclaration()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE

			PERFORM MY-SUB

			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE

			END
			""", expectNoDiagnosticOfType(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE));
	}

	@Test
	void reportNoDiagnosticIfASubroutineIsUsedAfterDeclaration()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE

			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE

			PERFORM MY-SUB

			END
			""", expectNoDiagnosticOfType(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE));
	}

	@Test
	void reportNoDiagnosticIfASubroutineIsUsedWithinAnotherSubroutine()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE

			DEFINE SUBROUTINE MY-SUB
			PERFORM MY-SECOND-SUB
			END-SUBROUTINE

			DEFINE SUBROUTINE MY-SECOND-SUB
			IGNORE
			END-SUBROUTINE

			PERFORM MY-SUB

			END
			""", expectNoDiagnosticOfType(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE));
	}

	@Test
	void reportADiagnosticIfASubroutineIsUnused()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE

			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE

			END
			""", expectDiagnostic(3, UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE, "Subroutine MY-SUB is unused"));
	}

	@Test
	void notReportADiagnosticForAnUnusedSubroutineDeclaredInACopycode(@ProjectName("unusedsubroutines")NaturalProject project)
	{
		testDiagnostics(
			project.findModule("UNUSEDSUBINCC"),
			expectNoDiagnosticOfType(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE)
		);
	}

	@Test
	void notReportADiagnosticForIfASubroutineIsUsedFromWithinACopyCode(@ProjectName("unusedsubroutines")NaturalProject project)
	{
		testDiagnostics(
			project.findModule("DECLARINGSUB"),
			expectNoDiagnosticOfType(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE)
		);
	}

	@Test
	void notReportAnExternalSubroutineAsLocallyUnused(@ProjectName("unusedsubroutines")NaturalProject project)
	{
		testDiagnostics(
			project.findModule("EXTERNAL-SUBROUTINE"),
			expectNoDiagnosticOfType(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE)
		);
	}

	@Test
	void reportADiagnosticForUnusedLocalSubroutinesWithinExternalSubroutines(@ProjectName("unusedsubroutines")NaturalProject project)
	{
		testDiagnostics(
			project.findModule("EXTERNAL-WITH-LOCAL"),
			expectDiagnostic(9, UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE)
		);
	}
}
