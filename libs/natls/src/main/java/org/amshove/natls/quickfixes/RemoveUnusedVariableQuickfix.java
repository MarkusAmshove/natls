package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.UnusedImportAnalyzer;
import org.amshove.natlint.analyzers.UnusedVariableAnalyzer;
import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

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
		return new CodeActionBuilder("Remove unused %s".formatted(name), CodeActionKind.QuickFix)
			.fixesDiagnostic(diagnostic)
			.appliesWorkspaceEdit(
				new WorkspaceEditBuilder()
					.removesLine(context.fileUri(), new Range(new Position(diagnostic.getRange().getStart().getLine(), 0), new Position(diagnostic.getRange().getEnd().getLine() + 1, 0))).build()
			)
			.build();
	}
}
