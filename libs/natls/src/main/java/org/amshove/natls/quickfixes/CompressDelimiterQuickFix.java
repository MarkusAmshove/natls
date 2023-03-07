package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.CompressAnalyzer;
import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natparse.lexing.SyntaxKind;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

public class CompressDelimiterQuickFix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(CompressAnalyzer.COMPRESS_SHOULD_HAVE_ALL_DELIMITERS, this::addAllToCompress);
	}

	private CodeAction addAllToCompress(QuickFixContext quickFixContext)
	{
		var withKeyword = quickFixContext.nodeAtPosition().findDescendantToken(SyntaxKind.WITH);

		if (withKeyword == null)
		{
			return null;
		}

		return new CodeActionBuilder("Add ALL to COMPRESS", CodeActionKind.QuickFix)
			.fixesDiagnostic(quickFixContext.diagnostic())
			.appliesWorkspaceEdit(
				new WorkspaceEditBuilder()
					.changesText(withKeyword.position(), "WITH ALL")
			)
			.build();
	}
}
