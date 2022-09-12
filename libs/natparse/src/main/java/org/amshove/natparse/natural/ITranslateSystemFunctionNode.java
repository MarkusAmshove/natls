package org.amshove.natparse.natural;

public interface ITranslateSystemFunctionNode extends ISystemFunctionNode
{
	IVariableReferenceNode toTranslate();
	boolean isToUpper();
}
