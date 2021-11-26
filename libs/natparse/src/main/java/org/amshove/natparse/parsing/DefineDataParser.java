package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;

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

		if (consumeOptionally(variable, SyntaxKind.LPAREN)
			&& (peek().kind() != SyntaxKind.ASTERISK && peek().kind() != SyntaxKind.NUMBER)) // group array
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

		if (previous().kind() == SyntaxKind.LPAREN)
		{
			addArrayDimensions(groupNode);
			consumeMandatory(groupNode, SyntaxKind.RPAREN);
		}

		while (peekKind(SyntaxKind.NUMBER))
		{
			if (peek().intValue() <= groupNode.level())
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
		var type = new VariableType();

		var dataType = consumeMandatoryIdentifier(typedVariable).source(); // DataTypes like A10 get recognized as identifier
		var format = DataFormat.fromSource(dataType.charAt(0));
		type.setFormat(format);

		var arrayConsumed = false;
		if (dataType.contains("/"))
		{
			addArrayDimensionsWorkaroundSlash(typedVariable);
			arrayConsumed = true;
		}

		if (!arrayConsumed && peekKind(SyntaxKind.SLASH))
		{
			// the data type has no user defined length, which means we're at a / which
			// won't be an int value
			addArrayDimensions(typedVariable);
			arrayConsumed = true;
		}
		else
		{
			var length = getLengthFromDataType(dataType);

			// N12.7 results in Tokens <IDENTIFIER (N12), DOT, NUMBER>
			if (consumeOptionally(typedVariable, SyntaxKind.COMMA) || consumeOptionally(typedVariable, SyntaxKind.DOT))
			{
				var number = consumeMandatory(typedVariable, SyntaxKind.NUMBER);
				length = getLengthFromDataType(dataType + "." + number.source());
			}
			type.setLength(length);
		}

		if (consumeOptionally(typedVariable, SyntaxKind.SLASH) && !arrayConsumed)
		{
			addArrayDimensions(typedVariable);
		}

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
		if (dataType.length() == 1 || dataType.indexOf("/") == 1)
		{
			return 0.0;
		}

		dataType = dataType.split("/")[0];
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

	private void addArrayDimensions(VariableNode variable)
	{
		addArrayDimension(variable);
		while (consumeOptionally(variable, SyntaxKind.COMMA))
		{
			addArrayDimension(variable);
		}

		for (var dimension : variable.dimensions())
		{
			checkBounds(dimension);
		}
	}

	private void addArrayDimension(VariableNode variable)
	{
		var dimension = new ArrayDimension();
		var lowerBound = extractArrayBound(new TokenNode(peek()));
		var upperBound = -1;
		consume(dimension);

		var workaroundNextDimension = false;
		if (consumeOptionally(dimension, SyntaxKind.COLON))
		{
			if(peekKind(SyntaxKind.NUMBER) && peek().source().contains(","))
			{
				// Workaround for (T/1:10,50:*) where 10,50 gets recognized as a number
				var numbers = peek().source().split(",");
				var relevantNumber = numbers[0];

				var firstNumberToken = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER, relevantNumber);
				upperBound = extractArrayBound(firstNumberToken);
				variable.addNode(firstNumberToken);
				// we now also have to handle the next dimension, because our current
				// token also contains the lower bound of the next dimension.
				// 50 in the example above.
				workaroundNextDimension = true;
			}
			else
			{
				upperBound = extractArrayBound(new TokenNode(peek()));
				consume(dimension);
			}
		}
		else
		{
			// only the upper bound was provided, like (A2/*)
			upperBound = lowerBound;
			lowerBound = 1;
		}

		if (!peekKind(SyntaxKind.RPAREN) && !peekKind(SyntaxKind.NUMBER)) // special case for (*)
		{
			consume(dimension);
		}

		dimension.setLowerBound(lowerBound);
		dimension.setUpperBound(upperBound);

		if(workaroundNextDimension)
		{
			addArrayDimensionWorkaroundComma(variable);
		}

		variable.addDimension(dimension);
	}

	private int extractArrayBound(ITokenNode token)
	{
		if (token.token().kind() == SyntaxKind.NUMBER)
		{
			return token.token().intValue();
		}

		return -1; // unbound
	}

	private void checkBounds(IArrayDimension dimension)
	{
		if (dimension.lowerBound() == 0)
		{
			report(ParserErrors.invalidArrayBound(dimension, dimension.lowerBound()));
		}
		if (dimension.upperBound() == 0)
		{
			report(ParserErrors.invalidArrayBound(dimension, dimension.upperBound()));
		}
	}

	// TODO: Try to generify bound detection with workarounds once tests are green

	/**
	 * Workaround when the lower bound of an array was consumed as identifier, because
	 * apparently / is a valid character for identifiers.
	 *
	 * @param typedVariable the variable to add the dimensions to.
	 */
	private void addArrayDimensionsWorkaroundSlash(TypedNode typedVariable)
	{
		var identifierToken = previous();
		var relevantSource = identifierToken.source().substring(identifierToken.source().indexOf('/'));

		var slashToken = SyntheticTokenNode.fromToken(identifierToken, SyntaxKind.SLASH, "/");
		typedVariable.addNode(slashToken);
		var boundToken = SyntheticTokenNode.fromToken(identifierToken, SyntaxKind.NUMBER, relevantSource.substring(1));

		var dimension = new ArrayDimension();
		dimension.addNode(boundToken);
		var lowerBound = consumeOptionally(dimension, SyntaxKind.ASTERISK)
			? -1 :
			extractArrayBound(boundToken);
		var upperBound = -1;


		var workaroundNextDimension = false;
		if (consumeOptionally(dimension, SyntaxKind.COLON))
		{
			if(peekKind(SyntaxKind.NUMBER) && peek().source().contains(","))
			{
				// Workaround for (T/1:10,50:*) where 10,50 gets recognized as a number
				var numbers = peek().source().split(",");
				var relevantNumber = numbers[0];

				var firstNumberToken = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER, relevantNumber);
				upperBound = extractArrayBound(firstNumberToken);
				typedVariable.addNode(firstNumberToken);
				// we now also have to handle the next dimension, because our current
				// token also contains the lower bound of the next dimension.
				// 50 in the example above.
				workaroundNextDimension = true;
			}
			else
			{
				upperBound = extractArrayBound(new TokenNode(peek()));
				consume(dimension);
			}
		}
		else
		{
			// only the upper bound was provided, like (A2/*)
			upperBound = lowerBound;
			lowerBound = 1;
		}

		dimension.setLowerBound(lowerBound);
		dimension.setUpperBound(upperBound);

		if(workaroundNextDimension)
		{
			addArrayDimensionWorkaroundComma(typedVariable);
		}

		if (consumeOptionally(typedVariable, SyntaxKind.COMMA))
		{
			addArrayDimensions(typedVariable);
		}
	}

	/**
	 * Workaround when the previous array dimension had a numeric upper bound
	 * and the current dimension has a numeric lower bound.
	 *
	 * This is because in (T/1:10,50:*) the 10,50 is recognized as a single number,
	 * although the comma means a separation here.
	 *
	 * @param typedVariable the variable to add the dimensions to.
	 */
	private void addArrayDimensionWorkaroundComma(VariableNode typedVariable)
	{
		var syntheticSeparator = SyntheticTokenNode.fromToken(peek(), SyntaxKind.COMMA, ",");
		typedVariable.addNode(syntheticSeparator);

		var numbers = peek().source().split(",");
		var lowerBoundNumber = numbers[1];
		var syntheticLowerBound = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER, lowerBoundNumber);

		var dimension = new ArrayDimension();
		dimension.addNode(syntheticLowerBound);
		var lowerBound = extractArrayBound(syntheticLowerBound);
		var upperBound = -1;

		discard(); // drop off the combined number which is actually separated

		var workaroundNextDimension = false;
		if (consumeOptionally(dimension, SyntaxKind.COLON))
		{
			if(peekKind(SyntaxKind.NUMBER) && peek().source().contains(","))
			{
				// Workaround for (T/1:10,50:*) where 10,50 gets recognized as a number
				numbers = peek().source().split(",");
				var relevantNumber = numbers[0];

				var firstNumberToken = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER, relevantNumber);
				upperBound = extractArrayBound(firstNumberToken);
				typedVariable.addNode(firstNumberToken);
				// we now also have to handle the next dimension, because our current
				// token also contains the lower bound of the next dimension.
				// 50 in the example above.
				workaroundNextDimension = true;
			}
			else
			{
				upperBound = extractArrayBound(new TokenNode(peek()));
				consume(dimension);
			}
		}
		else
		{
			// only the upper bound was provided, like (A2/*)
			upperBound = lowerBound;
			lowerBound = 1;
		}

		dimension.setLowerBound(lowerBound);
		dimension.setUpperBound(upperBound);

		if(workaroundNextDimension)
		{
			addArrayDimensionWorkaroundComma(typedVariable);
		}
	}

}
