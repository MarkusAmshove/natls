package org.amshove.natparse.natural;

public interface IRangedArrayAccessNode extends IOperandNode
{
	IOperandNode lowerBound();

	IOperandNode upperBound();

	boolean isAnyUnbound();
}
