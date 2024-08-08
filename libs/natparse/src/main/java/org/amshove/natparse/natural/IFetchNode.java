package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public interface IFetchNode extends IStatementNode, IModuleReferencingNode
{
	default boolean isFetchReturn()
	{
		return findDescendantToken(SyntaxKind.RETURN) != null;
	}
}
