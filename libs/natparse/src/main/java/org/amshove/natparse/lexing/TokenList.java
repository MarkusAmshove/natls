package org.amshove.natparse.lexing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class TokenList
{
	private final List<SyntaxToken> tokens;
	private final List<LexerDiagnostic> diagnostics;
	private final List<SyntaxToken> comments;
	private final Path filePath;

	private int currentOffset = 0;

	TokenList(Path filePath, List<SyntaxToken> tokens)
	{
		this.tokens = tokens;
		diagnostics = List.of();
		comments = List.of();
		this.filePath = filePath;
	}

	TokenList(Path filePath, List<SyntaxToken> tokens, List<LexerDiagnostic> diagnostics, List<SyntaxToken> comments)
	{
		this.tokens = tokens;
		this.diagnostics = diagnostics;
		this.comments = comments;
		this.filePath = filePath;
	}

	public static TokenList fromTokens(Path filePath, List<SyntaxToken> tokenList)
	{
		return new TokenList(filePath, tokenList);
	}

	public static TokenList fromTokensAndDiagnostics(Path filePath, List<SyntaxToken> tokenList, List<LexerDiagnostic> diagnostics, List<SyntaxToken> comments)
	{
		return new TokenList(filePath, tokenList, diagnostics, comments);
	}

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

	public Path filePath()
	{
		return filePath;
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
	 * Peeks the token `offset` times ahead.
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
	public SyntaxToken advance()
	{
		var token = peek();
		currentOffset++;
		return token;
	}

	/**
	 * Resets the position offset times back.
	 */
	public void rollback(int offset)
	{
		currentOffset -= offset;
	}

	public boolean isAtEnd()
	{
		return isAtEnd(currentOffset);
	}

	public boolean isAtEnd(int offset)
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
		return TokenList.fromTokensAndDiagnostics(filePath, tokens, diagnostics, comments);
	}

	public ReadOnlyList<SyntaxToken> comments()
	{
		return ReadOnlyList.from(comments); // TODO: Perf
	}

	/**
	 * Consumes the current token if it matches the kind and then advances.
	 * @param kind
	 */
	public boolean consume(SyntaxKind kind)
	{
		if(!isAtEnd() && peek().kind() == kind)
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
	 * Returns all tokens from start to end.
	 * @param start Inclusive index of the first token.
	 * @param end Inclusive index of the last token.
	 * @return
	 */
	public ReadOnlyList<SyntaxToken> subrange(int start, int end)
	{
		return ReadOnlyList.from(tokens.subList(start, end + 1));
	}

	public Stream<SyntaxToken> stream()
	{
		return tokens.stream();
	}
}
