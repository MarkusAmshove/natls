package org.amshove.natls.codeactions;

import org.eclipse.lsp4j.CodeAction;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public enum CodeActionRegistry
{
	INSTANCE;

	private final List<ICodeActionProvider> codeActionProviders;

	CodeActionRegistry()
	{
		var reflections = new Reflections("org.amshove.natls");
		var analyzers = new ArrayList<ICodeActionProvider>();
		for (var codeActionClass : reflections.getSubTypesOf(ICodeActionProvider.class))
		{
			try
			{
				if (Modifier.isAbstract(codeActionClass.getModifiers()))
				{
					continue;
				}
				var analyzer = codeActionClass.getConstructor().newInstance();
				analyzers.add(analyzer);
			}
			catch (Exception e)
			{
				throw new RuntimeException(
					"CodeAction %s can not be instantiated. Does it have a parameterless constructor?".formatted(codeActionClass.getName()),
					e
				);
			}
		}

		codeActionProviders = analyzers;
	}

	public void register(ICodeActionProvider codeAction)
	{
		codeActionProviders.add(codeAction);
	}

	public void register(AbstractQuickFix quickFix)
	{
		codeActionProviders.add(quickFix);
	}

	public void unregisterAll()
	{
		codeActionProviders.clear();
	}

	public int registeredCodeActionCount()
	{
		return codeActionProviders.size();
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
