package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISubroutineNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;

import java.util.ArrayList;
import java.util.List;

class SubroutineNode extends StatementWithBodyNode implements ISubroutineNode
{
	private final List<ISymbolReferenceNode> references = new ArrayList<>();
	private SyntaxToken nameToken;

	@Override
	public SyntaxToken declaration()
	{
		return nameToken;
	}

	@Override
	public IPosition position()
	{
		return nameToken;
	}

	void setName(SyntaxToken nameToken)
	{
		this.nameToken = nameToken;
	}

	@Override
	public ReadOnlyList<ISymbolReferenceNode> references()
	{
		return ReadOnlyList.from(references);
	}

	@Override
	public void removeReference(ISymbolReferenceNode node)
	{
		references.remove(node);
	}

	@Override
	public void addReference(ISymbolReferenceNode node)
	{
		references.add(node);
		((InternalPerformNode) node).setReference(this);
	}
}
