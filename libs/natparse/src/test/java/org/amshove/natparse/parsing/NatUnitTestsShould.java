package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class NatUnitTestsShould extends ParserIntegrationTest
{
	@Test
	void notReportAnUnresolvedSubroutineForSetup(@ProjectName("natunittests") NaturalProject project)
	{
		var module = project.findModule("NATUNIT", "TCSETUP");
		var subprogram = assertParsesWithoutAnyDiagnostics(module);
		assertThat(subprogram.isTestCase()).isTrue();
	}

	@Test
	void notReportAnUnresolvedSubroutineForTeardown(@ProjectName("natunittests") NaturalProject project)
	{
		var module = project.findModule("NATUNIT", "TCTEAR");
		var subprogram = assertParsesWithoutAnyDiagnostics(module);
		assertThat(subprogram.isTestCase()).isTrue();
	}
}
