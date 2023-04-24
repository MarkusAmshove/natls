package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IArrayDimension;
import org.amshove.natparse.natural.ITokenNode;

import java.util.Map;

class ViewParser extends AbstractParser<ViewNode>
{

	private final Map<String, VariableNode> declaredVariables;

	ViewParser(IModuleProvider moduleProvider, Map<String, VariableNode> declaredVariables)
	{
		super(moduleProvider);
		this.declaredVariables = declaredVariables;
	}

	@Override
	protected ViewNode parseInternal()
	{
		try
		{
			var viewVariable = new VariableNode();
			var level = consumeMandatory(viewVariable, SyntaxKind.NUMBER_LITERAL).intValue();
			viewVariable.setLevel(level);

			var identifierNode = consumeMandatoryIdentifierTokenNode(viewVariable);
			viewVariable.setDeclaration(identifierNode);

			var view = new ViewNode(viewVariable);

			consumeMandatory(view, SyntaxKind.VIEW);
			consumeOptionally(view, SyntaxKind.OF);

			var targetDdm = consumeMandatoryIdentifier(view);
			view.setDdmNameToken(targetDdm);

			if (moduleProvider != null)
			{
				var ddm = moduleProvider.findDdm(targetDdm.symbolName());
				view.setDdm(ddm);
			}

			while (peekKind(SyntaxKind.NUMBER_LITERAL) && peek().intValue() > view.level())
			{
				try
				{
					view.addVariable(variable());
				}
				catch (ParseError e)
				{
					skipToNextLineAsRecovery(e);
				}
				catch (Exception e)
				{
					skipToNextLineReportingEveryToken();
				}
			}

			return view;
		}
		catch (ParseError e)
		{
			return null;
		}
	}

	private VariableNode variable() throws ParseError
	{
		var variable = new VariableNode();
		var level = consumeMandatory(variable, SyntaxKind.NUMBER_LITERAL);
		variable.setLevel(level.intValue());

		if (consumeOptionally(variable, SyntaxKind.REDEFINE))
		{
			variable = new RedefinitionNode(variable);
		}

		var identifierNode = consumeMandatoryIdentifierTokenNode(variable);
		variable.setDeclaration(identifierNode);

		if (consumeOptionally(variable, SyntaxKind.LPAREN))
		{
			// Maybe Group Array
			if (peek().kind() == SyntaxKind.ASTERISK || peek().kind() == SyntaxKind.NUMBER_LITERAL)
			{
				var firstTokenInNextLine = peekNextLine();
				if (firstTokenInNextLine.kind() == SyntaxKind.NUMBER_LITERAL && firstTokenInNextLine.intValue() > variable.level())
				{
					return group(variable);
				}
			}

			if (peek().kind() == SyntaxKind.NUMBER_LITERAL || (peek().kind().isIdentifier() && isVariableDeclared(peek().symbolName())))
			{
				addArrayDimension(variable);
				var typedDdmArrayVariable = typedVariableFromDdm(variable);
				consumeMandatory(typedDdmArrayVariable, SyntaxKind.RPAREN);
				return typedDdmArrayVariable;
			}

			return typedVariable(variable);
		}

		if (peek().kind() == SyntaxKind.NUMBER_LITERAL && peek().intValue() > level.intValue())
		{
			return group(variable);
		}

		return typedVariableFromDdm(variable);
	}

	private TypedVariableNode typedVariable(VariableNode variable) throws ParseError
	{
		var typedVariable = new TypedVariableNode(variable);
		var type = new VariableType();

		var dataType = consumeMandatoryIdentifier(typedVariable).source();

		if (declaredVariables.containsKey(dataType))
		{
			addArrayDimension(typedVariable);
			return typedVariable;
		}

		var format = DataFormat.fromSource(dataType.charAt(0));
		type.setFormat(format);

		var arrayConsumed = false;
		if (dataType.contains("/"))
		{
			addArrayDimensionsWorkaroundSlash(typedVariable);
			arrayConsumed = true;
		}

		var length = getLengthFromDataType(dataType);
		// N12.7 results in Tokens <IDENTIFIER (N12), DOT, NUMBER>
		if (consumeOptionally(typedVariable, SyntaxKind.COMMA) || consumeOptionally(typedVariable, SyntaxKind.DOT))
		{
			var number = consumeMandatory(typedVariable, SyntaxKind.NUMBER_LITERAL);
			length = getLengthFromDataType(dataType + "." + number.source());
		}
		type.setLength(length);

		if (!arrayConsumed && consumeOptionally(typedVariable, SyntaxKind.SLASH))
		{
			// the data type has no user defined length, which means we're at a / which
			// won't be an int value
			addArrayDimensions(typedVariable);
			arrayConsumed = true;
		}

		if (consumeOptionally(typedVariable, SyntaxKind.SLASH) && !arrayConsumed)
		{
			addArrayDimension(typedVariable);
		}

		consumeMandatory(typedVariable, SyntaxKind.RPAREN);

		if (consumeOptionally(typedVariable, SyntaxKind.DYNAMIC))
		{
			type.setDynamicLength();
		}

		typedVariable.setType(type);

		checkVariableTypeAgainstDdm(typedVariable);
		return typedVariable;
	}

