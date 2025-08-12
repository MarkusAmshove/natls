package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

public interface ISyntaxTree extends Iterable<ISyntaxNode>
{
	ReadOnlyList<? extends ISyntaxNode> descendants();

	@Nullable
	@SuppressWarnings("unchecked")
	default <T extends ISyntaxNode> T findDescendantOfType(Class<T> type)
	{
		for (var node : descendants())
		{
			if (type.isAssignableFrom(node.getClass()))
			{
				return (T) node;
			}
		}

		return null;
	}

	@Nullable
	default ITokenNode findDescendantToken(SyntaxKind kind)
	{
		for (var node : descendants())
		{
			if (node instanceof ITokenNode tokenNode)
			{
				if (tokenNode.token().kind() == kind)
				{
					return tokenNode;
				}
			}
		}

		return null;
	}

	/**
	 * Checks if this node contains the other node, meaning that the other node is a descendant of this node.
	 *
	 * @param other The node to check for containment.
	 * @return true if this node contains the other node, false otherwise.
	 */
	default boolean contains(ISyntaxNode other)
	{
		var parent = other.parent();
		while (parent != null)
		{
			if (parent == this)
			{
				return true;
			}
			parent = parent.parent();
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	default <T extends ISyntaxNode> Stream<T> directDescendantsOfType(Class<T> type)
	{
		return descendants().stream()
			.filter(n -> type.isAssignableFrom(n.getClass()))
			.map(n -> (T) n);
	}

	void acceptNodeVisitor(ISyntaxNodeVisitor visitor);

	void acceptStatementVisitor(IStatementVisitor visitor);
}
