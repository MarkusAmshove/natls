package org.amshove.natls.refactorings;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.codeactions.RefactoringContext;
import org.amshove.natparse.natural.ITypedVariableNode;
import org.eclipse.lsp4j.*;

import java.util.List;
import java.util.Map;

public class CreateRedefineRefactoring implements ICodeActionProvider
{
	@Override
	public boolean isApplicable(RefactoringContext context)
	{
		return context.nodeAtPosition() instanceof ITypedVariableNode typedVariableNode && typedVariableNode.scope().isLocal();
	}

	@Override
	public List<CodeAction> createCodeAction(RefactoringContext context)
	{
		var typedVariable = ((ITypedVariableNode) context.nodeAtPosition());

		var textEdit = new TextEdit();
		var insertText = "%d REDEFINE %s".formatted(typedVariable.level(), typedVariable.name());
		insertText += "\n%d #R-%s %s\n".formatted(typedVariable.level() + 1, typedVariable.name(), typedVariable.type().toShortString());
		textEdit.setNewText(insertText);
		textEdit.setRange(new Range(new Position(typedVariable.position().line() + 1, 0), new Position(typedVariable.position().line() + 1, 0)));

		return List.of(createCurrentFileCodeAction("Redefine " + typedVariable.name(), context, List.of(textEdit)));
	}

	private CodeAction createCurrentFileCodeAction(String title, RefactoringContext context, List<TextEdit> textEdits)
	{
		var action = new CodeAction(title);
		var workspaceEdit = new WorkspaceEdit();
		workspaceEdit.setChanges(Map.of(context.fileUri(), textEdits));
		action.setEdit(workspaceEdit);
		action.setKind(CodeActionKind.Refactor);
		return action;
	}
}