	private GroupNode group(VariableNode variable) throws ParseError
	{
		var group = variable instanceof RedefinitionNode
			? (RedefinitionNode) variable
			: new GroupNode(variable);

		if (previousToken().kind() == SyntaxKind.LPAREN)
		{
			addArrayDimensions(group);
			consumeMandatory(group, SyntaxKind.RPAREN);
		}

		while (peekKind(SyntaxKind.NUMBER_LITERAL))
		{
			if (peek().intValue() <= group.level())
			{
				break;
			}

			if (peekKind(1, SyntaxKind.FILLER) && group instanceof RedefinitionNode)
			{
				if (mightBeFillerBytes(peek(1), peek(2)))
				{
					parseRedefineFiller((RedefinitionNode) group);
					continue;
				}
			}

			var nestedVariable = variable();
			group.addVariable(nestedVariable);
		}

		if (group.variables().size() == 0)
		{
			report(ParserErrors.emptyGroupVariable(group));
		}

		return group;
	}

	private VariableNode typedVariableFromDdm(VariableNode variable)
	{
		var typedVariable = new TypedVariableNode(variable);

		checkVariableTypeAgainstDdm(typedVariable);
		return typedVariable;
	}

	private void checkVariableTypeAgainstDdm(TypedVariableNode typed)
	{
		// TODO
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

		while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
		{
			var dimension = new ArrayDimension();
			var lowerBound = extractArrayBound(new TokenNode(peek()), dimension);
			var upperBound = ArrayDimension.UNBOUND_VALUE;
			consume(dimension);
			if (consumeOptionally(dimension, SyntaxKind.COLON))
			{
				upperBound = extractArrayBound(new TokenNode(peek()), dimension);
				consume(dimension);
			}
			else
			{
				// only the upper bound was provided, like (A2/*)
				upperBound = lowerBound;
				lowerBound = 1;
			}

			if (!peekKind(SyntaxKind.RPAREN) && !peekKind(SyntaxKind.NUMBER_LITERAL) && !peekKind(SyntaxKind.COMMA)) // special case for (*)
			{
				consume(dimension);
			}

			dimension.setLowerBound(lowerBound);
			dimension.setUpperBound(upperBound);
			variable.addDimension(dimension);
		}
	}

