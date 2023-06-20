package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

abstract class AbstractParser<T>
{
	protected IModuleProvider moduleProvider;
	protected TokenList tokens;
	protected List<ISymbolReferenceNode> unresolvedReferences;
	private TokenNode previousNode;

	private List<IDiagnostic> diagnostics = new ArrayList<>();
	protected IPosition relocatedDiagnosticPosition;

	protected static final Set<SyntaxKind> END_KINDS_THAT_END_ALL_ENDS = Set.of(SyntaxKind.END_REPEAT, SyntaxKind.END_FOR, SyntaxKind.END_FILE, SyntaxKind.END_LOOP, SyntaxKind.END_SORT, SyntaxKind.END_WORK, SyntaxKind.END_READ, SyntaxKind.END_FIND, SyntaxKind.END_HISTOGRAM);

	public AbstractParser(IModuleProvider moduleProvider)
	{
		this.moduleProvider = moduleProvider;
	}

	void setModuleProvider(IModuleProvider moduleProvider)
	{
		this.moduleProvider = moduleProvider;
	}

	public ParseResult<T> parse(TokenList tokens)
	{
		this.tokens = tokens;
		diagnostics = new ArrayList<>();

		var result = parseInternal();

		return new ParseResult<>(result, ReadOnlyList.from(diagnostics));
	}

	protected abstract T parseInternal();

	protected boolean shouldRelocateDiagnostics()
	{
		return relocatedDiagnosticPosition != null;
	}

	protected INaturalModule sideloadModule(String referableName, SyntaxToken moduleIdentifierToken)
	{
		if (moduleProvider == null)
		{
			return null;
		}

		var module = moduleProvider.findNaturalModule(referableName);

		if (module == null
			&& !(referableName.startsWith("USR") && referableName.endsWith("N"))
			&& !referableName.equals("SHCMD"))
		{
			report(ParserErrors.unresolvedExternalModule(moduleIdentifierToken));
		}

		return module;
	}

	protected IHasDefineData sideloadDefineData(TokenNode importNode)
	{
		if (sideloadModule(importNode.token().symbolName(), importNode.token())instanceof IHasDefineData hasDefineData)
		{
			return hasDefineData;
		}

		return null;
	}

	protected TokenNode getPreviousNode()
	{
		return previousNode;
	}

	protected SyntaxToken peek()
	{
		return tokens.peek();
	}

	/**
	 * Safely peeks the current kind.
	 */
	protected SyntaxKind peekKind()
	{
		var token = peek();
		if (token == null)
		{
			return SyntaxKind.EOF;
		}

		return token.kind();
	}

	protected boolean peekKind(int offset, SyntaxKind kind)
	{
		return !isAtEnd(offset) && peek(offset).kind() == kind;
	}

	/**
	 * Returns the kind of the token at the given offset. If the offset is out of bounds, returns EOF.
	 */
	protected SyntaxKind getKind(int offset)
	{
		if (isAtEnd(offset))
		{
			return SyntaxKind.EOF;
		}

		return peek(offset).kind();
	}

	protected boolean peekKind(SyntaxKind kind)
	{
		return peekKind(0, kind);
	}

	/**
	 * Consumes the next token.
	 *
	 * @param node to add to
	 */
	protected SyntaxToken consume(BaseSyntaxNode node)
	{
		if (!isAtEnd())
		{
			var token = tokens.advance();
			previousNode = new TokenNode(token);
			node.addNode(previousNode);
			return token;
		}

		return null;
	}

	protected SyntaxToken peek(int offset)
	{
		return tokens.peek(offset);
	}

	/**
	 * Consumes the current token only if the kind matches. This will not add any diagnostics.
	 *
	 * @param node the node to add the token to
	 * @param kind the kind of the token that should be consumed
	 * @return Whether the token was consumed or not
	 */
	protected boolean consumeOptionally(BaseSyntaxNode node, SyntaxKind kind)
	{
		if (!tokens.isAtEnd() && tokens.peek().kind() == kind)
		{
			previousNode = new TokenNode(tokens.peek());
			node.addNode(previousNode);
		}

		return tokens.consume(kind);
	}

	/**
	 * Consumes either firstKind, secondKind or none. This will not add any diagnostics.
	 *
	 * @param node the node to add the token to
	 * @param firstKind the first possible kind
	 * @param secondKind the second possible kind
	 * @return Whether any token was consumed or not
	 */
	protected boolean consumeEitherOptionally(BaseSyntaxNode node, SyntaxKind firstKind, SyntaxKind secondKind)
	{
		if (!tokens.isAtEnd() && (tokens.peek().kind() == firstKind || tokens.peek().kind() == secondKind))
		{
			previousNode = new TokenNode(tokens.peek());
			node.addNode(previousNode);
			tokens.advance();
			return true;
		}

		return false;
	}

