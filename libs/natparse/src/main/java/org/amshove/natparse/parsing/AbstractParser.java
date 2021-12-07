package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractParser<T>
{
	protected TokenList tokens;

	private List<IDiagnostic> diagnostics;

	public ParseResult<T> parse(TokenList tokens)
	{
		this.tokens = tokens;
		diagnostics = new ArrayList<>();

		var result = parseInternal();

		return new ParseResult<>(result, ReadOnlyList.from(diagnostics));
	}

	protected abstract T parseInternal();

	protected SyntaxToken peek()
	{
		return tokens.peek();
	}

	protected boolean peekKind(SyntaxKind kind)
	{
		return !isAtEnd() && peek().kind() == kind;
	}

	/**
	 * Consumes the next token.
	 * @param node to add to
	 */
	protected void consume(BaseSyntaxNode node)
	{
		if(!isAtEnd())
		{
			var token = tokens.advance();
			node.addNode(new TokenNode(token));
		}
	}

	protected SyntaxToken peek(int offset)
	{
		return tokens.peek(offset);
	}

	/**
	 * Consumes the current token only if the kind matches.
	 * This will not add any diagnostics.
	 * @param node the node to add the token to
	 * @param kind the kind of the token that should be consumed
	 * @return Whether the token was consumed or not
	 */
	protected boolean consumeOptionally(BaseSyntaxNode node, SyntaxKind kind)
	{
		if(!tokens.isAtEnd() && tokens.peek().kind() == kind)
		{
			node.addNode(new TokenNode(tokens.peek()));
		}

		return tokens.consume(kind);
	}

	protected boolean consume(BaseSyntaxNode node, SyntaxKind kind)
	{
		var tokenConsumed = consumeOptionally(node, kind);
		if(!tokenConsumed)
		{
			diagnostics.add(ParserDiagnostic.unexpectedToken(kind, tokens.peek()));
		}

		return tokenConsumed;
	}

	protected SyntaxToken identifier() throws ParseError
	{
		if(tokens.isAtEnd() || !tokens.peek().kind().isIdentifier())
		{
			diagnostics.add(ParserDiagnostic.unexpectedToken(SyntaxKind.IDENTIFIER, tokens.peek()));
			throw new ParseError(peek());
		}

		var token = tokens.peek();
		tokens.advance();
		return token;
	}

	protected SyntaxToken consumeMandatory(BaseSyntaxNode node, SyntaxKind kind) throws ParseError
	{
		if(consumeOptionally(node, kind))
		{
			return previous();
		}

		diagnostics.add(ParserDiagnostic.unexpectedToken(kind, peek()));
		throw new ParseError(peek());
	}

	protected SyntaxToken consumeLiteral(BaseSyntaxNode node) throws ParseError
	{
		 var literal = consumeAny(List.of(SyntaxKind.NUMBER, SyntaxKind.STRING, SyntaxKind.TRUE, SyntaxKind.FALSE));
		 node.addNode(new TokenNode(literal));
		 return literal;
	}

	// TODO: Remove/Change once IDENTIFIER_OR_KEYWORD is no more
	protected SyntaxToken consumeMandatoryIdentifier(BaseSyntaxNode node) throws ParseError
	{
		if(!isAtEnd() && peek().kind().isIdentifier())
		{
			node.addNode(new TokenNode(peek()));
			tokens.advance();
			return previous();
		}

		diagnostics.add(ParserDiagnostic.unexpectedToken(SyntaxKind.IDENTIFIER, peek()));
		throw new ParseError(peek());
	}


	protected SyntaxToken consumeAny(List<SyntaxKind> acceptedKinds) throws ParseError
	{
		if(tokens.isAtEnd() || !acceptedKinds.contains(tokens.peek().kind()))
		{
			diagnostics.add(ParserDiagnostic.unexpectedToken(acceptedKinds, tokens.peek()));
			throw new ParseError(peek());
		}

		return tokens.advance();
	}

	protected SyntaxToken previous()
	{
		return tokens.peek(-1);
	}

	protected boolean isAtEnd()
	{
		return tokens.isAtEnd();
	}

	protected void report(IDiagnostic diagnostic)
	{
		if(diagnostic != null)
		{
			diagnostics.add(diagnostic);
		}
	}

	protected void discard()
	{
		tokens.advance();
	}

	protected void rollbackOnce()
	{
		tokens.rollback(1);
	}

}
