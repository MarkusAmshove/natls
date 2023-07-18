package org.amshove.natparse.natural;

public interface IForLoopNode extends IStatementWithBodyNode, IMutateVariables
{
	IOperandNode upperBound();
}
