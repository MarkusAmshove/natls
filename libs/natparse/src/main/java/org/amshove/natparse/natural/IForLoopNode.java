package org.amshove.natparse.natural;

public interface IForLoopNode extends IStatementWithBodyNode, IMutateVariables
{
	IVariableReferenceNode loopControl();

	IOperandNode upperBound();
}
