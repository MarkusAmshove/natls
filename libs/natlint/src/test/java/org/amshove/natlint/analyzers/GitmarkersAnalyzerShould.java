package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class GitmarkersAnalyzerShould extends AbstractAnalyzerTest
{
	protected GitmarkersAnalyzerShould()
	{
		super(new GitmarkersAnalyzer());
	}

	@Test
	void reportDiagnosticsForGitmarkersInLineComment()
	{
		configureEditorConfig("""
			[*]
			natls.style.discouraged_gitmarkers=true
			""");

		testDiagnostics(
			"""
            DEFINE DATA LOCAL
            1 A(A1)
            END-DEFINE
			* =======
			""",
			expectDiagnostic(3, GitmarkersAnalyzer.DISCOURAGED_GITMARKERS_IN_COMMENT)
		);
	}

	@Test
	void reportDiagnosticsForGitmarkersInInlineComment()
	{
		configureEditorConfig("""
			[*]
			natls.style.discouraged_gitmarkers=true
			""");

		testDiagnostics(
			"""
            DEFINE DATA LOCAL /* <<<<<<< >>>>>>>
            1 A(A1)
            END-DEFINE
			""",
			expectDiagnostic(0, GitmarkersAnalyzer.DISCOURAGED_GITMARKERS_IN_COMMENT)
		);
	}

	@Test
	void reportNoDiagnosticsForNonGitmarkersInLineComment()
	{
		configureEditorConfig("""
			[*]
			natls.style.discouraged_gitmarkers=true
			""");

		testDiagnostics(
			"""
            DEFINE DATA LOCAL
            1 A(A1)
            END-DEFINE
			* << >>
			""",
			expectNoDiagnosticOfType(GitmarkersAnalyzer.DISCOURAGED_GITMARKERS_IN_COMMENT)
		);
	}

	@Test
	void reportNoDiagnosticsForNonGitmarkersInInlineComment()
	{
		configureEditorConfig("""
			[*]
			natls.style.discouraged_gitmarkers=true
			""");

		testDiagnostics(
			"""
            DEFINE DATA LOCAL /* << >>
            1 A(A1)
            END-DEFINE
			""",
			expectNoDiagnosticOfType(GitmarkersAnalyzer.DISCOURAGED_GITMARKERS_IN_COMMENT)
		);
	}

}
