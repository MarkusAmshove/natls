package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IVariableTypeNode;

class VariableTypeNode implements IVariableTypeNode
{
	static final int ONE_GIGABYTE = 1073741824;


	private boolean hasDynamicLength;
	private DataFormat format;
	private double length;
	private SyntaxToken initialValue;
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
	public SyntaxToken initialValue()
	{
		return initialValue;
	}

	@Override
	public boolean isConstant()
	{
		return isConstant;
	}

	@Override
	public int byteSize()
	{
		return switch (format)
			{
				case ALPHANUMERIC, BINARY -> hasDynamicLength
					? ONE_GIGABYTE // max internal length in bytes
					: (int)length;
				case FLOAT, INTEGER -> (int) length;
				case CONTROL -> 2;
				case DATE -> 4;
				case LOGIC -> 1;
				case NUMERIC -> calculateNumericSize();
				case PACKED -> calculatePackedSize();
				case TIME -> 7;
				case UNICODE -> hasDynamicLength()
					? ONE_GIGABYTE // max internal length in bytes
					: Math.max((int) length, 2);
				case NONE -> 0;
			};
	}

	@Override
	public boolean fitsInto(IVariableTypeNode other)
	{
		return this.byteSize() <= other.byteSize();
	}

	@Override
	public int sumOfDigits()
	{
		return calculateNumericSize();
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

	void setInitialValue(SyntaxToken token)
	{
		initialValue = token;
	}

	void setConstant()
	{
		isConstant = true;
	}

	private int calculateNumericSize()
	{
		var digitsBeforeDecimalPoint = (int) length;
		var digitsAfterDecimalPoint = calculateDigitsAfterDecimalPoint();

		return Math.max(1, digitsBeforeDecimalPoint + digitsAfterDecimalPoint);
	}

	private int calculatePackedSize()
	{
		var numericSize = calculateNumericSize() / 2;
		if(numericSize % 2 != 0)
		{
			numericSize++;
		}

		return Math.max(1, numericSize);
	}

	private int calculateDigitsAfterDecimalPoint()
	{
		return Integer.parseInt((Double.toString(length).split("\\.")[1]));
	}
}
