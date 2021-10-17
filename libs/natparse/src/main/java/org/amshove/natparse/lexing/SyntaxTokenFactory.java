package org.amshove.natparse.lexing;

public class SyntaxTokenFactory
{
	static SyntaxToken create(SyntaxKind kind, int offset, int offsetInLine, int line, String source)
	{
		return new SyntaxToken(kind, offset, offsetInLine, line, source);
	}
}
