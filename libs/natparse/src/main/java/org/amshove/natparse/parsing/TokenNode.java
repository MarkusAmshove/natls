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
		return "%s{token=%s}".formatted(getClass().getSimpleName(), token);
	}

	@Override
	public boolean enclosesPosition(int line, int column)
	{
		var position = token.diagnosticPosition();
		return position.line() == line
			&& position.offsetInLine() <= column
			&& position.endOffset() >= column;
	}
}
