package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IIncludeNode;
import org.amshove.natparse.natural.ITokenNode;

class IncludeNode extends ModuleReferencingNode implements IIncludeNode
{
	private ReadOnlyList<ITokenNode> body;

	@Override
	public ReadOnlyList<ITokenNode> body()
	{
		return body;
	}

	void setBody(ReadOnlyList<ITokenNode> body)
	{
		this.body = body;
	}
}
