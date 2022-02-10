package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

public class CopyCodesShould extends ParserIntegrationTest
{
	@Test
	void notReportDiagnosticsForUnresolvedReferences(@ProjectName("copycodetests") NaturalProject project)
	{
		assertParsesWithoutAnyDiagnostics(project.findModule("LIBONE", "NODIAG"));
	}
}
