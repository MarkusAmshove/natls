package org.amshove.natparse.parsing;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFileType;

import java.util.*;

public class DefineDataParser extends AbstractParser<IDefineData>
{
	private static final List<SyntaxKind> SCOPE_SYNTAX_KINDS = List.of(SyntaxKind.LOCAL, SyntaxKind.PARAMETER, SyntaxKind.GLOBAL, SyntaxKind.INDEPENDENT);

	/**
	 * Do not use this directly, use getDeclaredVariable or isVariableDeclared for proper case handling. Also use
	 * addDeclaredVariable for error handling.
	 */
	private Map<String, VariableNode> declaredVariables;
	private Deque<GroupNode> groupStack;

	private VariableScope currentScope;

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
		groupStack = new ArrayDeque<>();

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

		for (var variable : defineData.variables())
		{
			if (variable instanceof GroupNode groupNode)
			{
				checkGroupIsNotEmpty(groupNode);
				if (groupNode.level() == 1 && !(groupNode instanceof IRedefinitionNode))
				{
					ensureAllConstOrNoneConst(groupNode.variables(), new GroupConstStatistic());
				}
			}

			if (variable instanceof RedefinitionNode redefinitionNode)
			{
				addTargetToRedefine(redefinitionNode);
				checkRedefineLength(redefinitionNode);
			}

		}

