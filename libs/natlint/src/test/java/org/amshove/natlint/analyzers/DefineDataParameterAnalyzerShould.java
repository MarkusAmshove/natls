package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

class DefineDataParameterAnalyzerShould extends AbstractAnalyzerTest
{
	protected DefineDataParameterAnalyzerShould()
	{
		super(new DefineDataParameterAnalyzer());
	}

	@Test
	void reportInlineParameters()
	{
		configureEditorConfig("""
			[*]
			natls.style.discourageinlineparameters=true
			""");

		testDiagnostics(
			"""
			DEFINE DATA
			PARAMETER
            1 +VAR (N1/1:10)
			END-DEFINE
			END
			""",
			expectDiagnostic(1, DefineDataParameterAnalyzer.USE_OF_INLINE_PARAMETER_IS_DISCOURAGED)
		);
	}

	@Test
	void reportInlineParametersEvenIfParameterUsing(@ProjectName("editorconfig") NaturalProject project)
	{
		configureEditorConfig("""
			[*]
			natls.style.discourageinlineparameters=true
			""");

		// This subprogram should raise a diagnostic, because it uses inline parameters along side
		// with a PDA called SOMEPDA
		testDiagnostics(
			project.findModule("SUBINLIN"),
			expectDiagnostic(2, DefineDataParameterAnalyzer.USE_OF_INLINE_PARAMETER_IS_DISCOURAGED)
		);
	}

	@Test
	void reportNoDiagnosticForParameterUsing(@ProjectName("editorconfig") NaturalProject project)
	{
		configureEditorConfig("""
			[*]
			natls.style.discourageinlineparameters=true
			""");

		// This subprogram should not raise a diagnostic, because it only uses a PDA
		testDiagnostics(
			project.findModule("SUBPDA"),
			expectNoDiagnosticOfType(DefineDataParameterAnalyzer.USE_OF_INLINE_PARAMETER_IS_DISCOURAGED)
		);
	}
}
