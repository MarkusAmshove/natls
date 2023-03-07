package org.amshove.natls.refactorings;

import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.codeactions.RefactoringContext;
import org.amshove.natls.quickfixes.CodeActionBuilder;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.ICompressStatementNode;
import org.amshove.natparse.natural.ITokenNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompressRefactorings implements ICodeActionProvider
{
	@Override
	public boolean isApplicable(RefactoringContext context)
	{
		return context.nodeAtPosition()instanceof ICompressStatementNode compressStatementNode
			&& (!compressStatementNode.isFull() || !compressStatementNode.isNumeric() || !compressStatementNode.isWithDelimiters());
	}

	@Override
	public List<CodeAction> createCodeAction(RefactoringContext context)
	{
		var compress = (ICompressStatementNode) context.nodeAtPosition();
		var compressKeyword = Objects.requireNonNull(compress.findDescendantToken(SyntaxKind.COMPRESS));

		var actions = new ArrayList<CodeAction>();

		if (!compress.isFull())
		{
			var position = compress.isNumeric()
				? Objects.requireNonNull(compress.findDescendantToken(SyntaxKind.NUMERIC)).position()
				: compressKeyword.position();
			var replacement = compress.isNumeric()
				? "NUMERIC FULL"
				: "COMPRESS FULL";

			actions.add(
				new CodeActionBuilder("Add FULL to COMPRESS", CodeActionKind.RefactorRewrite)
					.appliesWorkspaceEdit(
						new WorkspaceEditBuilder()
							.changesText(position, replacement)
					)
					.build()
			);
		}

		if (!compress.isNumeric())
		{
			var position = compress.isFull()
				? Objects.requireNonNull(compress.findDescendantToken(SyntaxKind.FULL)).position()
				: compressKeyword.position();
			var replacement = compress.isFull()
				? "NUMERIC FULL"
				: "COMPRESS NUMERIC";

			actions.add(
				new CodeActionBuilder("Add NUMERIC to COMPRESS", CodeActionKind.RefactorRewrite)
					.appliesWorkspaceEdit(
						new WorkspaceEditBuilder()
							.changesText(position, replacement)
					)
					.build()
			);
		}

		if (!compress.isWithDelimiters())
		{
			actions.add(
				new CodeActionBuilder("Add WITH DELIMITERS to COMPRESS", CodeActionKind.RefactorRewrite)
					.appliesWorkspaceEdit(
						new WorkspaceEditBuilder()
							.appendsToNode(compress, " WITH DELIMITERS ';'")
					)
					.build()
			);

			actions.add(
				new CodeActionBuilder("Add WITH ALL DELIMITERS to COMPRESS", CodeActionKind.RefactorRewrite)
					.appliesWorkspaceEdit(
						new WorkspaceEditBuilder()
							.appendsToNode(compress, " WITH ALL DELIMITERS ';'")
					)
					.build()
			);
		}

		if (compress.isWithDelimiters() && !compress.isWithAllDelimiters())
		{
			var withToken = Objects.requireNonNull(compress.findDescendantToken(SyntaxKind.WITH));

			actions.add(
				new CodeActionBuilder("Add ALL to DELIMITERS", CodeActionKind.RefactorRewrite)
					.appliesWorkspaceEdit(
						new WorkspaceEditBuilder()
							.changesText(withToken.position(), "WITH ALL")
					)
					.build()
			);
		}

		if (compress.isLeavingSpace())
		{
			var intoPosition = compress.intoTarget().position();
			var intoSource = Objects.requireNonNull(compress.intoTarget().findDescendantOfType(ITokenNode.class)).token().source();

			actions.add(
				new CodeActionBuilder("Add LEAVING NO SPACE to COMPRESS", CodeActionKind.RefactorRewrite)
					.appliesWorkspaceEdit(
						new WorkspaceEditBuilder()
							.changesText(intoPosition, intoSource + " LEAVING NO SPACE")
					)
					.build()
			);
		}

		return actions;
	}
}
