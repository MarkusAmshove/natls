package org.amshove.natls.languageserver.inputstructure;

public class InputColumnPositionElement extends InputResponseElement
{
	private int column;

	protected InputColumnPositionElement(int column)
	{
		super(InputStructureElementKind.COLUMN_POSITION);
		this.column = column;
	}

	public int getColumn()
	{
		return column;
	}

	public void setColumn(int column)
	{
		this.column = column;
	}
}
