package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractParser<T>
{
	protected IModuleProvider moduleProvider;
	protected TokenList tokens;
	private TokenNode previousNode;

	private List<IDiagnostic> diagnostics = new ArrayList<>();

	public AbstractParser(IModuleProvider moduleProvider)
	{
		this.moduleProvider = moduleProvider;
	}

	void setModuleProvider(IModuleProvider moduleProvider)
	{
		this.moduleProvider = moduleProvider;
	}

	public ParseResult<T> parse(TokenList tokens)
	{
		this.tokens = tokens;
		diagnostics = new ArrayList<>();

		var result = parseInternal();

		return new ParseResult<>(result, ReadOnlyList.from(diagnostics));
	}

	protected abstract T parseInternal();

	protected INaturalModule sideloadModule(String referableName, ITokenNode importNode)
	{
		if(moduleProvider == null)
		{
			return null;
		}

		var module = moduleProvider.findNaturalModule(referableName);

		if(module == null && !(referableName.startsWith("USR") && referableName.endsWith("N")))
		{
			report(ParserErrors.unresolvedImport(importNode));
		}

		return module;
	}

	protected IHasDefineData sideloadDefineData(ISymbolReferenceNode importNode)
	{
		if(sideloadModule(importNode.token().symbolName(), importNode) instanceof IHasDefineData hasDefineData)
		{
			return hasDefineData;
		}

		return null;
	}

	protected TokenNode getPreviousNode()
	{
		return previousNode;
	}

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
			previousNode = new TokenNode(token);
			node.addNode(previousNode);
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
			previousNode = new TokenNode(tokens.peek());
			node.addNode(previousNode);
		}

		return tokens.consume(kind);
	}

	protected boolean consume(BaseSyntaxNode node, SyntaxKind kind)
	{
		var tokenConsumed = consumeOptionally(node, kind);
		if(!tokenConsumed)
		{
			diagnostics.add(ParserErrors.unexpectedToken(kind, tokens.peek()));
		}

		return tokenConsumed;
	}

	protected SyntaxToken identifier() throws ParseError
	{
		// TODO(kcheck): This currently allows keywords as identifier
//		if(tokens.isAtEnd() || !tokens.peek().kind().isIdentifier())
//		{
//			diagnostics.add(ParserErrors.unexpectedToken(SyntaxKind.IDENTIFIER, tokens.peek()));
//			throw new ParseError(peek());
//		}

		var token = tokens.peek();
		tokens.advance();
		return token;
	}

	protected SyntaxToken consumeMandatory(BaseSyntaxNode node, SyntaxKind kind) throws ParseError
	{
		if(consumeOptionally(node, kind))
		{
			return previousToken();
		}

		diagnostics.add(ParserErrors.unexpectedToken(kind, peek()));
		throw new ParseError(peek());
	}

	protected SyntaxToken consumeLiteral(BaseSyntaxNode node) throws ParseError
	{
		if(peek().kind().isSystemVariable())
		{
			var systemVariable = peek();
			node.addNode(new SystemVariableNode(systemVariable));
			discard();
			return systemVariable;
		}

		if(peek().kind() == SyntaxKind.LPAREN) // Attributes
		{
			var lparen = peek(); // TODO(attributes): This is not correct but good for now.
			while (!isAtEnd() && peek().kind() != SyntaxKind.RPAREN && peek().kind() != SyntaxKind.END_DEFINE)
			{
				consume(node);
			}
			consumeMandatory(node, SyntaxKind.RPAREN);
			return lparen;
		}

		var literal = consumeAny(List.of(SyntaxKind.NUMBER, SyntaxKind.STRING, SyntaxKind.TRUE, SyntaxKind.FALSE));
		previousNode = new TokenNode(literal);
		node.addNode(previousNode);
		return literal;
	}

	// TODO: Remove/Change once IDENTIFIER_OR_KEYWORD is no more
	protected SyntaxToken consumeMandatoryIdentifier(BaseSyntaxNode node) throws ParseError
	{
		// TODO(kcheck): This currently allows keywords as identifier
		var kind = peek().kind();
		if(!isAtEnd() && !kind.isLiteralOrConst())
		{
			previousNode = new TokenNode(peek());
			node.addNode(previousNode);
			tokens.advance();
			return previousToken();
		}

		diagnostics.add(ParserErrors.unexpectedToken(SyntaxKind.IDENTIFIER, peek()));
		throw new ParseError(peek());
	}


	protected SyntaxToken consumeAny(List<SyntaxKind> acceptedKinds) throws ParseError
	{
		if(tokens.isAtEnd() || !acceptedKinds.contains(tokens.peek().kind()))
		{
			diagnostics.add(ParserErrors.unexpectedToken(acceptedKinds, tokens.peek()));
			throw new ParseError(peek());
		}

		return tokens.advance();
	}

	protected boolean peekAny(List<SyntaxKind> acceptedKinds)
	{
		return !tokens.isAtEnd() && acceptedKinds.contains(tokens.peek().kind());
	}

	protected TokenNode previousTokenNode()
	{
		return previousNode;
	}

	protected SyntaxToken previousToken()
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

	protected SyntaxToken peekNextLine()
	{
		var offset = 0;
		var currentLine = peek().line();
		while(!tokens.isAtEnd(offset) && peek(offset).line() == currentLine)
		{
			offset++;
		}

		return peek(offset);
	}

	protected void skipToNextLineAsRecovery(ParseError e)
	{
		// Skip to next line or END-DEFINE to recover
		while (!tokens.isAtEnd() && peek().line() == e.getErrorToken().line() && peek().kind() != SyntaxKind.END_DEFINE)
		{
			tokens.advance();
		}
	}

	protected void skipToNextLineReportingEveryToken()
	{
		var currentLine = peek().line();
		// Skip to next line or END-DEFINE to recover
		while (!tokens.isAtEnd() && peek().line() == currentLine && peek().kind() != SyntaxKind.END_DEFINE)
		{
			report(ParserErrors.trailingToken(peek()));
			discard();
		}
	}

}
