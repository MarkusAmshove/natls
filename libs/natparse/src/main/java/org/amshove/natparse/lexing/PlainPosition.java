package org.amshove.natparse.lexing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.natural.ISyntaxNode;

import java.nio.file.Path;

public record PlainPosition(int offset, int offsetInLine, int line, int length, Path filePath) implements IPosition
{
	public static PlainPosition spanning(IPosition startPosition, IPosition endPosition)
	{
		return new PlainPosition(
			startPosition.offset(),
			startPosition.offsetInLine(),
			startPosition.line(),
			endPosition.totalEndOffset() - startPosition.offset(),
			startPosition.filePath()
		);
	}

	public static PlainPosition spanning(ISyntaxNode start, ISyntaxNode end)
	{
		return spanning(start.position(), end.position());
	}
}
