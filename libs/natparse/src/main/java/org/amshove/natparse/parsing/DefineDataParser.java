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

	/**
	 * Do not use this directly, use getDeclaredVariable or isVariableDeclared for proper case handling.
	 * Also use addDeclaredVariable for error handling.
	 */
	private Map<String, VariableNode> declaredVariables;

	private VariableScope currentScope;

	private RedefinitionNode currentRedefineNode;

	private DefineDataNode defineData;

	public DefineDataParser(IModuleProvider moduleProvider)
	{
		super(moduleProvider);
	}

	@Override
	protected IDefineData parseInternal()
	{
		defineData = new DefineDataNode();
		declaredVariables = new HashMap<>();

		advanceToDefineData(tokens);
		if (!isAtStartOfDefineData(tokens))
		{
			report(ParserDiagnostic.create("DEFINE DATA expected", 0, 0, 0, 0, tokens.filePath(), ParserError.NO_DEFINE_DATA_FOUND));
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
				skipToNextLineAsRecovery(e);
			}
			catch (Exception e)
			{
				skipToNextLineReportingEveryToken();
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
			report(ParserErrors.unexpectedToken(SCOPE_SYNTAX_KINDS, peek()));
			discard();
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
		currentScope = VariableScope.fromSyntaxKind(scope.kind());

		scopeNode.addNode(new TokenNode(scope));
		scopeNode.setScope(currentScope);

		while (peekKind(SyntaxKind.NUMBER)) // level
		{
			try
			{
				var variable = variable();
				variable.setScope(currentScope);
				for (var dimension : variable.dimensions())
				{
					checkBounds(dimension);
				}

				if (variable.scope().isIndependent())
				{
					checkIndependentVariable(variable);
				}

				if (variable instanceof RedefinitionNode redefinitionNode)
				{
					addTargetToRedefine(scopeNode, redefinitionNode);
				}

				scopeNode.addVariable(variable);
				addDeclaredVariable(variable);
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

		return scopeNode;
	}

	private VariableNode variable() throws ParseError
	{
		return variable(List.of());
	}

	private VariableNode variable(List<IArrayDimension> inheritedDimensions) throws ParseError
	{
		if (peek(2).kind() == SyntaxKind.VIEW)
		{
			return view();
		}

		var variable = new VariableNode();
		try
		{
			if (!inheritedDimensions.isEmpty())
			{
				for (var inheritedDimension : inheritedDimensions)
				{
					variable.addDimension((ArrayDimension) inheritedDimension);
				}
			}

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
				variable = groupVariable(variable);
			}
		}
		catch (ParseError e)
		{
			// Skip to the next line, but still return however far we've come to let groups be kinda complete.
			// This results in an unfinished variable, but provides way better error recovery.
			skipToNextLineAsRecovery(e);
		}

		return variable;
	}

	private ViewNode view() throws ParseError
	{
		var view = new ViewParser(moduleProvider, declaredVariables).parse(tokens);

		view.diagnostics().forEach(this::report);
		if (view.result() == null)
		{
			throw new ParseError(peek());
		}
		return view.result();
	}

	private GroupNode groupVariable(VariableNode variable) throws ParseError
	{
		var groupNode = variable instanceof RedefinitionNode
			? (RedefinitionNode) variable
			: new GroupNode(variable);

		var previousRedefine = currentRedefineNode;
		if(groupNode instanceof RedefinitionNode redefine)
		{
			currentRedefineNode = redefine;
		}

		if (previousToken().kind() == SyntaxKind.LPAREN)
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

			if (peek(1).kind() == SyntaxKind.FILLER
				&& peek(2).kind() == SyntaxKind.NUMBER
				&& currentRedefineNode != null)
			{
				parseRedefineFiller(currentRedefineNode);
			}
			else
			{
				var nestedVariable = variable(groupNode.getDimensions());
				groupNode.addVariable(nestedVariable);

				if (peek().line() == previousToken().line()
					&& peek().kind() != SyntaxKind.NUMBER) // multiple variables declared in the same line...
				{
					// Error handling for trailing stuff that shouldn't be there
					skipToNextLineReportingEveryToken();
				}
			}
		}

		if (groupNode.variables().size() == 0)
		{
			report(ParserErrors.emptyGroupVariable(groupNode));
		}

		currentRedefineNode = previousRedefine;

		return groupNode;
	}

	private void parseRedefineFiller(RedefinitionNode redefinitionNode) throws ParseError
	{
		consume(redefinitionNode, SyntaxKind.NUMBER); // Level
		consume(redefinitionNode, SyntaxKind.FILLER);
		var fillerBytes = consumeMandatory(redefinitionNode, SyntaxKind.NUMBER);
		redefinitionNode.addFillerBytes(fillerBytes.intValue());

		if (!peek().kind().isIdentifier())
		{
			report(ParserErrors.fillerMustHaveXKeyword(fillerBytes));
			return;
		}

		var x = consumeMandatoryIdentifier(redefinitionNode); // the X
		if (!x.symbolName().equals("X"))
		{
			report(ParserErrors.fillerMustHaveXKeyword(fillerBytes));
		}
	}

	private VariableNode typedVariable(VariableNode variable) throws ParseError
	{
		var typedVariable = new TypedVariableNode(variable);
		var type = new VariableTypeNode();

		var dataType = consumeMandatoryIdentifier(typedVariable).source(); // DataTypes like A10 get recognized as identifier

		DataFormat format;
		try
		{
			format = DataFormat.fromSource(dataType);
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

		var length = getLengthFromDataType(dataType);

		// N12.7 results in Tokens <IDENTIFIER (N12), DOT, NUMBER>
		if (consumeOptionally(typedVariable, SyntaxKind.COMMA) || consumeOptionally(typedVariable, SyntaxKind.DOT))
		{
			var number = consumeMandatory(typedVariable, SyntaxKind.NUMBER);
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
			addArrayDimensions(typedVariable);
		}

		consumeMandatory(typedVariable, SyntaxKind.RPAREN);

		if (consumeOptionally(typedVariable, SyntaxKind.DYNAMIC))
		{
			type.setDynamicLength();
		}

		if (consumeOptionally(typedVariable, SyntaxKind.INIT) || consumeOptionally(typedVariable, SyntaxKind.CONST) || consumeOptionally(typedVariable, SyntaxKind.CONSTANT))
		{
			if (previousToken().kind() == SyntaxKind.CONST || previousToken().kind() == SyntaxKind.CONSTANT)
			{
				type.setConstant();
			}

			if (consumeOptionally(typedVariable, SyntaxKind.FULL))
			{
				consumeMandatory(typedVariable, SyntaxKind.LENGTH);
			}

			if (consumeOptionally(typedVariable, SyntaxKind.LENGTH))
			{
				consumeMandatory(typedVariable, SyntaxKind.NUMBER);
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
		if (consumeOptionally(typedVariable, SyntaxKind.LPAREN))
		{
			// TODO(masks): Parse for real and add to variable
			// TODO(masks): Not legal for parameter?
			while (!isAtEnd() && peek().kind() != SyntaxKind.RPAREN && peek().kind() != SyntaxKind.END_DEFINE)
			{
				consume(typedVariable);
			}
			consumeMandatory(typedVariable, SyntaxKind.RPAREN);
		}

		// TODO: Only for parameter
		if (consumeOptionally(typedVariable, SyntaxKind.BY))
		{
			if (currentScope != VariableScope.PARAMETER)
			{
				report(ParserErrors.byValueNotAllowedInCurrentScope(getPreviousNode(), currentScope));
				consumeOptionally(typedVariable, SyntaxKind.VALUE);
				consumeOptionally(typedVariable, SyntaxKind.RESULT);
			}
			else
			{
				consumeMandatory(typedVariable, SyntaxKind.VALUE);
				consumeOptionally(typedVariable, SyntaxKind.RESULT);
			}
		}

		if (consumeOptionally(typedVariable, SyntaxKind.OPTIONAL) && currentScope != VariableScope.PARAMETER)
		{
			report(ParserErrors.optionalNotAllowedInCurrentScope(getPreviousNode(), currentScope));
		}

		return typedVariable;
	}

	private void consumeArrayInitializer(TypedVariableNode typedVariable) throws ParseError
	{
		// TODO(array-initializer): Feed values

		// TODO(array-initializer): Do something with these
		consumeOptionally(typedVariable, SyntaxKind.ALL);
		if (consumeOptionally(typedVariable, SyntaxKind.FULL))
		{
			consumeMandatory(typedVariable, SyntaxKind.LENGTH);
		}
		else
		{
			consumeOptionally(typedVariable, SyntaxKind.LENGTH);
		}
		consumeOptionally(typedVariable, SyntaxKind.NUMBER);

		if (peekKind(SyntaxKind.LPAREN))
		{
			var lparen = consumeMandatory(typedVariable, SyntaxKind.LPAREN);

			while (!consumeOptionally(typedVariable, SyntaxKind.RPAREN) && peek().line() == lparen.line())
			{
				consume(typedVariable);
			}
		}

		if (peekKind(SyntaxKind.LESSER))
		{
			consumeMandatory(typedVariable, SyntaxKind.LESSER);

			while (!consumeOptionally(typedVariable, SyntaxKind.GREATER) && peek().kind() != SyntaxKind.END_DEFINE)
			{
				consume(typedVariable);
			}
		}

		if (peekKind(SyntaxKind.LPAREN)) // Theres more...
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
		var identifierReference = new SymbolReferenceNode(identifier);
		using.addNode(identifierReference);

		for (var presentUsing : defineData.usings())
		{
			if(presentUsing.target().symbolName().equals(identifier.symbolName()))
			{
				report(ParserErrors.duplicatedImport(identifier));
			}
		}


		var defineDataModule = sideloadDefineData(identifierReference);
		if(defineDataModule != null)
		{
			using.setReferencingModule((NaturalModule)defineDataModule);
			using.setDefineData(defineDataModule.defineData());
			for (var variable : defineDataModule.defineData().variables())
			{
				if(variable.level() == 1)
				{
					addDeclaredVariable((VariableNode) variable);
				}
			}
		}

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
		var variableLength = variable.type().length();
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

			if (variableLength > 0.0)
			{
				report(ParserErrors.dynamicAndFixedLength(variable));
			}
		}

		if (variableLength != 0.0)
		{
			switch (variable.type().format())
			{
				case ALPHANUMERIC, BINARY, UNICODE -> {
					if (variableLength < 1 || variableLength > VariableTypeNode.ONE_GIGABYTE)
					{
						report(ParserErrors.invalidLengthForDataTypeRange(variable, 1, VariableTypeNode.ONE_GIGABYTE));
					}
				}
				case CONTROL, DATE, LOGIC, TIME -> report(ParserErrors.typeCantHaveLength(variable));
				case FLOAT -> {
					if (variableLength != 4 && variableLength != 8)
					{
						report(ParserErrors.invalidLengthForDataType(variable, 4, 8));
					}
				}
				case INTEGER -> {
					if (variableLength != 1 && variableLength != 2 && variableLength != 4)
					{
						report(ParserErrors.invalidLengthForDataType(variable, 1, 2, 4));
					}
				}
				case NUMERIC, PACKED -> {
					var sumOfDigits = variable.type().sumOfDigits();
					if (sumOfDigits < 1 || sumOfDigits > 29)
					{
						report(ParserErrors.invalidLengthForDataTypeRange(variable, 1, 29));
					}
				}
			}
		}

		if (variableLength == 0.0)
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
					if (variable.type().initialValue().kind().isBoolean())
					{
						break;
					}
					expectInitialValueType(variable, SyntaxKind.STRING, SyntaxKind.NUMBER);
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

	private void expectInitialValueType(TypedVariableNode variableNode, SyntaxKind... expectedKinds)
	{
		for (var expectedKind : expectedKinds)
		{
			if(variableNode.type().initialValue().kind() == expectedKind)
			{
				return;
			}
		}

		if (!variableNode.type().initialValue().kind().isSystemVariable()) // TODO(system-variables): Check type
		{
			report(ParserErrors.initValueMismatch(variableNode, expectedKinds));
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

		if (!peekKind(SyntaxKind.RPAREN) && !peekKind(SyntaxKind.NUMBER) && !peekKind(SyntaxKind.COMMA)) // special case for (*)
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
			return token.token().source().contains(",")
				? Integer.parseInt(token.token().source().split(",")[0])
				: token.token().intValue();
		}

		if (token.token().kind().isIdentifier())
		{
			var isUnboundV = token.token().symbolName().equals("V"); // (1:V) is allowed in parameter scope, where V stands for unbound

			if (currentScope.isParameter() && isUnboundV && !isVariableDeclared(token.token().symbolName()))
			{
				return ArrayDimension.UNBOUND_VALUE;
			}

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
	 * Workaround when the lower bound of an array was consumed as identifier, because
	 * apparently / is a valid character for identifiers.
	 *
	 * @param typedVariable the variable to add the dimensions to.
	 */
	private void addArrayDimensionsWorkaroundSlash(TypedVariableNode typedVariable) throws ParseError
	{
		var identifierToken = previousToken();
		var relevantSource = identifierToken.source().substring(identifierToken.source().indexOf('/'));

		var slashToken = SyntheticTokenNode.fromToken(identifierToken, SyntaxKind.SLASH, "/");
		typedVariable.addNode(slashToken);

		var boundTokenKind = relevantSource.substring(1).matches("\\d+,?\\d*")
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

		if (boundTokenKind == SyntaxKind.NUMBER && boundToken.token().source().contains(",") && peekKind(SyntaxKind.RPAREN))
		{
			// We're here because we found something like: (A10/5,10)
			// At this position, boundToken has 5,10 which is two dimensions: 1:5 and 1:10
			// before the tokens get recognized separately within the Lexer, we have to add both bounds now.
			var bothNumbers = boundToken.token().source().split(",");
			var firstDimensionBound = Integer.parseInt(bothNumbers[0]);
			var secondDimensionBound = Integer.parseInt(bothNumbers[1]);
			dimension.setLowerBound(1);
			dimension.setUpperBound(firstDimensionBound);
			typedVariable.addDimension(dimension);
			var secondDimension = new ArrayDimension();
			secondDimension.setLowerBound(1);
			secondDimension.setUpperBound(secondDimensionBound);
			secondDimension.addNode(boundToken);
			typedVariable.addDimension(secondDimension);
			return;
		}

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
				if (peekKind(SyntaxKind.IDENTIFIER) && peek().source().contains(","))
				{
					// Workaround for (A01/1:V,1:#MAX) where V,1 gets recognized as identifier, but V is the actual identifier
					var identifier = peek().source().split(",")[0];
					var syntheticIdentifier = SyntheticTokenNode.fromToken(peek(), SyntaxKind.IDENTIFIER, identifier);
					upperBound = extractArrayBound(syntheticIdentifier, dimension);
					typedVariable.addNode(syntheticIdentifier);
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

		if (consumeOptionally(typedVariable, SyntaxKind.COMMA)
			|| relevantSource.contains(",") && peekKind(SyntaxKind.IDENTIFIER))
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

		if (variable instanceof IGroupNode && !(variable instanceof IRedefinitionNode))
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

		if (target instanceof TypedVariableNode typedTarget && typedTarget.type().hasDynamicLength())
		{
			report(ParserErrors.redefineCantTargetDynamic(redefinitionNode));
			return;
		}

		redefinitionNode.setTarget(target);

		var targetLength = calculateVariableLengthInBytes(target);
		var redefineLength = calculateVariableLengthInBytes(redefinitionNode);

		var skipLengthCheck = false;

		if (target.isArray())
		{
			var totalOccurrences = 0;
			for (var dimension : target.dimensions())
			{
				if (dimension.isLowerUnbound() || dimension.isUpperUnbound())
				{
					report(ParserErrors.redefineTargetCantBeXArray(dimension));
					skipLengthCheck = true;
				}
				else
				{
					totalOccurrences += dimension.occurerences();
				}
			}
			targetLength *= totalOccurrences;
		}

		for (var variable : redefinitionNode.variables())
		{
			if (variable instanceof ITypedVariableNode typedVariableNode)
			{
				if (typedVariableNode.type().hasDynamicLength())
				{
					report(ParserErrors.redefineCantContainVariableWithDynamicLength(typedVariableNode));
					skipLengthCheck = true;
				}
			}
		}

		if (skipLengthCheck)
		{
			return;
		}

		if (redefineLength > targetLength)
		{
			report(ParserErrors.redefinitionLengthIsTooLong(redefinitionNode, redefineLength, targetLength));
		}
	}

	private double calculateVariableLengthInBytes(IVariableNode target)
	{
		if (target instanceof ITypedVariableNode typedNode)
		{
			return typedNode.type().byteSize();
		}

		if (target instanceof IRedefinitionNode redefinitionNode)
		{
			var groupLength = 0.0;
			for (var member : redefinitionNode.variables())
			{
				if (member instanceof ITypedVariableNode typedVariableNode)
				{
					groupLength += typedVariableNode.type().byteSize();
				}
			}

			return groupLength;
		}
		else
			if (target instanceof IGroupNode groupNode)
			{
				var groupLength = 0.0;
				for (var member : groupNode.variables())
				{
					groupLength += calculateVariableLengthInBytes(member);
				}

				return groupLength;
			}

		return 0.0;
	}

	private boolean isVariableDeclared(String potentialVariableName)
	{
		return declaredVariables.containsKey(potentialVariableName.toUpperCase());
	}

	private VariableNode getDeclaredVariable(ITokenNode tokenNode)
	{
		// Natural is case-insensitive, as that it considers everything upper case
		return declaredVariables.get(tokenNode.token().symbolName());
	}

	private void addDeclaredVariable(VariableNode variable)
	{
		if(variable instanceof GroupNode groupNode)
		{
			for (var nestedVariable : groupNode.variables())
			{
				addDeclaredVariable((VariableNode)nestedVariable);
			}
		}

		if(variable instanceof IRedefinitionNode)
		{
			// Nested variables are already handled above. The #VAR in `REDEFINE #VAR` doesn't need to be added
			return;
		}

		if(declaredVariables.containsKey(variable.name()))
		{
			var alreadyDefined = declaredVariables.get(variable.name());
			if(!variable.qualifiedName().equals(alreadyDefined.qualifiedName()))
			{
				declaredVariables.remove(variable.name());
				declaredVariables.put(alreadyDefined.qualifiedName(), alreadyDefined);
				declaredVariables.put(variable.qualifiedName(), variable);
				alreadyDefined.useQualifiedName();
				variable.useQualifiedName();
			}
			else
			{
				report(ParserErrors.duplicatedSymbols(variable, alreadyDefined));
			}

			return;
		}

		declaredVariables.put(variable.name(), variable);
	}
}
