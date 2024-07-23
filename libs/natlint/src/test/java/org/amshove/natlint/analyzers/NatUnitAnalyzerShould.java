package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

class NatUnitAnalyzerShould extends AbstractAnalyzerTest
{
	protected NatUnitAnalyzerShould()
	{
		super(new NatUnitAnalyzer());
	}

	@Test
	void raiseNoDiagnosticWhenNoDuplicatedTestsAreDefined(@ProjectName("natunit") NaturalProject project)
	{
		testDiagnostics(project.findModule("TCNODUP"), expectNoDiagnosticOfType(NatUnitAnalyzer.DUPLICATED_TEST_NAME));
	}

	@Test
	void raiseADiagnosticIfATestNameIsDuplicated(@ProjectName("natunit") NaturalProject project)
	{
		testDiagnostics(project.findModule("TCDUP"), expectDiagnostic(11, NatUnitAnalyzer.DUPLICATED_TEST_NAME));
	}

	@Test
	void raiseDiagnosticsIfATestCaseIsNotWithinTheTestSubroutine(@ProjectName("natunit") NaturalProject project)
	{
		testDiagnostics(
			project.findModule("TCNOSUB"),
			expectDiagnostic(4, NatUnitAnalyzer.TEST_CASE_NOT_IN_TEST_ROUTINE), // Test case is not within TEST routine
			expectDiagnostic(9, NatUnitAnalyzer.TEST_CASE_NOT_IN_TEST_ROUTINE), // Test case is in a different routine
			expectNoDiagnostic(15, NatUnitAnalyzer.TEST_CASE_NOT_IN_TEST_ROUTINE) // Test case is correctly within TEST routine
		);
	}
}
