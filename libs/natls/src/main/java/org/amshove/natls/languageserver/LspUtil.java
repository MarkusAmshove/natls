package org.amshove.natls.languageserver;

import org.amshove.natls.DiagnosticOriginalUri;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;
import org.eclipse.lsp4j.*;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LspUtil
{
	private LspUtil()
	{}

	public static Path uriToPath(String uri)
	{
		return Paths.get(URI.create(uri));
	}

	public static String pathToUri(Path path)
	{
		return path.toUri().toString();
	}

	public static Position toPosition(IPosition position)
	{
		return new Position(
			position.line(),
			position.offsetInLine()
		);
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
		var positions = new DiagnosticOriginalUri(pathToUri(diagnostic.originalPosition().filePath()));
		var lspDiagnostic = new Diagnostic(
			new Range(
				new Position(diagnostic.line(), diagnostic.offsetInLine()),
				new Position(diagnostic.line(), diagnostic.offsetInLine() + diagnostic.length())
			),
			diagnostic.message(),
			mapSeverity(diagnostic),
			sourceTool,
			diagnostic.id()
		);
		lspDiagnostic.setData(positions);
		return lspDiagnostic;
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

	public static Range toRangeAfter(IPosition position)
	{
		return new Range(
			new Position(position.line(), position.offsetInLine() + position.length()),
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

	public static Range toRange(ISyntaxNode startNode, ISyntaxNode endNode)
	{
		var firstRange = toRange(startNode);
		var secondRange = toRange(endNode);
		return new Range(
			new Position(firstRange.getStart().getLine(), firstRange.getStart().getCharacter()),
			new Position(secondRange.getEnd().getLine(), secondRange.getEnd().getCharacter())
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

	/**
	 * Converts the {@link IPosition} to an LSP position which is <strong>after</strong> the {@link IPosition} (using
	 * {@link IPosition#endOffset()})
	 * 
	 * @param position
	 * @return
	 */
	public static Position toPositionAfter(IPosition position)
	{
		return new Position(
			position.line(),
			position.endOffset()
		);
	}

	public static Range newLineRange(int startLine, int startColumn, int endColumn)
	{
		return new Range(
			new Position(startLine, startColumn),
			new Position(startLine, endColumn)
		);
	}

	public static Range newRange(int startLine, int startColumn, int endLine, int endColumn)
	{
		return new Range(
			new Position(startLine, startColumn),
			new Position(endLine, endColumn)
		);
	}

	public static Range toRangeBefore(IPosition position)
	{
		return new Range(
			new Position(position.line(), position.offsetInLine()),
			new Position(position.line(), position.offsetInLine())
		);
	}
}