	private int extractArrayBound(ITokenNode token, ArrayDimension dimension)
	{
		if (token.token().kind() == SyntaxKind.NUMBER_LITERAL)
		{
			return token.token().intValue();
		}

		if (token.token().kind().isIdentifier())
		{
			if (!isVariableDeclared(token.token().symbolName()))
			{
				report(ParserErrors.unresolvedReference(token));
				return ArrayDimension.UNBOUND_VALUE;
			}

			var constReference = getDeclaredVariable(token);
			if (!(constReference instanceof TypedVariableNode typedNode) || typedNode.type().initialValue() == null)
			{
				report(ParserErrors.arrayDimensionMustBeConstOrInitialized(token));
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
		if (dimension.lowerBound() < 0)
		{
			report(ParserErrors.invalidArrayBound(dimension, dimension.lowerBound()));
		}
		if (dimension.upperBound() < 0)
		{
			report(ParserErrors.invalidArrayBound(dimension, dimension.upperBound()));
		}
	}

	// TODO: Try to generify bound detection with workarounds once tests are green

	/**
	 * Workaround when the lower bound of an array was consumed as identifier, because apparently / is a valid character
	 * for identifiers.
	 *
	 * @param typedVariable the variable to add the dimensions to.
	 */
	private void addArrayDimensionsWorkaroundSlash(TypedVariableNode typedVariable) throws ParseError
	{
		var identifierToken = previousToken();
		var relevantSource = identifierToken.source().substring(identifierToken.source().indexOf('/'));

		var slashToken = SyntheticTokenNode.fromToken(identifierToken, SyntaxKind.SLASH, "/");
		typedVariable.addNode(slashToken);

		var boundTokenKind = relevantSource.substring(1).matches("\\d+")
			? SyntaxKind.NUMBER_LITERAL
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
			? ArrayDimension.UNBOUND_VALUE : extractArrayBound(boundToken, dimension);
		var upperBound = ArrayDimension.UNBOUND_VALUE;

		var workaroundNextDimension = false;
		if (consumeOptionally(dimension, SyntaxKind.COLON))
		{
			if (peekKind(SyntaxKind.NUMBER_LITERAL) && peek().source().contains(","))
			{
				// Workaround for (T/1:10,50:*) where 10,50 gets recognized as a number
				var numbers = peek().source().split(",");
				var relevantNumber = numbers[0];

				var firstNumberToken = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER_LITERAL, relevantNumber);
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
	 * Workaround when the previous array dimension had a numeric upper bound and the current dimension has a numeric
	 * lower bound.
	 * <p>
	 * This is because in (T/1:10,50:*) the 10,50 is recognized as a single number, although the comma means a
	 * separation here.
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

		var syntheticLowerBound = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER_LITERAL, lowerBoundNumber);

		var dimension = new ArrayDimension();
		dimension.addNode(syntheticLowerBound);
		var lowerBound = extractArrayBound(syntheticLowerBound, dimension);
		var upperBound = ArrayDimension.UNBOUND_VALUE;

		discard(); // drop off the combined number which is actually separated

		var workaroundNextDimension = false;
		if (consumeOptionally(dimension, SyntaxKind.COLON))
		{
			if (peekKind(SyntaxKind.NUMBER_LITERAL) && peek().source().contains(","))
			{
				// Workaround for (T/1:10,50:*) where 10,50 gets recognized as a number
				numbers = peek().source().split(",");
				var relevantNumber = numbers[0];

				var firstNumberToken = SyntheticTokenNode.fromToken(peek(), SyntaxKind.NUMBER_LITERAL, relevantNumber);
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

	private boolean mightBeFillerBytes(SyntaxToken fillerToken, SyntaxToken maybeFillerBytes)
	{
		return maybeFillerBytes.kind() == SyntaxKind.OPERAND_SKIP
			// This happens when it's e.g.
			// 2 FILLER 5
			// the user forgot the X but meant to write a filler, because the number is in the same line.
			// we can use this information to raise a better diagnostic message.
			|| (maybeFillerBytes.kind() == SyntaxKind.NUMBER_LITERAL && maybeFillerBytes.line() == fillerToken.line());
	}

	private boolean isVariableDeclared(String potentionalVariableName)
	{
		return declaredVariables.containsKey(potentionalVariableName.toUpperCase());
	}

	private VariableNode getDeclaredVariable(ITokenNode tokenNode)
	{
		// Natural is case-insensitive, as that it considers everything upper case
		return declaredVariables.get(tokenNode.token().symbolName());
	}

	private void parseRedefineFiller(RedefinitionNode redefinitionNode)
	{
		consume(redefinitionNode, SyntaxKind.NUMBER_LITERAL); // Level
		consume(redefinitionNode, SyntaxKind.FILLER);
		var fillerToken = previousToken();
		var errored = false;
		if (!consumeOptionally(redefinitionNode, SyntaxKind.OPERAND_SKIP))
		{
			report(ParserErrors.fillerMustHaveXKeyword(fillerToken));
			consume(redefinitionNode, SyntaxKind.NUMBER_LITERAL);
			errored = true;
		}

		var fillerBytesToken = previousToken();
		var fillerBytes = fillerBytesToken.kind() == SyntaxKind.KW_NUMBER
			? fillerBytesToken.intValue()
			: Integer.parseInt(fillerBytesToken.source().substring(0, fillerBytesToken.length() - 1));
		redefinitionNode.addFillerBytes(fillerBytes);

		if (errored)
		{
			skipToNextLineAsRecovery(fillerToken.line());
		}
	}
}
