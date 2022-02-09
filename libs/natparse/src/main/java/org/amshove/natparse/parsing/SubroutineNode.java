package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.ISubroutineNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

class SubroutineNode extends StatementListNode implements ISubroutineNode
{
	private final List<ISymbolReferenceNode> references = new ArrayList<>();
	private IStatementListNode body;
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

	@Override
	public IStatementListNode body()
	{
		return body;
	}

	void setName(SyntaxToken nameToken)
	{
		this.nameToken = nameToken;
	}

	void setBody(IStatementListNode statementListNode)
	{
		for (var statement : statementListNode.statements())
		{
			addNode((BaseSyntaxNode) statement);
			addStatement((StatementNode) statement);
		}
		body = statementListNode;
	}

	@Nonnull
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

	void addReference(ISymbolReferenceNode node)
	{
		references.add(node);
		((InternalPerformNode) node).setReference(this);
	}
}
