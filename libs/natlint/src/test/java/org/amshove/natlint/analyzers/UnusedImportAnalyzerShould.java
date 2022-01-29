package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.LinterTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

public class UnusedImportAnalyzerShould extends LinterTest
{
	protected UnusedImportAnalyzerShould()
	{
		super(new UnusedImportAnalyzer());
	}

	@Test
	void reportADiagnosticForAnUnusedUsing(@ProjectName("unusedimports")NaturalProject project)
	{
		assertDiagnostic(1, UnusedImportAnalyzer.UNUSED_IMPORT, project.findModule("SUBTWO"));
	}

	@Test
	void reportNoDiagnosticIfAVariableFromUsingIsUsed(@ProjectName("unusedimports")NaturalProject project)
	{
		assertNoDiagnostic(UnusedImportAnalyzer.UNUSED_IMPORT, project.findModule("SUBONE"));
	}
}
