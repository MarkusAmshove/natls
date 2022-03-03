package org.amshove.natls.codeactions;

import org.amshove.natlint.api.DiagnosticDescription;
import org.eclipse.lsp4j.CodeAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractQuickFix implements ICodeActionProvider
{
	private final Map<String, List<Function<QuickFixContext, CodeAction>>> quickfixes = new HashMap<>();

	protected abstract void registerQuickfixes();

	public AbstractQuickFix()
	{
		registerQuickfixes();
	}

	protected void registerQuickFix(DiagnosticDescription description, Function<QuickFixContext, CodeAction> quickFixer)
	{
		registerQuickFix(description.getId(), quickFixer);
	}

	protected void registerQuickFix(String diagnosticId, Function<QuickFixContext, CodeAction> quickFixer)
	{
		quickfixes.putIfAbsent(diagnosticId, new ArrayList<>());
		quickfixes.get(diagnosticId).add(quickFixer);
	}

	@Override
	public boolean isApplicable(RefactoringContext context)
	{
		return context.diagnosticsAtPosition().stream().anyMatch(d -> d.getCode() != null && d.getCode().isLeft() && quickfixes.containsKey(d.getCode().getLeft()));
	}

	@Override
	public List<CodeAction> createCodeAction(RefactoringContext context)
	{
		return context.diagnosticsAtPosition().stream()
			.filter(d -> d.getCode() != null && d.getCode().isLeft() && quickfixes.containsKey(d.getCode().getLeft()))
			.flatMap(d -> quickfixes.get(d.getCode().getLeft()).stream().map(qf -> qf.apply(QuickFixContext.fromCodeActionContext(context, d))))
			.toList();
	}
}
