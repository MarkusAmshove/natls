package org.amshove.natparse.natural;

public interface IVariableType extends IDataType
{
	/**
	 * Returns the initial value of the variable or the constant variable if isConstant() returns true.
	 * 
	 * @see IVariableType#isConstant()
	 */
	IOperandNode initialValue();

	boolean isConstant();
}
