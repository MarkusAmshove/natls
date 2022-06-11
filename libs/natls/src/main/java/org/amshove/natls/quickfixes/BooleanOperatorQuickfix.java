package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.BooleanOperatorAnalyzer;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.lexing.SyntaxKind;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.List;
import java.util.Map;

public class BooleanOperatorQuickfix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR, this::fixOperator);
	}

	private CodeAction fixOperator(QuickFixContext quickFixContext)
	{
		// TODO: Duplicate code with variables. Simplify
		var diagnostic = quickFixContext.diagnostic();
		var message = diagnostic.getMessage();
		var discouragedOperator = message.split(" ")[1];
		var preferredOperator = BooleanOperatorAnalyzer.PREFERRED_OPERATORS.get(SyntaxKind.valueOf(discouragedOperator));
		var action = new CodeAction();
		action.setTitle("Change operator to %s".formatted(preferredOperator));
		action.setKind(CodeActionKind.QuickFix);
		action.setDiagnostics(List.of(diagnostic));
		var edit = new WorkspaceEdit();
		var change = new TextEdit();
		var node = quickFixContext.nodeAtPosition();
		change.setRange(LspUtil.toRange(node));
		change.setNewText(preferredOperator);
		edit.setChanges(Map.of(quickFixContext.fileUri(), List.of(change)));
		action.setEdit(edit);
		return action;
	}
}
