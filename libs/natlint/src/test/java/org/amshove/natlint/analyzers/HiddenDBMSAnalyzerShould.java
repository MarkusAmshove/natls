package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HiddenDBMSAnalyzerShould extends AbstractAnalyzerTest
{
	protected HiddenDBMSAnalyzerShould()
	{
		super(new HiddenDBMSAnalyzer());
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"READ DDM BY DESC IGNORE END-READ",
		"FIND DDM WITH DESC IGNORE END-FIND",
		"HISTOGRAM DDM FOR DESC IGNORE END-HISTOGRAM",
		"GET SAME",
		"GET DDM #ISN",
		"SELECT * INTO VIEW FROM DDM IGNORE END-SELECT",
		"UPDATE",
		"DELETE",
	})
	void raiseADiagnosticForDBMSForCopycode(String statement)
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_hidden_dbms=true
			""");

		testDiagnostics(
			"OBJECT.NSC", """
            %s
            END
			""".formatted(statement),
			expectDiagnostic(0, HiddenDBMSAnalyzer.HIDDEN_DBMS_IS_DISCOURAGED)
		);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"H, READ DDM BY DESC IGNORE END-READ",
			"H, FIND DDM WITH DESC IGNORE END-FIND",
			"H, HISTOGRAM DDM FOR DESC IGNORE END-HISTOGRAM",
			"H, GET SAME",
			"H, GET DDM #ISN",
			"H, SELECT * INTO VIEW FROM DDM IGNORE END-SELECT",
			"H, UPDATE",
			"H ,DELETE",
			"M, READ DDM BY DESC IGNORE END-READ",
			"M, FIND DDM WITH DESC IGNORE END-FIND",
			"M, HISTOGRAM DDM FOR DESC IGNORE END-HISTOGRAM",
			"M, GET SAME",
			"M, GET DDM #ISN",
			"M, SELECT * INTO VIEW FROM DDM IGNORE END-SELECT",
			"M, UPDATE",
			"M ,DELETE",
			"N, READ DDM BY DESC IGNORE END-READ",
			"N, FIND DDM WITH DESC IGNORE END-FIND",
			"N, HISTOGRAM DDM FOR DESC IGNORE END-HISTOGRAM",
			"N, GET SAME",
			"N, GET DDM #ISN",
			"N, SELECT * INTO VIEW FROM DDM IGNORE END-SELECT",
			"N, UPDATE",
			"N ,DELETE",
			"S, READ DDM BY DESC IGNORE END-READ",
			"S, FIND DDM WITH DESC IGNORE END-FIND",
			"S, HISTOGRAM DDM FOR DESC IGNORE END-HISTOGRAM",
			"S, GET SAME",
			"S, GET DDM #ISN",
			"S, SELECT * INTO VIEW FROM DDM IGNORE END-SELECT",
			"S, UPDATE",
			"S ,DELETE",
			"P, READ DDM BY DESC IGNORE END-READ",
			"P, FIND DDM WITH DESC IGNORE END-FIND",
			"P, HISTOGRAM DDM FOR DESC IGNORE END-HISTOGRAM",
			"P, GET SAME",
			"P, GET DDM #ISN",
			"P, SELECT * INTO VIEW FROM DDM IGNORE END-SELECT",
			"P, UPDATE",
			"P ,DELETE",
		}
	)
	void notRaiseADiagnosticForDBMSInOtherObjtypes(String objtype, String statement)
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_hidden_dbms=true
			""");

		testDiagnostics(
			"OBJECT.NS%s".formatted(objtype), """
            %s
            END
			""".formatted(statement),
			expectNoDiagnostic(0, HiddenDBMSAnalyzer.HIDDEN_DBMS_IS_DISCOURAGED)
		);
	}

	@Test
	void raiseNoDiagnosticIfOptionIsFalse()
	{
		configureEditorConfig("""
			[*]
			natls.style.discourage_hidden_dbms=false
			""");

		testDiagnostics(
			"OBJECT.NSN", """
			READ DDM BY DESC IGNORE END-READ
			END
			""",
			expectNoDiagnosticOfType(HiddenDBMSAnalyzer.HIDDEN_DBMS_IS_DISCOURAGED)
		);
	}

}
