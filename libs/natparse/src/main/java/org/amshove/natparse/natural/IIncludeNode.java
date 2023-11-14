package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IIncludeNode extends IModuleReferencingNode
{
	ReadOnlyList<ITokenNode> body();
}
