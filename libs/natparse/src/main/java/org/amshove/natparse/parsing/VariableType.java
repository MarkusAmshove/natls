package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IVariableType;

class VariableType implements IVariableType
{

	private boolean hasDynamicLength;
	private DataFormat format;
	private double length;
	private IOperandNode initialValue;
	private boolean isConstant = false;

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

	@Override
	public IOperandNode initialValue()
	{
		return initialValue;
	}

	@Override
	public boolean isConstant()
	{
		return isConstant;
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

	void setInitialValue(IOperandNode node)
	{
		initialValue = node;
	}

	void setConstant()
	{
		isConstant = true;
	}

}
