package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

import javax.annotation.Nullable;

// TODO(labels): Use this interface for statements that can be labeled

/**
 * Statements implementing this interface can be labeled with a
 * {@link org.amshove.natparse.lexing.SyntaxKind::LABEL_IDENTIFIER}
 */
public interface ILabelReferencable
{
	/**
	 * Returns the label token that a statement has been labeled with. Returns null if not labeled.
	 * 
	 * @return the label token or null if not labeled
	 */
	@Nullable
	SyntaxToken labelIdentifier();
}
