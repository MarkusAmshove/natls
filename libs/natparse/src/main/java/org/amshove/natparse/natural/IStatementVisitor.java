package org.amshove.natparse.natural;

@FunctionalInterface
public interface IStatementVisitor
{
	void visit(IStatementNode statement);
}
