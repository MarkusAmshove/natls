package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

public class ModuleCallAnalyzerShould extends AbstractAnalyzerTest
{
	public ModuleCallAnalyzerShould()
	{
		super(new ModuleCallAnalyzer());
	}

	@Test
	void reportNoDiagnosticIfACallnatHasNoTrailingWhitespace()
	{
		allowParserError("NPP026");
		testDiagnostics(
			"""
			define data
			end-define
			callnat 'MODULE'
			end
			""",
			expectNoDiagnosticOfType(ModuleCallAnalyzer.CALLED_MODULE_NAME_TRAILING_WHITESPACE)
		);
	}

	@Test
	void reportNoDiagnosticIfAFetchReturnHasNoTrailingWhitespace()
	{
		allowParserError("NPP026");
		testDiagnostics(
			"""
			define data
			end-define
			FETCH RETURN 'MODULE'
			end
			""",
			expectNoDiagnosticOfType(ModuleCallAnalyzer.CALLED_MODULE_NAME_TRAILING_WHITESPACE)
		);
	}

	@Test
	void reportNoDiagnosticIfAFetchRepeatHasNoTrailingWhitespace()
	{
		allowParserError("NPP026");
		testDiagnostics(
			"""
			define data
			end-define
			FETCH REPEAT 'MODULE'
			end
			""",
			expectNoDiagnosticOfType(ModuleCallAnalyzer.CALLED_MODULE_NAME_TRAILING_WHITESPACE)
		);
	}

	@Test
	void reportADiagnosticIfACallnatHasTrailingWhitespace()
	{
		allowParserError("NPP026");
		testDiagnostics(
			"""
			define data
			end-define
			callnat 'MODULE '
			end
			""",
			expectDiagnostic(2, ModuleCallAnalyzer.CALLED_MODULE_NAME_TRAILING_WHITESPACE)
		);
	}

	@Test
	void reportADiagnosticIfAFetchReturnHasTrailingWhitespace()
	{
		allowParserError("NPP026");
		testDiagnostics(
			"""
			define data
			end-define
			FETCH RETURN 'MODULE '
			end
			""",
			expectDiagnostic(2, ModuleCallAnalyzer.CALLED_MODULE_NAME_TRAILING_WHITESPACE)
		);
	}

	@Test
	void reportADiagnosticIfAFetchRepeatHasTrailingWhitespace()
	{
		allowParserError("NPP026");
		testDiagnostics(
			"""
			define data
			end-define
			FETCH REPEAT 'MODULE '
			end
			""",
			expectDiagnostic(2, ModuleCallAnalyzer.CALLED_MODULE_NAME_TRAILING_WHITESPACE)
		);
	}
}
