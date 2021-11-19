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

	// TODO: ReadOnlyList

	public List<LexerDiagnostic> diagnostics()
	{
		return diagnostics;
	}

	// TODO: ReadOnlyList
	public List<SyntaxToken> tokensUntilNext(SyntaxKind kind)
	{
		var startOffset = currentOffset;
		if(!advanceUntil(kind))
		{
			return List.of();
		}
		return List.copyOf(tokens.subList(startOffset, currentOffset));
	}

	/**
	 * Peeks the next token, skipping whitespace.
	 * @return
	 */
	public SyntaxToken peek()
	{
		return peek(0);
	}

	/**
	 * Peeks the token <see>offset</see> times ahead, skipping all whitespace.
	 * @return
	 */
	public SyntaxToken peek(int offset)
	{
		var targetIndex = currentOffset + offset;
		var validTokensLeft = offset;
		while(!isAtEnd(targetIndex) && validTokensLeft > 0)
		{
			if(lookahead(targetIndex).kind().isWhitespace())
			{
				targetIndex++;
			}
			else
			{
				validTokensLeft--;
			}
		}

		if(isAtEnd(targetIndex))
		{
			return null;
		}
		return tokens.get(targetIndex);
	}

	/**
	 * Advances over the current token until the next non whitespace token.
	 */
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

	// TODO: ReadOnlyList
	List<SyntaxToken> allTokens()
	{
		return List.copyOf(tokens);
	}

	public boolean advanceAfterNext(SyntaxKind kind)
	{
		if(advanceUntil(kind))
		{
			advance();
			return !isAtEnd();
		}

		return false;
	}

	public boolean advanceUntil(SyntaxKind kind)
	{
		while(!isAtEnd() && peek().kind() != kind)
		{
			advance();
		}

		if(isAtEnd())
		{
			return false;
		}

		return true;
	}

	// TODO: Figure out a better name
	public TokenList newResetted()
	{
		return TokenList.fromTokensAndDiagnostics(tokens, diagnostics);
	}

	private SyntaxToken lookahead(int offset)
	{
		return tokens.get(currentOffset + offset);
	}
}
