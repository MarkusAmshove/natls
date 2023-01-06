package org.amshove.natparse.natural;

public interface ITranslateSystemFunctionNode extends ISystemFunctionNode
{
	IOperandNode toTranslate();

	boolean isToUpper();
}
