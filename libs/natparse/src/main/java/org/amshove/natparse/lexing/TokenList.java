package org.amshove.natparse.lexing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;

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

	public ReadOnlyList<IDiagnostic> diagnostics()
	{
		return ReadOnlyList.from(diagnostics.stream().map(d -> (IDiagnostic)d).toList()); // TODO: Perf
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
	 * Peeks the next token.
	 * @return
	 */
	public SyntaxToken peek()
	{
		return peek(0);
	}

	/**
	 * Peeks the token <see>offset</see> times ahead.
	 * @return
	 */
	public SyntaxToken peek(int offset)
	{
		var index = currentOffset + offset;
		if(isAtEnd(index))
		{
			return null;
		}
		return tokens.get(index);
	}

	/**
	 * Advances over the current token.
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

	/**
	 * Consumes the current token if it matches the kind and then advances.
	 * @param kind
	 */
	public boolean consume(SyntaxKind kind)
	{
		if(peek().kind() == kind)
		{
			advance();
			return true;
		}

		return false;
	}

	public int getCurrentOffset()
	{
		return currentOffset;
	}

	/**
	 * Returns all tokens from <see>start</see> to <see>end</see>.
	 * @param start Inclusive index of the first token.
	 * @param end Inclusive index of the last token.
	 * @return
	 */
	public ReadOnlyList<SyntaxToken> subrange(int start, int end)
	{
		return ReadOnlyList.from(tokens.subList(start, end + 1));
	}
}
