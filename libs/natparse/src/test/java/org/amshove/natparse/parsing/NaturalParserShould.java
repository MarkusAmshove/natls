package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

public class NaturalParserShould extends ParserIntegrationTest
{
	@Test
	void notReportDiagnosticsForUnresolvedCopyCodeVariables(@ProjectName("copycodetests") NaturalProject project)
	{
		assertParsesWithoutAnyDiagnostics(project.findModule("LIBONE", "SUBPROG3"));
	}
}
