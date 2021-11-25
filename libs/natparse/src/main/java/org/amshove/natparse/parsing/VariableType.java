package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IVariableType;

class VariableType implements IVariableType
{
	private boolean hasDynamicLength;
	private DataFormat format;
	private double length;

	@Override
	public DataFormat format()
	{
		return format;
	}

	@Override
	public double length()
	{
		return length;
	}

	@Override
	public boolean hasDynamicLength()
	{
		return hasDynamicLength;
	}

	void setDynamicLength()
	{
		hasDynamicLength = true;
	}

	void setFormat(DataFormat format)
	{
		this.format = format;
	}

	void setLength(double length)
	{
		this.length = length;
	}
}
