package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;

import java.util.Optional;
import java.util.stream.Stream;

public interface ISyntaxTree extends Iterable<ISyntaxNode>
{
	ReadOnlyList<? extends ISyntaxNode> descendants();

	@SuppressWarnings("unchecked")
	default <T extends ISyntaxNode> Optional<T> findDescendantOfType(Class<T> type)
	{
		for (var node : descendants())
		{
			if (type.isAssignableFrom(node.getClass()))
			{
				return Optional.of((T) node);
			}
		}

		return Optional.empty();
	}

	default Optional<ITokenNode> findDescendantToken(SyntaxKind kind)
	{
		for (var node : descendants())
		{
			if (node instanceof ITokenNode tokenNode)
			{
				if (tokenNode.token().kind() == kind)
				{
					return Optional.of(tokenNode);
				}
			}
		}

		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	default <T extends ISyntaxNode> Stream<T> directDescendantsOfType(Class<T> type)
	{
		return descendants().stream()
			.filter(n -> type.isAssignableFrom(n.getClass()))
			.map(n -> (T)n);
	}

	void accept(ISyntaxNodeVisitor visitor);
}
