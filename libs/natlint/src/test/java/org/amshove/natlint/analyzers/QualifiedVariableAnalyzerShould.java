package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

class QualifiedVariableAnalyzerShould extends AbstractAnalyzerTest
{
	protected QualifiedVariableAnalyzerShould()
	{
		super(new QualifiedVariableAnalyzer());
	}

	@Test
	void reportIndividualDiagnosticsForEach()
	{
		configureEditorConfig("""
			[*]
			natls.style.level1vars=true
			natls.style.qualifyvars=true
			""");

		testDiagnostics(
			"""
			DEFINE DATA LOCAL
            1 WORK
            2 #VAR (N1/1:10)
            2 #I (I1)
			1 TYPEDLEVEL1 (I1) /* Discouraged
			END-DEFINE
			MOVE 1 TO #VAR(#I) /* Should be qualified with WORK
			END
			""",
			expectDiagnostic(4, QualifiedVariableAnalyzer.LEVEL_1_TYPED_VARIABLES_IS_DISCOURAGED),
			expectDiagnostic(6, QualifiedVariableAnalyzer.VARIABLE_SHOULD_BE_QUALIFIED)
		);
	}

	@Test
	void notReportDiagnostics()
	{
		configureEditorConfig("""
			[*]
			natls.style.level1vars=true
			natls.style.qualifyvars=true
			""");

		testDiagnostics(
			"""
			DEFINE DATA LOCAL
            1 WORK
            2 #VAR (N1/1:10)
            2 #I (I1)
			1 #GRP
			2 NOTATYPEDLEVEL1 (I1)
			END-DEFINE
			MOVE 1 TO WORK.#VAR(#I)
			MOVE 2 TO #GRP.NOTATYPEDLEVEL1
			END
			""",
			expectNoDiagnostic(5, QualifiedVariableAnalyzer.LEVEL_1_TYPED_VARIABLES_IS_DISCOURAGED),
			expectNoDiagnostic(7, QualifiedVariableAnalyzer.VARIABLE_SHOULD_BE_QUALIFIED),
			expectNoDiagnostic(8, QualifiedVariableAnalyzer.VARIABLE_SHOULD_BE_QUALIFIED)
		);
	}

	@Test
	void reportNoDiagnosticForUnqualifiedVarsInCopycode(@ProjectName("natunit") NaturalProject project)
	{
		configureEditorConfig("""
			[*]
			natls.style.qualifyvars=true
			""");

		testDiagnostics(
			project.findModule("INCL-C1"),
			expectNoDiagnosticOfType(QualifiedVariableAnalyzer.LEVEL_1_TYPED_VARIABLES_IS_DISCOURAGED),
			expectNoDiagnosticOfType(QualifiedVariableAnalyzer.VARIABLE_SHOULD_BE_QUALIFIED)
		);
	}

	@Test
	void reportNoDiagnosticForLevel1TypedVariablesInCopycode(@ProjectName("natunit") NaturalProject project)
	{
		configureEditorConfig("""
			[*]
			natls.style.level1vars=true
			""");

		testDiagnostics(
			project.findModule("USELDA"),
			expectNoDiagnosticOfType(QualifiedVariableAnalyzer.LEVEL_1_TYPED_VARIABLES_IS_DISCOURAGED)
		);
	}
}
