package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.output.ISpaceElementNode;

class SpaceElementNode extends BaseSyntaxNode implements ISpaceElementNode
{
	private int spaces;

	@Override
	public int spaces()
	{
		return spaces;
	}

	void setSpaces(int spaces)
	{
		this.spaces = spaces;
	}
}