		return defineData;
	}

	private void checkGroupIsNotEmpty(GroupNode groupNode)
	{
		if (groupNode instanceof ViewNode)
		{
			return;
		}
		if (groupNode.variables().isEmpty())
		{
			report(ParserErrors.emptyGroupVariable(groupNode));
		}
	}

	private BaseSyntaxNode dataDefinition() throws ParseError
	{
		if (!isScopeToken(peek()) && !peekKind(SyntaxKind.BLOCK))
		{
			report(ParserErrors.unexpectedToken(SCOPE_SYNTAX_KINDS, tokens));
			discard();
			throw new ParseError(peek());
		}

		if (peek(1).kind() == SyntaxKind.USING)
			return using();
		else
			return scope();
	}

	private ScopeNode scope() throws ParseError
	{
		var scope = consumeAny(SCOPE_SYNTAX_KINDS);
		var scopeNode = new ScopeNode();
		currentScope = VariableScope.fromSyntaxKind(scope.kind());

		scopeNode.addNode(new TokenNode(scope));
		scopeNode.setScope(currentScope);

		while (peekKind(SyntaxKind.NUMBER_LITERAL) || peekKind(SyntaxKind.BLOCK)) // level or BLOCK
		{
			try
			{
				popGroupsIfNecessary();

				if (peekKind(SyntaxKind.BLOCK))
				{
					/*var block = */
					scopeNode.addNode(block());
				}

				var variable = variable(currentGroupsDimensions());
				variable.setScope(currentScope);
				for (var dimension : variable.dimensions())
				{
					checkBounds(dimension);
				}

				if (variable.scope().isIndependent())
				{
					checkIndependentVariable(variable);
				}

				if (peekKind(1, SyntaxKind.FILLER) && peekKind(2, SyntaxKind.OPERAND_SKIP))
				{
					var currentRedefineNode = currentRedefine(variable);
					if (currentRedefineNode != null)
					{
						while (mightBeFillerBytes(peek(1), peek(2)))
						{
							parseRedefineFiller(currentRedefineNode);
						}
					}
					else
					{
						report(ParserErrors.unexpectedToken(peek(1), "FILLER can only be used in redefinitions"));
					}
				}

				if (variable.level() == 1)
				{
					scopeNode.addVariable(variable);
				}
				else
				{
					addVariableToCurrentGroup(variable);
				}

				addDeclaredVariable(variable);

				if (variable instanceof GroupNode groupNode)
				{
					groupStack.addLast(groupNode);
				}
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

		passDownArrayDimensions(scopeNode);
		groupStack.clear();

		return scopeNode;
	}

	private void passDownArrayDimensions(ScopeNode scope)
	{
		for (var variable : scope.variables())
		{
			inheritDimensions(variable);
		}
	}

	private void inheritDimensions(IVariableNode variable)
	{
		if (variable.parent()instanceof IVariableNode parentVariable && parentVariable.isArray())
		{
			((VariableNode) variable).inheritDimensions(parentVariable.dimensions());
		}

		if (variable instanceof GroupNode group)
		{
			for (var subVariable : group.variables())
			{
				inheritDimensions(subVariable);
			}
		}
	}

	private UsingNode using() throws ParseError
	{
		var using = new UsingNode();

		var scopeToken = consumeAny(SCOPE_SYNTAX_KINDS);
		using.setScope(scopeToken.kind());
		using.addNode(new TokenNode(scopeToken));

		consume(using, SyntaxKind.USING);

		var identifier = consumeIdentifierTokenOnly();
		using.setUsingTarget(identifier);
		var identifierTokenNode = new TokenNode(identifier);
		using.addNode(identifierTokenNode);

		// For "GLOBAL USING WITH BLOCK"
		if (using.isGlobalUsing() && consumeOptionally(using, SyntaxKind.WITH))
		{
			using.setWithBlock(consumeIdentifierTokenOnly());
		}

		for (var presentUsing : defineData.usings())
		{
			if (presentUsing.target().symbolName().equals(identifier.symbolName()))
			{
				report(ParserErrors.duplicatedImport(identifier));
			}
		}

		var defineDataModule = sideloadDefineData(identifierTokenNode);
		if (defineDataModule != null)
		{
			using.setReferencingModule((NaturalModule) defineDataModule);
			for (var diagnostic : ((NaturalModule) defineDataModule).diagnostics())
			{
				if (diagnostic instanceof ParserDiagnostic pd)
				{
					report(pd.relocate(identifierTokenNode.diagnosticPosition()));
				}
			}
			using.setDefineData(defineDataModule.defineData());
			for (var variable : defineDataModule.defineData().variables())
			{
				addDeclaredVariable((VariableNode) variable, using);
			}

			if (using.isParameterUsing()
				&& ((NaturalModule) defineDataModule).file().getFiletype() != NaturalFileType.PDA)
			{
				report(
					ParserErrors.invalidModuleType(
						"Only PDAs can be used for PARAMETER USING",
						identifierTokenNode.token()
					)
				);
			}

			if (using.isLocalUsing()
				&& ((NaturalModule) defineDataModule).file().getFiletype() == NaturalFileType.GDA)
			{
				report(
					ParserErrors.invalidModuleType(
						"Only LDAs and PDAs can be used for LOCAL USING",
						identifierTokenNode.token()
					)
				);
			}
		}

		return using;
	}

	private BlockNode block() throws ParseError
	{
		var block = new BlockNode();
		consumeOptionally(block, SyntaxKind.BLOCK);

		var identifier = consumeIdentifierTokenOnly();
		block.setBlock(identifier);

		if (consumeOptionally(block, SyntaxKind.CHILD) && consumeOptionally(block, SyntaxKind.OF))
		{
			identifier = consumeIdentifierTokenOnly();
			block.setParent(identifier);
		}

		return block;
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

			var level = consumeMandatory(variable, SyntaxKind.NUMBER_LITERAL).intValue();
			variable.setLevel(level);

			if (consumeOptionally(variable, SyntaxKind.REDEFINE))
			{
				variable = new RedefinitionNode(variable);
			}

			var identifierNode = consumeMandatoryIdentifierTokenNode(variable);
			variable.setDeclaration(identifierNode);

			if (consumeOptionally(variable, SyntaxKind.LPAREN)
				&& (peek().kind() != SyntaxKind.ASTERISK && peek().kind() != SyntaxKind.NUMBER_LITERAL)) // group array
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
		var groupNode = variable instanceof RedefinitionNode redefine
			? redefine
			: new GroupNode(variable);

		if (variable.dimensions().hasItems())
		{
			for (var dimension : variable.dimensions())
			{
				if (!groupNode.dimensions.contains(dimension))
				{
					groupNode.addDimension((ArrayDimension) dimension);
				}
			}
		}

		if (previousToken().kind() == SyntaxKind.LPAREN)
		{
			addArrayDimensions(groupNode);
			consumeMandatory(groupNode, SyntaxKind.RPAREN);
		}

		return groupNode;
	}

	private void ensureAllConstOrNoneConst(Iterable<IVariableNode> variables, GroupConstStatistic statistic)
	{
		for (var variable : variables)
		{
			if (variable instanceof IGroupNode nestedGroup && !(variable instanceof IRedefinitionNode))
			{
				ensureAllConstOrNoneConst(nestedGroup.variables(), statistic);
				continue;
			}

			if (variable instanceof ITypedVariableNode typedVar
				&& typedVar.type() != null
				&& !(typedVar.parent() instanceof IRedefinitionNode)) // doesn't matter for REDEFINE children
			{
				if (typedVar.type().isConstant())
				{
					statistic.constEncountered++;
				}
				else
				{
					statistic.nonConstEncountered++;
				}

				if (statistic.hasMixedConst())
				{
					report(ParserErrors.groupHasMixedConstVariables(variable.identifierNode()));
				}
			}
		}
	}

	private boolean mightBeFillerBytes(SyntaxToken fillerToken, SyntaxToken maybeFillerBytes)
	{
		if (fillerToken == null || maybeFillerBytes == null)
		{
			return false;
		}

		return maybeFillerBytes.kind() == SyntaxKind.OPERAND_SKIP
			// This happens when it's e.g.
			// 2 FILLER 5
			// the user forgot the X but meant to write a filler, because the number is in the same line.
			// we can use this information to raise a better diagnostic message.
			|| (maybeFillerBytes.kind() == SyntaxKind.NUMBER_LITERAL && maybeFillerBytes.line() == fillerToken.line());
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

	private VariableNode typedVariable(VariableNode variable) throws ParseError
	{
		var typedVariable = new TypedVariableNode(variable);
		var type = new VariableType();

		if (peekKind(SyntaxKind.RPAREN))
		{
			report(ParserErrors.incompleteArrayDefinition(variable));
			throw new ParseError(peek());
		}

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
			var number = consumeMandatory(typedVariable, SyntaxKind.NUMBER_LITERAL);
			length = getLengthFromDataType(dataType + "." + number.source());
		}
		type.setLength(length);
		typedVariable.setType(type);

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

			if (previousToken().fileType() == NaturalFileType.PDA)
			{
				report(ParserErrors.unexpectedToken(previousToken(), "CONST and INIT are not allowed in PDAs"));
			}

			if (consumeOptionally(typedVariable, SyntaxKind.FULL))
			{
				consumeMandatory(typedVariable, SyntaxKind.LENGTH);
			}

			if (consumeOptionally(typedVariable, SyntaxKind.LENGTH))
			{
				consumeMandatory(typedVariable, SyntaxKind.NUMBER_LITERAL);
			}

			if (consumeOptionally(typedVariable, SyntaxKind.LESSER_GREATER))
			{
				// special case for a better error message. <> is  just an empty initial value
				report(ParserErrors.emptyInitValue(typedVariable));
			}
			else
			{
				if (!typedVariable.dimensions().isEmpty())
				{
					consumeArrayInitializer(typedVariable);
				}
				else
				{
					consumeMandatory(typedVariable, SyntaxKind.LESSER_SIGN);
					if (peek().kind().isSystemVariable())
					{
						type.setInitialValue(consumeSystemVariableNode(typedVariable));
					}
					else
					{
						var literal = consumeLiteralNode(typedVariable);
						type.setInitialValue(literal);
					}
					consumeMandatory(typedVariable, SyntaxKind.GREATER_SIGN);
				}
			}
		}

		if (consumeOptionally(typedVariable, SyntaxKind.LPAREN))
		{
			if (currentScope == VariableScope.PARAMETER)
			{
				report(ParserErrors.emhdpmNotAllowedInCurrentScope(getPreviousNode(), currentScope));
			}

			while (!isAtEnd() && peek().kind() != SyntaxKind.RPAREN && peek().kind() != SyntaxKind.END_DEFINE)
			{
				consume(typedVariable);
			}
			consumeMandatory(typedVariable, SyntaxKind.RPAREN);
		}

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
		consumeOptionally(typedVariable, SyntaxKind.NUMBER_LITERAL);

		if (peekKind(SyntaxKind.LPAREN))
		{
			var lparen = consumeMandatory(typedVariable, SyntaxKind.LPAREN);

			while (!consumeOptionally(typedVariable, SyntaxKind.RPAREN) && peek().line() == lparen.line())
			{
				consume(typedVariable);
			}
		}

		if (peekKind(SyntaxKind.LESSER_SIGN))
		{
			consumeMandatory(typedVariable, SyntaxKind.LESSER_SIGN);

			while (!consumeOptionally(typedVariable, SyntaxKind.GREATER_SIGN) && peek().kind() != SyntaxKind.END_DEFINE)
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

	private boolean isScopeToken(SyntaxToken token)
	{
		return SCOPE_SYNTAX_KINDS.contains(token.kind());
	}

	private static void advanceToDefineData(TokenList tokens)
	{
		while (!tokens.isAtEnd(1) && !(tokens.peek().kind() == SyntaxKind.DEFINE && tokens.peek(1).kind() == SyntaxKind.DATA))
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
				case ALPHANUMERIC, BINARY, UNICODE ->
				{}
				case CONTROL, DATE, FLOAT, INTEGER, LOGIC, NUMERIC, PACKED, TIME, NONE -> report(ParserErrors.dynamicVariableLengthNotAllowed(variable));
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
				case ALPHANUMERIC, BINARY, UNICODE ->
				{
					if (variableLength < 1 || variableLength > VariableType.ONE_GIGABYTE)
					{
						report(ParserErrors.invalidLengthForDataTypeRange(variable, 1, VariableType.ONE_GIGABYTE));
					}
				}
				case CONTROL, DATE, LOGIC, TIME -> report(ParserErrors.typeCantHaveLength(variable));
				case FLOAT ->
				{
					if (variableLength != 4 && variableLength != 8)
					{
						report(ParserErrors.invalidLengthForDataType(variable, 4, 8));
					}
				}
				case INTEGER ->
				{
					if (variableLength != 1 && variableLength != 2 && variableLength != 4)
					{
						report(ParserErrors.invalidLengthForDataType(variable, 1, 2, 4));
					}
				}
				case NUMERIC, PACKED ->
				{
					var sumOfDigits = variable.type().sumOfDigits();
					if (sumOfDigits < 1 || sumOfDigits > 29)
					{
						report(ParserErrors.invalidLengthForDataTypeRange(variable, 1, 29));
					}
				}
				default ->
				{}
			}
		}

		if (variableLength == 0.0)
		{
			switch (variable.type().format())
			{
				case ALPHANUMERIC, BINARY, UNICODE ->
				{
					if (!variable.type().hasDynamicLength())
					{
						report(ParserErrors.dataTypeNeedsLength(variable));
					}
				}
				case CONTROL, DATE, LOGIC, TIME, NONE ->
				{}
				case FLOAT, INTEGER, NUMERIC, PACKED -> report(ParserErrors.dataTypeNeedsLength(variable));
			}
		}

		if (variable.type().initialValue() != null)
		{
			var initialValueKind = inferInitialValueKind(variable.type().initialValue());

			switch (variable.type().format())
			{
				case ALPHANUMERIC ->
				{
					if (initialValueKind.isBoolean())
					{
						break;
					}
					expectInitialValueType(variable, initialValueKind, SyntaxKind.STRING_LITERAL, SyntaxKind.NUMBER_LITERAL, SyntaxKind.HEX_LITERAL);
				}
				case DATE -> expectInitialValueType(variable, initialValueKind, SyntaxKind.DATE_LITERAL);
				case BINARY, CONTROL, TIME, UNICODE, NONE ->
				{}
				case FLOAT, NUMERIC, PACKED, INTEGER -> expectInitialValueType(variable, initialValueKind, SyntaxKind.NUMBER_LITERAL);
				case LOGIC ->
				{
					if (initialValueKind != SyntaxKind.TRUE && initialValueKind != SyntaxKind.FALSE)
					{
						report(ParserErrors.initValueMismatch(variable, SyntaxKind.TRUE, SyntaxKind.FALSE));
					}
				}
			}
		}
	}

	private SyntaxKind inferInitialValueKind(IOperandNode initialValueNode)
	{
		if (initialValueNode instanceof ITokenNode tokenNode)
		{
			return tokenNode.token().kind();
		}

		return SyntaxKind.STRING_LITERAL; // concat
	}

	private void expectInitialValueType(TypedVariableNode variableNode, SyntaxKind initialValueKind, SyntaxKind... expectedKinds)
	{
		for (var expectedKind : expectedKinds)
		{
			if (initialValueKind == expectedKind)
			{
				return;
			}
		}

		if (!initialValueKind.isSystemVariable())
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

		while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN) && !peekKind(SyntaxKind.COMMA))
		{
			var dimension = new ArrayDimension();
			var lowerBound = extractArrayBound(new TokenNode(peek()), dimension);
			var upperBound = 0;
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
			while (!isAtEnd() && !peekKind(SyntaxKind.COMMA) && !peekKind(SyntaxKind.RPAREN))
			{
				consume(dimension);
			}
		}
	}

	private int extractArrayBound(ITokenNode token, ArrayDimension dimension)
	{
		if (token.token().kind() == SyntaxKind.NUMBER_LITERAL)
		{
			return token.token().source().contains(",")
				? Integer.parseInt(token.token().source().split(",")[0])
				: token.token().intValue();
		}

		if (token.token().kind().isIdentifier())
		{
			var isUnboundV = token.token().symbolName().equals("V"); // (1:V) is allowed in parameter scope, where V stands for variable

			if (currentScope.isParameter() && isUnboundV && isVariableUndeclared(token.token().symbolName()))
			{
				return IArrayDimension.VARIABLE_BOUND;
			}

			if (isVariableUndeclared(token.token().symbolName()))
			{
				report(ParserErrors.unresolvedReference(token));
				return IArrayDimension.UNBOUND_VALUE;
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
				return ((ILiteralNode) typedNode.type().initialValue()).token().intValue();
			}
		}

		return IArrayDimension.UNBOUND_VALUE;
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

		var boundTokenKind = relevantSource.substring(1).matches("\\d+,?\\d*")
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

		if (boundTokenKind == SyntaxKind.NUMBER_LITERAL && boundToken.token().source().contains(",") && peekKind(SyntaxKind.RPAREN))
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

	private void checkIndependentVariable(VariableNode variable)
	{
		if (currentRedefine(variable) != null)
		{
			return;
		}

		if (!variable.name().startsWith("+"))
		{
			report(ParserErrors.invalidAivNaming(variable));
		}

		if (variable instanceof IGroupNode && !(variable instanceof IRedefinitionNode))
		{
			report(ParserErrors.independentCantBeGroup(variable));
		}
	}

	private void addTargetToRedefine(RedefinitionNode redefinitionNode)
	{
		if (redefinitionNode.parent()instanceof ScopeNode scope)
		{
			addTargetToRedefine(scope.variables(), redefinitionNode);
		}

		if (redefinitionNode.parent()instanceof IGroupNode group)
		{
			addTargetToRedefine(group.variables(), redefinitionNode);
		}
	}

	private void addTargetToRedefine(Iterable<IVariableNode> possibleVariables, RedefinitionNode redefinitionNode)
	{
		IVariableNode target = null;

		for (var variable : possibleVariables)
		{
			if (variable.name() != null && variable.name().equalsIgnoreCase(redefinitionNode.name()))
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

		if (target instanceof TypedVariableNode typedTarget
			&& typedTarget.type() != null // TODO: no types for view stuff yet :(
			&& typedTarget.type().hasDynamicLength())
		{
			report(ParserErrors.redefineCantTargetDynamic(redefinitionNode));
			return;
		}

		redefinitionNode.setTarget(target);

		// length check for redefine will be done afterward
	}

	private void checkRedefineLength(IRedefinitionNode redefinitionNode)
	{
		var target = redefinitionNode.target();

		if (target instanceof ITypedVariableNode typedTarget && typedTarget.type() == null)
		{
			// The target is a VIEW variable which has no explicit type
			return;
		}

		var targetLength = calculateVariableLengthInBytes(target);
		var redefineLength = calculateVariableLengthInBytes(redefinitionNode);
		var skipLengthCheck = false;

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

	private int calculateVariableLengthInBytes(IVariableNode target)
	{
		if (target instanceof ITypedVariableNode typedNode)
		{
			if (typedNode.isArray())
			{
				return calculateLengthInBytesWithArray(typedNode);
			}

			return typedNode.type().byteSize();
		}

		if (target instanceof IRedefinitionNode redefinitionNode)
		{
			var groupLength = 0;
			for (var member : redefinitionNode.variables())
			{
				if (member instanceof ITypedVariableNode typedVariableNode)
				{
					if (typedVariableNode.isArray())
					{
						groupLength += calculateLengthInBytesWithArray(typedVariableNode);
					}
					else
					{
						groupLength += typedVariableNode.type().byteSize();
					}
				}
			}

			return groupLength;
		}
		else
			if (target instanceof IGroupNode groupNode)
			{
				var groupLength = 0;
				for (var member : groupNode.variables())
				{
					groupLength += calculateVariableLengthInBytes(member);
				}

				return groupLength;
			}

		return 0;
	}

	private int calculateLengthInBytesWithArray(ITypedVariableNode target)
	{
		var totalOccurrences = 0;
		for (var dimension : target.dimensions())
		{
			if (dimension.isLowerUnbound() || dimension.isUpperUnbound())
			{
				report(ParserErrors.redefineTargetCantBeXArray(dimension));
			}
			else
			{
				totalOccurrences = totalOccurrences == 0 ? dimension.occurerences() : totalOccurrences * dimension.occurerences();
			}
		}

		return (target.type().byteSize()) * totalOccurrences;
	}

	private boolean isVariableUndeclared(String potentialVariableName)
	{
		return !declaredVariables.containsKey(potentialVariableName.toUpperCase());
	}

	private VariableNode getDeclaredVariable(ITokenNode tokenNode)
	{
		// Natural is case-insensitive, as that it considers everything upper case
		return declaredVariables.get(tokenNode.token().symbolName());
	}

	private void addDeclaredVariable(VariableNode variable)
	{
		addDeclaredVariable(variable, variable);
	}

	private void addDeclaredVariable(VariableNode variable, ISyntaxNode diagnosticPosition)
	{
		if (variable.level() > 1 && variable.parent() == null)
		{
			// will be added by the group in `groupVariable()` after it has been assigned its parent
			return;
		}

		if (variable instanceof RedefinitionNode)
		{
			// REDEFINE doesn't need to be a declared variable, because
			// the target of REDEFINE has been declared.
			return;
		}

		if (declaredVariables.containsKey(variable.name()))
		{
			var alreadyDefined = declaredVariables.get(variable.name());
			if (alreadyDefined.position().isSamePositionAs(variable.position()))
			{
				return;
			}

			if (!variable.qualifiedName().equals(alreadyDefined.qualifiedName()))
			{
				// Variable with the same name exists, but qualified names differ.
				// Re-add the old with the qualified name and also add the new one
				// qualified
				declaredVariables.remove(variable.name());
				declaredVariables.put(alreadyDefined.qualifiedName(), alreadyDefined);
				declaredVariables.put(variable.qualifiedName(), variable);
			}
			else
			{
				report(ParserErrors.duplicatedSymbols(variable, alreadyDefined, diagnosticPosition));
			}

			return;
		}

		declaredVariables.put(variable.name(), variable);
	}

	private void addVariableToCurrentGroup(VariableNode variable)
	{
		if (groupStack.isEmpty())
		{
			return;
		}

		var last = groupStack.peekLast();
		if (last.level() == variable.level() - 1)
		{
			last.addVariable(variable);
		}
	}

	private void popGroupsIfNecessary()
	{
		if (groupStack.isEmpty())
		{
			return;
		}

		if (!peekKind(SyntaxKind.NUMBER_LITERAL))
		{
			return;
		}

		var newLevel = peek().intValue();
		while (!groupStack.isEmpty() && newLevel <= groupStack.peekLast().level())
		{
			groupStack.removeLast();
		}
	}

	private RedefinitionNode currentRedefine(VariableNode currentVar)
	{
		if (currentVar instanceof RedefinitionNode redefine)
		{
			return redefine;
		}

		if (groupStack.isEmpty())
		{
			return null;
		}

		for (var group : groupStack)
		{
			if (group instanceof RedefinitionNode redefine)
			{
				return redefine;
			}
		}

		return null;
	}

	private List<IArrayDimension> currentGroupsDimensions()
	{
		if (groupStack.isEmpty())
		{
			return List.of();
		}
		return groupStack.peekLast().getDimensions();
	}

	private static class GroupConstStatistic
	{
		private int constEncountered;
		private int nonConstEncountered;

		private boolean hasMixedConst()
		{
			return constEncountered > 0 && nonConstEncountered > 0;
		}
	}

	@SuppressWarnings("ClassEscapesDefinedScope")
	@Override
	protected ITokenNode consumeMandatoryIdentifierTokenNode(BaseSyntaxNode node)
	{
		var currentToken = tokens.peek();
		if (tokens.isAtEnd() || (currentToken.kind() != SyntaxKind.IDENTIFIER && !currentToken.kind().canBeIdentifier()))
		{
			// In case of DEFINE DATA we don't throw here to keep parsing a whole DEFINE DATA.
			// These variables won't be resolvable though, because the original implementation
			// that the StatementListParser uses is throwing, which is fine.
			report(ParserErrors.unexpectedTokenWhenIdentifierWasExpected(currentToken));
		}

		tokens.advance();
		var tokenNode = new TokenNode(currentToken.withKind(SyntaxKind.IDENTIFIER));
		node.addNode(tokenNode);
		return tokenNode;
	}
}
