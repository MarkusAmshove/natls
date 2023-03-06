package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.CompressNumericAnalyzer;
import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natparse.lexing.SyntaxKind;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

public class CompressNumericQuickFix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(CompressNumericAnalyzer.COMPRESS_SHOULD_HAVE_NUMERIC, this::addNumericToCompress);
	}

	private CodeAction addNumericToCompress(QuickFixContext quickFixContext)
	{
		var compressKeyword = quickFixContext.nodeAtPosition().findDescendantToken(SyntaxKind.COMPRESS);

		if (compressKeyword == null)
		{
			return null;
		}

		return new CodeActionBuilder("Add NUMERIC to COMPRESS", CodeActionKind.QuickFix)
			.fixesDiagnostic(quickFixContext.diagnostic())
			.appliesWorkspaceEdit(new WorkspaceEditBuilder()
				.changesText(compressKeyword.position(), "COMPRESS NUMERIC")
			)
			.build();
	}
}
