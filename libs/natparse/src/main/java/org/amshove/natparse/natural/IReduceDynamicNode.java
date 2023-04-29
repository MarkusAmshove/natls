package org.amshove.natparse.natural;

public interface IReduceDynamicNode extends IStatementNode
{
	IVariableReferenceNode variableToReduce();

	int sizeToReduceTo();
}
