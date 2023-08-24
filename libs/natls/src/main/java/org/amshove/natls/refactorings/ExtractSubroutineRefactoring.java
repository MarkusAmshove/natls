package org.amshove.natls.refactorings;

import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.codeactions.RefactoringContext;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.quickfixes.CodeActionBuilder;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;

public class ExtractSubroutineRefactoring implements ICodeActionProvider
{
	@Override
	public boolean isApplicable(RefactoringContext context)
	{
		return context.isMultiSelect()
			&& context.file().getType().canHaveBody()
			&& isNotInDefineData(context, context.nodeAtStartPosition(), context.originalRange().getStart())
			&& isNotInDefineData(context, context.nodeAtEndPosition(), context.originalRange().getEnd());
	}

	@Override
	public List<CodeAction> createCodeAction(RefactoringContext context)
	{
		var enclosedStatements = NodeUtil.findEnclosedStatements(
			context.file().getPath(),
			((IModuleWithBody) context.file().module()).body(),
			context.originalRange().getStart().getLine(),
			context.originalRange().getEnd().getLine()
		);

		for (var statement : enclosedStatements)
		{
			if (statement instanceof ISubroutineNode || statement instanceof IOnErrorNode)
			{
				// Can't wrap these into a subroutine
				return List.of();
			}
		}

		var wholeRange = extractWholeRange(enclosedStatements);

		return List.of(
			new CodeActionBuilder("Extract inline subroutine", CodeActionKind.RefactorExtract)
				.appliesWorkspaceEdit(
					new WorkspaceEditBuilder()
						.changesRange(context.file(), wholeRange, "PERFORM EXTRACTED")
						.addsSubroutine(context.file(), "EXTRACTED", extractLinesInSelection(context, wholeRange))
				)
				.build()
		);
	}

	private Range extractWholeRange(ReadOnlyList<IStatementNode> statements)
	{
		var earliestStatement = statements.stream().min(Comparator.comparing(s -> s.position().line())).orElseThrow();
		var latestStatement = statements.stream().max(Comparator.comparing(s -> s.descendants().last().position().line())).orElseThrow();

		return LspUtil.toRange(
			earliestStatement,
			latestStatement
		);
	}

	private static boolean isNotInDefineData(RefactoringContext context, ISyntaxNode node, Position position)
	{
		if (node != null)
		{
			return NodeUtil.findFirstParentOfType(node, IDefineData.class) == null;
		}

		if (context.file().module()instanceof IHasDefineData hasDefineData && hasDefineData.defineData() != null)
		{
			return hasDefineData.defineData().descendants().last().position().line() < position.getLine();
		}

		return true;
	}

	private static String extractLinesInSelection(RefactoringContext context, Range range)
	{
		try
		{
			var lines = Files.readAllLines(context.file().getPath());

			var extractedLines = new StringBuilder();
			for (var i = range.getStart().getLine(); i <= range.getEnd().getLine(); i++)
			{
				extractedLines.append(lines.get(i));
				extractedLines.append(System.lineSeparator());
			}
			return extractedLines.toString();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
