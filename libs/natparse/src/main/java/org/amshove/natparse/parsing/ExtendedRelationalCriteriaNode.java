package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.conditionals.IExtendedRelationalCriteriaNode;
import org.amshove.natparse.natural.conditionals.IExtendedRelationalCriteriaPartNode;

import java.util.ArrayList;
import java.util.List;

class ExtendedRelationalCriteriaNode extends BaseSyntaxNode implements IExtendedRelationalCriteriaNode
{
	private IOperandNode left;
	private List<IExtendedRelationalCriteriaPartNode> rights = new ArrayList<>();

	ExtendedRelationalCriteriaNode(RelationalCriteriaNode relationalCriteriaNode)
	{
		addNode((BaseSyntaxNode) relationalCriteriaNode.left());
		left = relationalCriteriaNode.left();

		var right = new ExtendedRelationalCriteriaPartNode();
		right.setComparisonToken(relationalCriteriaNode.comparisonToken());
		right.setRhs(relationalCriteriaNode.right());
		addRight(right);
	}

	@Override
	public IOperandNode left()
	{
		return left;
	}

	@Override
	public ReadOnlyList<IExtendedRelationalCriteriaPartNode> rights()
	{
		return ReadOnlyList.from(rights);
	}

	void addRight(IExtendedRelationalCriteriaPartNode right)
	{
		addNode((BaseSyntaxNode) right);
		rights.add(right);
	}
}
