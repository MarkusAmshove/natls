package org.amshove.natparse.natural;

public interface IDefineWorkFileNode extends IStatementNode
{
	ILiteralNode number();
	IOperandNode path();
	IOperandNode type();
	IOperandNode attributes();
}
