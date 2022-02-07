package org.amshove.natparse;

import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxTree;
import org.amshove.natparse.natural.ITokenNode;

public class NodeUtil
{
	private NodeUtil()
	{
	}

	public static boolean moduleContainsNode(INaturalModule module, ISyntaxNode node)
	{
		return module.file().getPath().equals(node.position().filePath());
	}

	public static ISyntaxNode findNodeAtPosition(int line, int character, INaturalModule module)
	{
		return findNodeAtPosition(line, character, module.syntaxTree());
	}

	/**
	 * Tries to find the node at the given position.
	 * It does try to not return an {@link ITokenNode}, but the node that contains the {@link ITokenNode}.
	 */
	public static ISyntaxNode findNodeAtPosition(int line, int character, ISyntaxTree syntaxTree)
	{
		if(syntaxTree == null)
		{
			return null;
		}

		ISyntaxNode previousNode = null;

		for (var node : syntaxTree)
		{
			if (node.position().line() == line && node.position().offsetInLine() == character)
			{
				return node;
			}

			if (node.position().line() == line && node.position().offsetInLine() > character)
			{
				return previousNode instanceof ITokenNode ? (ISyntaxNode) syntaxTree : previousNode;
			}

			if (node.position().line() == line && node.position().offsetInLine() < character && node.position().endOffset() > character)
			{
				return node;
			}

			if (node.position().line() > line)
			{
				return findNodeAtPosition(line, character, previousNode);
			}

			previousNode = node;
		}

		if (previousNode != null
			&& previousNode.position().line() == line
			&& previousNode.position().offsetInLine() < character
			&& previousNode.position().offsetInLine() + previousNode.position().length() >= character)
		{
			return previousNode;
		}

		if (previousNode != null
			&& previousNode.position().line() < line)
		{
			return findNodeAtPosition(line, character, previousNode);
		}

		return null;
	}
}
