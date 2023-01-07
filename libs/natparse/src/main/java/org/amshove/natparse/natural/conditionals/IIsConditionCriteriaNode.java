package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IOperandNode;

public non-sealed interface IIsConditionCriteriaNode extends ILogicalConditionCriteriaNode
{
	IOperandNode left();

	SyntaxToken checkedType();
}
