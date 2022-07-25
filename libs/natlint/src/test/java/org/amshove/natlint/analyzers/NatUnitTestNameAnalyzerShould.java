package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

public class NatUnitTestNameAnalyzerShould extends AbstractAnalyzerTest
{
	protected NatUnitTestNameAnalyzerShould()
	{
		super(new NatUnitTestNameAnalyzer());
	}

	@Test
	void raiseNoDiagnosticWhenNoDuplicatedTestsAreDefined(@ProjectName("natunit") NaturalProject project)
	{
		testDiagnostics(project.findModule("TCNODUP"), expectNoDiagnosticOfType(NatUnitTestNameAnalyzer.DUPLICATED_TEST_NAME));
	}

	@Test
	void raiseADiagnosticIfATestNameIsDuplicated(@ProjectName("natunit") NaturalProject project)
	{
		testDiagnostics(project.findModule("TCDUP"), expectDiagnostic(7, NatUnitTestNameAnalyzer.DUPLICATED_TEST_NAME));
	}
}
