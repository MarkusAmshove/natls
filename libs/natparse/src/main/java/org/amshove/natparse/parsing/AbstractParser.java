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

abstract class AbstractParser<T>
{
	protected IModuleProvider moduleProvider;
	protected TokenList tokens;
	protected List<ISymbolReferenceNode> unresolvedReferences;
	private TokenNode previousNode;

	private List<IDiagnostic> diagnostics = new ArrayList<>();
	protected IPosition relocatedDiagnosticPosition;

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

	protected INaturalModule sideloadModule(String referableName, ITokenNode importNode)
	{
		if (moduleProvider == null)
		{
			return null;
		}

		var module = moduleProvider.findNaturalModule(referableName);

		if (module == null && !(referableName.startsWith("USR") && referableName.endsWith("N")))
		{
			report(ParserErrors.unresolvedImport(importNode));
		}

		return module;
	}

	protected IHasDefineData sideloadDefineData(TokenNode importNode)
	{
		if (sideloadModule(importNode.token().symbolName(), importNode) instanceof IHasDefineData hasDefineData)
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

	protected boolean peekKind(int offset, SyntaxKind kind)
	{
		return !isAtEnd(offset) && peek(offset).kind() == kind;
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
	 * Consumes the current token only if the kind matches.
	 * This will not add any diagnostics.
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
	 * Consumes either firstKind, secondKind or none.
	 * This will not add any diagnostics.
	 *
	 * @param node       the node to add the token to
	 * @param firstKind  the first possible kind
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
		if(!consumeOptionally(node, closingTokenType))
		{
			diagnostics.add(ParserErrors.missingClosingToken(closingTokenType, openingToken));
			throw new ParseError(peek());
		}

		return previousToken();
	}

	protected ILiteralNode consumeLiteralNode(BaseSyntaxNode node) throws ParseError
	{
		if(peekKind(SyntaxKind.LPAREN))
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

		var literal = consumeAny(List.of(SyntaxKind.NUMBER_LITERAL, SyntaxKind.STRING_LITERAL, SyntaxKind.TRUE, SyntaxKind.FALSE, SyntaxKind.ASTERISK));
		var literalNode = new LiteralNode(literal);
		node.addNode(literalNode);
		return literalNode;
	}

	protected ILiteralNode consumeLiteralNode(BaseSyntaxNode node, SyntaxKind literalKind) throws ParseError
	{
		var literal = consumeLiteralNode(node);
		if(literal.token().kind() != literalKind)
		{
			report(ParserErrors.unexpectedToken(literalKind, literal.token()));
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

		var literal = consumeAny(List.of(SyntaxKind.NUMBER_LITERAL, SyntaxKind.STRING_LITERAL, SyntaxKind.TRUE, SyntaxKind.FALSE));
		previousNode = new TokenNode(literal);
		node.addNode(previousNode);
		return literal;
	}

	protected SyntaxToken consumeMandatoryIdentifier(BaseSyntaxNode node) throws ParseError
	{
		var identifierToken = consumeIdentifierTokenOnly();
		previousNode = new TokenNode(identifierToken);
		node.addNode(previousNode);
		return identifierToken;
	}

	protected SyntaxToken consumeIdentifierTokenOnly() throws ParseError
	{
		var currentToken = tokens.peek();
		if(tokens.isAtEnd() || (currentToken.kind() != SyntaxKind.IDENTIFIER && !currentToken.kind().canBeIdentifier()))
		{
			diagnostics.add(ParserErrors.unexpectedToken(SyntaxKind.IDENTIFIER, tokens));
			throw new ParseError(peek());
		}

		if(currentToken.kind() != SyntaxKind.IDENTIFIER)
		{
			diagnostics.add(ParserErrors.keywordUsedAsIdentifier(currentToken));
		}

		var identifierToken = currentToken.withKind(SyntaxKind.IDENTIFIER);
		tokens.advance();
		return identifierToken;
	}

	protected SyntaxToken consumeAny(List<SyntaxKind> acceptedKinds) throws ParseError
	{
		if (tokens.isAtEnd() || !acceptedKinds.contains(tokens.peek().kind()))
		{
			diagnostics.add(ParserErrors.unexpectedToken(acceptedKinds, tokens.peek()));
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
		return new SyntheticVariableStatementNode(node);
	}

	protected FunctionCallNode functionCall(SyntaxToken token) throws ParseError
	{
		var node = new FunctionCallNode();

		var functionName = new TokenNode(token);
		node.setReferencingToken(token);
		node.addNode(functionName);
		var module = sideloadModule(token.symbolName(), functionName);
		node.setReferencedModule((NaturalModule) module);

		consumeMandatory(node, SyntaxKind.LPAREN);

		while (!peekKind(SyntaxKind.RPAREN))
		{
			if (peekKind(SyntaxKind.IDENTIFIER))
			{
				node.addNode(identifierReference());
			}
			else
			{
				consume(node);
			}
		}

		consumeMandatory(node, SyntaxKind.RPAREN);

		return node;
	}

	protected static final List<SyntaxKind> ARITHMETIC_OPERATOR_KINDS = List.of(SyntaxKind.PLUS, SyntaxKind.MINUS, SyntaxKind.ASTERISK, SyntaxKind.SLASH);
	protected IOperandNode consumeArithmeticExpression(BaseSyntaxNode node) throws ParseError
	{
		var needRParen = consumeOptionally(node, SyntaxKind.LPAREN);
		var operand = consumeOperandNode(node);
		if(peekAny(ARITHMETIC_OPERATOR_KINDS))
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
		}

		if(needRParen)
		{
			consumeMandatory(node, SyntaxKind.RPAREN);
		}
		return operand;
	}

	protected IOperandNode consumeOperandNode(BaseSyntaxNode node) throws ParseError
	{
		if(peekKind(SyntaxKind.IDENTIFIER))
		{
			if(peekKind(1, SyntaxKind.LPAREN) && peekKind(2, SyntaxKind.LESSER_SIGN))
			{
				var token = peek();
				discard(); // this is kinda strange. reiterate on why functionCall() needs to get the token
				var functionCall = functionCall(token);
				node.addNode(functionCall);
				return functionCall;
			}
			return consumeVariableReferenceNode(node);
		}
		if(peek().kind().isSystemVariable() && peek().kind().isSystemFunction()) // can be both, like *COUNTER
		{
			return peekKind(1, SyntaxKind.LPAREN) ? consumeSystemFunctionNode(node) : consumeSystemVariableNode(node);
		}
		if(peek().kind().isSystemVariable())
		{
			return consumeSystemVariableNode(node);
		}
		if(peek().kind().isSystemFunction())
		{
			return consumeSystemFunctionNode(node);
		}
		if(peek().kind() == SyntaxKind.VAL)
		{
			return valOperand(node);
		}
		if(peek().kind() == SyntaxKind.ABS)
		{
			return absOperand(node);
		}

		if(peek().kind() == SyntaxKind.LABEL_IDENTIFIER)
		{
			return consumeLabelIdentifier(node);
		}

		return consumeLiteralNode(node);
	}

	private IOperandNode valOperand(BaseSyntaxNode node) throws ParseError
	{
		var valOperand = new ValOperandNode();
		node.addNode(valOperand);
		consumeMandatory(valOperand, SyntaxKind.VAL);
		consumeMandatory(valOperand, SyntaxKind.LPAREN);
		valOperand.setVariable(consumeVariableReferenceNode(valOperand));
		consumeMandatory(valOperand, SyntaxKind.RPAREN);
		return valOperand;
	}

	private IOperandNode absOperand(BaseSyntaxNode node) throws ParseError
	{
		var valOperand = new AbsOperandNode();
		node.addNode(valOperand);
		consumeMandatory(valOperand, SyntaxKind.ABS);
		consumeMandatory(valOperand, SyntaxKind.LPAREN);
		valOperand.setVariable(consumeVariableReferenceNode(valOperand));
		consumeMandatory(valOperand, SyntaxKind.RPAREN);
		return valOperand;
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
		if(peek().kind() == SyntaxKind.TRANSLATE)
		{
			return consumeTranslateSystemFunction(node);
		}

		var systemFunction = new SystemFunctionNode();
		systemFunction.setSystemFunction(peek().kind());
		consume(systemFunction);
		consumeMandatory(systemFunction, SyntaxKind.LPAREN);
		if(consumeOptionally(systemFunction, SyntaxKind.LPAREN)
			&& (systemFunction.systemFunction() == SyntaxKind.MAXVAL || systemFunction.systemFunction() == SyntaxKind.MINVAL)
			&& peek().kind() == SyntaxKind.IDENTIFIER && peek().symbolName().equals("IR"))
		{
			consumeMandatory(systemFunction, SyntaxKind.IDENTIFIER); // IR
			consumeMandatory(systemFunction, SyntaxKind.EQUALS_SIGN);
			while(!isAtEnd() && tokens.peek().kind() != SyntaxKind.RPAREN)
			{
				consume(systemFunction);
			}
			consumeMandatory(systemFunction, SyntaxKind.RPAREN);
		}
		systemFunction.addParameter(consumeOperandNode(systemFunction));
		while(consumeOptionally(systemFunction, SyntaxKind.COMMA))
		{
			if(systemFunction.systemFunction() == SyntaxKind.TRANSLATE)
			{
				consumeAnyMandatory(systemFunction, List.of(SyntaxKind.UPPER, SyntaxKind.LOWER)); // TODO: Save if upper or lower
			}
			else
			{
				systemFunction.addParameter(consumeOperandNode(systemFunction));
			}

		}
		consumeMandatory(systemFunction, SyntaxKind.RPAREN);
		node.addNode(systemFunction);
		return systemFunction;
	}

	private ISystemFunctionNode consumeTranslateSystemFunction(BaseSyntaxNode node) throws ParseError
	{
		var translate = new TranslateSystemFunctionNode();
		node.addNode(translate);
		consumeMandatory(translate, SyntaxKind.TRANSLATE);
		consumeMandatory(translate, SyntaxKind.LPAREN);
		var reference = consumeVariableReferenceNode(translate);
		translate.setToTranslate(reference);
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

		if(consumeOptionally(reference, SyntaxKind.LPAREN))
		{
			reference.addDimension(consumeOperandNode(reference));
			while(peekKind(SyntaxKind.COMMA))
			{
				consume(reference);
				reference.addDimension(consumeOperandNode(reference));
			}
			consumeMandatory(reference, SyntaxKind.RPAREN);
		}

		unresolvedReferences.add(reference);
		return reference;
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
			if(consumeOptionally(node, acceptedKind))
			{
				return true;
			}
		}

		return false;
	}

	protected void consumeAnyMandatory(BaseSyntaxNode node, List<SyntaxKind> acceptedKinds) throws ParseError
	{
		for (SyntaxKind acceptedKind : acceptedKinds)
		{
			if(consumeOptionally(node, acceptedKind))
			{
				return;
			}
		}

		diagnostics.add(ParserErrors.unexpectedToken(acceptedKinds, tokens.peek()));
		throw new ParseError(peek());
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
		if(peekAny(acceptedKinds))
		{
			return true;
		}

		report(ParserErrors.unexpectedToken(acceptedKinds, peek()));
		tokens.advance();
		return false;
	}

	/**
	 * Determines if any of the given {@link SyntaxKind}s is in the same line as the current peekable token.
	 */
	protected boolean peekSameLine(Collection<SyntaxKind> kindsToLookFor)
	{
		var startLine = peek().line();
		var peekOffset = 0;
		while(!isAtEnd(peekOffset) && peek(peekOffset).line() == startLine)
		{
			if(kindsToLookFor.contains(peek(peekOffset).kind()))
			{
				return true;
			}

			peekOffset++;
		}

		return false;
	}

	/**
	 * Scans all {@link SyntaxKind}s (starting from the next one, not current) until the stopKind is encountered<br/>
	 * Returns true if any of the scanned {@link SyntaxKind}s is in the given list.<br/>
	 * Returns false if none is in the given list or the end of tokens is encountered.
	 */
	protected boolean peekAnyUntil(SyntaxKind stopKind, List<SyntaxKind> kinds)
	{
		var offset = 1;
		while(!isAtEnd(offset) && !peekKind(offset, stopKind))
		{
			if(peek(offset).kind() == SyntaxKind.LPAREN)
			{
				// skip nested parens
				while(!isAtEnd(offset) && !peekKind(offset, SyntaxKind.RPAREN))
				{
					offset++;
				}
			}

			if(kinds.contains(peek(offset).kind()))
			{
				return true;
			}
			offset++;
		}

		return false;
	}

}
