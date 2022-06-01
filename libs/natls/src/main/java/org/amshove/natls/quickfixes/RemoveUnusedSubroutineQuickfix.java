package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.UnusedLocalSubroutineAnalyzer;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.List;
import java.util.Map;

public class RemoveUnusedSubroutineQuickfix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(UnusedLocalSubroutineAnalyzer.UNUSED_SUBROUTINE, this::fixUnusedSubroutine);
	}

	private CodeAction fixUnusedSubroutine(QuickFixContext quickFixContext)
	{
		// TODO: Duplicate code with variables. Simplify
		var diagnostic = quickFixContext.diagnostic();
		var action = new CodeAction();
		action.setTitle("Remove unused subroutine");
		action.setKind(CodeActionKind.QuickFix);
		action.setDiagnostics(List.of(diagnostic));
		var edit = new WorkspaceEdit();
		var change = new TextEdit();
		var node = quickFixContext.nodeAtPosition();
		if(node instanceof ITokenNode)
		{
			node = (ISyntaxNode) node.parent();
		}
		change.setRange(LspUtil.toRange(node));
		change.setNewText("");
		edit.setChanges(Map.of(quickFixContext.fileUri(), List.of(change)));
		action.setEdit(edit);
		return action;
	}
}
