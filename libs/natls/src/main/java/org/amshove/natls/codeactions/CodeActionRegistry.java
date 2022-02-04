package org.amshove.natls.codeactions;

import org.eclipse.lsp4j.CodeAction;

import java.util.ArrayList;
import java.util.List;

public class CodeActionRegistry
{
	private static final List<ICodeActionProvider> codeActionProviders = new ArrayList<>();

	public static void register(ICodeActionProvider codeAction)
	{
		codeActionProviders.add(codeAction);
	}

	public static void register(AbstractQuickFix quickFix)
	{
		codeActionProviders.add(quickFix);
	}

	public static void unregisterAll()
	{
		codeActionProviders.clear();
	}

	public List<CodeAction> createCodeActions(RefactoringContext context)
	{
		var codeActions = new ArrayList<CodeAction>();
		for (var codeActionProvider : codeActionProviders)
		{
			if (codeActionProvider.isApplicable(context))
			{
				codeActions.addAll(codeActionProvider.createCodeAction(context));
			}
		}

		return codeActions;
	}
}