	protected boolean consume(BaseSyntaxNode node, SyntaxKind kind)
	{
		var tokenConsumed = consumeOptionally(node, kind);
		if (!tokenConsumed)
		{
			diagnostics.add(ParserErrors.unexpectedToken(kind, tokens));
		}

		return tokenConsumed;
	}

	protected SyntaxToken consumeMandatory(BaseSyntaxNode node, SyntaxKind kind) throws ParseError
	{
		if (consumeOptionally(node, kind))
		{
			return previousToken();
		}

		diagnostics.add(ParserErrors.unexpectedToken(kind, tokens));
		throw new ParseError(peek());
	}

	protected SyntaxToken consumeMandatoryClosing(BaseSyntaxNode node, SyntaxKind closingTokenType, SyntaxToken openingToken) throws ParseError
	{
		if (peekKind(SyntaxKind.END_ALL) && END_KINDS_THAT_END_ALL_ENDS.contains(closingTokenType)) // sort
		{
			return peek();
		}

		if (!consumeOptionally(node, closingTokenType))
		{
			diagnostics.add(ParserErrors.missingClosingToken(closingTokenType, openingToken));
			throw new ParseError(peek());
		}

		return previousToken();
	}

	protected ILiteralNode consumeLiteralNode(BaseSyntaxNode node) throws ParseError
	{
		if (peekKind(SyntaxKind.LPAREN))
		{
			var attribute = new AttributeNode(peek());
			node.addNode(attribute);
			consumeMandatory(node, SyntaxKind.LPAREN);
			while (!isAtEnd() && peek().kind() != SyntaxKind.RPAREN && peek().kind() != SyntaxKind.END_DEFINE)
			{
				// TODO(attributes): Look for the actual value after the '=' as initial value token
				consume(attribute);
			}
			consumeMandatory(node, SyntaxKind.RPAREN);
			return attribute;
		}

		if (peekKind(SyntaxKind.MINUS) && peekKind(1, SyntaxKind.NUMBER_LITERAL))
		{
			var combinedLiteral = peek().combine(peek(1), SyntaxKind.NUMBER_LITERAL);
			var literal = new LiteralNode(combinedLiteral);
			discard();
			discard();
			node.addNode(literal);
			return literal;
		}

		var literal = consumeAny(List.of(SyntaxKind.NUMBER_LITERAL, SyntaxKind.STRING_LITERAL, SyntaxKind.HEX_LITERAL, SyntaxKind.TRUE, SyntaxKind.FALSE, SyntaxKind.ASTERISK, SyntaxKind.DATE_LITERAL, SyntaxKind.TIME_LITERAL, SyntaxKind.EXTENDED_TIME_LITERAL));
		var literalNode = new LiteralNode(literal);
		node.addNode(literalNode);
		return literalNode;
	}

	protected ILiteralNode consumeLiteralNode(BaseSyntaxNode node, SyntaxKind literalKind) throws ParseError
	{
		var literal = consumeLiteralNode(node);
		if (literal.token().kind() != literalKind)
		{
			report(ParserErrors.unexpectedTokenUnsafe(literalKind, literal.token()));
		}

		return literal;
	}

	protected SyntaxToken consumeLiteral(BaseSyntaxNode node) throws ParseError
	{
		if (peek().kind().isSystemVariable())
		{
			var systemVariable = peek();
			node.addNode(new SystemVariableNode(systemVariable));
			discard();
			return systemVariable;
		}

		if (peek().kind() == SyntaxKind.LPAREN) // Attributes
		{
			var lparen = peek(); // TODO(attributes): This is not correct but good for now.
			while (!isAtEnd() && peek().kind() != SyntaxKind.RPAREN && peek().kind() != SyntaxKind.END_DEFINE)
			{
				consume(node);
			}
			consumeMandatory(node, SyntaxKind.RPAREN);
			return lparen;
		}

		var literal = consumeAny(List.of(SyntaxKind.NUMBER_LITERAL, SyntaxKind.STRING_LITERAL, SyntaxKind.HEX_LITERAL, SyntaxKind.TRUE, SyntaxKind.FALSE, SyntaxKind.DATE_LITERAL, SyntaxKind.TIME_LITERAL, SyntaxKind.EXTENDED_TIME_LITERAL));
		previousNode = new TokenNode(literal);
		node.addNode(previousNode);
		return literal;
	}

	protected ITokenNode consumeMandatoryIdentifierTokenNode(BaseSyntaxNode node) throws ParseError
	{
		var identifierToken = consumeIdentifierTokenOnly();
		previousNode = new TokenNode(identifierToken);
		node.addNode(previousNode);
		return previousNode;
	}

	protected SyntaxToken consumeMandatoryIdentifier(BaseSyntaxNode node) throws ParseError
	{
		var identifierNode = consumeMandatoryIdentifierTokenNode(node);
		return identifierNode.token();
	}

