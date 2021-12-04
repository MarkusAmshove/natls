package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

import java.text.DecimalFormat;

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
			var decimalFormat = new DecimalFormat("#.#");
			details += "%s".formatted(decimalFormat.format(length()));
		}
		details += ")";

		if(hasDynamicLength())
		{
			details += " DYNAMIC";
		}

		return details;
	}
}
