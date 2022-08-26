package org.amshove.natparse.natural;

import org.amshove.natparse.IPosition;

import java.nio.file.Path;

public interface ISyntaxNode extends ISyntaxTree
{
	ISyntaxTree parent();

	IPosition position();
	IPosition diagnosticPosition();

	boolean isInFile(Path path);

	/**
	 * Clean up resources that may leak, like references.
	 * Called when a Node is no longer valid.
	 */
	void destroy();
}
