package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IVariableTypeNode
{
	DataFormat format();

	/**
	 * Returns the `literal` length as defined in the source, e.g. 2 for A2.
	 */
	double length();
	boolean hasDynamicLength();

	/**
	 * Returns the initial value of the variable or the constant variable if isConstant() returns true.
	 * @see IVariableTypeNode#isConstant()
	 */
	SyntaxToken initialValue();
	boolean isConstant();

	/**
	 * Returns the actual size in bytes.
	 */
	int byteSize();

	/**
	 * Returns the sum of all digits. For example 9 for N7,2
	 */
	int sumOfDigits();

	boolean fitsInto(IVariableTypeNode other);

    default String toShortString()
	{
		var details = "";

		details += "(%s".formatted(format().identifier());
		if(length() > 0.0)
		{
			details += "%s".formatted(DataFormat.formatLength(length()));
		}
		details += ")";

		if(hasDynamicLength())
		{
			details += " DYNAMIC";
		}

		return details;
	}
}
