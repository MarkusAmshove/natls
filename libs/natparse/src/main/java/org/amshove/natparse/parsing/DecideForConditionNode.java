package org.amshove.natparse.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDecideForConditionBranchNode;
import org.amshove.natparse.natural.IDecideForConditionNode;
import org.amshove.natparse.natural.IStatementListNode;

class DecideForConditionNode extends StatementNode implements IDecideForConditionNode
{
	private List<IDecideForConditionBranchNode> branches = new ArrayList<>();
	private IStatementListNode whenAny;
	private IStatementListNode whenAll;
	private IStatementListNode whenNone;

	@Override
	public ReadOnlyList<IDecideForConditionBranchNode> branches()
	{
		return ReadOnlyList.from(branches);
	}

	@Override
	public Optional<IStatementListNode> whenAny()
	{
		return Optional.ofNullable(whenAny);
	}

	@Override
	public Optional<IStatementListNode> whenAll()
	{
		return Optional.ofNullable(whenAll);
	}

	@Override
	public IStatementListNode whenNone()
	{
		return whenNone;
	}

	void addBranch(DecideForConditionBranchNode branch)
	{
		addNode(branch);
		branches.add(branch);
	}

	void setWhenAny(StatementListNode whenAny)
	{
		addNode(whenAny);
		this.whenAny = whenAny;
	}

	void setWhenAll(StatementListNode whenAll)
	{
		addNode(whenAll);
		this.whenAll = whenAll;
	}

	void setWhenNone(StatementListNode whenNone)
	{
		addNode(whenNone);
		this.whenNone = whenNone;
	}

}
