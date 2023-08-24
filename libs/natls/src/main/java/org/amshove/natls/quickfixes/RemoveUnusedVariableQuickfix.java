package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.UnusedImportAnalyzer;
import org.amshove.natlint.analyzers.UnusedVariableAnalyzer;
import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.ReadOnlyList;
import org.eclipse.lsp4j.*;

import java.util.stream.Stream;

public class RemoveUnusedVariableQuickfix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(UnusedVariableAnalyzer.UNUSED_VARIABLE, this::fixUnusedVariable);
		registerQuickFix(UnusedImportAnalyzer.UNUSED_IMPORT, this::fixUnusedImport);
		registerFixAll(UnusedVariableAnalyzer.UNUSED_VARIABLE.getId(), this::removeAllUnused);
		registerFixAll(UnusedImportAnalyzer.UNUSED_IMPORT.getId(), this::removeAllUnused);
	}

	private CodeAction removeAllUnused(LanguageServerFile languageServerFile, ReadOnlyList<Diagnostic> diagnostics)
	{
		var editBuilder = new WorkspaceEditBuilder();

		Stream.concat(
			languageServerFile.diagnosticsInFileOfType(UnusedImportAnalyzer.UNUSED_IMPORT.getId()).stream(),
			languageServerFile.diagnosticsInFileOfType(UnusedVariableAnalyzer.UNUSED_VARIABLE.getId()).stream()
		)
			.forEach(d -> editBuilder.removesLine(languageServerFile.getUri(), toLine(d.getRange())));

		return new CodeActionBuilder("Remove all unused symbols in DEFINE DATA", CodeActionKind.QuickFix)
			.appliesWorkspaceEdit(editBuilder)
			.build();
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
					.removesLine(context.fileUri(), toLine(diagnostic.getRange()))
			)
			.build();
	}

	private Range toLine(Range range)
	{
		return new Range(new Position(range.getStart().getLine(), 0), new Position(range.getEnd().getLine() + 1, 0));
	}
}
