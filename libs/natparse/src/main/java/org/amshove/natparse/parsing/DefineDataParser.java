package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.IDefineData;

import java.util.List;

public class DefineDataParser extends AbstractParser<IDefineData>
{
	private DefineData defineData;

	@Override
	protected IDefineData parseInternal()
	{
		defineData = new DefineData();

		advanceToDefineData(tokens);
		if (!isAtStartOfDefineData(tokens))
		{
			diagnostics.add(ParserDiagnostic.create("DEFINE DATA expected", 0, 0, 0, 0, ParserError.NO_DEFINE_DATA_FOUND));
			return null;
		}

		defineData.setStartingNode(new TokenNode(tokens.peek()));
		tokens.advance();
		tokens.advance();

		while (!tokens.isAtEnd() && tokens.peek().kind() != SyntaxKind.END_DEFINE)
		{
			parseData();

			tokens.advance();
		}

		if (tokens.isAtEnd())
		{
			diagnostics.add(ParserDiagnostic.create("No END-DEFINE found", tokens.peek(-1), ParserError.MISSING_END_DEFINE));
			return null;
		}

		defineData.setEndNode(new TokenNode(tokens.peek()));

		return defineData;
	}

	private void parseData()
	{
		var startToken = tokens.peek();

		if(!isScopeToken(startToken))
		{
			diagnostics.add(ParserDiagnostic.unexpectedToken(List.of(SyntaxKind.LOCAL, SyntaxKind.PARAMETER), startToken));
			return;
		}
		tokens.advance();

		var startIndex = tokens.getCurrentOffset();
		if (tokens.consume(SyntaxKind.USING))
		{
			parseUsing(startToken, startIndex);
			return;
		}
	}

	private void parseUsing(SyntaxToken startToken, int startIndex)
	{
		var node = new UsingNode();
		node.setStart(startToken);
		node.setScope(startToken.kind());

		var identifier = tokens.peek();
		if(!identifier.kind().isIdentifier())
		{
			diagnostics.add(ParserDiagnostic.unexpectedToken(SyntaxKind.IDENTIFIER, identifier));
			return;
		}

		node.setUsingTarget(identifier);
		node.setEnd(identifier);
		var nodeTokens = tokens.subrange(startIndex, tokens.getCurrentOffset());
		ParserUtil.addTokensToNode(node, nodeTokens);
		defineData.addUsing(node);
	}

	private boolean isScopeToken(SyntaxToken token)
	{
		var kind = token.kind();
		return kind == SyntaxKind.LOCAL || kind == SyntaxKind.PARAMETER;
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
