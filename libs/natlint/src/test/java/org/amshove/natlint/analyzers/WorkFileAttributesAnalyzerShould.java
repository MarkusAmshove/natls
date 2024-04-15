package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WorkFileAttributesAnalyzerShould extends AbstractAnalyzerTest
{
	protected WorkFileAttributesAnalyzerShould()
	{
		super(new WorkFileAttributesAnalyzer());
	}

	@Test
	void notRaiseADiagnosticIfNoAttributesArePresent()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE WORK FILE 1
			END
			""",
			expectNoDiagnosticOfType(WorkFileAttributesAnalyzer.MULTIPLE_ATTRIBUTES_OF_SAME_TYPE)
		);
	}

	@Test
	void notRaiseADiagnosticIfAttributesAreAVariableReference()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE
			DEFINE WORK FILE 1 ATTRIBUTES #VAR
			END
			""",
			expectNoDiagnosticOfType(WorkFileAttributesAnalyzer.MULTIPLE_ATTRIBUTES_OF_SAME_TYPE)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"NOAPPEND, DELETE, BOM , KEEPCR",
		"DELETE BOM",
		"REMOVECR, NOBOM",
	})
	void notRaiseADiagnosticIfAttributesAreDifferentTypes(String attributes)
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE WORK FILE 1 ATTRIBUTES '%s'
			END
			""".formatted(attributes),
			expectNoDiagnosticOfType(WorkFileAttributesAnalyzer.MULTIPLE_ATTRIBUTES_OF_SAME_TYPE)
		);
	}

	@Test
	void raiseADiagnosticIfMultipleAttributesOfTheSameTypeArePresent()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE WORK FILE 1 ATTRIBUTES 'BOM,NOBOM'
			END
			""",
			expectDiagnostic(2, WorkFileAttributesAnalyzer.MULTIPLE_ATTRIBUTES_OF_SAME_TYPE)
		);
	}
}
