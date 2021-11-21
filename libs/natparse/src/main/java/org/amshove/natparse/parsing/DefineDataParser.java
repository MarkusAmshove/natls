package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.IDefineData;

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

		while (!tokens.isAtEnd() && tokens.peek().kind() != SyntaxKind.END_DEFINE)
		{
			var currentToken = tokens.peek();

			switch (currentToken.kind())
			{
				case LOCAL:
					parseLocal();
				default:
					break;
			}

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

	private void parseLocal()
	{
		var startIndex = tokens.getCurrentOffset();
		var startToken = tokens.peek();
		if (!tokens.consume(SyntaxKind.LOCAL))
		{
			diagnostics.add(ParserDiagnostic.unexpectedToken(SyntaxKind.LOCAL, tokens.peek()));
			return;
		}

		if (tokens.consume(SyntaxKind.USING))
		{
			var node = new UsingNode();
			node.setLocal();
			node.setStart(startToken);

			var identifier = tokens.peek();
			if (!identifier.kind().isIdentifier())
			{
				diagnostics.add(ParserDiagnostic.unexpectedToken(SyntaxKind.IDENTIFIER, identifier));
				return;
			}

			node.setUsing(identifier);
			node.setEnd(identifier);
			var end = tokens.getCurrentOffset();
			var nodeTokens = tokens.subrange(startIndex, end);
			ParserUtil.addTokensToNode(node,nodeTokens);
			defineData.addLocalUsing(node);
		}
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