	protected SyntaxToken consumeIdentifierTokenOnly() throws ParseError
	{
		var currentToken = tokens.peek();
		if (tokens.isAtEnd() || (currentToken.kind() != SyntaxKind.IDENTIFIER && !currentToken.kind().canBeIdentifier()))
		{
			diagnostics.add(ParserErrors.unexpectedToken(SyntaxKind.IDENTIFIER, tokens));
			throw new ParseError(peek());
		}

		var identifierToken = currentToken.withKind(SyntaxKind.IDENTIFIER);
		tokens.advance();
		return identifierToken;
	}

	protected SyntaxToken consumeAny(List<SyntaxKind> acceptedKinds) throws ParseError
	{
		if (tokens.isAtEnd() || !acceptedKinds.contains(tokens.peek().kind()))
		{
			diagnostics.add(ParserErrors.unexpectedToken(acceptedKinds, tokens));
			throw new ParseError(peek());
		}

		return tokens.advance();
	}

	protected SymbolReferenceNode symbolReferenceNode(SyntaxToken token)
	{
		var node = new SymbolReferenceNode(token);
		unresolvedReferences.add(node);
		return node;
	}

	protected StatementNode identifierReference() throws ParseError
	{
		var token = consumeIdentifierTokenOnly();
		if (peekKind(SyntaxKind.LPAREN)
			&& (peekKind(1, SyntaxKind.LESSER_SIGN) || peekKind(1, SyntaxKind.LESSER_GREATER)))
		{
			return functionCall(token);
		}

		var node = symbolReferenceNode(token);
		var variableNode = new SyntheticVariableStatementNode(node);
		if (peekKind(SyntaxKind.LPAREN)
			&& !getKind(1).isAttribute()
			&& !peekKind(1, SyntaxKind.LABEL_IDENTIFIER))
		{
			consumeMandatory(node, SyntaxKind.LPAREN);
			variableNode.addDimension(consumeArrayAccess(variableNode));
			while (peekKind(SyntaxKind.COMMA))
			{
				consume(variableNode);
				variableNode.addDimension(consumeArrayAccess(variableNode));
			}
			consumeMandatory(variableNode, SyntaxKind.RPAREN);
		}
		return variableNode;
	}

	protected FunctionCallNode functionCall(SyntaxToken token) throws ParseError
	{
		var node = new FunctionCallNode();

		var functionName = new TokenNode(token);
		node.setReferencingToken(token);
		node.addNode(functionName);
		var module = sideloadModule(token.symbolName(), functionName.token());
		node.setReferencedModule((NaturalModule) module);

		consumeMandatory(node, SyntaxKind.LPAREN);
		if (peekKind(SyntaxKind.LESSER_GREATER) && peekKind(1, SyntaxKind.RPAREN))
		{
			// function call wihtout parameter
			consumeMandatory(node, SyntaxKind.LESSER_GREATER);
			consumeMandatory(node, SyntaxKind.RPAREN);
			return node;
		}

		consumeMandatory(node, SyntaxKind.LESSER_SIGN);

		while (!isAtEnd() && !peekKind(SyntaxKind.GREATER_SIGN))
		{
			var parameter = consumeModuleParameter(node);
			node.addParameter(parameter);
			consumeOptionally(node, SyntaxKind.COMMA);
		}

		if (previousToken().kind() == SyntaxKind.COMMA)
		{
			report(ParserErrors.trailingToken(previousToken()));
		}

		consumeMandatory(node, SyntaxKind.GREATER_SIGN);
		consumeMandatory(node, SyntaxKind.RPAREN);

		return node;
	}

	protected static final List<SyntaxKind> ARITHMETIC_OPERATOR_KINDS = List.of(SyntaxKind.PLUS, SyntaxKind.MINUS, SyntaxKind.ASTERISK, SyntaxKind.SLASH, SyntaxKind.EXPONENT_OPERATOR);

	protected IOperandNode consumeArithmeticExpression(BaseSyntaxNode node) throws ParseError
	{
		// Does not take operator precedence into account. Maybe some day?

		var needRParen = consumeOptionally(node, SyntaxKind.LPAREN);
		var operand = consumeOperandNode(node);

		if (needRParen && peekKind(SyntaxKind.RPAREN))
		{
			needRParen = false;
			consumeMandatory(node, SyntaxKind.RPAREN);
		}

		while (peekAny(ARITHMETIC_OPERATOR_KINDS))
		{
			var arithmetic = new ArithmeticExpressionNode();
			arithmetic.addNode((BaseSyntaxNode) operand);
			arithmetic.setLeft(operand);
			var operator = consume(arithmetic);
			arithmetic.setOperator(operator.kind());
			var rhs = consumeArithmeticExpression(arithmetic);
			arithmetic.setRight(rhs);
			node.replaceChild((BaseSyntaxNode) operand, arithmetic);
			operand = arithmetic;

			if (needRParen && peekKind(SyntaxKind.RPAREN))
			{
				needRParen = false;
				consumeMandatory(node, SyntaxKind.RPAREN);
			}
		}

		if (needRParen)
		{
			consumeMandatory(node, SyntaxKind.RPAREN);
		}
		return operand;
	}

