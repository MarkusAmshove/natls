package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.CodeConsistencyAnalyzer;
import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natparse.lexing.SyntaxKind;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

public class CodeConsistencyQuickfix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(CodeConsistencyAnalyzer.PREFER_DIFFERENT_TOKEN, this::replaceTokenFix);
	}

	private CodeAction replaceTokenFix(QuickFixContext quickFixContext)
	{
		var oldToken = quickFixContext.tokenUnderCursor();
		var newTokenKindName = quickFixContext.diagnostic().getMessage().split(" ")[1];
		var newTokenKind = SyntaxKind.valueOf(newTokenKindName);
		var newTokenSource = newTokenKind.isSystemFunction() || newTokenKind.isSystemVariable() ? "*%s".formatted(newTokenKindName) : newTokenKindName;

		return new CodeActionBuilder("Replace %s with %s".formatted(oldToken.source(), newTokenSource), CodeActionKind.QuickFix)
			.fixesDiagnostic(quickFixContext.diagnostic())
			.appliesWorkspaceEdit(
				new WorkspaceEditBuilder()
					.changesText(oldToken, newTokenSource)
			)
			.build();
	}
}
