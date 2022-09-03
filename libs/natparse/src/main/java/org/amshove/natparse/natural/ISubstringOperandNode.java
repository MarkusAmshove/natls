package org.amshove.natparse.natural;

// TODO: (type-checking) check operand for typ(A, B, U)
public interface ISubstringOperandNode extends IOperandNode
{
	IOperandNode operand();
	IOperandNode startPosition();
	IOperandNode length();
}