	protected IOperandNode consumeModuleParameter(BaseSyntaxNode node) throws ParseError
	{
		if (peekKind(SyntaxKind.OPERAND_SKIP))
		{
			return consumeSkipOperand(node);
		}

		return consumeOperandNode(node);
	}

	private IOperandNode consumeSkipOperand(BaseSyntaxNode node) throws ParseError
	{
		var skip = new SkipOperandNode();
		node.addNode(skip);
		consumeMandatory(skip, SyntaxKind.OPERAND_SKIP);
		return skip;
	}

	protected IOperandNode consumeOperandNode(BaseSyntaxNode node) throws ParseError
	{
		if (peekKind(SyntaxKind.MINUS) || peekKind(SyntaxKind.PLUS))
		{
			return prefixUnary(node);
		}

		if ((peekKind(SyntaxKind.IDENTIFIER) || peek().kind().canBeIdentifier())
			&& !peek().kind().isSystemVariable() && !peek().kind().isSystemFunction() && !peekKind(SyntaxKind.LOG))
		{
			if (peekKind(1, SyntaxKind.LPAREN) && (peekKind(2, SyntaxKind.LESSER_SIGN) || peekKind(2, SyntaxKind.LESSER_GREATER)))
			{
				var token = peek();
				discard(); // this is kinda strange. reiterate on why functionCall() needs to get the token
				var functionCall = functionCall(token);
				node.addNode(functionCall);
				return functionCall;
			}
			return consumeVariableReferenceNode(node);
		}
		if (peek().kind().isSystemVariable() && peek().kind().isSystemFunction()) // can be both, like *COUNTER
		{
			return peekKind(1, SyntaxKind.LPAREN) && !getKind(2).isAttribute() ? consumeSystemFunctionNode(node) : consumeSystemVariableNode(node);
		}
		if (peek().kind().isSystemVariable())
		{
			return consumeSystemVariableNode(node);
		}
		if (peek().kind().isSystemFunction())
		{
			return consumeSystemFunctionNode(node);
		}
		if (peekKind(1, SyntaxKind.LPAREN))
		{
			if (peek().kind() == SyntaxKind.VAL)
			{
				return valOperand(node);
			}
			if (peek().kind() == SyntaxKind.INT)
			{
				return intOperand(node);
			}
			if (peek().kind() == SyntaxKind.SUM)
			{
				return sumOperand(node);
			}
			if (peek().kind() == SyntaxKind.TOTAL)
			{
				return totalOperand(node);
			}
			if (peek().kind() == SyntaxKind.COUNT)
			{
				return countOperand(node);
			}
			if (peek().kind() == SyntaxKind.LOG)
			{
				return logOperand(node);
			}
			if (peek().kind() == SyntaxKind.OLD)
			{
				return oldOperand(node);
			}
			if (peek().kind() == SyntaxKind.ABS)
			{
				return absOperand(node);
			}
			if (peek().kind() == SyntaxKind.SGN)
			{
				return sgnOperand(node);
			}
			if (peek().kind() == SyntaxKind.POS)
			{
				return posOperand(node);
			}
			if (peek().kind() == SyntaxKind.FRAC)
			{
				return fracOperand(node);
			}
			if (peek().kind() == SyntaxKind.RET)
			{
				return retOperand(node);
			}
		}
		if (peek().kind() == SyntaxKind.LABEL_IDENTIFIER)
		{
			return consumeLabelIdentifier(node);
		}

		if (peek().kind().canBeIdentifier())
		{
			return consumeVariableReferenceNode(node);
		}

		return consumeLiteralNode(node);
	}

	private IOperandNode prefixUnary(BaseSyntaxNode node) throws ParseError
	{
		var unary = new PrefixUnaryArithmeticExpressionNode();
		node.addNode(unary);
		unary.setPostfixOperator(consumeAnyMandatory(unary, List.of(SyntaxKind.PLUS, SyntaxKind.MINUS)).kind());
		unary.setOperand(consumeOperandNode(unary));
		return unary;
	}

	private IOperandNode posOperand(BaseSyntaxNode node) throws ParseError
	{
		var posNode = new PosNode();
		node.addNode(posNode);
		consumeMandatory(posNode, SyntaxKind.POS);
		consumeMandatory(posNode, SyntaxKind.LPAREN);
		posNode.setPositionOf(consumeVariableReferenceNode(posNode));
		consumeMandatory(posNode, SyntaxKind.RPAREN);
		return posNode;
	}

