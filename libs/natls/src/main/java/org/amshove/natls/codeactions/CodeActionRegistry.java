package org.amshove.natls.codeactions;

import org.eclipse.lsp4j.CodeAction;

import java.util.ArrayList;
import java.util.List;

public class CodeActionRegistry
{
	private static final List<ICodeAction> codeActionProviders = new ArrayList<>();

	public static void register(ICodeAction codeAction)
	{
		codeActionProviders.add(codeAction);
	}

	public List<CodeAction> createCodeActions(CodeActionContext context)
	{
		var codeActions = new ArrayList<CodeAction>();
		for (var codeActionProvider : codeActionProviders)
		{
			if(codeActionProvider.isApplicable(context))
			{
				codeActions.add(codeActionProvider.createCodeAction(context));
			}
		}

		return codeActions;
	}
}
