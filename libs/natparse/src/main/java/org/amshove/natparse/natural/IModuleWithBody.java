package org.amshove.natparse.natural;

public interface IModuleWithBody
{
	IStatementListNode body();

	void acceptStatementVisitor(IStatementVisitor visitor);
}
