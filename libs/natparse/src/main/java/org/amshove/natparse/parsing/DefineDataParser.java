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

		if (!consumeAdding(defineData, SyntaxKind.DEFINE))
		{
			return null;
		}

		if (!consumeAdding(defineData, SyntaxKind.DATA))
		{
			return null;
		}

		while (!tokens.isAtEnd() && tokens.peek().kind() != SyntaxKind.END_DEFINE)
		{
			try
			{
				defineData.addNode(dataDefinition());
			}
			catch (ParseError e)
			{
				// Most liberal error handling currently is trying to advance to the next scope token
				while(!tokens.isAtEnd() && (peek().kind() != SyntaxKind.END_DEFINE || SCOPE_SYNTAX_KINDS.contains(peek().kind())))
				{
					tokens.advance();
					continue;
				}
			}
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

	private BaseSyntaxNode dataDefinition() throws ParseError
	{
		if (!isScopeToken(peek()))
		{
			diagnostics.add(ParserDiagnostic.unexpectedToken(SCOPE_SYNTAX_KINDS, peek()));
			throw new ParseError();
		}

		if(peek(1).kind() == SyntaxKind.USING)
		{
			return using();
		}

		tokens.advance();
		return null;
	}

	private UsingNode using() throws ParseError
	{
		var node = new UsingNode();

		var scopeToken = consumeAny(SCOPE_SYNTAX_KINDS);
		node.setScope(scopeToken.kind());
		node.addNode(new TokenNode(scopeToken));

		consumeAdding(node, SyntaxKind.USING);

		var identifier = identifier();
		node.setUsingTarget(identifier);
		node.addNode(new TokenNode(identifier));

		return node;
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
