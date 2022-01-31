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
	void reportADiagnosticForAnUnusedUsing(@ProjectName("unusedimports")NaturalProject project)
	{
		assertDiagnostics(
			project.findModule("SUBTWO"),
			expectDiagnostic(1, UnusedImportAnalyzer.UNUSED_IMPORT)
		);
	}

	@Test
	void reportNoDiagnosticIfAVariableFromUsingIsUsed(@ProjectName("unusedimports")NaturalProject project)
	{
		assertNoDiagnostics(project.findModule("SUBONE"), UnusedImportAnalyzer.UNUSED_IMPORT);
	}
}
