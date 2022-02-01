package org.amshove.natls.quickfixes;

import org.amshove.natls.codeactions.CodeActionContext;
import org.amshove.natls.codeactions.ICodeAction;
import org.eclipse.lsp4j.*;

import java.util.List;
import java.util.Map;

public class RemoveUnusedVariableQuickfix implements ICodeAction
{
	@Override
	public boolean isApplicable(CodeActionContext context)
	{
		return context.diagnosticsAtPosition().stream().anyMatch(d -> d.getCode().getLeft().equals("NL001"));
	}

	@Override
	public CodeAction createCodeAction(CodeActionContext context)
	{
		var diagnostic = context.diagnosticsAtPosition().stream().filter(d -> d.getCode().getLeft().equals("NL001")).findFirst().get();
		var action = new CodeAction("Remove variable");
		action.setKind(CodeActionKind.QuickFix);
		action.setDiagnostics(List.of(diagnostic));
		var edit = new WorkspaceEdit();
		var change = new TextEdit();
		change.setRange(new Range(new Position(diagnostic.getRange().getStart().getLine(), 0), new Position(diagnostic.getRange().getEnd().getLine() + 1, 0)));
		change.setNewText("");
		edit.setChanges(Map.of(context.fileUri(), List.of(change)));
		action.setEdit(edit);
		return action;
	}
}
