package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.UnusedImportAnalyzer;
import org.amshove.natlint.analyzers.UnusedVariableAnalyzer;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.eclipse.lsp4j.*;

import java.util.List;
import java.util.Map;

public class RemoveUnusedVariableQuickfix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(UnusedVariableAnalyzer.UNUSED_VARIABLE, this::fixUnusedVariable);
		registerQuickFix(UnusedImportAnalyzer.UNUSED_IMPORT, this::fixUnusedImport);
	}

	private CodeAction fixUnusedImport(QuickFixContext context)
	{
		return createRemovedUnused("using", context);
	}

	private CodeAction fixUnusedVariable(QuickFixContext context)
	{
		return createRemovedUnused("variable", context);
	}

	private CodeAction createRemovedUnused(String name, QuickFixContext context)
	{
		var diagnostic = context.diagnostic();
		var action = new CodeAction();
		action.setTitle("Remove unused " + name);
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