	private IOperandNode valOperand(BaseSyntaxNode node) throws ParseError
	{
		var valOperand = new ValOperandNode();
		node.addNode(valOperand);
		consumeMandatory(valOperand, SyntaxKind.VAL);
		consumeMandatory(valOperand, SyntaxKind.LPAREN);
		valOperand.setVariable(consumeOperandNode(valOperand));
		consumeMandatory(valOperand, SyntaxKind.RPAREN);
		return valOperand;
	}

	private IOperandNode intOperand(BaseSyntaxNode node) throws ParseError
	{
		var intOperand = new IntOperandNode();
		node.addNode(intOperand);
		consumeMandatory(intOperand, SyntaxKind.INT);
		consumeMandatory(intOperand, SyntaxKind.LPAREN);
		intOperand.setVariable(consumeVariableReferenceNode(intOperand));
		consumeMandatory(intOperand, SyntaxKind.RPAREN);
		return intOperand;
	}

	private IOperandNode sumOperand(BaseSyntaxNode node) throws ParseError
	{
		var sumOperand = new SumOperandNode();
		node.addNode(sumOperand);
		consumeMandatory(sumOperand, SyntaxKind.SUM);
		consumeMandatory(sumOperand, SyntaxKind.LPAREN);
		sumOperand.setVariable(consumeVariableReferenceNode(sumOperand));
		consumeMandatory(sumOperand, SyntaxKind.RPAREN);
		return sumOperand;
	}

	private IOperandNode totalOperand(BaseSyntaxNode node) throws ParseError
	{
		var totalOperand = new TotalOperandNode();
		node.addNode(totalOperand);
		consumeMandatory(totalOperand, SyntaxKind.TOTAL);
		consumeMandatory(totalOperand, SyntaxKind.LPAREN);
		totalOperand.setVariable(consumeVariableReferenceNode(totalOperand));
		consumeMandatory(totalOperand, SyntaxKind.RPAREN);
		return totalOperand;
	}

	private IOperandNode countOperand(BaseSyntaxNode node) throws ParseError
	{
		var countOperand = new CountOperandNode();
		node.addNode(countOperand);
		consumeMandatory(countOperand, SyntaxKind.COUNT);
		consumeMandatory(countOperand, SyntaxKind.LPAREN);
		countOperand.setVariable(consumeVariableReferenceNode(countOperand));
		consumeMandatory(countOperand, SyntaxKind.RPAREN);
		return countOperand;
	}

	private IOperandNode logOperand(BaseSyntaxNode node) throws ParseError
	{
		var logOperand = new LogOperandNode();
		node.addNode(logOperand);
		consumeMandatory(logOperand, SyntaxKind.LOG);
		consumeMandatory(logOperand, SyntaxKind.LPAREN);
		logOperand.setParameter(consumeOperandNode(logOperand));
		consumeMandatory(logOperand, SyntaxKind.RPAREN);
		return logOperand;
	}

	private IOperandNode oldOperand(BaseSyntaxNode node) throws ParseError
	{
		var oldOperand = new OldOperandNode();
		node.addNode(oldOperand);
		consumeMandatory(oldOperand, SyntaxKind.OLD);
		consumeMandatory(oldOperand, SyntaxKind.LPAREN);
		oldOperand.setVariable(consumeVariableReferenceNode(oldOperand));
		consumeMandatory(oldOperand, SyntaxKind.RPAREN);
		return oldOperand;
	}

	private IOperandNode absOperand(BaseSyntaxNode node) throws ParseError
	{
		var absOperand = new AbsOperandNode();
		node.addNode(absOperand);
		consumeMandatory(absOperand, SyntaxKind.ABS);
		consumeMandatory(absOperand, SyntaxKind.LPAREN);
		absOperand.setParameter(consumeOperandNode(absOperand));
		consumeMandatory(absOperand, SyntaxKind.RPAREN);
		return absOperand;
	}

	private IOperandNode sgnOperand(BaseSyntaxNode node) throws ParseError
	{
		var sgnOperand = new SignOperandNode();
		node.addNode(sgnOperand);
		consumeMandatory(sgnOperand, SyntaxKind.SGN);
		consumeMandatory(sgnOperand, SyntaxKind.LPAREN);
		sgnOperand.setParameter(consumeOperandNode(sgnOperand));
		consumeMandatory(sgnOperand, SyntaxKind.RPAREN);
		return sgnOperand;
	}

	private IOperandNode fracOperand(BaseSyntaxNode node) throws ParseError
	{
		var fracOperand = new FracOperandNode();
		node.addNode(fracOperand);
		consumeMandatory(fracOperand, SyntaxKind.FRAC);
		consumeMandatory(fracOperand, SyntaxKind.LPAREN);
		fracOperand.setParameter(consumeOperandNode(fracOperand));
		consumeMandatory(fracOperand, SyntaxKind.RPAREN);
		return fracOperand;
	}

