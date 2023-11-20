package org.amshove.natls.refactorings;

import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.codeactions.RefactoringContext;
import org.amshove.natls.quickfixes.CodeActionBuilder;
import org.amshove.natparse.natural.IFunction;
import org.amshove.natparse.natural.IFunctionCallNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

import java.util.List;

public class DefinePrototypeAction implements ICodeActionProvider
{
	@Override
	public boolean isApplicable(RefactoringContext context)
	{
		return context.nodeAtStartPosition() instanceof IFunctionCallNode;
	}

	@Override
	public List<CodeAction> createCodeAction(RefactoringContext context)
	{
		var functionCall = (IFunctionCallNode) context.nodeAtStartPosition();
		if (functionCall.reference() == null)
		{
			return List.of();
		}

		// TODO: Hack. Why does functionCall.reference() not have returnType set?
		var function = (IFunction) context.service().findNaturalFile(functionCall.reference().file().getPath()).module();

		return List.of(
			new CodeActionBuilder("Define Prototype", CodeActionKind.Refactor)
				.appliesWorkspaceEdit(
					new WorkspaceEditBuilder()
						.addPrototype(context.file(), function)
				)
				.build()
		);
	}
}
