package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

abstract class AbstractParser<T>
{
	protected TokenList tokens;
	protected SyntaxToken lastToken;

	private Stack<BaseSyntaxNode> parsedNodes;

	protected BaseSyntaxNode currentNode;
	private List<BaseSyntaxNode> childNodesOfCurrentNode;

	protected List<IDiagnostic> diagnostics;

	public ParseResult<T> parse(TokenList tokens)
	{
		this.tokens = tokens;
		diagnostics = new ArrayList<>();
		parsedNodes = new Stack<>();
		childNodesOfCurrentNode = new ArrayList<>();

		var result = parseInternal();

		return new ParseResult(result, ReadOnlyList.from(diagnostics));
	}

	protected abstract T parseInternal();

	protected void startNewNode(BaseSyntaxNode node)
	{
		for(var childNodes : childNodesOfCurrentNode)
		{
			currentNode.addNode(childNodes);
		}

		parsedNodes.push(currentNode);

		childNodesOfCurrentNode = new ArrayList<>();
		currentNode = node;
	}

	protected void finishNode()
	{
		for(var childNodes : childNodesOfCurrentNode)
		{
			currentNode.addNode(childNodes);
		}

		if(parsedNodes.empty())
		{
			currentNode = null;
		}
		else
		{
			var childNode = currentNode;
			currentNode = parsedNodes.pop();
			if(currentNode != null)
			{
				currentNode.addNode(childNode);
			}
		}
		childNodesOfCurrentNode = new ArrayList<>();
	}

	protected SyntaxToken peek()
	{
		return tokens.peek();
	}

	protected SyntaxToken peek(int offset)
	{
		return tokens.peek(1);
	}

	protected boolean consume(SyntaxKind kind)
	{
		if(!tokens.isAtEnd() && tokens.peek().kind() == kind)
		{
			childNodesOfCurrentNode.add(new TokenNode(tokens.peek()));
			lastToken = tokens.peek();
		}
		else
		{
			diagnostics.add(ParserDiagnostic.unexpectedToken(kind, tokens.peek()));
		}
		return tokens.consume(kind);
	}

	protected boolean consumeIdentifier()
	{
		if(!tokens.isAtEnd() && tokens.peek().kind().isIdentifier())
		{
			return consume(tokens.peek().kind());
		}

		diagnostics.add(ParserDiagnostic.unexpectedToken(SyntaxKind.IDENTIFIER, tokens.peek()));
		return false;
	}

}