	private IOperandNode retOperand(BaseSyntaxNode node) throws ParseError
	{
		var retOperand = new RetOperandNode();
		node.addNode(retOperand);
		consumeMandatory(retOperand, SyntaxKind.RET);
		consumeMandatory(retOperand, SyntaxKind.LPAREN);
		retOperand.setParameter(consumeLiteralNode(retOperand));
		consumeMandatory(retOperand, SyntaxKind.RPAREN);
		return retOperand;
	}

	private IOperandNode consumeLabelIdentifier(BaseSyntaxNode node) throws ParseError
	{
		var labelNode = new LabelIdentifierNode();
		node.addNode(labelNode);
		var label = consumeMandatory(labelNode, SyntaxKind.LABEL_IDENTIFIER);
		labelNode.setLabel(label);
		return labelNode;
	}

	protected ISystemFunctionNode consumeSystemFunctionNode(BaseSyntaxNode node) throws ParseError
	{
		if (peek().kind() == SyntaxKind.TRANSLATE)
		{
			return consumeTranslateSystemFunction(node);
		}

		if (peek().kind() == SyntaxKind.PAGE_NUMBER || peek().kind() == SyntaxKind.LINE_COUNT)
		// TODO: Get the entry for the function from BuiltInFunctionTable and check if it takes one parameter that is rep
		{
			return consumeSystemFunctionWithRepParameter(node, peek().kind());
		}

		if (peek().kind() == SyntaxKind.TRIM)
		{
			return consumeTrimFunction(node);
		}

		var systemFunction = new SystemFunctionNode();
		systemFunction.setSystemFunction(peek().kind());
		consume(systemFunction);
		consumeMandatory(systemFunction, SyntaxKind.LPAREN);
		if (consumeOptionally(systemFunction, SyntaxKind.LPAREN)
			&& (systemFunction.systemFunction() == SyntaxKind.MAXVAL || systemFunction.systemFunction() == SyntaxKind.MINVAL)
			&& peek().kind() == SyntaxKind.IDENTIFIER && peek().symbolName().equals("IR"))
		{
			consumeMandatory(systemFunction, SyntaxKind.IDENTIFIER); // IR
			consumeMandatory(systemFunction, SyntaxKind.EQUALS_SIGN);
			while (!isAtEnd() && tokens.peek().kind() != SyntaxKind.RPAREN)
			{
				consume(systemFunction);
			}
			consumeMandatory(systemFunction, SyntaxKind.RPAREN);
		}
		systemFunction.addParameter(consumeOperandNode(systemFunction));
		while (consumeOptionally(systemFunction, SyntaxKind.COMMA))
		{
			systemFunction.addParameter(consumeOperandNode(systemFunction));
		}
		consumeMandatory(systemFunction, SyntaxKind.RPAREN);
		node.addNode(systemFunction);
		return systemFunction;
	}

	private ISystemFunctionNode consumeTrimFunction(BaseSyntaxNode node) throws ParseError
	{
		var trim = new TrimFunctionNode();
		trim.setSystemFunction(consume(trim).kind());

		consumeMandatory(trim, SyntaxKind.LPAREN);
		trim.addParameter(consumeOperandNode(trim));
		if (consumeOptionally(trim, SyntaxKind.COMMA))
		{
			trim.setOption(consumeAnyMandatory(trim, List.of(SyntaxKind.LEADING, SyntaxKind.TRAILING)).kind());
		}
		consumeMandatory(trim, SyntaxKind.RPAREN);
		node.addNode(trim);
		return trim;
	}

	private ISystemFunctionNode consumeSystemFunctionWithRepParameter(BaseSyntaxNode node, SyntaxKind kind) throws ParseError
	{
		var systemFunction = new SystemFunctionNode();
		node.addNode(systemFunction);
		consumeMandatory(systemFunction, kind);
		systemFunction.setSystemFunction(kind);
		if (consumeOptionally(systemFunction, SyntaxKind.LPAREN))
		{
			systemFunction.addParameter(consumeReportSpecificationOperand(systemFunction));
			consumeMandatory(systemFunction, SyntaxKind.RPAREN);
		}

		return systemFunction;
	}

	protected IReportSpecificationOperandNode consumeReportSpecificationOperand(BaseSyntaxNode parent)
	{
		var operand = new ReportSpecificationOperandNode();
		parent.addNode(operand);
		var spec = consume(operand);
		operand.setReportSpecification(spec);
		return operand;
	}

