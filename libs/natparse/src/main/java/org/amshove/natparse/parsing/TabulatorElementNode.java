package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.output.ITabulatorElementNode;

class TabulatorElementNode extends BaseSyntaxNode implements ITabulatorElementNode
{
	private int tabs;

	@Override
	public int tabs()
	{
		return tabs;
	}

	void setTabs(int tabs)
	{
		this.tabs = tabs;
	}
}
