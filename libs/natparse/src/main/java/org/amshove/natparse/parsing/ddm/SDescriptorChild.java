package org.amshove.natparse.parsing.ddm;

import org.amshove.natparse.natural.ddm.IDdmField;
import org.amshove.natparse.natural.ddm.ISDescriptorChild;

class SDescriptorChild implements ISDescriptorChild
{
	private IDdmField field;
	private final String name;
	private final int rangeFrom;
	private final int rangeTo;

	public SDescriptorChild(String name, int rangeFrom, int rangeTo)
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
