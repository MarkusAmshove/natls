package org.amshove.natparse.natural.output;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IAttributeListNode;
import org.amshove.natparse.natural.IAttributeNode;
import org.amshove.natparse.natural.IOperandNode;

import javax.annotation.Nullable;

public interface IOutputOperandNode extends IOutputElementNode
{
	IOperandNode operand();

	@Nullable
	IAttributeListNode attributeNode();

	ReadOnlyList<IAttributeNode> attributes();
}
