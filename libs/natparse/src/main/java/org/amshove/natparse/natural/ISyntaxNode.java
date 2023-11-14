package org.amshove.natparse.natural;

import org.amshove.natparse.IPosition;

import java.nio.file.Path;

public interface ISyntaxNode extends ISyntaxTree
{
	ISyntaxNode parent();

	IPosition position();

	IPosition diagnosticPosition();

	default boolean enclosesPosition(int line, int column)
	{
		var firstDescendant = descendants().first().diagnosticPosition();
		var lastDescendant = descendants().last().diagnosticPosition();

		if (line > lastDescendant.line() || line < firstDescendant.line())
		{
			return false;
		}

		if (firstDescendant.line() < line && lastDescendant.line() > line)
		{
			return true;
		}

		if (firstDescendant.line() == line && firstDescendant.offsetInLine() <= column && firstDescendant.endOffset() >= column)
		{
			return true;
		}

		return lastDescendant.line() == line && lastDescendant.endOffset() >= column;
	}

	boolean isInFile(Path path);

	/**
	 * Clean up resources that may leak, like references. Called when a Node is no longer valid.
	 */
	void destroy();
}
