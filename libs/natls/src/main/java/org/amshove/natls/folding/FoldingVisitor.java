package org.amshove.natls.folding;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.FoldingRange;

import java.util.ArrayList;
import java.util.List;

public class FoldingVisitor implements ISyntaxNodeVisitor
{
	private final List<FoldingRange> foldings = new ArrayList<>();
	private final INaturalModule module;

	public FoldingVisitor(INaturalModule module)
	{
		this.module = module;
	}

	public List<FoldingRange> getFoldings()
	{
		return foldings;
	}

	@Override
	public void visit(ISyntaxNode node)
	{
		if (node instanceof IIfStatementNode ifStatementNode)
		{
			createFolding(ifStatementNode);
			if (ifStatementNode.elseBranch() != null)
			{
				var elseKeyword = ifStatementNode.findDescendantToken(SyntaxKind.ELSE);
				createFolding(elseKeyword, ifStatementNode.descendants().last().position().line());
			}
			return;
		}

		if (node instanceof IStatementWithBodyNode
			|| node instanceof IDecideOnNode
			|| node instanceof IDecideForConditionNode
			|| node instanceof IDefineData)
		{
			createFolding(node);
		}
	}

	private void createFolding(ISyntaxNode node)
	{
		if (node == null)
		{
			return;
		}

		if (isInDifferentFile(node))
		{
			return;
		}

		var range = new FoldingRange(
			node.descendants().first().position().line(),
			node.descendants().last().position().line()
		);
		foldings.add(range);
	}

	private void createFolding(ITokenNode tokenNode, int endLine)
	{
		if (isInDifferentFile(tokenNode))
		{
			return;
		}

		foldings.add(
			new FoldingRange(
				tokenNode.position().line(),
				endLine
			)
		);
	}

	private boolean isInDifferentFile(ISyntaxNode node)
	{
		return !node.position().filePath().equals(module.file().getPath());
	}
}
