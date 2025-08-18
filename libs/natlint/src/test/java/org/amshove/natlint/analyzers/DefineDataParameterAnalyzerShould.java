package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

class DefineDataParameterAnalyzerShould extends AbstractAnalyzerTest
{
	protected DefineDataParameterAnalyzerShould()
	{
		super(new DefineDataParameterAnalyzer());
	}

	@Disabled("This test is not relevant anymore, as we do not support IN/OUT groups in PDAs")
	void reportLevel1NameMismatch()
	{
		configureEditorConfig("""
			[*]
			natls.style.in_out_groups=true
			""");

		testDiagnostics(
			"PDANAME.NSA", """
			DEFINE DATA PARAMETER
			1 #PDA
			2 #IN-NAME (A10)
			END-DEFINE
			END
			""",
			expectDiagnostic(0, DefineDataParameterAnalyzer.PDA_STRUCTURE_DIAGNOSTIC, "Missing group: PDANAME-IN")
		);
	}

	@Disabled("This test is not relevant anymore, as we do not support IN/OUT groups in PDAs")
	void reportMoreThan1Level1()
	{
		configureEditorConfig("""
			[*]
			natls.style.in_out_groups=true
			""");

		testDiagnostics(
			"PDANAME.NSA", """
			DEFINE DATA PARAMETER
			1 PDANAME
			2 PDANAME-IN
			3 #IN-NAME1 (A20)
			1 PDANAME-IN2
			2 #IN-NAME2 (A20)
			END-DEFINE
			""",
			expectDiagnostic(0, DefineDataParameterAnalyzer.PDA_STRUCTURE_DIAGNOSTIC, "There must be exactly one level 1 element. Found: 2")
		);
	}

	@Test
	void reportLevel2MustBeGroup()
	{
		configureEditorConfig("""
			[*]
			natls.style.in_out_groups=true
			""");

		testDiagnostics(
			"PDANAME.NSA", """
			DEFINE DATA PARAMETER
			1 PDANAME
			2 PDANAME-IN (20)
			3 #IN-NAME (A20)
			END-DEFINE
			""",
			expectDiagnostic(0, DefineDataParameterAnalyzer.PDA_STRUCTURE_DIAGNOSTIC, "Level 2 must be a group, and not an array group: PDANAME-IN")
		);
	}

	@Test
	void reportInlineParameters()
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_inlineparameters=true
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
			natls.style.discourage_inlineparameters=true
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
			natls.style.discourage_inlineparameters=true
			""");

		// This subprogram should not raise a diagnostic, because it only uses a PDA
		testDiagnostics(
			project.findModule("SUBPDA"),
			expectNoDiagnosticOfType(DefineDataParameterAnalyzer.USE_OF_INLINE_PARAMETER_IS_DISCOURAGED)
		);
	}
}
