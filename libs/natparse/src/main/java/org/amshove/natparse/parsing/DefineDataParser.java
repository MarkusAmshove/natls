package org.amshove.natparse.parsing;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefineDataParser extends AbstractParser<IDefineData>
{
	private static final List<SyntaxKind> SCOPE_SYNTAX_KINDS = List.of(SyntaxKind.LOCAL, SyntaxKind.PARAMETER, SyntaxKind.GLOBAL, SyntaxKind.INDEPENDENT);
	private static final ViewParser viewParser = new ViewParser();

	private Map<String, VariableNode> declaredVariables;

	@Override
	protected IDefineData parseInternal()
	{
		var defineData = new DefineData();
		declaredVariables = new HashMap<>();

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
				// Skip to next line or END-DEFINE to recover
				while (!tokens.isAtEnd() && peek().line() == e.getErrorToken().line() && peek().kind() != SyntaxKind.END_DEFINE)
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
			throw new ParseError(peek());
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
			for (var dimension : variable.dimensions())
			{
				checkBounds(dimension);
			}

			variable.setScope(VariableScope.fromSyntaxKind(scope.kind()));
			if (variable.scope().isIndependent())
			{
				checkIndependentVariable(variable);
			}
			scopeNode.setScope(variable.scope());

			if (variable instanceof RedefinitionNode redefinitionNode)
			{
				addTargetToRedefine(scopeNode, redefinitionNode);
			}

			scopeNode.addVariable(variable);
			declaredVariables.put(variable.name(), variable);
		}

		return scopeNode;
	}

	private VariableNode variable() throws ParseError
	{
		if (peek(2).kind() == SyntaxKind.VIEW)
		{
			return view();
		}

		var variable = new VariableNode();

		var level = consumeMandatory(variable, SyntaxKind.NUMBER).intValue();
		variable.setLevel(level);

		if (consumeOptionally(variable, SyntaxKind.REDEFINE))
		{
			variable = new RedefinitionNode(variable);
		}

		var identifier = consumeMandatoryIdentifier(variable);
		variable.setDeclaration(identifier);

		if (consumeOptionally(variable, SyntaxKind.LPAREN)
			&& (peek().kind() != SyntaxKind.ASTERISK && peek().kind() != SyntaxKind.NUMBER)) // group array
		{
			variable = typedVariable(variable);
			if (variable instanceof TypedVariableNode typedVariableNode)
			{
				checkVariableType(typedVariableNode);
			}
		}
		else
		{
			if (consumeOptionally(variable, SyntaxKind.VIEW))
			{
				variable = view(variable);
			}
			else
			{
				variable = groupVariable(variable);
			}
		}

		return variable;
	}

	private ViewNode view()
	{
		var view = viewParser.parse(tokens);

		view.diagnostics().forEach(this::report);
		return view.result();
	}

	private ViewNode view(VariableNode variable) throws ParseError
	{
		var view = new ViewNode(variable);

		consumeOptionally(view, SyntaxKind.OF);

		var ddm = consumeMandatoryIdentifier(view);
		view.setDdmNameToken(ddm);

		while (peekKind(SyntaxKind.NUMBER))
		{
			if (peek().intValue() <= view.level())
			{
				break;
			}

			var nestedField = variable();
			view.addVariable(nestedField);
		}

		return view;
	}

	private GroupNode groupVariable(VariableNode variable) throws ParseError
	{
		var groupNode = variable instanceof RedefinitionNode
			? (RedefinitionNode) variable
			: new GroupNode(variable);

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

		if (groupNode.variables().size() == 0)
		{
			report(ParserErrors.emptyGroupVariable(groupNode));
		}

		return groupNode;
	}

	private VariableNode typedVariable(VariableNode variable) throws ParseError
	{
		var typedVariable = new TypedVariableNode(variable);
		var type = new VariableType();

		var dataType = consumeMandatoryIdentifier(typedVariable).source(); // DataTypes like A10 get recognized as identifier
		if (declaredVariables.containsKey(dataType))
		{
			// It is not a datatype, but an array dimension reference for a group.
			rollbackOnce();
			return groupVariable(variable);
		}
		DataFormat format;
		try
		{
			format = DataFormat.fromSource(dataType.charAt(0));
		}
		catch (NaturalParseException e)
		{
			// This only happens if the variable is actually a group, but the array
			// dimension is a reference to a constant.
			rollbackOnce();
			return groupVariable(variable);
		}

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
				if (typedVariable.dimensions().size() > 0)
				{
					consumeArrayInitializer(typedVariable);
				}
				else
				{
					consumeMandatory(typedVariable, SyntaxKind.LESSER);
					var literal = consumeLiteral(typedVariable);
					type.setInitialValue(literal);
					consumeMandatory(typedVariable, SyntaxKind.GREATER);
				}
			}
		}

		typedVariable.setType(type);
		return typedVariable;
	}

	private void consumeArrayInitializer(TypedVariableNode typedVariable) throws ParseError
	{
		// TODO(array-initializer): Feed values

		if(peekKind(SyntaxKind.LPAREN))
		{
			var lparen = consumeMandatory(typedVariable, SyntaxKind.LPAREN);

			while (!consumeOptionally(typedVariable, SyntaxKind.RPAREN) && peek().line() == lparen.line())
			{
				consume(typedVariable);
			}
		}

		if (peekKind(SyntaxKind.LESSER))
		{
			var lesser = consumeMandatory(typedVariable, SyntaxKind.LESSER);

			while(!consumeOptionally(typedVariable, SyntaxKind.GREATER) && peek().line() == lesser.line())
			{
				consume(typedVariable);
			}
		}

		if(peekKind(SyntaxKind.LPAREN)) // Theres more...
		{
			consumeArrayInitializer(typedVariable);
		}
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
		using.addNode(new SymbolReferenceNode(identifier)); // TODO(references): Add Reference to foreign DEFINE DATA

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

	private void checkVariableType(TypedVariableNode variable)
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

	private void expectInitialValueType(TypedVariableNode variableNode, SyntaxKind expectedKind)
	{
		if (variableNode.type().initialValue().kind() != expectedKind)
		{
			report(ParserErrors.initValueMismatch(variableNode, expectedKind));
		}
	}

	private void addArrayDimensions(VariableNode variable) throws ParseError
	{
		addArrayDimension(variable);
		while (consumeOptionally(variable, SyntaxKind.COMMA))
		{
			addArrayDimension(variable);
		}
	}

	private void addArrayDimension(VariableNode variable) throws ParseError
	{
		if (peek().kind() == SyntaxKind.RPAREN)
		{
			report(ParserErrors.incompleteArrayDefinition(variable));
			throw new ParseError(peek());
		}

		var dimension = new ArrayDimension();
		var lowerBound = extractArrayBound(new TokenNode(peek()), dimension);
		var upperBound = ArrayDimension.UNBOUND_VALUE;
		consume(dimension);

		var workaroundNextDimension = false;
		if (consumeOptionally(dimension, SyntaxKind.COLON))
		{
			if (peekKind(SyntaxKind.NUMBER) && peek().source().contains(","))
			{
				// Workaround for (T/1:10,50:*) where 10,50 gets recognized as a number
				var numbers = peek().source().split(",");
				var relevantNumber = numbers[0];

				var firstNumberToken = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER, relevantNumber);
				upperBound = extractArrayBound(firstNumberToken, dimension);
				variable.addNode(firstNumberToken);
				// we now also have to handle the next dimension, because our current
				// token also contains the lower bound of the next dimension.
				// 50 in the example above.
				workaroundNextDimension = true;
			}
			else
			{
				upperBound = extractArrayBound(new TokenNode(peek()), dimension);
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

		if (workaroundNextDimension)
		{
			addArrayDimensionWorkaroundComma(variable);
		}

		variable.addDimension(dimension);
	}

	private int extractArrayBound(ITokenNode token, ArrayDimension dimension)
	{
		if (token.token().kind() == SyntaxKind.NUMBER)
		{
			return token.token().intValue();
		}

		if (token.token().kind().isIdentifier())
		{
			if (!declaredVariables.containsKey(token.token().source()))
			{
				report(ParserErrors.unresolvedReference(token));
				return ArrayDimension.UNBOUND_VALUE;
			}

			var constReference = declaredVariables.get(token.token().source());
			if (!(constReference instanceof TypedVariableNode typedNode) || !typedNode.type().isConstant())
			{
				report(ParserErrors.arrayDimensionMustBeConst(token));
			}
			else
			{
				var referenceNode = new SymbolReferenceNode(token.token());
				typedNode.addReference(referenceNode);
				dimension.addNode(referenceNode);
				return typedNode.type().initialValue().intValue();
			}
		}

		return ArrayDimension.UNBOUND_VALUE;
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
	private void addArrayDimensionsWorkaroundSlash(TypedVariableNode typedVariable) throws ParseError
	{
		var identifierToken = previous();
		var relevantSource = identifierToken.source().substring(identifierToken.source().indexOf('/'));

		var slashToken = SyntheticTokenNode.fromToken(identifierToken, SyntaxKind.SLASH, "/");
		typedVariable.addNode(slashToken);

		var boundTokenKind = relevantSource.substring(1).matches("\\d+")
			? SyntaxKind.NUMBER
			: SyntaxKind.IDENTIFIER; // when the bound is a reference to a variable

		var boundToken = SyntheticTokenNode.fromToken(identifierToken, boundTokenKind, relevantSource.substring(1));

		if (boundToken.token().length() == 0 && peek().kind() != SyntaxKind.ASTERISK)
		{
			report(ParserErrors.incompleteArrayDefinition(slashToken));
			throw new ParseError(peek());
		}

		var dimension = new ArrayDimension();
		dimension.addNode(boundToken);
		var lowerBound = consumeOptionally(dimension, SyntaxKind.ASTERISK)
			? ArrayDimension.UNBOUND_VALUE :
			extractArrayBound(boundToken, dimension);
		var upperBound = ArrayDimension.UNBOUND_VALUE;

		var workaroundNextDimension = false;
		if (consumeOptionally(dimension, SyntaxKind.COLON))
		{
			if (peekKind(SyntaxKind.NUMBER) && peek().source().contains(","))
			{
				// Workaround for (T/1:10,50:*) where 10,50 gets recognized as a number
				var numbers = peek().source().split(",");
				var relevantNumber = numbers[0];

				var firstNumberToken = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER, relevantNumber);
				upperBound = extractArrayBound(firstNumberToken, dimension);
				typedVariable.addNode(firstNumberToken);
				// we now also have to handle the next dimension, because our current
				// token also contains the lower bound of the next dimension.
				// 50 in the example above.
				workaroundNextDimension = true;
			}
			else
			{
				upperBound = extractArrayBound(new TokenNode(peek()), dimension);
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

		typedVariable.addDimension(dimension);

		if (workaroundNextDimension)
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
	 * <p>
	 * This is because in (T/1:10,50:*) the 10,50 is recognized as a single number,
	 * although the comma means a separation here.
	 *
	 * @param variable the variable to add the dimensions to.
	 */
	private void addArrayDimensionWorkaroundComma(VariableNode variable) throws ParseError
	{
		var syntheticSeparator = SyntheticTokenNode.fromToken(peek(), SyntaxKind.COMMA, ",");
		variable.addNode(syntheticSeparator);

		var numbers = peek().source().split(",");
		if (numbers.length < 2) // There is a whitespace in between, so not actual the lower bound
		{
			discard();
			// Back to normal, yay \o/
			addArrayDimension(variable);
			return;
		}

		var lowerBoundNumber = numbers[1];

		var syntheticLowerBound = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER, lowerBoundNumber);

		var dimension = new ArrayDimension();
		dimension.addNode(syntheticLowerBound);
		var lowerBound = extractArrayBound(syntheticLowerBound, dimension);
		var upperBound = ArrayDimension.UNBOUND_VALUE;

		discard(); // drop off the combined number which is actually separated

		var workaroundNextDimension = false;
		if (consumeOptionally(dimension, SyntaxKind.COLON))
		{
			if (peekKind(SyntaxKind.NUMBER) && peek().source().contains(","))
			{
				// Workaround for (T/1:10,50:*) where 10,50 gets recognized as a number
				numbers = peek().source().split(",");
				var relevantNumber = numbers[0];

				var firstNumberToken = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER, relevantNumber);
				upperBound = extractArrayBound(firstNumberToken, dimension);
				variable.addNode(firstNumberToken);
				// we now also have to handle the next dimension, because our current
				// token also contains the lower bound of the next dimension.
				// 50 in the example above.
				workaroundNextDimension = true;
			}
			else
			{
				upperBound = extractArrayBound(new TokenNode(peek()), dimension);
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
		variable.addDimension(dimension);

		if (workaroundNextDimension)
		{
			addArrayDimensionWorkaroundComma(variable);
		}
	}

	private void checkIndependentVariable(VariableNode variable)
	{
		if (!variable.name().startsWith("+"))
		{
			report(ParserErrors.invalidAivNaming(variable));
		}

		if (variable instanceof IGroupNode)
		{
			report(ParserErrors.independentCantBeGroup(variable));
		}
	}

	private void addTargetToRedefine(ScopeNode scopeNode, RedefinitionNode redefinitionNode)
	{
		IVariableNode target = null;

		for (var variable : scopeNode.variables())
		{
			if (variable.name().equalsIgnoreCase(redefinitionNode.name()))
			{
				target = variable;
				break;
			}
		}

		if (target == null)
		{
			report(ParserErrors.noTargetForRedefineFound(redefinitionNode));
			return;
		}

		redefinitionNode.setTarget(target);

		var targetLength = calculateVariableLength(target);
		var redefineLength = calculateVariableLength(redefinitionNode);

		if (targetLength < redefineLength)
		{
			report(ParserErrors.redefinitionLengthIsTooLong(redefinitionNode, redefineLength, targetLength));
		}
	}

	private double calculateVariableLength(IVariableNode target)
	{
		if (target instanceof ITypedNode typedNode)
		{
			return typedNode.type().length();
		}

		if (target instanceof IGroupNode groupNode) // TODO: This should be redefine node
		{
			var groupLength = 0.0;
			for (var member : groupNode.variables())
			{
				groupLength += calculateVariableLength(member);
			}

			return groupLength;
		}

		return 0.0;
	}

}
