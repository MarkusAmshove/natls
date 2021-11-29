package org.amshove.natls.languageserver;

import org.amshove.natparse.lexing.SyntaxToken;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

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

	public static Location toLocation(String fileUri, SyntaxToken token)
	{
		return new Location(
			fileUri,
			new Range(
				new Position(token.line(), token.offsetInLine()),
				new Position(token.line(), token.offsetInLine() + token.length())
			)
		);
	}
}
