package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface ISyntaxTree
{
	ReadOnlyList<? extends ISyntaxNode> nodes();
}
