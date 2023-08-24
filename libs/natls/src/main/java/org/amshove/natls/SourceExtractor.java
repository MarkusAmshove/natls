package org.amshove.natls;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SourceExtractor
{
	/**
	 * Extracts the source of the node. If the node is not a {@link ITokenNode}, then the complete source of the
	 * descendants will be taken, including whitespace.
	 */
	public static String extractSource(ISyntaxNode node)
	{
		if (node instanceof ITokenNode)
		{
			return extractLine(node.position()).substring(node.position().offsetInLine(), node.position().endOffset());
		}
		else
		{
			var source = readWholeFile(node.position().filePath());
			var startOffset = node.descendants().first().position().offset();
			var endOffset = node.descendants().last().position().totalEndOffset();
			return source.substring(startOffset, endOffset);
		}
	}

	public static String extractLine(IPosition position)
	{
		return readLine(position);
	}

	private static String readLine(IPosition position)
	{
		var lines = readAllLinesOfFile(position.filePath());
		for (var line = 0; line < lines.size(); line++)
		{
			if (line == position.line())
			{
				return lines.get(line);
			}
		}

		throw new IllegalStateException("Could not extract line. File has %d lines but requested %d".formatted(lines.size(), position.line()));
	}

	private static List<String> readAllLinesOfFile(Path path)
	{
		try
		{
			return Files.readAllLines(path);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private static String readWholeFile(Path path)
	{
		try
		{
			return Files.readString(path);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private SourceExtractor()
	{}
}
