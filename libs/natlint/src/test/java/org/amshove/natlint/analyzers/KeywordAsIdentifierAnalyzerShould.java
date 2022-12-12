package org.amshove.natlint.analyzers;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.stream.Stream;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.lexing.SyntaxKind;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class KeywordAsIdentifierAnalyzerShould extends AbstractAnalyzerTest
{
	protected KeywordAsIdentifierAnalyzerShould()
	{
		super(new KeywordAsIdentifierAnalyzer());
	}

	@TestFactory
	Stream<DynamicTest> reportADiagnosticForKeywordsThatCanBeIdentifiers()
	{
		return Arrays.stream(SyntaxKind.values())
			.filter(SyntaxKind::canBeIdentifier)
			.filter(sk -> sk != SyntaxKind.IDENTIFIER)
			.map(sk -> dynamicTest("%s should be discouraged as identifier".formatted(sk), () -> {
				testDiagnostics("""
					DEFINE DATA LOCAL
					1 %s (A10)
					END-DEFINE
					""".formatted(sk.toString().replace("KW_", "").replace("_", "-").replace("WITH-CTE", "WITH_CTE")),
					expectDiagnostic(1, KeywordAsIdentifierAnalyzer.KEYWORD_USED_AS_IDENTIFIER));
			}));
	}

	@Test
	void onlyReportADiagnosticAtTheDeclarationSite()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 FILLER (A10)
			END-DEFINE
			WRITE FILLER
			END
			""",
			expectDiagnostic(1, KeywordAsIdentifierAnalyzer.KEYWORD_USED_AS_IDENTIFIER),
			expectNoDiagnostic(3, KeywordAsIdentifierAnalyzer.KEYWORD_USED_AS_IDENTIFIER));
	}

	@Test
	void onlyReportADiagnosticAtTheDeclarationSiteForSubroutines()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE
			PERFORM RESULT
			DEFINE SUBROUTINE RESULT
			IGNORE
			END-SUBROUTINE
			END
			""",
			expectNoDiagnostic(2, KeywordAsIdentifierAnalyzer.KEYWORD_USED_AS_IDENTIFIER),
			expectDiagnostic(3, KeywordAsIdentifierAnalyzer.KEYWORD_USED_AS_IDENTIFIER));
	}

	@Test
	void notReportADiagnosticForVariablesWithNonKeywordName()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #MYVAR (A10)
			END-DEFINE
			END""",
			expectNoDiagnosticOfType(KeywordAsIdentifierAnalyzer.KEYWORD_USED_AS_IDENTIFIER));
	}

	@Test
	void notReportADiagnosticForSubroutinesWithNonKeywordName()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE
			END""",
			expectNoDiagnosticOfType(KeywordAsIdentifierAnalyzer.KEYWORD_USED_AS_IDENTIFIER));
	}
}
