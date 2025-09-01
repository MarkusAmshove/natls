package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.parsing.ParserError;
import org.junit.jupiter.api.Test;

class VariableReferenceAnalyzerShould extends AbstractAnalyzerTest
{
	protected VariableReferenceAnalyzerShould()
	{
		super(new VariableReferenceAnalyzer());
	}

	@Test
	void reportNoDiagnosticIfAVariableIsUsed()
	{
		testDiagnostics(
			"""
				define data
				local
				1 #myvar (a10)
				end-define
				write #myvar
				end
				""",
			expectNoDiagnostic(2, VariableReferenceAnalyzer.UNUSED_VARIABLE)
		);
	}

	@Test
	void reportADiagnosticIfAVariableIsUnused()
	{
		testDiagnostics(
			"""
				define data
				local
				1 #myvar (a10)
				end-define
				end
				""",
			expectDiagnostic(2, VariableReferenceAnalyzer.UNUSED_VARIABLE, "Variable #MYVAR is unused")
		);
	}

	@Test
	void notReportADiagnosticForTheGroupIfAVariableWithinIsUsed()
	{
		testDiagnostics(
			"""
				define data
				local
				1 #group
				  2 #used (n1)
				end-define
				write #used
				end
				""",
			expectNoDiagnosticOfType(VariableReferenceAnalyzer.UNUSED_VARIABLE)
		);
	}

	@Test
	void notReportADiagnosticForNestedGroupsIfAVariableWithinIsUsed()
	{
		testDiagnostics(
			"""
			define data
			local
			1 #group
			  2 #group2
				3 #used (n2)
			end-define
			write #used
			end
			""",
			expectNoDiagnostic(2, VariableReferenceAnalyzer.UNUSED_VARIABLE),
			expectNoDiagnostic(3, VariableReferenceAnalyzer.UNUSED_VARIABLE),
			expectNoDiagnostic(4, VariableReferenceAnalyzer.UNUSED_VARIABLE)
		);
	}

	@Test
	void notReportADiagnosticIfTheGroupNameIsReferenced()
	{
		testDiagnostics(
			"""
               define data
               local
               1 #group
                 2 #inside (a10) /* this is not referenced directly, but it should not have a diagnostic because its group is used
               end-define
               write #group
               end
            """,
			expectNoDiagnostic(3, VariableReferenceAnalyzer.UNUSED_VARIABLE)
		);
	}

	@Test
	void notReportADiagnosticForTheRedefineItselfIfAMemberIsUsed()
	{
		testDiagnostics(
			"""
               define data
               local
               1 #var (a20)
               1 redefine #var
               2 #var2 (a20)
               end-define

               write #var2

               end
            """,
			expectNoDiagnosticOfType(VariableReferenceAnalyzer.UNUSED_VARIABLE)
		);
	}

	@Test
	void notReportADiagnosticForARedefineMemberIfAMemberAfterwardsIsUsed()
	{
		testDiagnostics(
			"""
               define data
               local
               1 #var (a20)
               1 redefine #var
               2 #var1 (a10)
               2 #var2 (a10)
               end-define
               write #var2
               end
            """,
			expectNoDiagnosticOfType(VariableReferenceAnalyzer.UNUSED_REDEFINE_VARIABLE)
		);
	}

	@Test
	void reportADiagnosticForATrailingRedefineMemberThatIsUnused()
	{
		testDiagnostics(
			"""
               define data
               local
               1 #var (a20)
               1 redefine #var
               2 #var1 (a10)
               2 #var2 (a5)
               2 #var3 (a5)
               end-define
               write #var2
               end
            """,
			expectDiagnostic(6, VariableReferenceAnalyzer.UNUSED_REDEFINE_VARIABLE)
		);
	}

	@Test
	void reportADiagnosticForVariablesThatAreOnlyModifiedByReset()
	{
		testDiagnostics(
			"""
			define data local
			1 #var (a10)
			end-define
			reset #var
			end
			""",
			expectDiagnostic(1, VariableReferenceAnalyzer.VARIABLE_MODIFIED_ONLY)
		);
	}

	@Test
	void reportADiagnosticForVariablesThatAreOnlyModifiedByAssignment()
	{
		testDiagnostics(
			"""
			define data local
			1 #var (a10)
			end-define
			#var := 'A'
			end
			""",
			expectDiagnostic(1, VariableReferenceAnalyzer.VARIABLE_MODIFIED_ONLY)
		);
	}

	@Test
	void notReportADiagnosticIfAVariableIsModifiedAndRead()
	{
		testDiagnostics(
			"""
			define data local
			1 #var (a10)
			end-define
			#var := 'A'
			WRITE #var
			end
			""",
			expectNoDiagnosticOfType(VariableReferenceAnalyzer.VARIABLE_MODIFIED_ONLY)
		);
	}

	@Test
	void notReportDiagnosticsTwiceWhenViewsAreInvolved()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE.id());
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			1 #VIEW VIEW OF DDM
			  2 #FIELD (A10)
			END-DEFINE
			END
			""",
			expectSingleDiagnosticOfTypeInLine(1, VariableReferenceAnalyzer.UNUSED_VARIABLE), // #VIEW
			expectSingleDiagnosticOfTypeInLine(2, VariableReferenceAnalyzer.UNUSED_VARIABLE) // #FIELD
		);
	}
}
