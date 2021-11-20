package org.amshove.natparse.natural;

import org.amshove.natparse.IPosition;

public interface ISyntaxNode extends IPosition, ISyntaxTree
{
	ISyntaxTree parent();
}
