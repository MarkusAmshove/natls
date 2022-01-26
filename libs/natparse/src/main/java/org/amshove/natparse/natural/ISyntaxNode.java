package org.amshove.natparse.natural;

import org.amshove.natparse.IPosition;

public interface ISyntaxNode extends ISyntaxTree
{
	ISyntaxTree parent();

	IPosition position();

	/**
	 * Clean up resources that may leak, like references.
	 * Called when a Node is no longer valid.
	 */
	void destroy();
}
