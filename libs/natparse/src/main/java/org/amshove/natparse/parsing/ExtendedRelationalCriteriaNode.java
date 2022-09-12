package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.conditionals.IExtendedRelationalCriteriaNode;

import java.util.ArrayList;
import java.util.List;

class ExtendedRelationalCriteriaNode extends BaseSyntaxNode implements IExtendedRelationalCriteriaNode
{
	private IOperandNode left;
	private List<IOperandNode> rights = new ArrayList<>();

	ExtendedRelationalCriteriaNode(RelationalCriteriaNode relationalCriteriaNode)
	{
		copyFrom(relationalCriteriaNode);
		left = relationalCriteriaNode.left();
		rights.add(relationalCriteriaNode.right());
	}

	@Override
	public IOperandNode left()
	{
		return left;
	}

	@Override
	public ReadOnlyList<IOperandNode> rights()
	{
		return ReadOnlyList.from(rights);
	}

	void addRight(IOperandNode right)
	{
		rights.add(right);
	}
}
