package org.amshove.natparse.natural;

public interface ILiteralNode extends ITokenNode, IOperandNode
{
	IDataType dataType();
}
