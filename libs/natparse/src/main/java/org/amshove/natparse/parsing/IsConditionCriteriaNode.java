package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.conditionals.IIsConditionCriteriaNode;

class IsConditionCriteriaNode extends BaseSyntaxNode implements IIsConditionCriteriaNode
{
	private IOperandNode left;
	private SyntaxToken checkedType;
	
	@Override
	public IOperandNode left()
	{
		return left;
	}

	@Override
	public SyntaxToken checkedType()
	{
		return checkedType;
	}

	void setCheckedType(SyntaxToken checkedType)
	{
		this.checkedType = checkedType;
	}

	void setLeft(IOperandNode left)
	{
		this.left = left;
	}
}
