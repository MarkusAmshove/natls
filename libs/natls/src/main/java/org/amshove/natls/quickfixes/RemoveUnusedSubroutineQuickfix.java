package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.UnusedLocalSubroutineAnalyzer;
import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

public class RemoveUnusedSubroutineQuickfix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE, this::fixUnusedSubroutine);
	}

	private CodeAction fixUnusedSubroutine(QuickFixContext quickFixContext)
	{
		var diagnostic = quickFixContext.diagnostic();
		var node = quickFixContext.nodeAtPosition();
		if(node instanceof ITokenNode)
		{
			node = (ISyntaxNode) node.parent();
		}
		return new CodeActionBuilder("Remove unused subroutine", CodeActionKind.QuickFix)
			.fixesDiagnostic(diagnostic)
			.appliesWorkspaceEdit(new WorkspaceEditBuilder()
				.removesNode(node))
			.build();
	}
}
