package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

import javax.annotation.Nullable;

public interface IOutputOperandNode extends IOutputElementNode
{
	IOperandNode operand();

	@Nullable
	IAttributeListNode attributeNode();

	ReadOnlyList<IAttributeNode> attributes();
}
