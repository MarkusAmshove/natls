package org.amshove.natparse.lexing;

import java.nio.file.Path;

public class SyntaxTokenFactory
{
	static SyntaxToken create(SyntaxKind kind, int offset, int offsetInLine, int line, String source, Path filePath)
	{
		return new SyntaxToken(kind, offset, offsetInLine, line, source, filePath);
	}
}
