package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NaturalParserShould extends ParserIntegrationTest
{
	@Test
	void notReportDiagnosticsForUnresolvedCopyCodeVariables(@ProjectName("copycodetests") NaturalProject project)
	{
		assertParsesWithoutAnyDiagnostics(project.findModule("LIBONE", "SUBPROG3"));
	}

	@Test
	void notReportDiagnosticsForReferencesToTheFunctionName(@ProjectName("variablereferencetests") NaturalProject project)
	{
		assertParsesWithoutAnyDiagnostics(project.findModule("LIBONE", "FUNC"));
	}

	@Test
	void reportADiagnosticsForUnreferencedVariablesInFunctions(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var module = parse(project.findModule("LIBONE", "FUNC2"));
		assertThat(module.diagnostics()).anyMatch(d -> d.id().equals("NPP016") && d.message().equals("Unresolved reference: FUNC"));
	}
}
