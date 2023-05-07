package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;

import javax.annotation.Nullable;
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
