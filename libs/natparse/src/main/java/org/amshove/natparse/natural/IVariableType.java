package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IVariableType
{
	DataFormat format();
	double length();
	boolean hasDynamicLength();

	/**
	 * Returns the initial value of the variable or the constant variable if isConstant() returns true.
	 * @return
	 * @see IVariableType#isConstant()
	 */
	SyntaxToken initialValue();
	boolean isConstant();
}
