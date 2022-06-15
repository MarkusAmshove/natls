package org.amshove.natls.languageserver;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;
import org.eclipse.lsp4j.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LspUtil
{
	private LspUtil()
	{
	}

	public static String readUnchecked(Path filepath)
	{
		try
		{
			return Files.readString(filepath);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public static Path uriToPath(String uri)
	{
		return Paths.get(URI.create(uri));
	}

	public static String pathToUri(Path path)
	{
		return path.toUri().toString();
	}

	public static Location toLocation(String fileUri, SyntaxToken token)
	{
		return new Location(
			fileUri,
			toRange(token)
		);
	}

	public static Diagnostic toLspDiagnostic(String sourceTool, IDiagnostic diagnostic)
	{
		return new Diagnostic(
			new Range(
				new Position(diagnostic.line(), diagnostic.offsetInLine()),
				new Position(diagnostic.line(), diagnostic.offsetInLine() + diagnostic.length())
			),
			diagnostic.message(),
			mapSeverity(diagnostic),
			sourceTool,
			diagnostic.id()
		);
	}

	private static DiagnosticSeverity mapSeverity(IDiagnostic diagnostic)
	{
		return switch (diagnostic.severity())
			{
				case ERROR -> DiagnosticSeverity.Error;
				case WARNING -> DiagnosticSeverity.Warning;
				case INFO -> DiagnosticSeverity.Information;
			};
	}

	public static Range toRange(Position position)
	{
		return new Range(
				position,
				position
		);
	}

	public static Range toRange(IPosition position)
	{
		return new Range(
			new Position(position.line(), position.offsetInLine()),
			new Position(position.line(), position.offsetInLine() + position.length())
		);
	}

	public static Range toRange(SyntaxToken token)
	{
		return toRange((IPosition) token);
	}

	public static Range toRange(ISyntaxNode node)
	{
		var firstNode = node.descendants().first();
		var lastNode = node.descendants().last();
		return new Range(
			new Position(firstNode.position().line(), firstNode.position().offsetInLine()),
			new Position(lastNode.position().line(), lastNode.position().offsetInLine() + lastNode.position().length())
		);
	}

	public static Location toLocation(ISyntaxNode node)
	{
		var position = node.position();
		return toLocation(position, node.descendants().last().position());
	}

	public static Location toLocation(IPosition position)
	{
		return new Location(
			pathToUri(position.filePath()),
			new Range(
				new Position(position.line(), position.offsetInLine()),
				new Position(position.line(), position.offsetInLine() + position.length())
			)
		);
	}

	public static Location toLocation(IPosition position, IPosition endPosition)
	{
		return new Location(
			pathToUri(position.filePath()),
			new Range(
				new Position(position.line(), position.offsetInLine()),
				new Position(endPosition.line(), endPosition.offsetInLine())
			)
		);
	}

	public static boolean isInSameLine(Range first, Range second)
	{
		return first.getStart().getLine() == second.getStart().getLine();
	}

	public static Location toLocation(INaturalModule module)
	{
		return new Location(
			module.file().getPath().toUri().toString(),
			new Range(
				new Position(0, 0),
				new Position(0, 0)
			)
		);
	}

	public static Range toSingleRange(int line, int column)
	{
		var position = new Position(line, column);
		return new Range(position, position);
	}

	public static Range lineRange(int line)
	{
		return new Range(
			new Position(line, 0),
			new Position(line + 1, 0)
		);
	}
}
