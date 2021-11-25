package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.VariableScope;

import java.util.List;

public class DefineDataParser extends AbstractParser<IDefineData>
{
	private static final List<SyntaxKind> SCOPE_SYNTAX_KINDS = List.of(SyntaxKind.LOCAL, SyntaxKind.PARAMETER, SyntaxKind.GLOBAL);

	@Override
	protected IDefineData parseInternal()
	{
		var defineData = new DefineData();
		startNewNode(defineData);

		advanceToDefineData(tokens);
		if (!isAtStartOfDefineData(tokens))
		{
			diagnostics.add(ParserDiagnostic.create("DEFINE DATA expected", 0, 0, 0, 0, ParserError.NO_DEFINE_DATA_FOUND));
			return null;
		}

		if (!consume(defineData, SyntaxKind.DEFINE))
		{
			return null;
		}

		if (!consume(defineData, SyntaxKind.DATA))
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
				while (!tokens.isAtEnd() && (peek().kind() != SyntaxKind.END_DEFINE && !SCOPE_SYNTAX_KINDS.contains(peek().kind())))
				{
					tokens.advance();
				}
			}
		}

		if (tokens.isAtEnd())
		{
			diagnostics.add(ParserDiagnostic.create("No END-DEFINE found", defineData, ParserError.MISSING_END_DEFINE));
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

		if (peek(1).kind() == SyntaxKind.USING)
		{
			return using();
		}

		var scope = consumeAny(SCOPE_SYNTAX_KINDS);

		var variable = variable();

		variable.setScope(VariableScope.fromSyntaxKind(scope.kind()));

		return variable;
	}

	private VariableNode variable() throws ParseError
	{
		var variable = new VariableNode();

		var level = consumeMandatory(variable, SyntaxKind.NUMBER).intValue();
		variable.setLevel(level);

		var identifier = consumeMandatoryIdentifier(variable);
		variable.setDeclaration(identifier);

		if (consume(variable, SyntaxKind.LPAREN))
		{
			var dataType = consumeMandatoryIdentifier(variable).source(); // DataTypes like A10 get recognized as identifier
			var format = DataFormat.fromSource(dataType.charAt(0));
			var length = getLengthFromDataType(dataType);

			// N12.7 results in Tokens <IDENTIFIER (N12), DOT, NUMBER>
			if(consumeOptionally(variable, SyntaxKind.COMMA) || consumeOptionally(variable, SyntaxKind.DOT))
			{
				var number = consumeMandatory(variable, SyntaxKind.NUMBER);
				length = getLengthFromDataType(dataType + "." + number.source());
			}

			variable.setDataFormat(format);
			variable.setDataLength(length);

			consumeMandatory(variable, SyntaxKind.RPAREN);

			if(consumeOptionally(variable, SyntaxKind.DYNAMIC))
			{
				variable.setDynamicLength();
			}
		}

		// TODO: Typecheck possible INITs
		return variable;
	}

	private double getLengthFromDataType(String dataType)
	{
		if (dataType.length() == 1)
		{
			return 0.0;
		}

		dataType = dataType.replace(",", ".");
		return Double.parseDouble(dataType.substring(1));
	}

	private UsingNode using() throws ParseError
	{
		var using = new UsingNode();

		var scopeToken = consumeAny(SCOPE_SYNTAX_KINDS);
		using.setScope(scopeToken.kind());
		using.addNode(new TokenNode(scopeToken));

		consume(using, SyntaxKind.USING);

		var identifier = identifier();
		using.setUsingTarget(identifier);
		using.addNode(new TokenNode(identifier));

		// TODO: add imported variables as synthetic nodes

		return using;
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
