package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IAdabasIndexAccess extends IOperandNode
{
	ReadOnlyList<IOperandNode> operands();
}
