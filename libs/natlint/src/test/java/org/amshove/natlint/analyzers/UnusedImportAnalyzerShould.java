package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

public class UnusedImportAnalyzerShould extends AbstractAnalyzerTest
{
	protected UnusedImportAnalyzerShould()
	{
		super(new UnusedImportAnalyzer());
	}

	@Test
	void reportADiagnosticForAnUnusedUsing(@ProjectName("unusedimports") NaturalProject project)
	{
		testDiagnostics(
			project.findModule("SUBTWO"),
			expectDiagnostic(1, UnusedImportAnalyzer.UNUSED_IMPORT, "Using MYLDA is unused")
		);
	}

	@Test
	void reportNoDiagnosticIfAVariableFromUsingIsUsed(@ProjectName("unusedimports") NaturalProject project)
	{
		testDiagnostics(project.findModule("SUBONE"), expectNoDiagnosticOfType(UnusedImportAnalyzer.UNUSED_IMPORT));
	}

	@Test
	void reportNoDiagnosticIfAVariableInAPdaIsUsedByFullQualifyingIt(@ProjectName("unusedimports") NaturalProject project)
	{
		testDiagnostics(project.findModule("MY-SUBROUTINE"), expectNoDiagnosticOfType(UnusedImportAnalyzer.UNUSED_IMPORT));
	}

	@Test
	void reportNoDiagnosticIfAVariableFromUsingIsUsedWithinCopyCode(@ProjectName("unusedimports") NaturalProject project)
	{
		testDiagnostics(project.findModule("INCC"), expectNoDiagnosticOfType(UnusedImportAnalyzer.UNUSED_IMPORT));
	}
}
