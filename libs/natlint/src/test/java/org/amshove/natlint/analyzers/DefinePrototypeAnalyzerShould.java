package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.parsing.ParserError;
import org.junit.jupiter.api.Test;

class DefinePrototypeAnalyzerShould extends AbstractAnalyzerTest
{

	@Test
	void beRaisedWhenMoreThanOnePrototypeForTheSameFunctionIsDefined()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			
			DEFINE PROTOTYPE FUNC RETURNS (L)
			END-PROTOTYPE
			
			DEFINE PROTOTYPE FUNC RETURNS (L)
			END-PROTOTYPE
			END
			""",
			expectDiagnostic(3, DefinePrototypeAnalyzer.PROTOTYPE_DEFINED_MORE_THAN_ONCE),
			expectDiagnostic(6, DefinePrototypeAnalyzer.PROTOTYPE_DEFINED_MORE_THAN_ONCE)
		);
	}

	@Test
	void notBeRaisedWhenUniquePrototypesAreDefined()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			
			DEFINE PROTOTYPE FUNC RETURNS (L)
			END-PROTOTYPE
			
			DEFINE PROTOTYPE FUNC2 RETURNS (L)
			END-PROTOTYPE
			END
			""",
			expectNoDiagnosticOfType(DefinePrototypeAnalyzer.PROTOTYPE_DEFINED_MORE_THAN_ONCE)
		);
	}

	@Test
	void notBeRaisedWhenNoPrototypeIsDefined()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			END
			""",
			expectNoDiagnosticOfType(DefinePrototypeAnalyzer.PROTOTYPE_DEFINED_MORE_THAN_ONCE)
		);
	}

	@Test
	void raiseAPrototypeUndefinedDiagnosticInFunctionsCallingFunctionsWithoutPrototype()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE.id());
		testDiagnostics(
			"FUNC.NS7",
			"""
			DEFINE FUNCTION FUNC
			DEFINE DATA LOCAL
			END-DEFINE
			FUNC2(<>)
			END-FUNCTION
			END
			""",
			expectDiagnostic(3, DefinePrototypeAnalyzer.PROTOTYPE_UNDEFINED)
		);
	}

	@Test
	void notRaiseAPrototypeUndefinedDiagnosticInFunctionsCallingFunctionsWithPrototype()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE.id());
		testDiagnostics(
			"FUNC.NS7",
			"""
			DEFINE FUNCTION FUNC
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE PROTOTYPE FUNC2 RETURNS (L)
			END-PROTOTYPE
			FUNC2(<>)
			END-FUNCTION
			END
			""",
			expectNoDiagnosticOfType(DefinePrototypeAnalyzer.PROTOTYPE_UNDEFINED)
		);
	}

	@Test
	void raiseAUnusedPrototypeDiagnosticInFunctionsWhenAPrototypeIsUnused()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE.id());
		testDiagnostics(
			"FUNC.NS7",
			"""
			DEFINE FUNCTION FUNC
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE PROTOTYPE FUNC RETURNS (L)
			END-PROTOTYPE
			END-FUNCTION
			END
			""",
			expectDiagnostic(3, DefinePrototypeAnalyzer.PROTOTYPE_UNUSED)
		);
	}

	@Test
	void notRaiseAUnusedDiagnosticInFunctionsWhenAPrototypeIsUsed()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE.id());
		testDiagnostics(
			"FUNC.NS7",
			"""
			DEFINE FUNCTION FUNC
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE PROTOTYPE FUNC RETURNS (L)
			END-PROTOTYPE
			FUNC(<>)
			END-FUNCTION
			END
			""",
			expectNoDiagnosticOfType(DefinePrototypeAnalyzer.PROTOTYPE_UNUSED)
		);
	}

	protected DefinePrototypeAnalyzerShould()
	{
		super(new DefinePrototypeAnalyzer());
	}
}
