package org.amshove.natparse.parsing;

import java.util.ArrayList;
import java.util.List;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISelectNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class SelectNode extends StatementWithBodyNode implements ISelectNode
{
	private final List<IVariableReferenceNode> views = new ArrayList<>();
	private final List<IVariableReferenceNode> viewCorrelations = new ArrayList<>();

	@Override
	public ReadOnlyList<IVariableReferenceNode> views()
	{
		return ReadOnlyList.from(views);
	}

	@Override
	public ReadOnlyList<IVariableReferenceNode> viewCorrelations()
	{
		return ReadOnlyList.from(viewCorrelations);
	}

	void addView(IVariableReferenceNode view, IVariableReferenceNode viewCorrelation)
	{
		views.add(view);
		viewCorrelations.add(viewCorrelation);
	}

}
