package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.output.IOutputPositioningNode;

class OutputPositioningNode extends BaseSyntaxNode implements IOutputPositioningNode
{
	private int row;
	private int column;

	@Override
	public int row()
	{
		return row;
	}

	@Override
	public int column()
	{
		return column;
	}

	void setRow(int row)
	{
		this.row = row;
	}

	void setColumn(int column)
	{
		this.column = column;
	}
}
