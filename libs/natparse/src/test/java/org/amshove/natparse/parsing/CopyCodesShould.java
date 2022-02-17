package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ISubprogram;
import org.amshove.natparse.natural.ISubroutineNode;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CopyCodesShould extends ParserIntegrationTest
{
	@Test
	void notReportDiagnosticsForUnresolvedReferences(@ProjectName("copycodetests") NaturalProject project)
	{
		assertParsesWithoutAnyDiagnostics(project.findModule("LIBONE", "NODIAG"));
	}

	@Test
	void exportDeclaredSubroutinesUpToTheIncludingModule(@ProjectName("copycodetests") NaturalProject project)
	{
		var module = assertParsesWithoutAnyDiagnostics(project.findModule("LIBONE", "USEDECL"));
		assertThat(module.referencableNodes()).anyMatch(n -> n instanceof ISubroutineNode subroutine && subroutine.declaration().symbolName().equals("INSIDE-CCODE"));
	}

	@Test
	void relocateDiagnosticsFromCopyCodesToTheirIncludeStatement(@ProjectName("copycodetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "SUBPROG"), ISubprogram.class);
		assertThat(subprogram.diagnostics()).hasSize(2);
		for (var diagnostic : subprogram.diagnostics())
		{
			assertThat(diagnostic.line()).as("Line mismatch for: " + diagnostic.message()).isEqualTo(2);
			assertThat(diagnostic.offsetInLine()).isEqualTo(8);
		}
	}

	@Test
	void relocateDiagnosticLocationsForCopyCodeNodesThatAreNestedMultipleTimes(@ProjectName("copycodetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "SUBPROG2"), ISubprogram.class);
		assertThat(subprogram.diagnostics()).hasSize(2);
		for (var diagnostic : subprogram.diagnostics())
		{
			assertThat(diagnostic.line()).as("Line mismatch for: " + diagnostic.message()).isEqualTo(2);
			assertThat(diagnostic.offsetInLine()).isEqualTo(8);
		}
	}
}
