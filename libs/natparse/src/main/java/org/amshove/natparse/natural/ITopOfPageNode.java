package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

import java.util.Optional;

public interface ITopOfPageNode extends IStatementWithBodyNode
{
	Optional<SyntaxToken> reportSpecification();
}
