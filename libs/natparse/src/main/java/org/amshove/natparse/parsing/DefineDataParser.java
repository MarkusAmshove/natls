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

		advanceToDefineData(tokens);
		if (!isAtStartOfDefineData(tokens))
		{
			report(ParserDiagnostic.create("DEFINE DATA expected", 0, 0, 0, 0, ParserError.NO_DEFINE_DATA_FOUND));
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
			report(ParserDiagnostic.create("No END-DEFINE found", defineData, ParserError.MISSING_END_DEFINE));
			return null;
		}

		try
		{
			consumeMandatory(defineData, SyntaxKind.END_DEFINE);
		}
		catch (ParseError e)
		{
			// it's okay, we're done here.
		}

		return defineData;
	}

	private BaseSyntaxNode dataDefinition() throws ParseError
	{
		if (!isScopeToken(peek()))
		{
			report(ParserDiagnostic.unexpectedToken(SCOPE_SYNTAX_KINDS, peek()));
			throw new ParseError();
		}

		if (peek(1).kind() == SyntaxKind.USING)
		{
			return using();
		}

		return scope();
	}

	private ScopeNode scope() throws ParseError
	{
		var scope = consumeAny(SCOPE_SYNTAX_KINDS);
		var scopeNode = new ScopeNode();
		scopeNode.addNode(new TokenNode(scope));

		while (peekKind(SyntaxKind.NUMBER)) // level
		{
			var variable = variable();

			variable.setScope(VariableScope.fromSyntaxKind(scope.kind()));
			scopeNode.setScope(variable.scope());

			scopeNode.addVariable(variable);
		}

		return scopeNode;
	}

	private VariableNode variable() throws ParseError
	{
		var variable = new VariableNode();

		var level = consumeMandatory(variable, SyntaxKind.NUMBER).intValue();
		variable.setLevel(level);

		var identifier = consumeMandatoryIdentifier(variable);
		variable.setDeclaration(identifier);

		if (consumeOptionally(variable, SyntaxKind.LPAREN))
		{
			variable = typedVariable(variable);
			checkVariableType((TypedNode) variable);
		}
		else
		{
			variable = groupVariable(variable);
		}

		return variable;
	}

	private GroupNode groupVariable(VariableNode variable) throws ParseError
	{
		var groupNode = new GroupNode(variable);

		while (peekKind(SyntaxKind.NUMBER))
		{
			if(peek().intValue() <= groupNode.level())
			{
				break;
			}

			var nestedVariable = variable();
			groupNode.addVariable(nestedVariable);
		}

		return groupNode;
	}

	private TypedNode typedVariable(VariableNode variable) throws ParseError
	{
		var typedVariable = new TypedNode(variable);

		var dataType = consumeMandatoryIdentifier(typedVariable).source(); // DataTypes like A10 get recognized as identifier
		var format = DataFormat.fromSource(dataType.charAt(0));
		var length = getLengthFromDataType(dataType);

		// N12.7 results in Tokens <IDENTIFIER (N12), DOT, NUMBER>
		if (consumeOptionally(typedVariable, SyntaxKind.COMMA) || consumeOptionally(typedVariable, SyntaxKind.DOT))
		{
			var number = consumeMandatory(typedVariable, SyntaxKind.NUMBER);
			length = getLengthFromDataType(dataType + "." + number.source());
		}

		var type = new VariableType();
		type.setFormat(format);
		type.setLength(length);

		consumeMandatory(typedVariable, SyntaxKind.RPAREN);

		if (consumeOptionally(typedVariable, SyntaxKind.DYNAMIC))
		{
			type.setDynamicLength();
		}

		if (consumeOptionally(typedVariable, SyntaxKind.INIT) || consumeOptionally(typedVariable, SyntaxKind.CONST))
		{
			if (previous().kind() == SyntaxKind.CONST)
			{
				type.setConstant();
			}
			if (consumeOptionally(typedVariable, SyntaxKind.LESSER_GREATER))
			{
				// special case for a better error message. <> is  just an empty initial value
				report(ParserErrors.emptyInitValue(typedVariable));
			}
			else
			{
				consumeMandatory(typedVariable, SyntaxKind.LESSER);
				var literal = consumeLiteral(typedVariable);
				type.setInitialValue(literal);
				consumeMandatory(typedVariable, SyntaxKind.GREATER);
			}
		}

		typedVariable.setType(type);
		return typedVariable;
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

	private void checkVariableType(TypedNode variable)
	{
		if (variable.type().hasDynamicLength())
		{
			switch (variable.type().format())
			{
				case ALPHANUMERIC:
				case BINARY:
				case UNICODE:
					break;

				case CONTROL:
				case DATE:
				case FLOAT:
				case INTEGER:
				case LOGIC:
				case NUMERIC:
				case PACKED:
				case TIME:
				case NONE:
					report(ParserErrors.dynamicVariableLengthNotAllowed(variable));
			}

			if (variable.type().length() > 0.0)
			{
				report(ParserErrors.dynamicAndFixedLength(variable));
			}
		}

		if (variable.type().length() == 0.0)
		{
			switch (variable.type().format())
			{
				case ALPHANUMERIC:
				case BINARY:
				case UNICODE:
					if (!variable.type().hasDynamicLength())
					{
						report(ParserErrors.dataTypeNeedsLength(variable));
					}
					break;

				case CONTROL:
				case DATE:
				case LOGIC:
				case TIME:
				case NONE:
					break;

				case FLOAT:
				case INTEGER:
				case NUMERIC:
				case PACKED:
					report(ParserErrors.dataTypeNeedsLength(variable));
			}
		}

		if (variable.type().initialValue() != null)
		{
			switch (variable.type().format())
			{
				case ALPHANUMERIC:
					expectInitialValueType(variable, SyntaxKind.STRING);
					break;

				case BINARY:
				case CONTROL:
				case DATE:
				case TIME:
				case UNICODE:
				case NONE:
					// TODO: Unsure about these at the moment
					break;

				case FLOAT:
				case NUMERIC:
				case PACKED:
				case INTEGER:
					expectInitialValueType(variable, SyntaxKind.NUMBER);
					break;

				case LOGIC:
					var initialValueType = variable.type().initialValue().kind();
					if (initialValueType != SyntaxKind.TRUE && initialValueType != SyntaxKind.FALSE)
					{
						report(ParserErrors.initValueMismatch(variable, SyntaxKind.TRUE, SyntaxKind.FALSE));
					}
					break;
			}
		}
	}

	private void expectInitialValueType(TypedNode variableNode, SyntaxKind expectedKind)
	{
		if (variableNode.type().initialValue().kind() != expectedKind)
		{
			report(ParserErrors.initValueMismatch(variableNode, expectedKind));
		}
	}

}
