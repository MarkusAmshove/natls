package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ITokenNode;

import java.nio.file.Path;

class TokenNode extends BaseSyntaxNode implements ITokenNode
{
	private final SyntaxToken token;

	public TokenNode(SyntaxToken token)
	{
		this.token = token;
	}

	@Override
	public SyntaxToken token()
	{
		return token;
	}

	@Override
	public IPosition position()
	{
		return token;
	}

	@Override
	public IPosition diagnosticPosition()
	{
		return token.diagnosticPosition();
	}

	@Override
	public boolean isInFile(Path path)
	{
		return token.filePath().equals(path);
	}

	@Override
	public String toString()
	{
		return "TokenNode{token=%s}".formatted(token);
	}
}
