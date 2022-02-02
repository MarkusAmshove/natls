package org.amshove.natls.quickfixes;

import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natls.codeactions.CodeActionContext;
import org.amshove.natls.codeactions.ICodeAction;
import org.eclipse.lsp4j.CodeAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractQuickFix implements ICodeAction
{
	private final Map<String, List<Function<QuickFixContext, CodeAction>>> quickfixes = new HashMap<>();

	protected abstract void registerQuickfixes();

	protected void registerQuickFix(DiagnosticDescription description, Function<QuickFixContext, CodeAction> quickFixer)
	{
		registerQuickFix(description.getId(), quickFixer);
	}

	protected void registerQuickFix(String diagnosticId, Function<QuickFixContext, CodeAction> quickFixer)
	{
		quickfixes.computeIfAbsent(diagnosticId, k -> new ArrayList<>())
			.add(quickFixer);
	}

	@Override
	public boolean isApplicable(CodeActionContext context)
	{
		return context.diagnosticsAtPosition().stream().anyMatch(d -> quickfixes.containsKey(d.getCode().getLeft()));
	}

	@Override
	public List<CodeAction> createCodeAction(CodeActionContext context)
	{
		return context.diagnosticsAtPosition().stream()
			.filter(d -> quickfixes.containsKey(d.getCode().getLeft()))
			.flatMap(d -> quickfixes.get(d.getCode().getLeft()).stream().map(qf -> qf.apply(QuickFixContext.fromCodeActionContext(context, d))))
			.toList();
	}
}
