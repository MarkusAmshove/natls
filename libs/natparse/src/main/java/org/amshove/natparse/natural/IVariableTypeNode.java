package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IVariableTypeNode
{
	DataFormat format();
	double length();
	boolean hasDynamicLength();

	/**
	 * Returns the initial value of the variable or the constant variable if isConstant() returns true.
	 * @return
	 * @see IVariableTypeNode#isConstant()
	 */
	SyntaxToken initialValue();
	boolean isConstant();

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
