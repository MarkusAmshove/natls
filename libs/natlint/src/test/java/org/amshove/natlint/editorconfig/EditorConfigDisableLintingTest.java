package org.amshove.natlint.editorconfig;

import org.amshove.natlint.analyzers.UnusedLocalSubroutineAnalyzer;
import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

class EditorConfigDisableLintingTest extends AbstractAnalyzerTest
{
	protected EditorConfigDisableLintingTest()
	{
		super(new UnusedLocalSubroutineAnalyzer());
	}

	@Test
	void reportNoDiagnosticIfLintingIsDisabled(@ProjectName("editorconfig") NaturalProject project)
	{
		configureEditorConfig("""
			[*]
			natls.linting.disable=true
			""");
		testDiagnostics(
			project.findModule("SUBPROG"),
			expectNoDiagnosticOfType(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE)
		);
	}

	@Test
	void reportDiagnosticsIfLintingIsEnabled(@ProjectName("editorconfig") NaturalProject project)
	{
		configureEditorConfig("""
			[*]
			natls.linting.disable=false
			""");
		testDiagnostics(
			project.findModule("SUBPROG"),
			expectDiagnostic(7, UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE)
		);
	}

	@Test
	void reportDiagnosticsIfNoConfigIsSpecified(@ProjectName("editorconfig") NaturalProject project)
	{
		testDiagnostics(
			project.findModule("SUBPROG"),
			expectDiagnostic(7, UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE)
		);
	}
}
