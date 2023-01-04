package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IVariableType extends IDataType
{
	/**
	 * Returns the initial value of the variable or the constant variable if isConstant() returns true.
	 * 
	 * @see IVariableType#isConstant()
	 */
	SyntaxToken initialValue();

	boolean isConstant();
}
