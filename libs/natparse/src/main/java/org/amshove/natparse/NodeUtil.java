package org.amshove.natparse;

import org.amshove.natparse.natural.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodeUtil
{
	private NodeUtil()
	{}

	/**
	 * Checks whether the module contains the given node. The comparison is done by the position of the node, comparing
	 * its file path to the module file path.
	 */
	public static boolean moduleContainsNode(INaturalModule module, ISyntaxNode node)
	{
		return node.position() != null
			&& node.position().filePath() != null
			&& module.file().getPath().equals(node.position().filePath());
	}

	/**
	 * Checks whether the module contains the given node. The comparison is done by the DiagnosticPosition, which is
	 * e.g. the copy code name in an INCULDE.
	 */
	public static boolean moduleContainsNodeByDiagnosticPosition(INaturalModule module, ISyntaxNode node)
	{
		return node.position() != null
			&& node.position().filePath() != null
			&& module.file().getPath().equals(node.diagnosticPosition().filePath());
	}

	public static @Nullable ISyntaxNode findNodeAtPosition(int line, int character, INaturalModule module)
	{
		return findNodeAtPosition(line, character, module.syntaxTree());
	}

	/**
	 * Tries to find the first node with a subtype of {@link ITokenNode} at the given position.<br/>
	 * Can be used to prefer finding {@link ISymbolReferenceNode}, {@link IVariableReferenceNode} etc.
	 */
	public static @Nullable ITokenNode findTokenNodeAtPosition(int line, int character, ISyntaxTree syntaxTree)
	{
		if (syntaxTree == null)
		{
			return null;
		}

		for (var node : syntaxTree)
		{
			var isInLine = node.position().line() == line;
			var isTokenNode = node instanceof ITokenNode;

			if (isTokenNode && isInLine && node.position().offsetInLine() == character)
			{
				return (ITokenNode) node;
			}

			if (isTokenNode && isInLine && node.position().offsetInLine() <= character && node.position().endOffset() >= character)
			{
				return (ITokenNode) node;
			}

			var foundDescendant = findTokenNodeAtPosition(line, character, node);
			if (foundDescendant != null)
			{
				return foundDescendant;
			}
		}

		return null;
	}

	/**
	 * Tries to find the node at the given position. It does try to not return an {@link ITokenNode}, but the node that
	 * contains the {@link ITokenNode}.
	 */
	public static @Nullable ISyntaxNode findNodeAtPosition(int line, int character, ISyntaxTree syntaxTree)
	{
		if (syntaxTree == null)
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
				if (node instanceof IStatementListNode)
				{
					return findNodeAtPosition(line, character, node);
				}
				if (node.descendants().hasItems())
				{
					var descendant = findNodeAtPosition(line, character, node);
					if (descendant != null && !(descendant instanceof ITokenNode))
					{
						return descendant;
					}
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
		while (current != null)
		{
			if (type.isInstance(current))
			{
				return type.cast(current);
			}
			current = current.parent();
		}

		return null;
	}

	public static IVariableNode findLevelOneParentOf(IVariableNode variable)
	{
		var owner = variable.parent();
		while (!(owner instanceof IGroupNode group) || ((IGroupNode) owner).level() > 1)
		{
			owner = ((ISyntaxNode) owner).parent();
		}

		return (IVariableNode) owner;
	}

	public static Optional<IStatementNode> findStatementInLine(int line, IStatementListNode statementList)
	{
		for (var statement : statementList.statements())
		{
			if (statement.diagnosticPosition().line() == line)
			{
				return Optional.of(statement);
			}

			if (statement instanceof IStatementWithBodyNode withBody
				&& withBody.descendants().first().diagnosticPosition().line() <= line
				&& withBody.descendants().last().diagnosticPosition().line() >= line)
			{
				var childStatement = findStatementInLine(line, withBody.body());
				if (childStatement.isPresent())
				{
					return childStatement;
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Scans the node <strong>and all descendants</strong> for the given type of node.<br/>
	 * Returns all found nodes.
	 */
	public static <T extends ISyntaxNode> List<T> findNodesOfType(ISyntaxTree start, Class<T> type)
	{
		var result = new ArrayList<T>();
		if (type.isInstance(start))
		{
			result.add(type.cast(start));
		}

		for (var descendant : start.descendants())
		{
			result.addAll(findNodesOfType(descendant, type));
		}

		return result;
	}
}
