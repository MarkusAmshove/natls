package org.amshove.natls;

import org.amshove.natparse.natural.ISyntaxNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public class SourceExtractor
{
	public static String extractSource(ISyntaxNode node)
	{
		try
		{
			var lines = Files.readAllLines(node.position().filePath());
			for (var line = 0; line < lines.size(); line++)
			{
				if (line == node.position().line())
				{
					return lines.get(line).substring(node.position().offsetInLine(), node.position().endOffset());
				}
			}

			throw new IllegalStateException("Could not extract source");
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private SourceExtractor() {}
}
