package org.amshove.natparse;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;

import javax.annotation.Nullable;

import java.nio.file.Path;
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

	public static SyntaxToken findTokenOnOrBeforePosition(List<SyntaxToken> tokens, int line, int column)
	{
		SyntaxToken lastToken = null;
		for (var token : tokens)
		{
			if (token.line() > line)
			{
				break;
			}
			if (token.line() == line && token.offsetInLine() > column)
			{
				break;
			}

			lastToken = token;
		}

		return lastToken;
	}

	/**
	 * Checks whether the module contains the given node. The comparison is done by the DiagnosticPosition, which is
	 * e.g. the copy code name in an INCLUDE.
	 */
	public static boolean moduleContainsNodeByDiagnosticPosition(INaturalModule module, ISyntaxNode node)
	{
		return node.position() != null
			&& node.position().filePath() != null
			&& module.file().getPath().equals(node.diagnosticPosition().filePath());
	}

	public static @Nullable ISyntaxNode findNodeAtPosition(int line, int character, INaturalModule module)
	{
		return findNodeAtPosition(module.file().getPath(), line, character, module.syntaxTree());
	}

	/**
	 * Tries to find the first node with a subtype of {@link ITokenNode} at the given position.<br/>
	 * Can be used to prefer finding {@link ISymbolReferenceNode}, {@link IVariableReferenceNode} etc.
	 */
	public static @Nullable ITokenNode findTokenNodeAtPosition(Path filePath, int line, int character, ISyntaxTree syntaxTree)
	{
		if (syntaxTree == null)
		{
			return null;
		}

		for (var node : syntaxTree)
		{
			if (!node.position().filePath().equals(filePath))
			{
				continue;
			}

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

			var foundDescendant = findTokenNodeAtPosition(filePath, line, character, node);
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
	public static @Nullable ISyntaxNode findNodeAtPosition(Path filePath, int line, int character, ISyntaxTree syntaxTree)
	{
		if (syntaxTree == null)
		{
			return null;
		}

		ISyntaxNode previousNode = null;

		for (var node : syntaxTree)
		{
			if (!node.position().filePath().equals(filePath))
			{
				continue;
			}

			if (node.position().line() == line && node.position().offsetInLine() == character)
			{
				if (node instanceof IStatementListNode statementListNode)
				{
					var descendantFoundNode = findNodeAtPosition(filePath, line, character, statementListNode);
					if (descendantFoundNode != null)
					{
						return descendantFoundNode;
					}
				}
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
					return findNodeAtPosition(filePath, line, character, node);
				}
				if (node.descendants().hasItems())
				{
					var descendant = findNodeAtPosition(filePath, line, character, node);
					if (descendant != null && !(descendant instanceof ITokenNode))
					{
						return descendant;
					}
				}
				return node;
			}

			if (node.position().line() > line)
			{
				return findNodeAtPosition(filePath, line, character, previousNode);
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
			return findNodeAtPosition(filePath, line, character, previousNode);
		}

		if (previousNode != null
			&& previousNode.position().line() == line)
		{
			return findNodeAtPosition(filePath, line, character, previousNode);
		}

		return null;
	}

	/**
	 * Searches for a node of a given type. The start node itself is also checked.<br/>
	 * If the node itself is not of the given type, the search will continue "upwards" through parents.
	 */
	@Nullable
	public static <T extends ISyntaxNode> T findNodeOfTypeUpwards(ISyntaxNode start, Class<T> type)
	{
		if (type.isInstance(start))
		{
			return type.cast(start);
		}

		return findFirstParentOfType(start, type);
	}

	@Nullable
	public static <T extends ISyntaxNode> T findFirstParentOfType(ISyntaxNode start, Class<T> type)
	{
		if (start == null)
		{
			return null;
		}

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

	public static Optional<IStatementNode> findStatementInLine(Path filePath, int line, IStatementListNode statementList)
	{
		for (var statement : statementList.statements())
		{
			if (!statement.position().filePath().equals(filePath))
			{
				continue;
			}

			if (statement.diagnosticPosition().line() == line)
			{
				return Optional.of(statement);
			}

			if (statement instanceof IStatementWithBodyNode withBody
				&& withBody.descendants().first().diagnosticPosition().line() <= line
				&& withBody.descendants().last().diagnosticPosition().line() >= line)
			{
				var childStatement = findStatementInLine(filePath, line, withBody.body());
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

	/**
	 * Returns the leaf that is traversed as the deepest from the starting point.<br/>
	 * This will return the last descendant of its descendant of its descendant ...
	 */
	public static <T extends ISyntaxNode> ISyntaxNode deepFindLeaf(T start)
	{
		if (start.descendants().isEmpty())
		{
			return start;
		}

		return deepFindLeaf(start.descendants().last());
	}

	public static ReadOnlyList<IStatementNode> findEnclosedStatements(Path path, IStatementListNode body, int startLine, int endLine)
	{
		var statements = new ArrayList<IStatementNode>();
		for (var i = startLine; i <= endLine; i++)
		{
			findStatementInLine(path, i, body).ifPresent(statements::add);
		}

		return ReadOnlyList.from(statements);
	}

	public static ITokenNode findTokenNodeForToken(SyntaxToken token, ISyntaxTree tree)
	{
		for (var node : tree)
		{
			if (node instanceof ITokenNode tokenNode && tokenNode.token() == token)
			{
				return tokenNode;
			}

			if (node.descendants().hasItems())
			{
				var maybeNode = findTokenNodeForToken(token, node);
				if (maybeNode != null)
				{
					return maybeNode;
				}
			}
		}

		return null;
	}

	public static boolean containsTokenWithKind(ISyntaxNode node, SyntaxKind kind)
	{
		for (var descendant : node.descendants())
		{
			if (descendant instanceof ITokenNode tokenNode && tokenNode.token().kind() == kind)
			{
				return true;
			}
		}

		return false;
	}

	public static <T extends IStatementNode> T findFirstStatementOfType(Class<T> statementType, ISyntaxTree tree)
	{
		if (statementType.isInstance(tree))
		{
			return statementType.cast(tree);
		}

		for (var descendant : tree.descendants())
		{
			var foundDescendant = findFirstStatementOfType(statementType, descendant);
			if (foundDescendant != null)
			{
				return foundDescendant;
			}
		}

		return null;
	}
}
