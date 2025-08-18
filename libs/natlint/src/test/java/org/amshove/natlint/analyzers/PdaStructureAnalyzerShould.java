package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PdaStructureAnalyzerShould extends AbstractAnalyzerTest
{
	protected PdaStructureAnalyzerShould()
	{
		super(new PdaStructureAnalyzer());
	}

	@Test
	void raiseADiagnosticIfMoreThan1Level1()
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
			1 PDANAME2
			2 PDANAME-OUT
			3 #IN-NAME2 (A20)
			END-DEFINE
			""",
			expectDiagnostic(0, PdaStructureAnalyzer.PDA_STRUCTURE_DIAGNOSTIC, "PDAs should only have one Level 1 group")
		);
	}

	@Test
	void raiseADiagnosticIfLevel1NameMismatch()
	{
		configureEditorConfig("""
			[*]
			natls.style.in_out_groups=true
			""");

		testDiagnostics(
			"PDANAME.NSA", """
			DEFINE DATA PARAMETER
			1 #PDA
			2 PDANAME-IN
			3 #VAR1 (A20)
			END-DEFINE
			""",
			expectDiagnostic(1, PdaStructureAnalyzer.PDA_STRUCTURE_DIAGNOSTIC, "Level 1 group name should match the PDA name")
		);
	}

	@Disabled
	void raiseADiagnosticIfLevel2NameMismatch()
	{
		configureEditorConfig("""
			[*]
			natls.style.in_out_groups=true
			""");

		testDiagnostics(
			"PDANAME.NSA", """
			DEFINE DATA PARAMETER
			1 PDANAME
			2 PDA-IN
			3 #VAR1 (A20)
			END-DEFINE
			""",
			expectDiagnostic(2, PdaStructureAnalyzer.PDA_STRUCTURE_DIAGNOSTIC, "Level 2 group name should have one of these suffixes: -OUT, -IN, -INOUT")
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"(1:10)", "(20)", "(A10)", "(L)", "(P3/1:10, 2:20)"
	})
	void raiseADiagnosticIfNotAGroupButNotAnArrayGroup(String suffix)
	{
		configureEditorConfig("""
			[*]
			natls.style.in_out_groups=true
			""");

		testDiagnostics(
			"PDANAME.NSA", """
   			DEFINE DATA PARAMETER
   			1 PDANAME
     		2 PDANAME-IN %s
        	3 #VAR (A1)
   			END-DEFINE
			""".formatted(suffix), expectDiagnostic(2, PdaStructureAnalyzer.PDA_STRUCTURE_DIAGNOSTIC, "Level 2 must be a group, and not an array group")
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"_IN", "_OUT", "_INOUT", "HELLO", "1", "#"
	})
	void raiseADiagnosticIfAGroupHasAnDisallowedSuffix(String suffix)
	{
		configureEditorConfig("""
			[*]
			natls.style.in_out_groups=true
			""");

		testDiagnostics(
			"PDANAME.NSA", """
   			DEFINE DATA PARAMETER
   			1 PDANAME
     		2 PDANAME%s
        	3 #VAR (A1)
   			END-DEFINE
   			""".formatted(suffix), expectDiagnostic(2, PdaStructureAnalyzer.PDA_STRUCTURE_DIAGNOSTIC, "Level 2 groups should have one of these suffixes: -OUT, -IN, -INOUT")
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"-IN", "-OUT", "-INOUT"
	})
	void raiseNoDiagnosticIfAGroupHasAnAllowedSuffix(String suffix)
	{
		configureEditorConfig("""
			[*]
			natls.style.in_out_groups=true
			""");

		testDiagnostics(
			"PDANAME.NSA", """
   			DEFINE DATA PARAMETER
   			1 PDANAME
     		2 PDANAME%s
        	3 #VAR (A1)
   			END-DEFINE
   			""".formatted(suffix), expectNoDiagnosticOfType(PdaStructureAnalyzer.PDA_STRUCTURE_DIAGNOSTIC)
		);
	}
}
