package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;

import java.util.Optional;

public interface IEscapeNode extends IStatementNode
{
	SyntaxKind escapeDirection();
	boolean isReposition();
	boolean isImmediate();
	Optional<SyntaxToken> label();
}
