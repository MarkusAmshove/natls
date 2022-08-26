package org.amshove.natparse.natural;

@FunctionalInterface
public interface ISyntaxNodeVisitor
{
	void visit(ISyntaxNode node);
}
