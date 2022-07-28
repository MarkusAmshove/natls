package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;

public interface ISystemFunctionNode extends IOperandNode
{
	SyntaxKind systemFunction();
	ReadOnlyList<IOperandNode> parameter();
}
