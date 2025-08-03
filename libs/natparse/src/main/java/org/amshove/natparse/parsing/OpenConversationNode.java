package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOpenConversationNode;
import org.amshove.natparse.natural.IOperandNode;

class OpenConversationNode extends StatementNode implements IOpenConversationNode
{
	private ReadOnlyList<IOperandNode> subprograms = ReadOnlyList.empty();

	@Override
	public ReadOnlyList<IOperandNode> subprograms()
	{
		return subprograms;
	}

	void setSubprograms(ReadOnlyList<IOperandNode> subprograms)
	{
		this.subprograms = subprograms;
	}
}
