package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HiddenTransactionAnalyzerShould extends AbstractAnalyzerTest
{
	protected HiddenTransactionAnalyzerShould()
	{
		super(new HiddenTransactionAnalyzer());
	}

	@ParameterizedTest
	@CsvSource(
		{
			"C,END TRANSACTION",
			"C,GET TRANSACTION #VAR",
			"C,BACKOUT TRANSACTION",
			"H,END OF TRANSACTION",
			"H,GET TRANSACTION DATA #VAR",
			"H,BACKOUT",
			"M,END TRANSACTION",
			"M,GET TRANSACTION #VAR",
			"M,BACKOUT TRANSACTION",
			"N,END OF TRANSACTION",
			"N,GET TRANSACTION DATA #VAR",
			"N,BACKOUT TRANSACTION",
			"S,END TRANSACTION",
			"S,GET TRANSACTION #VAR",
			"S,BACKOUT"
		}
	)
	void raiseADiagnosticForETForOtherObjectTypesThanProgram(String objtype, String statement)
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_hiddentransactions=true
			""");

		testDiagnostics(
			"OBJECT.NS%s".formatted(objtype), """
            %s
            END
			""".formatted(statement),
			expectDiagnostic(0, HiddenTransactionAnalyzer.HIDDEN_TRANSACTION_STATEMENT_IS_DISCOURAGED)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"END TRANSACTION",
		"END OF TRANSACTION",
		"GET TRANSACTION #VAR",
		"GET TRANSACTION DATA #VAR",
		"BACKOUT TRANSACTION",
		"BACKOUT"
	})
	void notRaiseADiagnosticForETInProgram(String statement)
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_hiddentransactions=true
			""");

		testDiagnostics(
			"OBJECT.NSP", """
            %s
            END
			""".formatted(statement),
			expectNoDiagnostic(0, HiddenTransactionAnalyzer.HIDDEN_TRANSACTION_STATEMENT_IS_DISCOURAGED)
		);
	}

	@Test
	void raiseNoDiagnosticIfOptionIsFalse()
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_hiddentransactions=false
			""");

		testDiagnostics(
			"OBJECT.NSN", """
			END TRANSACTION
			END
			""",
			expectNoDiagnosticOfType(HiddenTransactionAnalyzer.HIDDEN_TRANSACTION_STATEMENT_IS_DISCOURAGED)
		);
	}

}
