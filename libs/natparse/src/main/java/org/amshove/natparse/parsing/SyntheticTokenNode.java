package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.text.StringPool;
import org.amshove.natparse.natural.ITokenNode;

class SyntheticTokenNode extends BaseSyntaxNode implements ITokenNode
{
	private final SyntaxToken originalToken;
	private final SyntaxToken newToken;

	private SyntheticTokenNode(SyntaxToken originalToken, SyntaxKind newKind, String newSource)
	{
		this.originalToken = originalToken;

		var indexInOriginalSource = originalToken.source().indexOf(newSource);
		var newOffset = originalToken.offset() + indexInOriginalSource;
		var newOffsetInLine = originalToken.offsetInLine() + indexInOriginalSource;
		var line = originalToken.line();

		newToken = new SyntaxToken(newKind, newOffset, newOffsetInLine, line, StringPool.intern(newSource), originalToken.filePath());
	}

	public static SyntheticTokenNode fromToken(SyntaxToken token, SyntaxKind newKind, String newSource)
	{
		return new SyntheticTokenNode(token, newKind, newSource);
	}

	@Override
	public IPosition position()
	{
		return originalToken;
	}

	@Override
	public SyntaxToken token()
	{
		return newToken;
	}

	@Override
	public String toString()
	{
		return "SyntheticTokenNode{" +
			"originalToken=" + originalToken +
			", newToken=" + newToken +
			'}';
	}
}
