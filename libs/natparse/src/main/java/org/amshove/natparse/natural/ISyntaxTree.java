package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;

import javax.annotation.Nullable;

public interface ISyntaxTree
{
	ReadOnlyList<? extends ISyntaxNode> nodes();

	@Nullable
	@SuppressWarnings("unchecked")
	default <T extends ISyntaxNode> T findDirectChildOfType(Class<T> type)
	{
		for (var node : nodes())
		{
			if (type.isAssignableFrom(node.getClass()))
			{
				return (T) node;
			}
		}

		return null;
	}

	@Nullable
	default ITokenNode findDirectChildSyntaxToken(SyntaxKind kind)
	{
		for (var node : nodes())
		{
			if (node instanceof ITokenNode)
			{
				var tokenNode = (ITokenNode) node;
				if (tokenNode.token().kind() == kind)
				{
					return tokenNode;
				}
			}
		}

		return null;
	}
}
