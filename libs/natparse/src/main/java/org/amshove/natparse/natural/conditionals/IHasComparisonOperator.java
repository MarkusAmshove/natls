package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISyntaxNode;

public interface IHasComparisonOperator extends ISyntaxNode
{
	ComparisonOperator operator();

	SyntaxToken comparisonToken();
}
