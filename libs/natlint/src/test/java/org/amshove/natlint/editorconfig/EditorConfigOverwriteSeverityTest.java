package org.amshove.natlint.editorconfig;

import org.amshove.natlint.analyzers.UnusedLocalSubroutineAnalyzer;
import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

class EditorConfigOverwriteSeverityTest extends AbstractAnalyzerTest
{
	protected EditorConfigOverwriteSeverityTest()
	{
		super(new UnusedLocalSubroutineAnalyzer());
	}

	@Test
	void reportNoDiagnosticWhenSeverityIsSetToNone(@ProjectName("editorconfig") NaturalProject project)
	{
		configureEditorConfig("""
			[*]
			natls.NL005.severity=none
			""");
		testDiagnostics(
			project.findModule("SUBPROG"),
			expectNoDiagnosticOfType(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE)
		);
	}
}
