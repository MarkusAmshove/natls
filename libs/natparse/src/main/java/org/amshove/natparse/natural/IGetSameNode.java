package org.amshove.natparse.natural;

import java.util.Optional;
import org.amshove.natparse.lexing.SyntaxToken;

public interface IGetSameNode extends IStatementNode
{
	Optional<SyntaxToken> label();
}
