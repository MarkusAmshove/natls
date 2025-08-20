package org.amshove.natparse.natural;

public interface IForLoopNode extends IStatementWithBodyNode, IMutateVariables, ILabelReferencable
{
	IVariableReferenceNode loopControl();

	IOperandNode upperBound();
}