	private ISystemFunctionNode consumeTranslateSystemFunction(BaseSyntaxNode node) throws ParseError
	{
		var translate = new TranslateSystemFunctionNode();
		node.addNode(translate);
		consumeMandatory(translate, SyntaxKind.TRANSLATE);
		consumeMandatory(translate, SyntaxKind.LPAREN);
		if (peekKind(SyntaxKind.STRING_LITERAL))
		{
			var literal = consumeLiteralNode(translate, SyntaxKind.STRING_LITERAL);
			translate.setToTranslate(literal);
		}
		else
		{
			var reference = consumeVariableReferenceNode(translate);
			translate.setToTranslate(reference);
		}
		consumeMandatory(translate, SyntaxKind.COMMA);
		var translationToken = consumeAny(List.of(SyntaxKind.UPPER, SyntaxKind.LOWER));
		translate.setToUpper(translationToken.kind() == SyntaxKind.UPPER);
		consumeMandatory(translate, SyntaxKind.RPAREN);
		return translate;
	}

	protected IVariableReferenceNode consumeVariableReferenceNode(BaseSyntaxNode node) throws ParseError
	{
		var identifierToken = consumeIdentifierTokenOnly();
		var reference = new VariableReferenceNode(identifierToken);
		reference.addNode(new TokenNode(identifierToken));
		previousNode = reference;
		node.addNode(reference);

		if (peekKind(SyntaxKind.LPAREN) && !getKind(1).isAttribute() && !peekKind(1, SyntaxKind.LABEL_IDENTIFIER))
		{
			consumeMandatory(reference, SyntaxKind.LPAREN);
			reference.addDimension(consumeArrayAccess(reference));
			while (peekKind(SyntaxKind.COMMA))
			{
				consume(reference);
				reference.addDimension(consumeArrayAccess(reference));
			}
			consumeMandatory(reference, SyntaxKind.RPAREN);
		}

		if (peekKind(SyntaxKind.LPAREN) && peekKind(1, SyntaxKind.LABEL_IDENTIFIER))
		{
			while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
			{
				consume(reference);
			}
			consumeMandatory(reference, SyntaxKind.RPAREN);
		}

		unresolvedReferences.add(reference);
		return reference;
	}

	protected IOperandNode consumeArrayAccess(BaseSyntaxNode reference) throws ParseError
	{
		if (peekKind(SyntaxKind.ASTERISK) && peekKind(1, SyntaxKind.RPAREN))
		{
			var rangedAccess = new RangedArrayAccessNode();
			reference.addNode(rangedAccess);
			var asterisk = consumeOperandNode(rangedAccess);
			rangedAccess.setUpperBound(asterisk);
			rangedAccess.setLowerBound(asterisk);
			return rangedAccess;
		}

		var access = consumeArithmeticExpression(reference);
		if (peekKind(SyntaxKind.COLON))
		{
			return consumeRangedArrayAccess(reference, access);
		}

		return access;
	}

	protected IRangedArrayAccessNode consumeRangedArrayAccess(BaseSyntaxNode parent, IOperandNode lower) throws ParseError
	{
		var rangedAccess = new RangedArrayAccessNode();
		parent.replaceChild((BaseSyntaxNode) lower, rangedAccess);
		rangedAccess.addNode((BaseSyntaxNode) lower);
		consumeMandatory(rangedAccess, SyntaxKind.COLON);
		var upper = consumeArithmeticExpression(rangedAccess);

		rangedAccess.setLowerBound(lower);
		rangedAccess.setUpperBound(upper);
		return rangedAccess;
	}

	protected ISystemVariableNode consumeSystemVariableNode(BaseSyntaxNode node)
	{
		var systemVariableNode = new SystemVariableNode(peek());
		consume(node);
		node.addNode(systemVariableNode);
		return systemVariableNode;
	}

	protected boolean consumeAnyOptionally(BaseSyntaxNode node, Collection<SyntaxKind> acceptedKinds)
	{
		for (SyntaxKind acceptedKind : acceptedKinds)
		{
			if (consumeOptionally(node, acceptedKind))
			{
				return true;
			}
		}

		return false;
	}

	protected SyntaxToken consumeAnyMandatory(BaseSyntaxNode node, List<SyntaxKind> acceptedKinds) throws ParseError
	{
		for (SyntaxKind acceptedKind : acceptedKinds)
		{
			if (consumeOptionally(node, acceptedKind))
			{
				return previousToken();
			}
		}

		diagnostics.add(ParserErrors.unexpectedToken(acceptedKinds, tokens));
		throw new ParseError(peek());
	}

	protected IAttributeNode consumeSingleAttribute(BaseSyntaxNode node, SyntaxKind attributeKind) throws ParseError
	{
		if (!peekKind(SyntaxKind.LPAREN) && !peekKind(1, attributeKind))
		{
			report(ParserErrors.unexpectedToken(attributeKind, tokens));
			throw new ParseError(peek());
		}

		var attribute = new AttributeNode(peek());
		node.addNode(attribute);
		consumeMandatory(attribute, SyntaxKind.LPAREN);
		consume(attribute);
		consumeMandatory(attribute, SyntaxKind.RPAREN);
		return attribute;
	}

