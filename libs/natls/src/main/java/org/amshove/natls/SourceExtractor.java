package org.amshove.natls;

import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public class SourceExtractor
{
	/**
	 * Extracts the source of the node. If the node is not a {@link ITokenNode}, then the complete source of the
	 * descendants will be taken, including whitespace.
	 */
	public static String extractSource(ISyntaxNode node)
	{
		try
		{
			if (node instanceof ITokenNode)
			{
				var lines = Files.readAllLines(node.position().filePath());
				for (var line = 0; line < lines.size(); line++)
				{
					if (line == node.position().line())
					{
						return lines.get(line).substring(node.position().offsetInLine(), node.position().endOffset());
					}
				}
			}
			else
			{
				var source = Files.readString(node.position().filePath());
				var startOffset = node.descendants().first().position().offset();
				var endOffset = node.descendants().last().position().totalEndOffset();
				return source.substring(startOffset, endOffset);
			}

			throw new IllegalStateException("Could not extract source");
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private SourceExtractor()
	{}
}
