package org.amshove.natparse.natural;

import org.amshove.natparse.IPosition;

public interface ISyntaxNode extends IPosition, ISyntaxTree
{
	// TODO: IPosition as position() ?
	ISyntaxTree parent();
}
