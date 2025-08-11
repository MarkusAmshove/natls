package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IAttributeListNode;
import org.amshove.natparse.natural.IAttributeNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.output.IOutputOperandNode;
import org.jspecify.annotations.Nullable;

class OutputOperandNode extends BaseSyntaxNode implements IOutputOperandNode
{
	private IOperandNode operand;
	private IAttributeListNode attributeListNode;

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	@Override
	@Nullable
	public IAttributeListNode attributeNode()
	{
		return attributeListNode;
	}

	@Override
	public ReadOnlyList<IAttributeNode> attributes()
	{
		return attributeListNode != null ? attributeListNode.attributes() : ReadOnlyList.empty();
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	void setAttributes(IAttributeListNode attributes)
	{
		this.attributeListNode = attributes;
	}
}
