package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.IDefineData;

import java.util.List;

public class DefineDataParser extends AbstractParser<IDefineData>
{
	private static final List<SyntaxKind> SCOPE_SYNTAX_KINDS = List.of(SyntaxKind.LOCAL, SyntaxKind.PARAMETER, SyntaxKind.GLOBAL);
	private DefineData defineData;

	@Override
	protected IDefineData parseInternal()
	{
		defineData = new DefineData();
		startNewNode(defineData);

		advanceToDefineData(tokens);
		if (!isAtStartOfDefineData(tokens))
		{
			diagnostics.add(ParserDiagnostic.create("DEFINE DATA expected", 0, 0, 0, 0, ParserError.NO_DEFINE_DATA_FOUND));
			return null;
		}

		if (!consume(SyntaxKind.DEFINE))
		{
			return null;
		}

		if(!consume(SyntaxKind.DATA))
		{
			return null;
		}

		while (!tokens.isAtEnd() && tokens.peek().kind() != SyntaxKind.END_DEFINE)
		{
			parseData();
			finishNode();
		}

		if (tokens.isAtEnd())
		{
			diagnostics.add(ParserDiagnostic.create("No END-DEFINE found", tokens.peek(-1), ParserError.MISSING_END_DEFINE));
			return null;
		}

		consume(SyntaxKind.END_DEFINE);

		finishNode();
		return defineData;
	}

	private void parseData()
	{
		if (!isScopeToken(peek()))
		{
			diagnostics.add(ParserDiagnostic.unexpectedToken(SCOPE_SYNTAX_KINDS, peek()));
			return;
		}

		if(peek(1).kind() == SyntaxKind.USING)
		{
			parseUsing();
		}
	}

	private void parseUsing()
	{
		var node = new UsingNode();
		startNewNode(node);

		var scopeToken = peek();
		consume(scopeToken.kind());

		node.setScope(lastToken.kind());

		consume(SyntaxKind.USING);

		if(!consumeIdentifier())
		{
			return;
		}

		node.setUsingTarget(lastToken);
		defineData.addUsing(node);
	}

	private void parseUsing(SyntaxToken startToken, int startIndex)
	{
		var node = new UsingNode();
		currentNode = node;
		node.addNode(new TokenNode(startToken));
		node.setScope(startToken.kind());

		var identifier = tokens.peek();
		if (!identifier.kind().isIdentifier())
		{
			diagnostics.add(ParserDiagnostic.unexpectedToken(SyntaxKind.IDENTIFIER, identifier));
			return;
		}

		node.setUsingTarget(identifier);
		var nodeTokens = tokens.subrange(startIndex, tokens.getCurrentOffset());
		ParserUtil.addTokensToNode(node, nodeTokens);
		defineData.addUsing(node);
	}

	private boolean isScopeToken(SyntaxToken token)
	{
		return SCOPE_SYNTAX_KINDS.contains(token.kind());
	}

	private static void advanceToDefineData(TokenList tokens)
	{
		while (!tokens.isAtEnd() && tokens.peek().kind() != SyntaxKind.DEFINE)
		{
			tokens.advance();
		}
	}

	private static boolean isAtStartOfDefineData(TokenList tokens)
	{
		return !tokens.isAtEnd() && tokens.peek().kind() == SyntaxKind.DEFINE && tokens.peek(1) != null && tokens.peek(1).kind() == SyntaxKind.DATA;
	}
}