	protected void consumeAttributeDefinition(BaseSyntaxNode node) throws ParseError
	{
		// we don't do anything special yet, need some experience on where attribute definitions are allowed
		// this was built for CALLNAT, where a variable reference as parameter can have attribute definitions (only AD)
		// might be reusable for WRITE, DISPLAY, etc. for all kind of operands, but has to be fleshed out then
		// At that point, we could also add something similar for EM=
		consumeMandatory(node, SyntaxKind.LPAREN);
		while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
		{
			if (consumeOptionally(node, SyntaxKind.CV) || consumeOptionally(node, SyntaxKind.SB))
			{
				consumeVariableReferenceNode(node);
			}
			else
			{
				consume(node);
			}
		}
		consumeMandatory(node, SyntaxKind.RPAREN);
	}

	protected boolean peekAny(List<SyntaxKind> acceptedKinds)
	{
		return peekAny(0, acceptedKinds);
	}

	protected boolean peekAny(int offset, List<SyntaxKind> acceptedKinds)
	{
		return !tokens.isAtEnd() && acceptedKinds.contains(tokens.peek(offset).kind());
	}

	protected TokenNode previousTokenNode()
	{
		return previousNode;
	}

	protected SyntaxToken previousToken()
	{
		return tokens.peek(-1);
	}

	protected boolean isAtEnd()
	{
		return tokens.isAtEnd();
	}

	protected boolean isAtEnd(int offset)
	{
		return tokens.isAtEnd(offset);
	}

	protected void report(IDiagnostic diagnostic)
	{
		if (diagnostic != null)
		{
			if (shouldRelocateDiagnostics() && diagnostic instanceof ParserDiagnostic parserDiagnostic)
			{
				diagnostics.add(parserDiagnostic.relocate(relocatedDiagnosticPosition));
			}
			else
			{
				diagnostics.add(diagnostic);
			}
		}
	}

	protected void discard()
	{
		tokens.advance();
	}

	protected void rollbackOnce()
	{
		tokens.rollback(1);
	}

	protected SyntaxToken peekNextLine()
	{
		var offset = 0;
		var currentLine = peek().line();
		while (!tokens.isAtEnd(offset) && peek(offset).line() == currentLine)
		{
			offset++;
		}

		return peek(offset);
	}

	protected void skipToNextLineAsRecovery(ParseError e)
	{
		// Skip to next line or END-DEFINE to recover
		while (!tokens.isAtEnd() && peek().line() == e.getErrorToken().line() && peek().kind() != SyntaxKind.END_DEFINE)
		{
			tokens.advance();
		}
	}

	protected void skipToNextLineAsRecovery(int currentLine)
	{
		// Skip to next line or END-DEFINE to recover
		while (!tokens.isAtEnd() && peek().line() == currentLine && peek().kind() != SyntaxKind.END_DEFINE)
		{
			tokens.advance();
		}
	}

	protected void skipToNextLineReportingEveryToken()
	{
		var currentLine = peek().line();
		// Skip to next line or END-DEFINE to recover
		while (!tokens.isAtEnd() && peek().line() == currentLine && peek().kind() != SyntaxKind.END_DEFINE)
		{
			report(ParserErrors.trailingToken(peek()));
			discard();
		}
	}

	protected void relocateDiagnosticPosition(IPosition relocatedDiagnosticPosition)
	{
		this.relocatedDiagnosticPosition = relocatedDiagnosticPosition;
	}

	protected boolean peekAnyMandatoryOrAdvance(List<SyntaxKind> acceptedKinds)
	{
		if (peekAny(acceptedKinds))
		{
			return true;
		}

		report(ParserErrors.unexpectedToken(acceptedKinds, tokens));
		tokens.advance();
		return false;
	}

	protected boolean peekKindInLine(SyntaxKind kind)
	{
		var line = peek().line();
		var offset = 0;
		while (!isAtEnd(offset) && peek(offset).line() == line)
		{
			if (peek(offset).kind() == kind)
			{
				return true;
			}

			offset++;
		}

		return false;
	}

	/**
	 * Does a forward peek in the same line until a given kind and checks if the other comes directly after.
	 *
	 * @param search The token to search for
	 * @param after The expected token after {@code search}
	 */
	protected boolean isKindAfterKindInSameLine(SyntaxKind after, SyntaxKind search)
	{
		var line = peek().line();
		var offset = 0;
		while (!isAtEnd(offset) && peek(offset).line() == line && !isAtEnd(offset + 1))
		{
			if (peek(offset).kind() == search)
			{
				return peek(offset + 1).kind() == after;
			}

			offset++;
		}

		return false;
	}
}
