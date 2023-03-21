package org.amshove.natlint.editorconfig;

import org.amshove.natlint.analyzers.BooleanOperatorAnalyzer;
import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

class EditorConfigPositionSettingTests extends AbstractAnalyzerTest
{
	protected EditorConfigPositionSettingTests()
	{
		super(new BooleanOperatorAnalyzer());
	}

	@Test
	void reportNoDiagnosticWhenSeverityIsOverwrittenForCopyCodes(@ProjectName("editorconfig") NaturalProject project)
	{
		// Test if the severity setting for copycodes gets propagated to the INCLUDE.
		configureEditorConfig("""
			[*]
			natls.NL006.severity=error

			[**/*.NSC]
			natls.NL006.severity=none
			""");

		// This subprogram should raise a diagnostic, because it uses LT instead of < and
		// the editorconfig sets the severity to error.
		testDiagnostics(
			project.findModule("SUBSHORT"),
			expectDiagnostic(3, BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR)
		);

		// This subprogram should not raise a diagnostic, because it uses a copycode which has the offending operator
		// but the editorconfig sets the severity to none for copycodes.
		// This should work, because the diagnostic severity should be overwritten based on the original position of the
		// diagnostic (the copycode and note the INCLUDE).
		testDiagnostics(
			project.findModule("USESHORT"),
			expectNoDiagnosticOfType(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR)
		);
	}
}
