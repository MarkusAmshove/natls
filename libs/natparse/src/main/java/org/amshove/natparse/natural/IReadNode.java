package org.amshove.natparse.natural;

public interface IReadNode extends IStatementWithBodyNode, IAdabasAccessStatementNode, ILabelReferencable
{
	ReadSequence readSequence();
}
