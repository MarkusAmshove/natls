package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IAttributeListNode;
import org.amshove.natparse.natural.IAttributeNode;

import java.util.ArrayList;
import java.util.List;

class AttributeListNode extends BaseSyntaxNode implements IAttributeListNode
{
	private final List<IAttributeNode> attributes = new ArrayList<>();

	@Override
	public ReadOnlyList<IAttributeNode> attributes()
	{
		return ReadOnlyList.from(attributes);
	}

	void addAttribute(IAttributeNode attribute)
	{
		addNode((BaseSyntaxNode) attribute);
		attributes.add(attribute);
	}
}
