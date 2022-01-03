package org.amshove.natls.languageserver;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISyntaxNode;
import org.eclipse.lsp4j.*;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LspUtil
{
	private LspUtil()
	{
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
			DiagnosticSeverity.Error,
			sourceTool,
			diagnostic.id()
		);
	}

	public static Range toRange(SyntaxToken token)
	{
		return new Range(
			new Position(token.line(), token.offsetInLine()),
			new Position(token.line(), token.offsetInLine() + token.length())
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
}
