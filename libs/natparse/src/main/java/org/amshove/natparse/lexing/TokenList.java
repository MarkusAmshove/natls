package org.amshove.natparse.lexing;

import java.util.List;

public class TokenList
{
	private final List<SyntaxToken> tokens;
	private final List<LexerDiagnostic> diagnostics;

	private int currentOffset = 0;

	TokenList(List<SyntaxToken> tokens)
	{
		this.tokens = tokens;
		diagnostics = List.of();
	}

	TokenList(List<SyntaxToken> tokens, List<LexerDiagnostic> diagnostics)
	{
		this.tokens = tokens;
		this.diagnostics = diagnostics;
	}

	public static TokenList fromTokens(List<SyntaxToken> tokenList)
	{
		return new TokenList(tokenList);
	}

	public static TokenList fromTokensAndDiagnostics(List<SyntaxToken> tokenList, List<LexerDiagnostic> diagnostics)
	{
		return new TokenList(tokenList, diagnostics);
	}

	public List<LexerDiagnostic> diagnostics()
	{
		return diagnostics;
	}

	public SyntaxToken peek()
	{
		return peek(0);
	}

	public SyntaxToken peek(int offset)
	{
		var index = currentOffset + offset;
		if(isAtEnd(index))
		{
			return null;
		}
		return tokens.get(index);
	}

	public void advance()
	{
		currentOffset++;
	}

	public boolean isAtEnd()
	{
		return isAtEnd(currentOffset);
	}

	private boolean isAtEnd(int offset)
	{
		return offset >= tokens.size();
	}

	public int size()
	{
		return tokens.size();
	}

	List<SyntaxToken> allTokens()
	{
		return List.copyOf(tokens);
	}
}
