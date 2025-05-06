package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class LowercaseCodeAnalyzerShould extends AbstractAnalyzerTest
{
	protected LowercaseCodeAnalyzerShould()
	{
		super(new LowercaseCodeAnalyzer());
	}

	@Test
	void reportDiagnosticIfCodeInLowercase()
	{
		configureEditorConfig("""
			[*]
			natls.style.disallow_lowercase_code=true
			""");

		var source = """
            DEFINE DATA LOCAL
            1 #VAR (A20)
            END-DEFINE
            /* A comment in lowercase is of course OK!
			#VAR := 'This is a long literal.' /* Allowed
            if #VAR NE ' ' then /* Not allowed
              PRINT 'Filled'
            END-IF
            END
            """;
		testDiagnostics(source, expectDiagnostic(5, LowercaseCodeAnalyzer.LOWERCASE_CODE_IS_NOT_ALLOWED));
	}

	@Test
	void reportDiagnosticIfHexLiteralInLowercase()
	{
		configureEditorConfig("""
			[*]
			natls.style.disallow_lowercase_code=true
			""");

		var source = """
            DEFINE DATA LOCAL
            1 #HEX (A2)
            END-DEFINE
            #HEX := H'FeFe'
            END
            """;
		testDiagnostics(source, expectDiagnostic(3, LowercaseCodeAnalyzer.LOWERCASE_CODE_IS_NOT_ALLOWED));
	}

	@Test
	void reportNoDiagnosticIfCodeInUppercase()
	{
		configureEditorConfig("""
			[*]
			natls.style.disallow_lowercase_code=true
			""");

		var source = """
            DEFINE DATA LOCAL
            1 #VAR (A20)
            END-DEFINE
            /* A comment in lowercase is of course OK!
			#VAR := 'This is a long literal.' /* Allowed
            IF #VAR NE ' ' THEN /* Allowed
              PRINT 'Filled'
            END-IF
            END
            """;
		testDiagnostics(source, expectNoDiagnosticOfType(LowercaseCodeAnalyzer.LOWERCASE_CODE_IS_NOT_ALLOWED));
	}
}
