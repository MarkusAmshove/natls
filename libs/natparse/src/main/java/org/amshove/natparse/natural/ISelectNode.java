package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface ISelectNode extends IStatementWithBodyNode
{
	ReadOnlyList<IVariableReferenceNode> views();
    ReadOnlyList<IVariableReferenceNode> viewCorrelations();
    //ReadOnlyList<IOperandNode> columns();
    //ReadOnlyList<IConditionNode> where();
}
