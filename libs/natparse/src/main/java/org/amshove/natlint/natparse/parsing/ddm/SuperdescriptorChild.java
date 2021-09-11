package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.natural.ddm.IDdmField;
import org.amshove.natlint.natparse.natural.ddm.ISuperdescriptorChild;

class SuperdescriptorChild implements ISuperdescriptorChild
{
	private IDdmField field;
	private final String name;
	private final int rangeFrom;
	private final int rangeTo;

	public SuperdescriptorChild(String name, int rangeFrom, int rangeTo)
	{
		this.name = name;
		this.rangeFrom = rangeFrom;
		this.rangeTo = rangeTo;
	}

	public String name()
	{
		return name;
	}

	public IDdmField field()
	{
		return field;
	}

	void setField(IDdmField field)
	{
		this.field = field;
	}

	public int rangeFrom()
	{
		return rangeFrom;
	}

	public int rangeTo()
	{
		return rangeTo;
	}
}
