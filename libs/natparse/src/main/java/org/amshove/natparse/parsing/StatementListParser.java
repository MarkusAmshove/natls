package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IReferencableNode;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class StatementListParser extends AbstractParser<IStatementListNode>
{
	private List<IReferencableNode> referencableNodes;

	public List<IReferencableNode> getReferencableNodes()
	{
		return referencableNodes;
	}

	StatementListParser(IModuleProvider moduleProvider)
	{
		super(moduleProvider);
	}

	public List<ISymbolReferenceNode> getUnresolvedReferences()
	{
		return unresolvedReferences;
	}

	@Override
	protected IStatementListNode parseInternal()
	{
		unresolvedReferences = new ArrayList<>();
		referencableNodes = new ArrayList<>();
		var statementList = statementList();
		resolveUnresolvedInternalPerforms();
		if (!shouldRelocateDiagnostics())
		{
			// If diagnostics should be relocated, we're a copycode. So let the includer resolve it themselves.
			resolveUnresolvedExternalPerforms();
		}
		return statementList;
	}

	private StatementListNode statementList()
	{
		return statementList(null);
	}

	private StatementListNode statementList(SyntaxKind endTokenKind)
	{
		var statementList = new StatementListNode();
		while (!tokens.isAtEnd())
		{
			try
			{
				if (tokens.peek().kind() == endTokenKind)
				{
					break;
				}

				switch (tokens.peek().kind())
				{
					case CALLNAT:
						statementList.addStatement(callnat());
						break;
					case CLOSE:
						switch (peek(1).kind())
						{
							case PRINTER -> statementList.addStatement(closePrinter());
							default -> statementList.addStatement(consumeFallback());
						}
						break;
					case FORMAT:
						statementList.addStatement(formatNode());
						break;
					case INCLUDE:
						statementList.addStatement(include());
						break;
					case FETCH:
						statementList.addStatement(fetch());
						break;
					case IDENTIFIER:
						statementList.addStatement(identifierReference());
						break;
					case END:
						statementList.addStatement(end());
						break;
					case DEFINE:
						switch (peek(1).kind())
						{
							case SUBROUTINE, IDENTIFIER -> statementList.addStatement(subroutine());
							case PRINTER -> statementList.addStatement(definePrinter());
							case WINDOW -> statementList.addStatement(defineWindow());
							default ->
							{
								tokens.advance();
								tokens.advance();
							}
						}
						break;
					case IGNORE:
						statementList.addStatement(ignore());
						break;
					case FIND:
						statementList.addStatement(find());
						break;
					case PERFORM:
						if (peek(1).kind() == SyntaxKind.BREAK)
						{
							tokens.advance();
							break;
						}
						statementList.addStatement(perform());
						break;
					case RESET:
						statementList.addStatement(resetStatement());
						break;
					case SET:
						if (peek(1).kind() == SyntaxKind.KEY)
						{
							statementList.addStatement(setKey());
							break;
						}
						// FALLTHROUGH TO DEFAULT INTENDED - SET CONTROL etc. not implemented
					case IF:
						if (peekKind(SyntaxKind.IF) && (peek(-1) == null || peek(-1).kind() != SyntaxKind.REJECT && peek(-1).kind() != SyntaxKind.ACCEPT)) // TODO: until ACCEPT/REJECT IF
						{
							statementList.addStatement(ifStatement());
							break;
						}
						// FALLTHROUGH TO DEFAULT INTENDED
					case FOR:
						if (peekKind(SyntaxKind.FOR) && (peek(-1) == null || (peek(1).kind() == SyntaxKind.IDENTIFIER && peek(-1).kind() != SyntaxKind.REJECT && peek(-1).kind() != SyntaxKind.ACCEPT)))
						// TODO: until we support EXAMINE, DECIDE, ...
						//      just.. implement them already and don't try to understand the conditions
						{
							statementList.addStatement(forLoop());
							break;
						}
						// FALLTHROUGH TO DEFAULT INTENDED
					default:
						// While the parser is incomplete, we just add a node for every token
						var tokenStatementNode = new SyntheticTokenStatementNode();
						consume(tokenStatementNode);
						if (tokenStatementNode.token().kind() == SyntaxKind.MASK) // TODO(expressions): Remove once we can parse expressions
						{
							consumeMandatory(tokenStatementNode, SyntaxKind.LPAREN);
							while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
							{
								consume(tokenStatementNode);
							}
							consumeMandatory(tokenStatementNode, SyntaxKind.RPAREN);
						}
						statementList.addStatement(tokenStatementNode);
				}
			}
			catch (ParseError e)
			{
				// TODO: Add a ErrorRecoveryNode which eats every token until `isStatementStart()` returns true?
				tokens.advance();
			}
		}

		return statementList;
	}

	private static final Set<SyntaxKind> FORMAT_MODIFIERS = Set.of(SyntaxKind.AD, SyntaxKind.AL, SyntaxKind.CD, SyntaxKind.DF, SyntaxKind.DL, SyntaxKind.EM, SyntaxKind.ES, SyntaxKind.FC, SyntaxKind.FL, SyntaxKind.GC, SyntaxKind.HC, SyntaxKind.HW, SyntaxKind.IC, SyntaxKind.IP, SyntaxKind.IS, SyntaxKind.KD, SyntaxKind.LC, SyntaxKind.LS, SyntaxKind.MC, SyntaxKind.MP, SyntaxKind.MS, SyntaxKind.NL,
		SyntaxKind.PC, SyntaxKind.PM, SyntaxKind.PS, SyntaxKind.SF, SyntaxKind.SG, SyntaxKind.TC, SyntaxKind.UC, SyntaxKind.ZP);

	private StatementNode formatNode() throws ParseError
	{
		var format = new FormatNode();
		consumeMandatory(format, SyntaxKind.FORMAT);
		if (consumeOptionally(format, SyntaxKind.LPAREN))
		{
			consumeMandatoryIdentifier(format);
			consumeMandatory(format, SyntaxKind.RPAREN);
		}

		while (consumeAnyOptionally(format, FORMAT_MODIFIERS))
		{
			consumeMandatory(format, SyntaxKind.EQUALS_SIGN);
			if(!FORMAT_MODIFIERS.contains(peek().kind()) && peek().line() == previousToken().line())
			{
				consume(format);
			}
		}

		return format;
	}

	private StatementNode defineWindow() throws ParseError
	{
		var window = new DefineWindowNode();
		consumeMandatory(window, SyntaxKind.DEFINE);
		consumeMandatory(window, SyntaxKind.WINDOW);
		var name = consumeIdentifierTokenOnly();
		window.setName(name);
		window.addNode(new TokenNode(name));
		return window;
	}

	private StatementNode closePrinter() throws ParseError
	{
		var closePrinter = new ClosePrinterNode();
		consumeMandatory(closePrinter, SyntaxKind.CLOSE);
		consumeMandatory(closePrinter, SyntaxKind.PRINTER);
		consumeMandatory(closePrinter, SyntaxKind.LPAREN);

		if (peekAnyMandatoryOrAdvance(List.of(SyntaxKind.NUMBER_LITERAL, SyntaxKind.IDENTIFIER)))
		{
			if (peekKind(SyntaxKind.NUMBER_LITERAL))
			{
				var literal = consumeLiteralNode(closePrinter, SyntaxKind.NUMBER_LITERAL);
				closePrinter.setPrinter(literal.token());
			}
			if (peekKind(SyntaxKind.IDENTIFIER))
			{
				var identifier = consumeIdentifierTokenOnly();
				closePrinter.setPrinter(identifier);
			}
		}

		consumeMandatory(closePrinter, SyntaxKind.RPAREN);
		return closePrinter;
	}

	private SyntheticTokenStatementNode consumeFallback()
	{
		var tokenStatementNode = new SyntheticTokenStatementNode();
		consume(tokenStatementNode);
		return tokenStatementNode;
	}

	private StatementNode definePrinter() throws ParseError
	{
		var printer = new DefinePrinterNode();
		consumeMandatory(printer, SyntaxKind.DEFINE);
		consumeMandatory(printer, SyntaxKind.PRINTER);
		consumeMandatory(printer, SyntaxKind.LPAREN);
		if (peekKind(SyntaxKind.IDENTIFIER))
		{
			var name = consumeMandatoryIdentifier(printer);
			printer.setName(name);
			consumeMandatory(printer, SyntaxKind.EQUALS_SIGN);
		}
		var printerNumber = consumeLiteralNode(printer, SyntaxKind.NUMBER_LITERAL);
		if (printerNumber.token().kind() == SyntaxKind.NUMBER_LITERAL)
		{
			printer.setPrinterNumber(printerNumber.token().intValue());
		}
		consumeMandatory(printer, SyntaxKind.RPAREN);

		if (consumeOptionally(printer, SyntaxKind.OUTPUT))
		{
			if (peekKind(SyntaxKind.IDENTIFIER))
			{
				var reference = consumeVariableReferenceNode(printer);
				printer.setOutput(reference);
			}
			else
			{
				if (peekKind(SyntaxKind.STRING_LITERAL))
				{
					var literal = consumeLiteralNode(printer, SyntaxKind.STRING_LITERAL);
					printer.setOutput(literal);
					if (!literal.token().stringValue().matches("LPT\\d+"))
					{
						report(ParserErrors.invalidPrinterOutputFormat(literal.token()));
					}
				}
				else
				{
					report(ParserErrors.unexpectedToken(List.of(SyntaxKind.IDENTIFIER, SyntaxKind.STRING_LITERAL), peek()));
					tokens.advance();
				}
			}
		}

		while (peekAny(List.of(SyntaxKind.PROFILE, SyntaxKind.DISP, SyntaxKind.COPIES)))
		{
			if (consumeOptionally(printer, SyntaxKind.PROFILE))
			{
				var literal = consumeLiteralNode(printer, SyntaxKind.STRING_LITERAL);
				checkStringLength(literal.token(), literal.token().stringValue(), 8);
			}

			if (consumeOptionally(printer, SyntaxKind.DISP))
			{
				consumeAnyMandatory(printer, List.of(SyntaxKind.HOLD, SyntaxKind.KEEP, SyntaxKind.DEL));
			}

			if (consumeOptionally(printer, SyntaxKind.COPIES))
			{
				consumeLiteralNode(printer, SyntaxKind.NUMBER_LITERAL);
			}
		}

		return printer;
	}

	private void checkStringLength(SyntaxToken token, String stringValue, int maxLength)
	{
		if (stringValue.length() > maxLength)
		{
			report(ParserErrors.invalidLengthForLiteral(token, maxLength));
		}
	}

	private StatementNode forLoop() throws ParseError
	{
		var loopNode = new ForLoopNode();

		var opening = consumeMandatory(loopNode, SyntaxKind.FOR);
		consumeVariableReferenceNode(loopNode);
		consumeAnyOptionally(loopNode, List.of(SyntaxKind.COLON_EQUALS_SIGN, SyntaxKind.EQUALS_SIGN, SyntaxKind.EQ, SyntaxKind.FROM));
		consumeOperandNode(loopNode); // TODO(arithmetic-expression): Could also be arithmetic expression
		consumeAnyMandatory(loopNode, List.of(SyntaxKind.TO, SyntaxKind.THRU));
		var upperBound = consumeOperandNode(loopNode); // TODO(arithmetic-expression): Could also be arithmetic expression
		loopNode.setUpperBound(upperBound);
		if (consumeOptionally(loopNode, SyntaxKind.STEP))
		{
			consumeOperandNode(loopNode);
		}

		loopNode.setBody(statementList(SyntaxKind.END_FOR));
		consumeMandatoryClosing(loopNode, SyntaxKind.END_FOR, opening);

		return loopNode;
	}

	private StatementNode perform() throws ParseError
	{
		var internalPerform = new InternalPerformNode();

		consumeMandatory(internalPerform, SyntaxKind.PERFORM);
		var symbolName = consumeIdentifierTokenOnly();
		var referenceNode = new SymbolReferenceNode(symbolName);
		internalPerform.setReferenceNode(referenceNode);
		internalPerform.addNode(referenceNode);

		unresolvedReferences.add(internalPerform);
		return internalPerform;
	}

	private StatementNode ignore() throws ParseError
	{
		var ignore = new IgnoreNode();
		consumeMandatory(ignore, SyntaxKind.IGNORE);
		return ignore;
	}

	private StatementNode subroutine() throws ParseError
	{
		var subroutine = new SubroutineNode();
		var opening = consumeMandatory(subroutine, SyntaxKind.DEFINE);
		consumeOptionally(subroutine, SyntaxKind.SUBROUTINE);
		var nameToken = consumeMandatoryIdentifier(subroutine);
		subroutine.setName(nameToken);

		subroutine.setBody(statementList(SyntaxKind.END_SUBROUTINE));

		consumeMandatoryClosing(subroutine, SyntaxKind.END_SUBROUTINE, opening);

		referencableNodes.add(subroutine);

		return subroutine;
	}

	private StatementNode end() throws ParseError
	{
		var endNode = new EndNode();
		consumeMandatory(endNode, SyntaxKind.END);
		return endNode;
	}

	private StatementNode identifierReference() throws ParseError
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

	private SymbolReferenceNode symbolReferenceNode(SyntaxToken token)
	{
		var node = new SymbolReferenceNode(token);
		unresolvedReferences.add(node);
		return node;
	}

	private FunctionCallNode functionCall(SyntaxToken token) throws ParseError
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

	private CallnatNode callnat() throws ParseError
	{
		var callnat = new CallnatNode();

		consumeMandatory(callnat, SyntaxKind.CALLNAT);

		if (isNotCallnatOrFetchModule())
		{
			report(ParserErrors.unexpectedToken(List.of(SyntaxKind.STRING_LITERAL, SyntaxKind.IDENTIFIER), peek()));
		}

		if (consumeOptionally(callnat, SyntaxKind.IDENTIFIER))
		{
			callnat.setReferencingToken(previousToken());
		}

		if (consumeOptionally(callnat, SyntaxKind.STRING_LITERAL))
		{
			callnat.setReferencingToken(previousToken());
			var referencedModule = sideloadModule(callnat.referencingToken().stringValue().toUpperCase().trim(), previousTokenNode());
			callnat.setReferencedModule((NaturalModule) referencedModule);
		}

		return callnat;
	}

	private IncludeNode include() throws ParseError
	{
		var include = new IncludeNode();

		consumeMandatory(include, SyntaxKind.INCLUDE);

		var referencingToken = consumeMandatoryIdentifier(include);
		include.setReferencingToken(referencingToken);

		var referencedModule = sideloadModule(referencingToken.symbolName(), previousTokenNode());
		include.setReferencedModule((NaturalModule) referencedModule);

		if (referencedModule != null)
		{
			try
			{
				var includedSource = Files.readString(referencedModule.file().getPath());
				var lexer = new Lexer();
				lexer.relocateDiagnosticPosition(shouldRelocateDiagnostics() ? relocatedDiagnosticPosition : referencingToken);
				var tokens = lexer.lex(includedSource, referencedModule.file().getPath());

				for (var diagnostic : tokens.diagnostics())
				{
					report(diagnostic);
				}

				var nestedParser = new StatementListParser(moduleProvider);
				nestedParser.relocateDiagnosticPosition(
					shouldRelocateDiagnostics()
						? relocatedDiagnosticPosition
						: referencingToken
				);
				var statementList = nestedParser.parse(tokens);

				for (var diagnostic : statementList.diagnostics())
				{
					if (ParserError.isUnresolvedError(diagnostic.id()))
					{
						// Unresolved references will be resolved by the module including the copycode.
						report(diagnostic);
					}
				}
				unresolvedReferences.addAll(nestedParser.unresolvedReferences);
				referencableNodes.addAll(nestedParser.referencableNodes);
				include.setBody(statementList.result(),
					shouldRelocateDiagnostics()
						? relocatedDiagnosticPosition
						: referencingToken
				);
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}
		else
		{
			var unresolvedBody = new StatementListNode();
			unresolvedBody.setParent(include);
			include.setBody(unresolvedBody,
				shouldRelocateDiagnostics()
					? relocatedDiagnosticPosition
					: referencingToken
			);
		}

		return include;
	}

	private FetchNode fetch() throws ParseError
	{
		var fetch = new FetchNode();

		consumeMandatory(fetch, SyntaxKind.FETCH);

		consumeEitherOptionally(fetch, SyntaxKind.RETURN, SyntaxKind.REPEAT);

		if (isNotCallnatOrFetchModule())
		{
			report(ParserErrors.unexpectedToken(List.of(SyntaxKind.STRING_LITERAL, SyntaxKind.IDENTIFIER), peek()));
		}

		if (consumeOptionally(fetch, SyntaxKind.IDENTIFIER))
		{
			fetch.setReferencingToken(previousToken());
		}

		if (consumeOptionally(fetch, SyntaxKind.STRING_LITERAL))
		{
			fetch.setReferencingToken(previousToken());
			var referencedModule = sideloadModule(fetch.referencingToken().stringValue().toUpperCase().trim(), previousTokenNode());
			fetch.setReferencedModule((NaturalModule) referencedModule);
		}

		return fetch;
	}

	private StatementNode ifStatement() throws ParseError
	{
		if (peek(1).kind() == SyntaxKind.NO)
		{
			return ifNoRecord();
		}

		var ifStatement = new IfStatementNode();

		var opening = consumeMandatory(ifStatement, SyntaxKind.IF);

		ifStatement.setBody(statementList(SyntaxKind.END_IF));

		consumeMandatoryClosing(ifStatement, SyntaxKind.END_IF, opening);

		return ifStatement;
	}

	private IfNoRecordNode ifNoRecord() throws ParseError
	{
		var statement = new IfNoRecordNode();

		var opening = consumeMandatory(statement, SyntaxKind.IF);
		consumeMandatory(statement, SyntaxKind.NO);
		consumeOptionally(statement, SyntaxKind.RECORDS);
		consumeOptionally(statement, SyntaxKind.FOUND);

		statement.setBody(statementList(SyntaxKind.END_NOREC));

		consumeMandatoryClosing(statement, SyntaxKind.END_NOREC, opening);

		return statement;
	}

	private SetKeyStatementNode setKey() throws ParseError
	{
		var statement = new SetKeyStatementNode();

		consumeMandatory(statement, SyntaxKind.SET);
		consumeMandatory(statement, SyntaxKind.KEY);

		if (consumeOptionally(statement, SyntaxKind.NAMED))
		{
			consumeMandatory(statement, SyntaxKind.OFF);
		}

		while (peekKind(SyntaxKind.PF))
		{
			consumeMandatory(statement, SyntaxKind.PF);
			if (consumeOptionally(statement, SyntaxKind.EQUALS_SIGN))
			{
				if (consumeOptionally(statement, SyntaxKind.DATA))
				{
					consumeMandatory(statement, SyntaxKind.STRING_LITERAL);
				}
				else
					if (peekKind(SyntaxKind.IDENTIFIER))
					{
						consumeOperandNode(statement);
					}
					else
					{
						consumeAnyMandatory(statement, List.of(SyntaxKind.HELP, SyntaxKind.PROGRAM, SyntaxKind.PGM, SyntaxKind.ON, SyntaxKind.OFF, SyntaxKind.STRING_LITERAL, SyntaxKind.COMMAND, SyntaxKind.DISABLED));
					}
			}

			if (consumeOptionally(statement, SyntaxKind.NAMED))
			{
				if (!consumeOptionally(statement, SyntaxKind.OFF))
				{
					consumeOperandNode(statement);
				}
			}
		}

		return statement;
	}

	private FindNode find() throws ParseError
	{
		var find = new FindNode();

		var open = consumeMandatory(find, SyntaxKind.FIND);
		var hasNoBody = consumeOptionally(find, SyntaxKind.FIRST) || consumeOptionally(find, SyntaxKind.KW_NUMBER) || consumeOptionally(find, SyntaxKind.UNIQUE);
		consumeOptionally(find, SyntaxKind.ALL);
		if (consumeOptionally(find, SyntaxKind.LPAREN))
		{
			consumeOperandNode(find);
			consumeMandatory(find, SyntaxKind.RPAREN);
		}
		if (consumeOptionally(find, SyntaxKind.MULTI_FETCH))
		{
			consumeAnyMandatory(find, List.of(SyntaxKind.ON, SyntaxKind.OFF));
		}

		consumeEitherOptionally(find, SyntaxKind.RECORDS, SyntaxKind.RECORD);
		consumeOptionally(find, SyntaxKind.IN);
		consumeOptionally(find, SyntaxKind.FILE);

		var viewName = symbolReferenceNode(consumeIdentifierTokenOnly());
		find.setView(viewName);

		if (consumeOptionally(find, SyntaxKind.WITH))
		{
			if (consumeOptionally(find, SyntaxKind.LIMIT))
			{
				consumeLiteral(find);
			}

			var descriptor = consumeIdentifierTokenOnly(); // TODO(expressions): Must be ISearchCriteriaNode
			var descriptorNode = new DescriptorNode(descriptor);
			find.addNode(descriptorNode);
		}

		if (!hasNoBody)
		{
			find.setBody(statementList(SyntaxKind.END_FIND));

			consumeMandatoryClosing(find, SyntaxKind.END_FIND, open);
		}

		return find;
	}

	private ResetStatementNode resetStatement() throws ParseError
	{
		var resetNode = new ResetStatementNode();
		consumeMandatory(resetNode, SyntaxKind.RESET);
		consumeOptionally(resetNode, SyntaxKind.INITIAL);

		while (isOperand())
		{
			resetNode.addOperand(consumeOperandNode(resetNode));
		}

		return resetNode;
	}

	private boolean isOperand()
	{
		if (isAtEnd())
		{
			return false; // readability
		}

		return
			(peekKind(SyntaxKind.IDENTIFIER) && !isAtEnd(1) && peek(1).kind() != SyntaxKind.COLON_EQUALS_SIGN)
				|| peek().kind().isSystemFunction()
				|| peek().kind().isSystemVariable()
				|| peek().kind().canBeIdentifier(); // this should hopefully catch the begin of statements
	}

	private boolean isNotCallnatOrFetchModule()
	{
		return !peekKind(SyntaxKind.STRING_LITERAL) && !peekKind(SyntaxKind.IDENTIFIER);
	}

	private void resolveUnresolvedExternalPerforms()
	{
		var resolvedReferences = new ArrayList<ISymbolReferenceNode>();

		for (var unresolvedReference : unresolvedReferences)
		{
			if (unresolvedReference instanceof InternalPerformNode internalPerformNode)
			{
				var foundModule = sideloadModule(unresolvedReference.token().trimmedSymbolName(32), internalPerformNode.tokenNode());
				if (foundModule != null)
				{
					var externalPerform = new ExternalPerformNode(((InternalPerformNode) unresolvedReference));
					((BaseSyntaxNode) unresolvedReference.parent()).replaceChild((BaseSyntaxNode) unresolvedReference, externalPerform);
					externalPerform.setReference(foundModule);
				}

				// We mark the reference as resolved even though it might not be found.
				// We do this, because the `sideloadModule` already reports a diagnostic.
				resolvedReferences.add(unresolvedReference);
			}
		}

		unresolvedReferences.removeAll(resolvedReferences);
	}

	private void resolveUnresolvedInternalPerforms()
	{
		var resolvedReferences = new ArrayList<ISymbolReferenceNode>();
		for (var referencableNode : referencableNodes)
		{
			for (var unresolvedReference : unresolvedReferences)
			{
				if (!(unresolvedReference instanceof InternalPerformNode))
				{
					continue;
				}

				var unresolvedPerformName = unresolvedReference.token().trimmedSymbolName(32);
				if (unresolvedPerformName.equals(referencableNode.declaration().trimmedSymbolName(32)))
				{
					referencableNode.addReference(unresolvedReference);
					resolvedReferences.add(unresolvedReference);
				}
			}
		}

		unresolvedReferences.removeAll(resolvedReferences);
	}

	@SuppressWarnings({ "unused" }) // TODO: use this for error recovery
	private boolean isStatementStart()
	{
		if (tokens.isAtEnd())
		{
			return false;
		}

		var currentKind = tokens.peek().kind();
		if (currentKind.canBeIdentifier() && peekKind(1, SyntaxKind.COLON_EQUALS_SIGN))
		{
			return true;
		}

		return switch (currentKind)
			{
				case ACCEPT, ADD, ASSIGN, CALL, CALLNAT, CLOSE, COMMIT, COMPRESS, COMPUTE, DECIDE, DEFINE, DELETE, DISPLAY, DIVIDE, DO, DOEND, DOWNLOAD, EJECT, END, ESCAPE, EXAMINE, EXPAND, FETCH, FIND, FOR, FORMAT, GET, HISTOGRAM, IF, IGNORE, INCLUDE, INPUT, INSERT, INTERFACE, LIMIT, LOOP, METHOD, MOVE, MULTIPLY, NEWPAGE, OBTAIN, OPTIONS, PASSW, PERFORM, PRINT, PROCESS, PROPERTY, READ, REDEFINE, REDUCE, REINPUT, REJECT, RELEASE, REPEAT, RESET, RESIZE, RETRY, ROLLBACK, RUN, SELECT, SEPARATE, SET, SKIP, SORT, STACK, STOP, STORE, SUBTRACT, TERMINATE, UPDATE, WRITE ->
					true;
				case ON -> peekKind(1, SyntaxKind.ERROR);
				case OPEN -> peekKind(1, SyntaxKind.CONVERSATION);
				case PARSE -> peekKind(1, SyntaxKind.XML);
				case REQUEST -> peekKind(1, SyntaxKind.DOCUMENT);
				case SEND -> peekKind(1, SyntaxKind.METHOD);
				case SUSPEND -> peekKind(1, SyntaxKind.IDENTICAL) && peekKind(2, SyntaxKind.SUPPRESS);
				case UPLOAD -> peekKind(1, SyntaxKind.PC) && peekKind(2, SyntaxKind.FILE);
				default -> false;
			};
	}
}
