package org.amshove.natparse;

import org.amshove.natparse.natural.*;

public class NodeUtil
{
	private NodeUtil()
	{
	}

	/**
	 * Checks whether the module contains the given node.
	 * The comparison is done by the position of the node, comparing its file path to the module file path.
	 */
	public static boolean moduleContainsNode(INaturalModule module, ISyntaxNode node)
	{
		return module.file().getPath().equals(node.position().filePath());
	}

	/**
	 * Checks whether the module contains the given node.
	 * The comparison is done by the DiagnosticPosition, which is e.g. the copy code name in an INCULDE.
	 */
	public static boolean moduleContainsNodeByDiagnosticPosition(INaturalModule module, ISyntaxNode node)
	{
		return module.file().getPath().equals(node.diagnosticPosition().filePath());
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
				if(node instanceof IStatementListNode)
				{
					return findNodeAtPosition(line, character, node);
				}
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

		if (previousNode != null
			&& previousNode.position().line() == line)
		{
			return findNodeAtPosition(line, character, previousNode);
		}

		return null;
	}

	public static <T extends ISyntaxNode> T findFirstParentOfType(ISyntaxNode start, Class<T> type)
	{
		var current = (ISyntaxNode) start.parent();
		while(current != null)
		{
			if(type.isInstance(current))
			{
				return type.cast(current);
			}
			current = (ISyntaxNode) current.parent();
		}

		return null;
	}
}
