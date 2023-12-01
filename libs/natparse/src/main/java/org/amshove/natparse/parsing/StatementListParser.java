package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.conditionals.ChainedCriteriaOperator;
import org.amshove.natparse.natural.conditionals.ComparisonOperator;
import org.amshove.natparse.natural.conditionals.IHasComparisonOperator;
import org.amshove.natparse.natural.conditionals.ILogicalConditionCriteriaNode;
import org.amshove.natparse.natural.project.NaturalFileType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

public class StatementListParser extends AbstractParser<IStatementListNode>
{
	private static final Pattern SETKEY_PATTERN = Pattern.compile("(ENTR|CLR|PA[1-3]|PF([1-9]|[0-1][\\d]|2[0-4]))\\b");
	private static final List<SyntaxKind> TO_INTO = List.of(SyntaxKind.INTO, SyntaxKind.TO);

	private List<IReferencableNode> referencableNodes;

	private Set<String> currentModuleCallStack = new HashSet<>();

	public List<IReferencableNode> getReferencableNodes()
	{
		return referencableNodes;
	}

	public StatementListParser(IModuleProvider moduleProvider)
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
		return statementList(Set.of());
	}

	private boolean containsKindThatIsEndedByEndAll(Set<SyntaxKind> kinds)
	{
		for (var endAllKind : END_KINDS_THAT_END_ALL_ENDS)
		{
			if (kinds.contains(endAllKind))
			{
				return true;
			}
		}

		return false;
	}

	private StatementListNode statementList(SyntaxKind endTokenKind)
	{
		return statementList(Set.of(endTokenKind));
	}

	private StatementListNode statementList(Set<SyntaxKind> endTokenKinds)
	{
		var statementList = new StatementListNode();
		while (!tokens.isAtEnd())
		{
			try
			{
				if (!endTokenKinds.isEmpty()
					&& (endTokenKinds.contains(peekKind())
						|| (peekKind(SyntaxKind.END_ALL) && containsKindThatIsEndedByEndAll(endTokenKinds))))
				{
					break;
				}

				switch (tokens.peek().kind())
				{
					case ACCEPT:
						statementList.addStatement(acceptOrReject());
						break;
					case REJECT:
						statementList.addStatement(acceptOrReject());
						break;
					case ADD:
						statementList.addStatement(addStatement());
						break;
					case ASSIGN:
						statementList.addStatements(assignOrCompute(SyntaxKind.ASSIGN));
						break;
					case MOVE:
						statementList.addStatement(moveStatement());
						break;
					case AT:
						if (peekKind(1, SyntaxKind.END) && (peekKind(3, SyntaxKind.PAGE) || peekKind(2, SyntaxKind.PAGE)))
						{
							statementList.addStatement(parseAtPositionOf(SyntaxKind.END, SyntaxKind.PAGE, SyntaxKind.END_ENDPAGE, false, new EndOfPageNode()));
							break;
						}
						if (peekKind(1, SyntaxKind.TOP) && (peekKind(3, SyntaxKind.PAGE) || peekKind(2, SyntaxKind.PAGE)))
						{
							statementList.addStatement(parseAtPositionOf(SyntaxKind.TOP, SyntaxKind.PAGE, SyntaxKind.END_TOPPAGE, false, new TopOfPageNode()));
							break;
						}
						if (peekKind(1, SyntaxKind.START) && (peekKind(3, SyntaxKind.DATA) || peekKind(2, SyntaxKind.DATA)))
						{
							statementList.addStatement(parseAtPositionOf(SyntaxKind.START, SyntaxKind.DATA, SyntaxKind.END_START, true, new StartOfDataNode()));
							break;
						}
						if (peekKind(1, SyntaxKind.END) && (peekKind(3, SyntaxKind.DATA) || peekKind(2, SyntaxKind.DATA)))
						{
							statementList.addStatement(parseAtPositionOf(SyntaxKind.END, SyntaxKind.DATA, SyntaxKind.END_ENDDATA, true, new EndOfDataNode()));
							break;
						}
						if (peekKind(1, SyntaxKind.BREAK))
						{
							statementList.addStatement(breakOf());
							break;
						}
						tokens.advance(); // TODO: default case
						break;
					case BACKOUT:
						statementList.addStatement(backout());
						break;
					case BEFORE:
						statementList.addStatement(beforeBreak());
						break;
					case BREAK:
						statementList.addStatement(breakOf());
						break;
					case CALL:
						switch (peek(1).kind())
						{
							case FILE -> statementList.addStatement(callFile());
							case LOOP -> statementList.addStatement(callLoop());
							default -> statementList.addStatement(callStatement());
						}
						break;
					case CALLNAT:
						statementList.addStatement(callnat());
						break;
					case COMPRESS:
						statementList.addStatement(compress());
						break;
					case COMPOSE:
						statementList.addStatement(compose());
						break;
					case COMPUTE:
						statementList.addStatements(assignOrCompute(SyntaxKind.COMPUTE));
						break;
					case DOWNLOAD:
						statementList.addStatement(writeDownloadPc());
						break;
					case REDUCE:
						statementList.addStatement(reduce());
						break;
					case RESIZE:
						statementList.addStatement(resize());
						break;
					case CLOSE:
						switch (peek(1).kind())
						{
							case PRINTER -> statementList.addStatement(closePrinter());
							case WORK -> statementList.addStatement(closeWork());
							case PC -> statementList.addStatement(closePc());
							default -> statementList.addStatement(consumeFallback());
						}
						break;
					case EJECT:
						statementList.addStatement(eject());
						break;
					case SKIP:
						statementList.addStatement(skip());
						break;
					case ESCAPE:
						statementList.addStatement(escape());
						break;
					case FORMAT:
						statementList.addStatement(formatNode());
						break;
					case INPUT:
						statementList.addStatement(inputStatement());
						break;
					case OPTIONS:
						statementList.addStatement(options());
						break;
					case SELECT:
						statementList.addStatement(select());
						break;
					case INSERT:
						statementList.addStatement(insert());
						break;
					case UPDATE:
						statementList.addStatement(update());
						break;
					case DELETE:
						statementList.addStatement(delete());
						break;
					case PROCESS:
						if (peekKind(1, SyntaxKind.SQL))
						{
							statementList.addStatement(processSql());
							break;
						}
						statementList.addStatement(consumeFallback());
						break;
					case START:
						statementList.addStatement(parseAtPositionOf(SyntaxKind.START, SyntaxKind.DATA, SyntaxKind.END_START, true, new StartOfDataNode()));
						break;
					case INCLUDE:
						statementList.addStatement(include());
						break;
					case FETCH:
						statementList.addStatement(fetch());
						break;
					case MULTIPLY:
						statementList.addStatement(multiply());
						break;
					case LIMIT:
						if (peekKind(1, SyntaxKind.NUMBER_LITERAL))
						{
							statementList.addStatement(limit());
							break;
						}
						// fall through to IDENTIFIER
					case IDENTIFIER:
						statementList.addStatements(assignmentsOrIdentifierReference());
						break;
					case EXAMINE:
						statementList.addStatement(examine());
						break;
					case SEPARATE:
						statementList.addStatement(separate());
						break;
					case WRITE:
						if (peekKind(1, SyntaxKind.WORK))
						{
							statementList.addStatement(writeWork());
							break;
						}
						if (peekKind(1, SyntaxKind.PC))
						{
							statementList.addStatement(writeDownloadPc());
							break;
						}
						statementList.addStatement(write(SyntaxKind.WRITE));
						break;
					case PRINT:
						statementList.addStatement(write(SyntaxKind.PRINT));
						break;
					case DISPLAY:
						statementList.addStatement(display());
						break;
					case END:
						if (peekKind(1, SyntaxKind.PAGE) || peekKind(2, SyntaxKind.PAGE))
						{
							statementList.addStatement(parseAtPositionOf(SyntaxKind.END, SyntaxKind.PAGE, SyntaxKind.END_ENDPAGE, false, new EndOfPageNode()));
							break;
						}
						if (peekKind(1, SyntaxKind.DATA) || peekKind(2, SyntaxKind.DATA))
						{
							statementList.addStatement(parseAtPositionOf(SyntaxKind.END, SyntaxKind.DATA, SyntaxKind.END_ENDDATA, true, new EndOfDataNode()));
							break;
						}

						statementList.addStatement(end());
						break;
					case EXPAND:
						statementList.addStatement(expand());
						break;
					case DEFINE:
						switch (peek(1).kind())
						{
							case SUBROUTINE, IDENTIFIER -> statementList.addStatement(subroutine());
							case PRINTER -> statementList.addStatement(definePrinter());
							case WINDOW -> statementList.addStatement(defineWindow());
							case WORK -> statementList.addStatement(defineWork());
							case PROTOTYPE -> statementList.addStatement(definePrototype());
							case DATA ->
							{
								// can this even happen?
								tokens.advance();
								tokens.advance();
							}
							default ->
							{
								if (peek(1).kind().canBeIdentifier())
								{
									statementList.addStatement(subroutine());
								}
								else
								{
									tokens.advance();
									tokens.advance();
								}
							}
						}
						break;
					case IGNORE:
						statementList.addStatement(ignore());
						break;
					case NEWPAGE:
						statementList.addStatement(newPage());
						break;
					case HISTOGRAM:
						statementList.addStatement(histogram());
						break;
					case FIND:
						statementList.addStatement(find());
						break;
					case BROWSE:
						statementList.addStatement(readStatement());
						break;
					case READ:
						if (peek(1).kind() == SyntaxKind.WORK)
						{
							statementList.addStatement(readWork());
							break;
						}
						statementList.addStatement(readStatement());
						break;
					case GET:
						if (peek(1).kind() == SyntaxKind.TRANSACTION)
						{
							statementList.addStatement(getTransaction());
							break;
						}
						if (peek(1).kind() == SyntaxKind.SAME)
						{
							statementList.addStatement(getSame());
							break;
						}
						statementList.addStatement(getStatement());
						break;
					case PERFORM:
						if (peek(1).kind() == SyntaxKind.BREAK)
						{
							statementList.addStatement(performBreak());
							break;
						}
						statementList.addStatement(perform());
						break;
					case STACK:
						statementList.addStatement(stack());
						break;
					case TOP:
						statementList.addStatement(parseAtPositionOf(SyntaxKind.TOP, SyntaxKind.PAGE, SyntaxKind.END_TOPPAGE, false, new TopOfPageNode()));
						break;
					case REPEAT:
						statementList.addStatement(repeatLoop());
						break;
					case RESET:
						statementList.addStatement(resetStatement());
						break;
					case SUBTRACT:
						statementList.addStatement(subtractStatement());
						break;
					case DIVIDE:
						statementList.addStatement(divideStatement());
						break;
					case TERMINATE:
						statementList.addStatement(terminate());
						break;
					case DECIDE:
						if (peekKind(1, SyntaxKind.FOR))
						{
							statementList.addStatement(decideFor());
							break;
						}
						statementList.addStatement(decideOn());
						break;
					// Below this line, add statements that can be used as keywords within other statements.
					// Like FOR is a keyword in DECIDE and HISTOGRAM etc.
					case FOR:
						statementList.addStatement(forLoop());
						break;
					case IF:
						statementList.addStatement(ifStatement());
						break;
					// SORT statement has to start with END-ALL, so because consumeMandatoryClosing()
					// is checking for END-ALL, we have to parse it very late.
					case END_ALL, SORT:
						statementList.addStatement(sortStatement());
						break;
					case ON:
						if (peekKind(1, SyntaxKind.ERROR))
						{
							statementList.addStatement(onError());
							break;
						}
						// some statements use the ON keyword in them, so fallthrough until the statements are parsed
					case LPAREN:
						if (getKind(1).isAttribute())
						{
							// Workaround for attributes. Should be added to the operand they belong to.
							var tokenNode = new SyntheticTokenStatementNode();
							consumeAttributeDefinition(tokenNode);
							statementList.addStatement(tokenNode);
							break;
						}
					case SET:
						if (peekKind(1, SyntaxKind.KEY))
						{
							statementList.addStatement(setKey());
							break;
						}
						if (peekKind(1, SyntaxKind.WINDOW))
						{
							statementList.addStatement(setWindow());
							break;
						}
						// FALLTHROUGH TO DEFAULT INTENDED - SET CONTROL etc. not implemented
					default:

						if (isAssignmentStart())
						{
							statementList.addStatements(assignmentsOrIdentifierReference());
							break;
						}

						if (peek().kind().isSystemFunction())
						{
							// this came up for *PAGE-NUMBER(PRINTERREP) in a WRITE statement, because we don't parse WRITE operands yet
							// Can be removed in the future
							consumeOperandNode(statementList);
							break;
						}

						// While the parser is incomplete, we just add a node for every token
						var tokenStatementNode = new SyntheticTokenStatementNode();
						consume(tokenStatementNode);

						// Remove once we can parse expressions and all places where expressions can be used (REPEAT, IF, DECIDE, ...)
						if (tokenStatementNode.token().kind() == SyntaxKind.MASK)
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

	private StatementNode options() throws ParseError
	{
		var options = new OptionsStatementNode();
		consumeMandatory(options, SyntaxKind.OPTIONS);
		while (peekKind(1, SyntaxKind.EQUALS_SIGN))
		{
			// Currently consume anything in the form of A = B. There doesn't seem to be a good documentation about possible options
			consumeMandatoryIdentifierTokenNode(options);
			consumeMandatory(options, SyntaxKind.EQUALS_SIGN);
			consume(options);
		}

		return options;
	}

	private StatementNode onError() throws ParseError
	{
		var onError = new OnErrorNode();
		consumeMandatory(onError, SyntaxKind.ON);
		consumeMandatory(onError, SyntaxKind.ERROR);
		onError.setBody(statementList(SyntaxKind.END_ERROR));
		consumeMandatory(onError, SyntaxKind.END_ERROR);
		checkForEmptyBody(onError);
		return onError;
	}

	private StatementNode definePrototype() throws ParseError
	{
		if (peekKind(2, SyntaxKind.FOR) || peekKind(2, SyntaxKind.VARIABLE))
		{
			return definePrototypeVariable();
		}

		var prototype = new DefinePrototypeNode();
		var opening = consumeMandatory(prototype, SyntaxKind.DEFINE);
		consumeMandatory(prototype, SyntaxKind.PROTOTYPE);

		var name = consumeMandatoryIdentifier(prototype); // TODO: Sideload
		prototype.setPrototype(name);
		while (!isAtEnd() && !peekKind(SyntaxKind.END_PROTOTYPE))
		{
			consume(prototype); // incomplete
		}

		consumeMandatoryClosing(prototype, SyntaxKind.END_PROTOTYPE, opening);
		return prototype;
	}

	private StatementNode definePrototypeVariable() throws ParseError
	{
		var prototype = new DefinePrototypeNode();
		var opening = consumeMandatory(prototype, SyntaxKind.DEFINE);
		consumeMandatory(prototype, SyntaxKind.PROTOTYPE);
		consumeOptionally(prototype, SyntaxKind.FOR);
		consumeMandatory(prototype, SyntaxKind.VARIABLE);

		prototype.setVariableReference(consumeVariableReferenceNode(prototype));
		while (!isAtEnd() && !peekKind(SyntaxKind.END_PROTOTYPE))
		{
			consume(prototype); // incomplete
		}

		consumeMandatoryClosing(prototype, SyntaxKind.END_PROTOTYPE, opening);
		return prototype;
	}

	private StatementNode terminate() throws ParseError
	{
		var terminate = new TerminateNode();
		consumeMandatory(terminate, SyntaxKind.TERMINATE);
		if (isOperand())
		{
			var exitCode = consumeOperandNode(terminate);
			terminate.addOperand(exitCode);
			checkLiteralTypeIfLiteral(exitCode, SyntaxKind.NUMBER_LITERAL);
		}

		if (isOperand())
		{
			terminate.addOperand(consumeOperandNode(terminate));
		}

		return terminate;
	}

	private StatementNode setWindow() throws ParseError
	{
		var setWindow = new SetWindowNode();
		consumeMandatory(setWindow, SyntaxKind.SET);
		consumeMandatory(setWindow, SyntaxKind.WINDOW);
		if (peekKind(SyntaxKind.OFF))
		{
			setWindow.setWindow(consumeMandatory(setWindow, SyntaxKind.OFF));
		}
		else
		{
			var windowName = consumeNonConcatLiteralNode(setWindow, SyntaxKind.STRING_LITERAL);
			setWindow.setWindow(windowName.token());
		}

		return setWindow;
	}

	private StatementNode writeWork() throws ParseError
	{
		var writeWork = new WriteWorkNode();
		consumeMandatory(writeWork, SyntaxKind.WRITE);
		consumeMandatory(writeWork, SyntaxKind.WORK);
		consumeOptionally(writeWork, SyntaxKind.FILE);
		writeWork.setNumber(consumeNonConcatLiteralNode(writeWork, SyntaxKind.NUMBER_LITERAL));
		writeWork.setVariable(consumeOptionally(writeWork, SyntaxKind.VARIABLE));
		while (!isAtEnd() && isOperand())
		{
			writeWork.addOperand(consumeOperandNode(writeWork));
		}

		return writeWork;
	}

	private StatementNode writeDownloadPc() throws ParseError
	{
		var writePc = new WritePcNode();
		consumeAnyMandatory(writePc, List.of(SyntaxKind.WRITE, SyntaxKind.DOWNLOAD));
		consumeMandatory(writePc, SyntaxKind.PC);
		consumeOptionally(writePc, SyntaxKind.FILE);
		writePc.setNumber(consumeNonConcatLiteralNode(writePc, SyntaxKind.NUMBER_LITERAL));
		if (consumeOptionally(writePc, SyntaxKind.COMMAND))
		{
			writePc.setOperand(consumeOperandNode(writePc));
			consumeAnyOptionally(writePc, List.of(SyntaxKind.SYNC, SyntaxKind.ASYNC));
		}
		else
		{
			writePc.setVariable(consumeOptionally(writePc, SyntaxKind.VARIABLE));
			writePc.setOperand(consumeOperandNode(writePc));
		}

		return writePc;
	}

	private StatementNode closePc() throws ParseError
	{
		var closePc = new ClosePcNode();
		consumeMandatory(closePc, SyntaxKind.CLOSE);
		consumeMandatory(closePc, SyntaxKind.PC);
		consumeOptionally(closePc, SyntaxKind.FILE);

		var number = consumeNonConcatLiteralNode(closePc, SyntaxKind.NUMBER_LITERAL);
		closePc.setNumber(number);

		return closePc;
	}

	private StatementNode closeWork() throws ParseError
	{
		var closeWork = new CloseWorkNode();
		consumeMandatory(closeWork, SyntaxKind.CLOSE);
		consumeMandatory(closeWork, SyntaxKind.WORK);
		consumeOptionally(closeWork, SyntaxKind.FILE);

		var number = consumeNonConcatLiteralNode(closeWork, SyntaxKind.NUMBER_LITERAL);
		closeWork.setNumber(number);

		return closeWork;
	}

	private StatementNode backout() throws ParseError
	{
		var stmt = new BackoutNode();
		consumeMandatory(stmt, SyntaxKind.BACKOUT);
		consumeOptionally(stmt, SyntaxKind.TRANSACTION);
		return stmt;
	}

	private static final List<SyntaxKind> MATH_STATEMENT_TO_GIVING = List.of(SyntaxKind.TO, SyntaxKind.GIVING);

	private StatementNode divideStatement() throws ParseError
	{
		var divide = new DivideStatementNode();
		consumeMandatory(divide, SyntaxKind.DIVIDE);
		divide.setIsRounded(consumeOptionally(divide, SyntaxKind.ROUNDED));
		while (!isAtEnd() && !peekKind(SyntaxKind.INTO))
		{
			divide.addOperand(consumeArithmeticExpression(divide));
		}

		consumeMandatory(divide, SyntaxKind.INTO);
		divide.setTarget(consumeArithmeticExpression(divide));
		if (consumeOptionally(divide, SyntaxKind.GIVING))
		{
			divide.setIsGiving(true);
			divide.setGiving(consumeOperandNode(divide));
		}

		if (consumeOptionally(divide, SyntaxKind.REMAINDER))
		{
			divide.setRemainder(consumeOperandNode(divide));
		}

		return divide;
	}

	private StatementNode multiply() throws ParseError
	{
		var multiply = new MultiplyStatementNode();
		consumeMandatory(multiply, SyntaxKind.MULTIPLY);
		multiply.setIsRounded(consumeOptionally(multiply, SyntaxKind.ROUNDED));
		multiply.setTarget(consumeOperandNode(multiply));
		consumeMandatory(multiply, SyntaxKind.BY);
		while (!isAtEnd() && !isStatementStart() && isOperand())
		{
			multiply.addOperand(consumeArithmeticExpression(multiply));
		}

		if (peekKind(SyntaxKind.GIVING))
		{
			var giving = new MultiplyGivingStatementNode(multiply);
			consumeMandatory(giving, SyntaxKind.GIVING);
			giving.setGiving(consumeOperandNode(giving));
			return giving;
		}

		return multiply;
	}

	private StatementNode subtractStatement() throws ParseError
	{
		var subtract = new SubtractStatementNode();
		consumeMandatory(subtract, SyntaxKind.SUBTRACT);
		subtract.setIsRounded(consumeOptionally(subtract, SyntaxKind.ROUNDED));
		while (!isAtEnd() && !peekKind(SyntaxKind.FROM))
		{
			subtract.addOperand(consumeArithmeticExpression(subtract));
		}

		consumeMandatory(subtract, SyntaxKind.FROM);
		subtract.setTarget(consumeOperandNode(subtract));

		if (peekKind(SyntaxKind.GIVING))
		{
			var subtractGiving = new SubtractGivingStatementNode(subtract);
			consumeMandatory(subtractGiving, SyntaxKind.GIVING);
			subtractGiving.setGiving(consumeOperandNode(subtractGiving));
			return subtractGiving;
		}

		return subtract;
	}

	private StatementNode addStatement() throws ParseError
	{
		var add = new AddStatementNode();
		consumeMandatory(add, SyntaxKind.ADD);
		add.setIsRounded(consumeOptionally(add, SyntaxKind.ROUNDED));
		while (!isAtEnd() && !peekAny(MATH_STATEMENT_TO_GIVING))
		{
			add.addOperand(consumeArithmeticExpression(add));
		}

		consumeAnyMandatory(add, MATH_STATEMENT_TO_GIVING);
		add.setIsGiving(previousToken().kind() == SyntaxKind.GIVING);
		add.setTarget(consumeOperandNode(add));
		return add;
	}

	/**
	 * Prioritizes e.g. (AD=IO), then substring then default operand. Useful for expressions that can be a single
	 * attribute definition or a whole expression
	 */
	private IOperandNode consumeControlLiteralOrSubstringOrOperand(BaseSyntaxNode node) throws ParseError
	{
		if (peekKind(SyntaxKind.LPAREN) && getKind(1).isAttribute())
		{
			return consumeLiteralNode(node);
		}

		return consumeSubstringOrOperand(node);
	}

	private static final List<String> ALLOWED_WORK_FILE_ATTRIBUTES = List.of("NOAPPEND", "APPEND", "DELETE", "KEEP", "BOM", "NOBOM", "KEEPCR", "REMOVECR");
	private static final List<String> ALLOWED_WORK_FILE_TYPES = List.of("DEFAULT", "TRANSFER", "SAG", "ASCII", "ASCII-COMPRESSED", "ENTIRECONNECTION", "FORMATTED", "UNFORMATTED", "PORTABLE", "CSV");

	private StatementNode defineWork() throws ParseError
	{
		var work = new DefineWorkFileNode();

		consumeMandatory(work, SyntaxKind.DEFINE);
		consumeMandatory(work, SyntaxKind.WORK);
		consumeMandatory(work, SyntaxKind.FILE);

		var number = consumeNonConcatLiteralNode(work, SyntaxKind.NUMBER_LITERAL);
		checkNumericRange(number, 1, 32);
		work.setNumber(number);

		if (isOperand() && !peekKind(SyntaxKind.TYPE) && !peekKind(SyntaxKind.ATTRIBUTES))
		{
			var path = consumeOperandNode(work);
			checkOperand(path, "The path of a work file can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
			checkLiteralTypeIfLiteral(path, SyntaxKind.STRING_LITERAL);

			work.setPath(path);
		}

		if (consumeOptionally(work, SyntaxKind.TYPE))
		{
			var type = consumeOperandNode(work);
			checkOperand(type, "The type of a work file can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
			checkLiteralTypeIfLiteral(type, SyntaxKind.STRING_LITERAL);
			checkStringLiteralValue(type, ALLOWED_WORK_FILE_TYPES);

			work.setType(type);
		}

		if (consumeOptionally(work, SyntaxKind.ATTRIBUTES))
		{
			var attributes = consumeOperandNode(work);
			checkOperand(attributes, "The attributes of a work file can only be a constant string or a variable reference", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
			checkLiteralTypeIfLiteral(attributes, SyntaxKind.STRING_LITERAL);
			if (attributes instanceof ILiteralNode literal && literal.token().kind() == SyntaxKind.STRING_LITERAL)
			{
				var attributeValues = literal.token().stringValue();
				var separator = attributeValues.contains(",")
					? ","
					: " ";

				var values = attributeValues.split(separator);
				for (var value : values)
				{
					checkConstantStringValue(attributes, value.trim(), ALLOWED_WORK_FILE_ATTRIBUTES);
				}
			}

			work.setAttributes(attributes);
		}

		return work;
	}

	private CompressStatementNode compress() throws ParseError
	{
		var compress = new CompressStatementNode();
		consumeMandatory(compress, SyntaxKind.COMPRESS);

		compress.setNumeric(consumeOptionally(compress, SyntaxKind.NUMERIC));
		compress.setFull(consumeOptionally(compress, SyntaxKind.FULL));

		while (!peekAny(TO_INTO) && !tokens.isAtEnd())
		{
			var operand = consumeSubstringOrOperand(compress);
			compress.addOperand(operand);

			if (consumeOptionally((BaseSyntaxNode) operand, SyntaxKind.LPAREN))
			{
				while (!consumeOptionally((BaseSyntaxNode) operand, SyntaxKind.RPAREN) || tokens.isAtEnd())
				{
					consume((BaseSyntaxNode) operand);
				}
			}
		}

		consumeAnyMandatory(compress, TO_INTO); // TO not documented but okay
		compress.setIntoTarget(consumeSubstringOrOperand(compress));

		var consumedLeaving = consumeOptionally(compress, SyntaxKind.LEAVING);
		if (consumedLeaving)
		{
			compress.setLeavingSpace(!consumeOptionally(compress, SyntaxKind.NO));
			consumeOptionally(compress, SyntaxKind.SPACE);
		}

		if (consumeOptionally(compress, SyntaxKind.WITH))
		{
			if (consumedLeaving)
			{
				report(ParserErrors.compressCantHaveLeavingNoAndWithDelimiters(previousToken()));
			}

			compress.setWithAllDelimiters(consumeOptionally(compress, SyntaxKind.ALL));
			consumeAnyOptionally(compress, List.of(SyntaxKind.DELIMITER, SyntaxKind.DELIMITERS));
			if (isOperand())
			{
				var delimiter = consumeOperandNode(compress);
				if (delimiter instanceof ILiteralNode literal)
				{
					if (checkLiteralType(literal, SyntaxKind.STRING_LITERAL, SyntaxKind.HEX_LITERAL))
					{
						checkStringLength(literal.token(), literal.token().stringValue(), 1);
					}
				}
				compress.setDelimiter(delimiter);
			}
			compress.setLeavingSpace(false);
			compress.setWithDelimiters(true);
		}

		return compress;
	}

	private static final List<SyntaxKind> COMPOSE_SUBCLAUSES = List.of(SyntaxKind.RESETTING, SyntaxKind.MOVING, SyntaxKind.ASSIGNING, SyntaxKind.FORMATTING, SyntaxKind.EXTRACTING);
	private static final List<SyntaxKind> COMPOSE_RESETTING_SUBCLAUSES = List.of(SyntaxKind.DATAAREA, SyntaxKind.TEXTAREA, SyntaxKind.MACROAREA, SyntaxKind.ALL);
	private static final List<SyntaxKind> COMPOSE_FORMATTING_SUBCLAUSES = List.of(SyntaxKind.OUTPUT, SyntaxKind.INPUT, SyntaxKind.STATUS, SyntaxKind.PROFILE, SyntaxKind.MESSAGES, SyntaxKind.ERRORS, SyntaxKind.ENDING, SyntaxKind.STARTING);
	private static final List<SyntaxKind> COMPOSE_FORMATTING_OUTPUT = List.of(SyntaxKind.SUPPRESSED, SyntaxKind.CALLING, SyntaxKind.TO, SyntaxKind.DOCUMENT);
	private static final List<SyntaxKind> COMPOSE_ALL_SUBCLAUSES = List.of(SyntaxKind.DATAAREA, SyntaxKind.TO, SyntaxKind.LAST, SyntaxKind.VARIABLES, SyntaxKind.OUTPUT, SyntaxKind.INPUT, SyntaxKind.STATUS, SyntaxKind.PROFILE, SyntaxKind.MESSAGES, SyntaxKind.ERRORS, SyntaxKind.ENDING, SyntaxKind.STARTING);

	private ComposeStatementNode compose() throws ParseError
	{
		var compose = new ComposeStatementNode();
		consumeMandatory(compose, SyntaxKind.COMPOSE);

		while (peekAny(COMPOSE_SUBCLAUSES))
		{
			var consumed = consumeAnyMandatory(compose, COMPOSE_SUBCLAUSES);
			switch (consumed.kind())
			{
				case RESETTING:
					consumeAnyOptionally(compose, COMPOSE_RESETTING_SUBCLAUSES);
					break;
				case MOVING:
					if (consumeOptionally(compose, SyntaxKind.LAST))
					{
						consumeOptionally(compose, SyntaxKind.OUTPUT);
						if (consumeOptionally(compose, SyntaxKind.TO))
						{
							consumeMandatory(compose, SyntaxKind.VARIABLES);
						}
						consumeComposeOperands(compose);
						consumeComposeMovingStatus(compose);
						break;
					}

					if (consumeOptionally(compose, SyntaxKind.OUTPUT))
					{
						if (consumeOptionally(compose, SyntaxKind.TO))
						{
							consumeMandatory(compose, SyntaxKind.VARIABLES);
						}
						consumeComposeOperands(compose);
						consumeComposeMovingStatus(compose);
						break;
					}

					if (isOperand())
					{
						var numConsumed = consumeComposeOperands(compose);
						if (numConsumed == 1)
						{
							if (peekKind(SyntaxKind.TO) && peekKind(1, SyntaxKind.DATAAREA))
							{
								consumeMandatory(compose, SyntaxKind.TO);
								consumeMandatory(compose, SyntaxKind.DATAAREA);
							}
							consumeOptionally(compose, SyntaxKind.OUTPUT);
							if (consumeOptionally(compose, SyntaxKind.TO))
							{
								consumeMandatory(compose, SyntaxKind.VARIABLES);
							}
							consumeComposeOperands(compose);
						}
						else
						{
							if (consumeOptionally(compose, SyntaxKind.TO))
							{
								consumeMandatory(compose, SyntaxKind.DATAAREA);
							}
							consumeOptionally(compose, SyntaxKind.LAST);
						}
					}

					consumeComposeMovingStatus(compose);
					break;
				case FORMATTING:
					while (peekAny(COMPOSE_FORMATTING_SUBCLAUSES))
					{
						consumed = consumeAnyMandatory(compose, COMPOSE_FORMATTING_SUBCLAUSES);
						switch (consumed.kind())
						{
							case OUTPUT:
								// (rep) specified
								if (consumeOptionally(compose, SyntaxKind.LPAREN))
								{
									while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
									{
										consume(compose);
									}
									consumeMandatory(compose, SyntaxKind.RPAREN);
									break;
								}
								consumed = consumeAnyMandatory(compose, COMPOSE_FORMATTING_OUTPUT);
								switch (consumed.kind())
								{
									case SUPPRESSED:
										break;
									case CALLING:
										var node = consumeOperandNode(compose);
										checkOperand(node, "The type of %s can only be a constant string or a variable reference.".formatted(consumed.kind()), AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
										checkLiteralTypeIfLiteral(node, SyntaxKind.STRING_LITERAL);
										break;
									case TO:
										consumeMandatory(compose, SyntaxKind.VARIABLES);
										consumeOptionally(compose, SyntaxKind.CONTROL);
										consumeComposeOperands(compose);
										break;
									case DOCUMENT:
										consumeAnyOptionally(compose, TO_INTO);
										consumeEitherOptionally(compose, SyntaxKind.FINAL, SyntaxKind.INTERMEDIATE);
										if (consumeOptionally(compose, SyntaxKind.CABINET))
										{
											var cab = consumeOperandNode(compose);
											checkOperand(cab, "The CABINET can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
											checkLiteralTypeIfLiteral(cab, SyntaxKind.STRING_LITERAL);
											if (consumeOptionally(compose, SyntaxKind.PASSW))
											{
												consumeMandatory(compose, SyntaxKind.EQUALS_SIGN);
												var passw = consumeOperandNode(compose);
												checkOperand(passw, "The PASSW can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
												checkLiteralTypeIfLiteral(passw, SyntaxKind.STRING_LITERAL);
											}
										}
										consumeOptionally(compose, SyntaxKind.GIVING);
										consumeComposeOperands(compose);
										break;
									default:
										break;
								}

								break;
							case INPUT:
								if (!consumeOptionally(compose, SyntaxKind.DATAAREA))
								{
									var input = consumeOperandNode(compose);
									checkOperand(input, "The INPUT clause must be followed by DATAAREA or exactly one constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
									checkLiteralTypeIfLiteral(input, SyntaxKind.STRING_LITERAL);
								}

								if (consumeOptionally(compose, SyntaxKind.FROM))
								{
									while (peekAny(List.of(SyntaxKind.EXIT, SyntaxKind.CABINET)))
									{
										consumed = consumeAnyMandatory(compose, List.of(SyntaxKind.EXIT, SyntaxKind.CABINET));

										if (consumed.kind() == SyntaxKind.CABINET)
										{
											var cab = consumeOperandNode(compose);
											checkOperand(cab, "The CABINET can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
											checkLiteralTypeIfLiteral(cab, SyntaxKind.STRING_LITERAL);
											if (consumeOptionally(compose, SyntaxKind.PASSW))
											{
												consumeMandatory(compose, SyntaxKind.EQUALS_SIGN);
												var passw = consumeOperandNode(compose);
												checkOperand(passw, "The PASSW can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
												checkLiteralTypeIfLiteral(passw, SyntaxKind.STRING_LITERAL);
											}
										}
										if (consumed.kind() == SyntaxKind.EXIT)
										{
											var exit = consumeOperandNode(compose);
											checkOperand(exit, "The EXIT can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
											checkLiteralTypeIfLiteral(exit, SyntaxKind.STRING_LITERAL);
										}
									}
								}
								break;
							case STATUS:
								var stat = consumeOperandNode(compose);
								checkOperand(stat, "The STATUS clause must be followed by a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
								checkLiteralTypeIfLiteral(stat, SyntaxKind.STRING_LITERAL);
								consumeComposeOperands(compose);
								break;
							case PROFILE:
								var prof = consumeOperandNode(compose);
								checkOperand(prof, "The PROFILE name can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
								checkLiteralTypeIfLiteral(prof, SyntaxKind.STRING_LITERAL);
								break;
							case MESSAGES:
								if (!consumeOptionally(compose, SyntaxKind.SUPPRESSED))
								{
									consumeOptionally(compose, SyntaxKind.LISTED);
									consumeOptionally(compose, SyntaxKind.ON);
									consumeMandatory(compose, SyntaxKind.LPAREN);
									// currently consume everything until closing parenthesis to consume things like attribute definition etc.
									while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
									{
										consume(compose);
									}
									consumeMandatory(compose, SyntaxKind.RPAREN);
								}
								break;
							case ERRORS:
								if (!consumeOptionally(compose, SyntaxKind.INTERCEPTED))
								{
									consumeOptionally(compose, SyntaxKind.LISTED);
									consumeOptionally(compose, SyntaxKind.ON);
									consumeMandatory(compose, SyntaxKind.LPAREN);
									// currently consume everything until closing parenthesis to consume things like attribute definition etc.
									while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
									{
										consume(compose);
									}
									consumeMandatory(compose, SyntaxKind.RPAREN);
								}
								break;
							case ENDING:
								if (consumeOptionally(compose, SyntaxKind.AFTER))
								{
									var ending = consumeOperandNode(compose);
									checkOperand(ending, "The ENDING operand can only be a constant numeric or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
									checkLiteralTypeIfLiteral(ending, SyntaxKind.NUMBER_LITERAL);
									consumeOptionally(compose, SyntaxKind.PAGES);
								}
								else
								{
									consumeOptionally(compose, SyntaxKind.AT);
									consumeOptionally(compose, SyntaxKind.PAGE);
									var ending = consumeOperandNode(compose);
									checkOperand(ending, "The ENDING operand can only be a constant numeric or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
									checkLiteralTypeIfLiteral(ending, SyntaxKind.NUMBER_LITERAL);
								}
								break;
							case STARTING:
								consumeOptionally(compose, SyntaxKind.FROM);
								consumeOptionally(compose, SyntaxKind.PAGE);
								var starting = consumeOperandNode(compose);
								checkOperand(starting, "The STARTING operand can only be a constant numeric or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
								checkLiteralTypeIfLiteral(starting, SyntaxKind.NUMBER_LITERAL);
								break;
							default:
								break;
						}
					}
					break;
				case ASSIGNING:
					consumeOptionally(compose, SyntaxKind.TEXTVARIABLE);
					consumeAssigning(compose);
					while (peekKind(SyntaxKind.COMMA))
					{
						consumeMandatory(compose, SyntaxKind.COMMA);
						consumeAssigning(compose);
					}
					break;
				case EXTRACTING:
					consumeOptionally(compose, SyntaxKind.TEXTVARIABLE);
					consumeExtracting(compose);
					while (peekKind(SyntaxKind.COMMA))
					{
						consumeMandatory(compose, SyntaxKind.COMMA);
						consumeExtracting(compose);
					}
					break;
				default:
					break;
			}
		}
		return compose;
	}

	private void consumeComposeMovingStatus(BaseSyntaxNode node) throws ParseError
	{
		if (consumeOptionally(node, SyntaxKind.STATUS))
		{
			consumeOptionally(node, SyntaxKind.TO);
			consumeComposeOperands(node);
		}
	}

	private void consumeAssigning(BaseSyntaxNode node) throws ParseError
	{
		var left = consumeOperandNode(node);
		checkOperand(left, "The left side can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
		consumeMandatory(node, SyntaxKind.EQUALS_SIGN);
		var right = consumeOperandNode(node);
		checkOperand(right, "The right side can only be a constant string or numeric or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
		checkLiteralTypeIfLiteral(right, SyntaxKind.STRING_LITERAL, SyntaxKind.NUMBER_LITERAL);
	}

	private void consumeExtracting(BaseSyntaxNode node) throws ParseError
	{
		var left = consumeOperandNode(node);
		checkOperand(left, "The left side can only be a variable reference.", AllowedOperand.VARIABLE_REFERENCE);
		consumeMandatory(node, SyntaxKind.EQUALS_SIGN);
		var right = consumeOperandNode(node);
		checkOperand(right, "The right side can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
		checkLiteralTypeIfLiteral(right, SyntaxKind.STRING_LITERAL);
	}

	private int consumeComposeOperands(BaseSyntaxNode node) throws ParseError
	{
		int numFound = 0;
		while (isOperand() && !(peekAny(COMPOSE_SUBCLAUSES) || peekAny(COMPOSE_ALL_SUBCLAUSES) || isStatementStart()))
		{
			numFound++;
			consumeOperandNode(node);
		}
		return numFound;
	}

	private StatementNode reduce() throws ParseError
	{
		if (peekAny(1, List.of(SyntaxKind.SIZE, SyntaxKind.DYNAMIC)))
		{
			return reduceDynamic();
		}

		var reduce = new ReduceArrayNode();
		consumeMandatory(reduce, SyntaxKind.REDUCE);
		if (consumeOptionally(reduce, SyntaxKind.OCCURRENCES))
		{
			consumeMandatory(reduce, SyntaxKind.OF);
		}

		consumeMandatory(reduce, SyntaxKind.ARRAY);
		var array = consumeVariableReferenceNode(reduce);
		reduce.setArrayToReduce(array);
		consumeMandatory(reduce, SyntaxKind.TO);

		if (consumeOptionally(reduce, SyntaxKind.LPAREN))
		{
			do
			{
				reduce.addDimension(consumeArrayAccess(reduce));
			}
			while (consumeOptionally(reduce, SyntaxKind.COMMA));
			consumeMandatory(reduce, SyntaxKind.RPAREN);
		}
		else
		{
			var literal = consumeLiteralNode(reduce, SyntaxKind.NUMBER_LITERAL);
			checkIntLiteralValue(literal, 0);
			reduce.addDimension(literal);
		}

		if (consumeOptionally(reduce, SyntaxKind.GIVING))
		{
			reduce.setErrorVariable(consumeVariableReferenceNode(reduce));
		}

		return reduce;
	}

	private StatementNode reduceDynamic() throws ParseError
	{
		var reduce = new ReduceDynamicNode();
		consumeMandatory(reduce, SyntaxKind.REDUCE);
		if (consumeOptionally(reduce, SyntaxKind.SIZE))
		{
			consumeMandatory(reduce, SyntaxKind.OF);
		}

		consumeMandatory(reduce, SyntaxKind.DYNAMIC);
		consumeOptionally(reduce, SyntaxKind.VARIABLE);

		var toReduce = consumeVariableReferenceNode(reduce);
		reduce.setVariableToResize(toReduce);
		consumeMandatory(reduce, SyntaxKind.TO);
		var newSize = consumeOperandNode(reduce);
		reduce.setSizeToResizeTo(newSize);

		if (consumeOptionally(reduce, SyntaxKind.GIVING))
		{
			reduce.setErrorVariable(consumeVariableReferenceNode(reduce));
		}

		return reduce;
	}

	private StatementNode expand() throws ParseError
	{
		if (peekAny(1, List.of(SyntaxKind.SIZE, SyntaxKind.DYNAMIC)))
		{
			return expandDynamic();
		}

		var expand = new ExpandArrayNode();
		consumeMandatory(expand, SyntaxKind.EXPAND);
		if (consumeOptionally(expand, SyntaxKind.OCCURRENCES))
		{
			consumeMandatory(expand, SyntaxKind.OF);
		}

		consumeMandatory(expand, SyntaxKind.ARRAY);
		var array = consumeVariableReferenceNode(expand);
		expand.setArrayToExpand(array);
		consumeMandatory(expand, SyntaxKind.TO);

		consumeMandatory(expand, SyntaxKind.LPAREN);
		do
		{
			expand.addDimension(consumeArrayAccess(expand));
		}
		while (consumeOptionally(expand, SyntaxKind.COMMA));
		consumeMandatory(expand, SyntaxKind.RPAREN);

		if (consumeOptionally(expand, SyntaxKind.GIVING))
		{
			expand.setErrorVariable(consumeVariableReferenceNode(expand));
		}

		return expand;
	}

	private StatementNode expandDynamic() throws ParseError
	{
		var expand = new ExpandDynamicNode();
		consumeMandatory(expand, SyntaxKind.EXPAND);
		if (consumeOptionally(expand, SyntaxKind.SIZE))
		{
			consumeMandatory(expand, SyntaxKind.OF);
		}

		consumeMandatory(expand, SyntaxKind.DYNAMIC);
		consumeOptionally(expand, SyntaxKind.VARIABLE);

		var toReduce = consumeVariableReferenceNode(expand);
		expand.setVariableToResize(toReduce);
		consumeMandatory(expand, SyntaxKind.TO);
		var newSize = consumeOperandNode(expand);
		expand.setSizeToResizeTo(newSize);

		if (consumeOptionally(expand, SyntaxKind.GIVING))
		{
			expand.setErrorVariable(consumeVariableReferenceNode(expand));
		}

		return expand;
	}

	private StatementNode resize() throws ParseError
	{
		if (peekAny(1, List.of(SyntaxKind.SIZE, SyntaxKind.DYNAMIC)))
		{
			return resizeDynamic();
		}

		var resize = new ResizeArrayNode();
		consumeMandatory(resize, SyntaxKind.RESIZE);
		if (consumeOptionally(resize, SyntaxKind.AND))
		{
			consumeMandatory(resize, SyntaxKind.RESET);
		}

		if (consumeOptionally(resize, SyntaxKind.OCCURRENCES))
		{
			consumeMandatory(resize, SyntaxKind.OF);
		}

		consumeMandatory(resize, SyntaxKind.ARRAY);
		var array = consumeVariableReferenceNode(resize);
		resize.setArrayToResize(array);
		consumeMandatory(resize, SyntaxKind.TO);

		consumeMandatory(resize, SyntaxKind.LPAREN);
		do
		{
			resize.addDimension(consumeArrayAccess(resize));
		}
		while (consumeOptionally(resize, SyntaxKind.COMMA));
		consumeMandatory(resize, SyntaxKind.RPAREN);

		if (consumeOptionally(resize, SyntaxKind.GIVING))
		{
			resize.setErrorVariable(consumeVariableReferenceNode(resize));
		}

		return resize;
	}

	private StatementNode resizeDynamic() throws ParseError
	{
		var resize = new ResizeDynamicNode();
		consumeMandatory(resize, SyntaxKind.RESIZE);
		if (consumeOptionally(resize, SyntaxKind.SIZE))
		{
			consumeMandatory(resize, SyntaxKind.OF);
		}

		consumeMandatory(resize, SyntaxKind.DYNAMIC);
		consumeOptionally(resize, SyntaxKind.VARIABLE);
		var toResize = consumeVariableReferenceNode(resize);
		resize.setVariableToResize(toResize);
		consumeMandatory(resize, SyntaxKind.TO);
		var newSize = consumeOperandNode(resize);
		resize.setSizeToResizeTo(newSize);

		if (consumeOptionally(resize, SyntaxKind.GIVING))
		{
			resize.setErrorVariable(consumeVariableReferenceNode(resize));
		}

		return resize;
	}

	private StatementNode skip() throws ParseError
	{
		var skip = new SkipStatementNode();
		consumeMandatory(skip, SyntaxKind.SKIP);

		if (consumeOptionally(skip, SyntaxKind.LPAREN))
		{
			var spec = consumeReportSpecificationOperand(skip);
			skip.setReportSpecification(spec);
			consumeMandatory(skip, SyntaxKind.RPAREN);
		}

		var linesToSkip = consumeOperandNode(skip);
		skip.setToSkip(linesToSkip);

		consumeOptionally(skip, SyntaxKind.LINES);

		return skip;
	}

	private StatementNode select() throws ParseError
	{
		// Right now, just consume the SELECT entirely
		var select = new SelectNode();
		var opening = consumeMandatory(select, SyntaxKind.SELECT);

		var numSelectExpected = 0;
		while (!isAtEnd() && !peekKind(SyntaxKind.END_SELECT))
		{
			if (numSelectExpected > 0 && consumeOptionally(select, SyntaxKind.SELECT))
			{
				numSelectExpected--;
				continue;
			}

			if (peekAny(List.of(SyntaxKind.UNION, SyntaxKind.EXCEPT, SyntaxKind.INTERSECT)))
			{
				numSelectExpected++;
			}

			// Sub-select
			var prev = previousToken();
			if (peekKind(SyntaxKind.SELECT) && prev != null && prev.kind() == SyntaxKind.LPAREN)
			{
				consume(select);
				continue;
			}

			// FETCH FIRST clause
			if (peekKind(SyntaxKind.FETCH) && peekKind(1, SyntaxKind.FIRST))
			{
				consume(select);
				continue;
			}

			if (isStatementStart())
			{
				break;
			}

			consume(select);
		}

		if (peekKind(SyntaxKind.END_SELECT))
		{
			consumeMandatoryClosing(select, SyntaxKind.END_SELECT, opening);
			return select;
		}
		else
		{
			select.setBody(statementList(SyntaxKind.END_SELECT));
			consumeMandatoryClosing(select, SyntaxKind.END_SELECT, opening);
		}

		return select;
	}

	private StatementNode insert() throws ParseError
	{
		// Right now, just consume the INSERT entirely
		var insert = new InsertStatementNode();
		consumeMandatory(insert, SyntaxKind.INSERT);

		// The first VALUES is part of the INSERT, if 2nd is reached it's another statement.
		// This can occur: INSERT INTO DB2-TABEL (COL1) VALUES 'XYZ' VALUES 'xx' (as part of DECIDE statement)
		var numValues = 0;
		while (!isAtEnd())
		{
			if (consumeAnyOptionally(insert, List.of(SyntaxKind.VALUES, SyntaxKind.SELECT)))
			{
				numValues++;
			}

			if (numValues > 1 || isStatementStart() || isStatementEndOrBranch())
			{
				break;
			}
			consume(insert);
		}

		return insert;
	}

	private StatementNode update() throws ParseError
	{
		// Right now, just consume the UPDATE entirely
		var update = new UpdateStatementNode();
		consumeMandatory(update, SyntaxKind.UPDATE);

		var adabasUpdate = consumeOptionally(update, SyntaxKind.RECORD);
		adabasUpdate = consumeOptionally(update, SyntaxKind.IN) || adabasUpdate;
		adabasUpdate = consumeOptionally(update, SyntaxKind.STATEMENT) || adabasUpdate;
		if (consumeOptionally(update, SyntaxKind.LPAREN))
		{
			adabasUpdate = true;
			if (!consumeOptionally(update, SyntaxKind.LABEL_IDENTIFIER))
			{
				consumeOperandNode(update); // numbered label
			}
			consumeMandatory(update, SyntaxKind.RPAREN);
		}

		if (adabasUpdate || isAtEnd() || isStatementStart() || isStatementEndOrBranch())
		{
			return update;
		}

		// SQL Update begins here
		var numSet = 0;
		while (!isAtEnd())
		{
			// The first SET encountered is part of the UPDATE, if 2nd is reached it's another statement.
			// This can occur: UPDATE DB2-TABEL SET COL1 = 'XYZ' SET CONTROL etc
			if (consumeOptionally(update, SyntaxKind.SET))
			{
				numSet++;
			}

			if (numSet > 1 || (isStatementStart() || isStatementEndOrBranch()))
			{
				break;
			}
			consume(update);
		}

		return update;
	}

	private StatementNode delete() throws ParseError
	{
		// Right now, just consume the DELETE entirely
		var delete = new DeleteStatementNode();
		consumeMandatory(delete, SyntaxKind.DELETE);

		var adabasDelete = consumeOptionally(delete, SyntaxKind.RECORD);
		adabasDelete = consumeOptionally(delete, SyntaxKind.IN) || adabasDelete;
		adabasDelete = consumeOptionally(delete, SyntaxKind.STATEMENT) || adabasDelete;
		if (consumeOptionally(delete, SyntaxKind.LPAREN))
		{
			adabasDelete = true;
			if (!consumeOptionally(delete, SyntaxKind.LABEL_IDENTIFIER))
			{
				consumeOperandNode(delete); // numbered label
			}
			consumeMandatory(delete, SyntaxKind.RPAREN);
		}

		if (adabasDelete || isAtEnd() || isStatementStart() || isStatementEndOrBranch())
		{
			return delete;
		}

		// SQL Delete begins here
		consumeMandatory(delete, SyntaxKind.FROM);
		while (!isAtEnd() && !(isStatementStart() || isStatementEndOrBranch()))
		{
			consume(delete);
		}
		return delete;
	}

	private StatementNode processSql() throws ParseError
	{
		// Right now, just consume the PROCESS SQL entirely (only)
		var processSql = new ProcessSqlNode();
		consumeMandatory(processSql, SyntaxKind.PROCESS);
		consumeMandatory(processSql, SyntaxKind.SQL);
		consumeMandatoryIdentifier(processSql); // ddm name

		consumeMandatory(processSql, SyntaxKind.LESSER_SIGN);
		consumeMandatory(processSql, SyntaxKind.LESSER_SIGN);
		while (!isAtEnd() && !peekKind(SyntaxKind.GREATER_SIGN))
		{
			consume(processSql);
		}
		consumeMandatory(processSql, SyntaxKind.GREATER_SIGN);
		consumeMandatory(processSql, SyntaxKind.GREATER_SIGN);

		return processSql;
	}

	private StatementNode beforeBreak() throws ParseError
	{
		var beforeBreak = new BeforeBreakNode();
		var start = consumeMandatory(beforeBreak, SyntaxKind.BEFORE);
		consumeOptionally(beforeBreak, SyntaxKind.BREAK);
		consumeOptionally(beforeBreak, SyntaxKind.PROCESSING);

		beforeBreak.setBody(statementList(SyntaxKind.END_BEFORE));

		consumeMandatoryClosing(beforeBreak, SyntaxKind.END_BEFORE, start);
		return beforeBreak;
	}

	private StatementNode stack() throws ParseError
	{
		var stack = new StackNode();
		consumeMandatory(stack, SyntaxKind.STACK);
		consumeOptionally(stack, SyntaxKind.TOP);
		if (consumeOptionally(stack, SyntaxKind.COMMAND))
		{
			consumeOperandNode(stack);
			while (isOperand())
			{
				consumeOperandNode(stack);
			}

		}
		else
			if (consumeOptionally(stack, SyntaxKind.DATA) || consumeOptionally(stack, SyntaxKind.FORMATTED) || isOperand())
			{
				if (previousToken().kind() == SyntaxKind.DATA)
				{
					consumeOptionally(stack, SyntaxKind.FORMATTED);
				}

				consumeOperandNode(stack);
				while (isOperand())
				{
					consumeOperandNode(stack);
				}
			}

		return stack;
	}

	private StatementNode escape() throws ParseError
	{
		var escape = new EscapeNode();
		consumeMandatory(escape, SyntaxKind.ESCAPE);
		consumeAnyMandatory(escape, List.of(SyntaxKind.TOP, SyntaxKind.BOTTOM, SyntaxKind.ROUTINE, SyntaxKind.MODULE));
		var direction = previousToken().kind();
		escape.setDirection(direction);
		if (direction == SyntaxKind.TOP)
		{
			if (consumeOptionally(escape, SyntaxKind.REPOSITION))
			{
				escape.setReposition();
			}
		}
		else
		{
			if (direction == SyntaxKind.BOTTOM && consumeOptionally(escape, SyntaxKind.LPAREN))
			{
				var label = consumeMandatory(escape, SyntaxKind.LABEL_IDENTIFIER);
				escape.setLabel(label);
				consumeMandatory(escape, SyntaxKind.RPAREN);
			}

			if (consumeOptionally(escape, SyntaxKind.IMMEDIATE))
			{
				escape.setImmediate();
			}
		}

		return escape;
	}

	private StatementNode eject() throws ParseError
	{
		var eject = new EjectNode();
		consumeMandatory(eject, SyntaxKind.EJECT);

		if (consumeAnyOptionally(eject, List.of(SyntaxKind.ON, SyntaxKind.OFF)))
		{
			consumeOptionalReportSpecification(eject);
		}
		else
		{
			consumeOptionalReportSpecification(eject);
			consumeAnyOptionally(eject, List.of(SyntaxKind.IF, SyntaxKind.WHEN));
			if (consumeOptionally(eject, SyntaxKind.LESS))
			{
				consumeOptionally(eject, SyntaxKind.THAN);
				consumeOperandNode(eject);
				consumeOptionally(eject, SyntaxKind.LINES);
				consumeOptionally(eject, SyntaxKind.LEFT);
			}
		}

		return eject;
	}

	private <T extends BaseSyntaxNode & ICanSetReportSpecification> void consumeOptionalReportSpecification(T node) throws ParseError
	{
		if (consumeOptionally(node, SyntaxKind.LPAREN))
		{
			consumeAnyMandatory(node, List.of(SyntaxKind.IDENTIFIER, SyntaxKind.NUMBER_LITERAL));
			node.setReportSpecification(previousToken());
			consumeMandatory(node, SyntaxKind.RPAREN);
		}
	}

	private StatementNode performBreak() throws ParseError
	{
		var performBreak = new PerformBreakNode();
		consumeMandatory(performBreak, SyntaxKind.PERFORM);
		consumeMandatory(performBreak, SyntaxKind.BREAK);
		consumeOptionally(performBreak, SyntaxKind.PROCESSING);
		if (consumeOptionally(performBreak, SyntaxKind.LPAREN))
		{
			var identifier = consumeMandatory(performBreak, SyntaxKind.LABEL_IDENTIFIER);
			performBreak.setStatementIdentifier(identifier);
			consumeMandatory(performBreak, SyntaxKind.RPAREN);
		}

		performBreak.setBreakOf(breakOf());

		return performBreak;
	}

	private BreakOfNode breakOf() throws ParseError
	{
		var breakOf = new BreakOfNode();
		consumeOptionally(breakOf, SyntaxKind.AT);
		var openingToken = consumeMandatory(breakOf, SyntaxKind.BREAK);
		if (consumeOptionally(breakOf, SyntaxKind.LPAREN))
		{
			var identifier = consumeMandatory(breakOf, SyntaxKind.LABEL_IDENTIFIER);
			breakOf.setReportSpecification(identifier);
			consumeMandatory(breakOf, SyntaxKind.RPAREN);
		}

		consumeOptionally(breakOf, SyntaxKind.OF);
		breakOf.setOperand(consumeVariableReferenceNode(breakOf));

		if (consumeOptionally(breakOf, SyntaxKind.SLASH))
		{
			consumeLiteralNode(breakOf, SyntaxKind.NUMBER_LITERAL);
			consumeMandatory(breakOf, SyntaxKind.SLASH);
		}

		breakOf.setBody(statementList(SyntaxKind.END_BREAK));
		consumeMandatoryClosing(breakOf, SyntaxKind.END_BREAK, openingToken);

		return breakOf;
	}

	/**
	 * Parse any node in the form of:<br/>
	 * [AT] {@code location} [OF] {@code statementType} [(reportSpecification)]<br/>
	 * StatementBody<br/>
	 * {@code statementEndTokenType}
	 *
	 * @param location the "location", e.g. START, TOP, END
	 * @param statementType the type, e.g. PAGE, DATA
	 * @param statementEndToken the token which ends the body
	 * @param node the resulting node
	 */
	private <T extends StatementWithBodyNode & ICanSetReportSpecification> StatementNode parseAtPositionOf(
		SyntaxKind location,
		SyntaxKind statementType,
		SyntaxKind statementEndToken,
		boolean canHaveLabelIdentifier,
		T node
	) throws ParseError
	{
		consumeOptionally(node, SyntaxKind.AT);
		var openingToken = consumeMandatory(node, location);
		consumeOptionally(node, SyntaxKind.OF);
		consumeMandatory(node, statementType);

		if (consumeOptionally(node, SyntaxKind.LPAREN))
		{
			if (canHaveLabelIdentifier)
			{
				consumeMandatory(node, SyntaxKind.LABEL_IDENTIFIER);
			}
			else
			{
				consumeAnyMandatory(node, List.of(SyntaxKind.IDENTIFIER, SyntaxKind.NUMBER_LITERAL));
			}
			node.setReportSpecification(previousToken());
			consumeMandatory(node, SyntaxKind.RPAREN);
		}

		node.setBody(statementList(statementEndToken));
		consumeMandatoryClosing(node, statementEndToken, openingToken);
		return node;
	}

	private StatementNode newPage() throws ParseError
	{
		var newPage = new NewPageNode();
		consumeMandatory(newPage, SyntaxKind.NEWPAGE);
		if (consumeOptionally(newPage, SyntaxKind.LPAREN))
		{
			consumeAnyMandatory(newPage, List.of(SyntaxKind.IDENTIFIER, SyntaxKind.NUMBER_LITERAL));
			newPage.setReportSpecification(previousToken());
			consumeMandatory(newPage, SyntaxKind.RPAREN);
		}

		if (consumeOptionally(newPage, SyntaxKind.EVEN))
		{
			consumeOptionally(newPage, SyntaxKind.IF);
			consumeMandatory(newPage, SyntaxKind.TOP);
			consumeOptionally(newPage, SyntaxKind.OF);
			consumeOptionally(newPage, SyntaxKind.PAGE);
		}
		else
			if (peekAny(List.of(SyntaxKind.IF, SyntaxKind.WHEN)) && peekKind(1, SyntaxKind.LESS) // lookahead because it might also be just an if statement following, not belonging to NEWPAGE
				|| peekKind(SyntaxKind.LESS))
			{
				consumeAnyOptionally(newPage, List.of(SyntaxKind.IF, SyntaxKind.WHEN));
				consumeMandatory(newPage, SyntaxKind.LESS);

				consumeOptionally(newPage, SyntaxKind.THAN);
				consumeOperandNode(newPage);
				consumeOptionally(newPage, SyntaxKind.LINES);
				consumeOptionally(newPage, SyntaxKind.LEFT);
			}

		if (consumeAnyOptionally(newPage, List.of(SyntaxKind.WITH, SyntaxKind.TITLE)))
		{
			if (previousToken().kind() != SyntaxKind.TITLE)
			{
				consumeMandatory(newPage, SyntaxKind.TITLE);
			}
			if (consumeOptionally(newPage, SyntaxKind.LEFT))
			{
				consumeOptionally(newPage, SyntaxKind.JUSTIFIED);
			}
			consumeOptionally(newPage, SyntaxKind.UNDERLINED);
			consumeOperandNode(newPage);
		}

		return newPage;
	}

	private static final List<SyntaxKind> EXAMINE_REPLACE_DELETE_CLAUSES = List.of(SyntaxKind.AND, SyntaxKind.REPLACE, SyntaxKind.DELETE);
	private static final List<SyntaxKind> EXAMINE_GIVING_CLAUSES = List.of(SyntaxKind.GIVING, SyntaxKind.IN, SyntaxKind.KW_NUMBER, SyntaxKind.POSITION, SyntaxKind.LENGTH, SyntaxKind.INDEX);

	private StatementNode examine() throws ParseError
	{
		var examine = new ExamineNode();
		consumeMandatory(examine, SyntaxKind.EXAMINE);
		if (consumeOptionally(examine, SyntaxKind.DIRECTION) && !consumeAnyOptionally(examine, List.of(SyntaxKind.FORWARD, SyntaxKind.BACKWARD)))
		{
			// Direction can be specified as an operand (TODO: Type-check A1 or string literal of length 1)
			consumeOperandNode(examine);
		}

		if (consumeOptionally(examine, SyntaxKind.FULL))
		{
			if (consumeOptionally(examine, SyntaxKind.VALUE))
			{
				consumeOptionally(examine, SyntaxKind.OF);
			}
		}

		var examined = consumeSubstringOrOperand(examine);
		examine.setExamined(examined);

		if (consumeOptionally(examine, SyntaxKind.AND) || peekKind(SyntaxKind.TRANSLATE))
		{
			return examineTranslate(examine);
		}

		// [STARTING] FROM
		var hasPositionClause = consumeOptionally(examine, SyntaxKind.STARTING);
		hasPositionClause = consumeOptionally(examine, SyntaxKind.FROM) || hasPositionClause;
		if (hasPositionClause)
		{
			consumeOptionally(examine, SyntaxKind.POSITION);
			consumeOperandNode(examine);
			if (consumeAnyOptionally(examine, List.of(SyntaxKind.ENDING, SyntaxKind.THRU)))
			{
				consumeOptionally(examine, SyntaxKind.AT);
				consumeOptionally(examine, SyntaxKind.POSITION);
				consumeOperandNode(examine);
			}
		}

		consumeOptionally(examine, SyntaxKind.FOR);
		if (consumeOptionally(examine, SyntaxKind.FULL))
		{
			consumeOptionally(examine, SyntaxKind.VALUE);
			consumeOptionally(examine, SyntaxKind.OF);
		}
		consumeOptionally(examine, SyntaxKind.PATTERN);

		consumeOperandNode(examine);

		var hadAbsolute = consumeOptionally(examine, SyntaxKind.ABSOLUTE);
		if (!hadAbsolute && consumeOptionally(examine, SyntaxKind.WITH))
		{
			consumeAnyOptionally(examine, List.of(SyntaxKind.DELIMITERS, SyntaxKind.DELIMITER));
			if (peek().kind().isLiteralOrConst() || peek().kind().isIdentifier()
				|| (peek().kind().canBeIdentifier() && !peekAny(EXAMINE_REPLACE_DELETE_CLAUSES) && !peekAny(EXAMINE_GIVING_CLAUSES)))
			{
				// specifying a delimiter is optional
				consumeOperandNode(examine);
			}
		}

		consumeOptionally(examine, SyntaxKind.AND);
		if (consumeOptionally(examine, SyntaxKind.REPLACE))
		{
			consumeOptionally(examine, SyntaxKind.FIRST);
			consumeOptionally(examine, SyntaxKind.WITH);
			consumeOptionally(examine, SyntaxKind.FULL);
			consumeOptionally(examine, SyntaxKind.VALUE);
			consumeOptionally(examine, SyntaxKind.OF);
			consumeOperandNode(examine);
		}
		else
			if (consumeOptionally(examine, SyntaxKind.DELETE))
			{
				consumeOptionally(examine, SyntaxKind.FIRST);
			}

		while (consumeOptionally(examine, SyntaxKind.GIVING) || peekAny(EXAMINE_GIVING_CLAUSES))
		{
			if (consumeOptionally(examine, SyntaxKind.IN))
			{
				examine.setGivingNumber(consumeOperandNode(examine));
			}
			else
				if (consumeOptionally(examine, SyntaxKind.KW_NUMBER))
				{
					consumeOptionally(examine, SyntaxKind.IN);
					examine.setGivingNumber(consumeOperandNode(examine));
				}
				else
					if (consumeOptionally(examine, SyntaxKind.POSITION))
					{
						consumeOptionally(examine, SyntaxKind.IN);
						examine.setGivingPosition(consumeOperandNode(examine));
					}
					else
						if (consumeOptionally(examine, SyntaxKind.LENGTH))
						{
							consumeOptionally(examine, SyntaxKind.IN);
							examine.setGivingLength(consumeOperandNode(examine));
						}
						else
							if (consumeOptionally(examine, SyntaxKind.INDEX))
							{
								consumeOptionally(examine, SyntaxKind.IN);
								while (isOperand())
								{
									examine.addGivingIndex(consumeOperandNode(examine));
								}
							}
							else
							{
								examine.setGivingNumber(consumeOperandNode(examine));
							}
		}

		return examine;
	}

	private static final List<SyntaxKind> SEPARATE_KEYWORDS = List.of(SyntaxKind.WITH, SyntaxKind.IGNORE, SyntaxKind.REMAINDER, SyntaxKind.GIVING, SyntaxKind.KW_NUMBER);

	private StatementNode separate() throws ParseError
	{
		var separate = new SeparateStatementNode();
		consumeMandatory(separate, SyntaxKind.SEPARATE);

		var separated = consumeSubstringOrOperand(separate);
		separate.setSeparated(separated);

		var hasPositionClause = consumeOptionally(separate, SyntaxKind.STARTING);
		hasPositionClause = consumeOptionally(separate, SyntaxKind.FROM) || hasPositionClause;
		if (hasPositionClause)
		{
			consumeOptionally(separate, SyntaxKind.POSITION);
			consumeOperandNode(separate);
		}

		if (consumeOptionally(separate, SyntaxKind.LEFT))
		{
			consumeOptionally(separate, SyntaxKind.JUSTIFIED);
		}

		consumeMandatory(separate, SyntaxKind.INTO);
		while (isOperand() && !(peekAny(SEPARATE_KEYWORDS) || isStatementStart() || isStatementEndOrBranch()))
		{
			separate.addTarget(consumeOperandNode(separate));
		}

		if (consumeEitherOptionally(separate, SyntaxKind.IGNORE, SyntaxKind.REMAINDER) && previousToken().kind() == SyntaxKind.REMAINDER)
		{
			consumeOptionally(separate, SyntaxKind.POSITION);
			var operand = consumeOperandNode(separate);
			checkOperand(operand, "REMAINDER [POSITION] must be followed by a variable reference", AllowedOperand.VARIABLE_REFERENCE);
		}

		if (consumeOptionally(separate, SyntaxKind.WITH))
		{
			var operandRequired = true;
			consumeOptionally(separate, SyntaxKind.RETAINED);

			if (peekAny(List.of(SyntaxKind.ANY, SyntaxKind.INPUT)))
			{
				// DELIMITER is allowed even though it's not documented
				consumeAnyMandatory(separate, List.of(SyntaxKind.ANY, SyntaxKind.INPUT));
				consumeAnyMandatory(separate, List.of(SyntaxKind.DELIMITER, SyntaxKind.DELIMITERS));
				operandRequired = false;
			}
			else
			{
				consumeAnyMandatory(separate, List.of(SyntaxKind.DELIMITER, SyntaxKind.DELIMITERS));
				// if next is GIVEN or NUMBER (continuation of SEPARATE), then operand is not specified.
				// Same if next is a new statement or end of file.
				if (peekAny(List.of(SyntaxKind.GIVING, SyntaxKind.KW_NUMBER)) || isAtEnd() || isStatementStart() || isStatementEndOrBranch())
				{
					operandRequired = false;
				}
			}

			if (operandRequired)
			{
				var operand = consumeOperandNode(separate);
				checkOperand(operand, "DELIMITER(S) must be followed by a constant string or a variable reference", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
			}
		}

		if (consumeAnyOptionally(separate, List.of(SyntaxKind.GIVING, SyntaxKind.KW_NUMBER)))
		{
			if (previousToken().kind() == SyntaxKind.GIVING)
			{
				consumeMandatory(separate, SyntaxKind.KW_NUMBER);
			}
			consumeOptionally(separate, SyntaxKind.IN);
			consumeOperandNode(separate);
		}

		return separate;
	}

	private static final List<SyntaxKind> SUBSTRINGS = List.of(SyntaxKind.SUBSTR, SyntaxKind.SUBSTRING);

	private IOperandNode consumeSubstringOrOperand(BaseSyntaxNode node) throws ParseError
	{
		if (peekAny(SUBSTRINGS))
		{
			return consumeSubstring(node);
		}
		else
		{
			return consumeArithmeticExpression(node);
		}
	}

	private IOperandNode consumeSubstring(BaseSyntaxNode node) throws ParseError
	{
		var substring = new SubstringOperandNode();
		node.addNode(substring);
		consumeAnyMandatory(node, SUBSTRINGS);

		consumeMandatory(node, SyntaxKind.LPAREN);
		substring.setOperand(consumeOperandNode(substring));
		consumeMandatory(node, SyntaxKind.COMMA);
		if (peekKind(SyntaxKind.NUMBER_LITERAL) && peek().source().contains(","))
		{
			// HACK: this should be handled somewhere nicely
			var wrongToken = peek();
			discard();
			var split = wrongToken.source().split(",");
			var firstNumber = new SyntaxToken(SyntaxKind.NUMBER_LITERAL, wrongToken.offset(), wrongToken.offsetInLine(), wrongToken.line(), split[0], wrongToken.filePath());
			var comma = new SyntaxToken(SyntaxKind.COMMA, wrongToken.offset() + split[0].length(), wrongToken.offsetInLine() + split[0].length(), wrongToken.line(), ",", wrongToken.filePath());
			var secondNumber = new SyntaxToken(SyntaxKind.NUMBER_LITERAL, comma.offset() + comma.length(), comma.offsetInLine() + comma.length(), wrongToken.line(), split[1], wrongToken.filePath());

			var startingPosition = new LiteralNode(firstNumber);
			substring.addNode(startingPosition);
			substring.setStartingPosition(startingPosition);

			substring.addNode(new TokenNode(comma));

			var length = new LiteralNode(secondNumber);
			substring.addNode(length);
			substring.setLength(length);
		}
		else
		{
			if (!peekKind(SyntaxKind.COMMA))
			{
				substring.setStartingPosition(consumeOperandNode(substring));
			}
			if (consumeOptionally(node, SyntaxKind.COMMA))
			{
				substring.setLength(consumeOperandNode(substring));
			}
		}
		consumeMandatory(node, SyntaxKind.RPAREN);

		return substring;
	}

	private StatementNode examineTranslate(ExamineNode examine) throws ParseError
	{
		consumeMandatory(examine, SyntaxKind.TRANSLATE);
		if (consumeOptionally(examine, SyntaxKind.INTO))
		{
			consumeAnyMandatory(examine, List.of(SyntaxKind.UPPER, SyntaxKind.LOWER));
			consumeOptionally(examine, SyntaxKind.CASE);
		}
		else
		{
			consumeMandatory(examine, SyntaxKind.USING);
			consumeOptionally(examine, SyntaxKind.INVERTED);
			consumeOperandNode(examine);
		}

		return examine;
	}

	private StatementNode display() throws ParseError
	{
		var display = new DisplayNode();
		consumeMandatory(display, SyntaxKind.DISPLAY);

		if (consumeOptionally(display, SyntaxKind.LPAREN))
		{
			if (peek().kind().canBeIdentifier() && peekKind(1, SyntaxKind.RPAREN))
			{
				var token = consumeMandatoryIdentifier(display);
				display.setReportSpecification(token);
			}
			else
			{
				// currently consume everything until closing parenthesis to consume things like attribute definition etc.
				while (!peekKind(SyntaxKind.RPAREN))
				{
					consume(display);
				}
			}
			consumeMandatory(display, SyntaxKind.RPAREN);
		}

		// TODO: Parse options

		return display;
	}

	private StatementNode inputStatement() throws ParseError
	{
		var input = new InputStatementNode();
		consumeMandatory(input, SyntaxKind.INPUT);

		if (consumeOptionally(input, SyntaxKind.WINDOW))
		{
			consumeMandatory(input, SyntaxKind.EQUALS_SIGN);
			consumeLiteralNode(input, SyntaxKind.STRING_LITERAL);
		}

		if (consumeOptionally(input, SyntaxKind.NO))
		{
			consumeMandatory(input, SyntaxKind.ERASE);
		}

		if (consumeOptionally(input, SyntaxKind.LPAREN))
		{
			// statement attributes?
			while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
			{
				consume(input);
			}
			consumeMandatory(input, SyntaxKind.RPAREN);
		}

		if (peekKind(SyntaxKind.WITH) || peekKind(SyntaxKind.TEXT))
		{
			consumeOptionally(input, SyntaxKind.WITH);
			consumeMandatory(input, SyntaxKind.TEXT);

			consumeOptionally(input, SyntaxKind.ASTERISK);
			consumeOperandNode(input);

			if (consumeOptionally(input, SyntaxKind.LPAREN))
			{
				// statement attributes?
				while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
				{
					consume(input);
				}
				consumeMandatory(input, SyntaxKind.RPAREN);
			}

			while (consumeOptionally(input, SyntaxKind.COMMA))
			{
				consumeOperandNode(input);
			}
		}

		if (consumeOptionally(input, SyntaxKind.MARK))
		{
			if (consumeOptionally(input, SyntaxKind.POSITION))
			{
				consumeOperandNode(input);
				consumeOptionally(input, SyntaxKind.IN);
			}

			consumeOptionally(input, SyntaxKind.FIELD);
			consumeOptionally(input, SyntaxKind.ASTERISK);
			consumeOperandNode(input);
		}

		if (peekKind(SyntaxKind.AND) || peekKind(SyntaxKind.SOUND) || peekKind(SyntaxKind.ALARM))
		{
			consumeOptionally(input, SyntaxKind.AND);
			consumeOptionally(input, SyntaxKind.SOUND);
			consumeMandatory(input, SyntaxKind.ALARM);
		}

		if (peekKind(SyntaxKind.USING) || peekKind(SyntaxKind.MAP))
		{
			consumeOptionally(input, SyntaxKind.USING);
			consumeMandatory(input, SyntaxKind.MAP);
			consumeLiteralNode(input, SyntaxKind.STRING_LITERAL);
			// TODO: Side load map. Create new type for Input that uses map, so that it can be IModuleReferencingNode ?
			if (peekKind(SyntaxKind.NO) && peekKind(1, SyntaxKind.ERASE))
			{
				consumeMandatory(input, SyntaxKind.NO);
				consumeMandatory(input, SyntaxKind.ERASE);
			}

			if (consumeOptionally(input, SyntaxKind.NO))
			{
				consumeMandatory(input, SyntaxKind.PARAMETER);
				return input;
			}
		}

		while (!isAtEnd() && !isStatementStart())
		{
			if (peekKind(SyntaxKind.LPAREN) && getKind(1).isAttribute())
			{
				consumeAttributeDefinition(input);
			}
			else
			{
				// coordinates in form of x/y
				if (peekKind(SyntaxKind.NUMBER_LITERAL) && peekKind(1, SyntaxKind.SLASH))
				{
					consumeLiteralNode(input, SyntaxKind.NUMBER_LITERAL);
					consumeMandatory(input, SyntaxKind.SLASH);
					consumeLiteralNode(input, SyntaxKind.NUMBER_LITERAL);
					continue;
				}

				if ((consumeOptionally(input, SyntaxKind.NO) && consumeOptionally(input, SyntaxKind.PARAMETER))
					|| !isOperand() && !peekKind(SyntaxKind.TAB_SETTING) && !peekKind(SyntaxKind.SLASH) && !peekKind(SyntaxKind.OPERAND_SKIP))
				{
					break;
				}

				input.addOperand(consumeWriteOperand(input));
			}
		}

		return input;
	}

	private static final Set<SyntaxKind> OPTIONAL_WRITE_FLAGS = Set.of(SyntaxKind.NOTITLE, SyntaxKind.NOTIT, SyntaxKind.NOHDR, SyntaxKind.USING, SyntaxKind.MAP, SyntaxKind.FORM, SyntaxKind.TITLE, SyntaxKind.TRAILER, SyntaxKind.LEFT, SyntaxKind.JUSTIFIED, SyntaxKind.UNDERLINED);

	private StatementNode write(SyntaxKind statementKind) throws ParseError
	{
		var write = new WriteNode();
		consumeMandatory(write, statementKind);
		if (consumeOptionally(write, SyntaxKind.LPAREN))
		{
			if (peek().kind().canBeIdentifier() && peekKind(1, SyntaxKind.RPAREN))
			{
				var token = consumeMandatoryIdentifier(write);
				write.setReportSpecification(token);
			}
			else
			{
				// currently consume everything until closing parenthesis to consume things like attribute definition etc.
				while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
				{
					consume(write);
				}
			}
			consumeMandatory(write, SyntaxKind.RPAREN);
		}

		while (consumeAnyOptionally(write, OPTIONAL_WRITE_FLAGS))
		{
			// advances automatically
		}
		while (!isAtEnd() && !isStatementStart())
		{
			if (peekKind(SyntaxKind.LPAREN) && getKind(1).isAttribute())
			{
				consumeAttributeDefinition(write);
			}
			else
			{
				if ((consumeOptionally(write, SyntaxKind.NO) && consumeOptionally(write, SyntaxKind.PARAMETER))
					|| !isOperand() && !peekKind(SyntaxKind.TAB_SETTING) && !peekKind(SyntaxKind.SLASH) && !peekKind(SyntaxKind.OPERAND_SKIP))
				{
					break;
				}
				consumeWriteOperand(write);
			}
		}

		// TODO: Actual operands to WRITE not added as operands
		return write;
	}

	private IOperandNode consumeWriteOperand(BaseSyntaxNode writeLikeNode) throws ParseError
	{
		if (consumeOptionally(writeLikeNode, SyntaxKind.TAB_SETTING)
			|| consumeOptionally(writeLikeNode, SyntaxKind.SLASH)
			|| consumeOptionally(writeLikeNode, SyntaxKind.OPERAND_SKIP))
		{
			return null;
		}

		if (peekKind().isLiteralOrConst())
		{
			var literal = consumeLiteralNode(writeLikeNode, SyntaxKind.STRING_LITERAL);
			if (peekKind(SyntaxKind.LPAREN))
			{
				// currently consume everything until closing parenthesis to consume attribute definition shorthands
				while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
				{
					consume(writeLikeNode);
				}
				consumeMandatory(writeLikeNode, SyntaxKind.RPAREN);
			}
			return literal;
		}
		else
		{
			return consumeOperandNode(writeLikeNode);
		}
	}

	private static final Set<SyntaxKind> FORMAT_MODIFIERS = Set.of(
		SyntaxKind.AD, SyntaxKind.AL, SyntaxKind.CD, SyntaxKind.DF, SyntaxKind.DL, SyntaxKind.EM, SyntaxKind.ES, SyntaxKind.FC, SyntaxKind.FL, SyntaxKind.GC, SyntaxKind.HC, SyntaxKind.HW, SyntaxKind.IC, SyntaxKind.IP, SyntaxKind.IS, SyntaxKind.KD, SyntaxKind.LC, SyntaxKind.LS, SyntaxKind.MC, SyntaxKind.MP, SyntaxKind.MS, SyntaxKind.NL,
		SyntaxKind.PC, SyntaxKind.PM, SyntaxKind.PS, SyntaxKind.SF, SyntaxKind.SG, SyntaxKind.TC, SyntaxKind.UC, SyntaxKind.ZP
	);

	private StatementNode formatNode() throws ParseError
	{
		var format = new FormatNode();
		consumeMandatory(format, SyntaxKind.FORMAT);
		if (consumeOptionally(format, SyntaxKind.LPAREN))
		{
			consumeAnyMandatory(format, List.of(SyntaxKind.IDENTIFIER, SyntaxKind.NUMBER_LITERAL));
			consumeMandatory(format, SyntaxKind.RPAREN);
		}

		while (consumeAnyOptionally(format, FORMAT_MODIFIERS))
		{
			consumeMandatory(format, SyntaxKind.EQUALS_SIGN);
			if (!FORMAT_MODIFIERS.contains(peek().kind()) && peek().line() == previousToken().line())
			{
				consume(format);
			}
		}

		return format;
	}

	private StatementNode limit() throws ParseError
	{
		var limit = new LimitNode();
		consumeMandatory(limit, SyntaxKind.LIMIT);
		limit.setLimit(consumeNonConcatLiteralNode(limit, SyntaxKind.NUMBER_LITERAL));
		return limit;
	}

	private StatementNode defineWindow() throws ParseError
	{
		var window = new DefineWindowNode();
		consumeMandatory(window, SyntaxKind.DEFINE);
		consumeMandatory(window, SyntaxKind.WINDOW);
		var name = consumeMandatoryIdentifierTokenNode(window);
		window.setName(name.token());

		if (consumeOptionally(window, SyntaxKind.SIZE))
		{
			if (peekAny(List.of(SyntaxKind.AUTO, SyntaxKind.QUARTER)))
			{
				consumeAnyMandatory(window, List.of(SyntaxKind.AUTO, SyntaxKind.QUARTER));
			}
			else
			{
				consumeOperandNode(window);
				consumeMandatory(window, SyntaxKind.ASTERISK);
				consumeOperandNode(window);
			}
		}

		if (consumeOptionally(window, SyntaxKind.BASE))
		{
			if (consumeAnyOptionally(window, List.of(SyntaxKind.TOP, SyntaxKind.BOTTOM)))
			{
				consumeAnyMandatory(window, List.of(SyntaxKind.LEFT, SyntaxKind.RIGHT));
			}
			else
				if (peekKind(SyntaxKind.CURSOR))
				{
					consumeMandatory(window, SyntaxKind.CURSOR);
				}
				else
				{
					consumeOperandNode(window);
					consumeMandatory(window, SyntaxKind.SLASH);
					consumeOperandNode(window);
				}
		}

		if (consumeOptionally(window, SyntaxKind.REVERSED))
		{
			if (peekKind(SyntaxKind.LPAREN))
			{
				consumeSingleAttribute(window, SyntaxKind.CD);
			}
		}

		if (consumeOptionally(window, SyntaxKind.TITLE))
		{
			consumeOperandNode(window);
		}

		if (consumeOptionally(window, SyntaxKind.CONTROL))
		{
			consumeAnyMandatory(window, List.of(SyntaxKind.WINDOW, SyntaxKind.SCREEN));
		}

		if (consumeOptionally(window, SyntaxKind.FRAMED))
		{
			if (peekAny(List.of(SyntaxKind.ON, SyntaxKind.OFF)))
			{
				var token = consumeAnyMandatory(window, List.of(SyntaxKind.ON, SyntaxKind.OFF));
				if (token != null && token.kind() == SyntaxKind.ON)
				{
					if (peekKind(SyntaxKind.LPAREN))
					{
						consumeSingleAttribute(window, SyntaxKind.CD);
					}

					if (consumeOptionally(window, SyntaxKind.POSITION))
					{
						var pos = consumeAnyMandatory(window, List.of(SyntaxKind.SYMBOL, SyntaxKind.TEXT, SyntaxKind.OFF));
						if (pos.kind() == SyntaxKind.SYMBOL)
						{
							consumeAnyOptionally(window, List.of(SyntaxKind.TOP, SyntaxKind.BOTTOM));
							consumeOptionally(window, SyntaxKind.AUTO);
							consumeOptionally(window, SyntaxKind.SHORT);
							consumeAnyOptionally(window, List.of(SyntaxKind.LEFT, SyntaxKind.RIGHT));
						}
						else
							if (pos.kind() == SyntaxKind.TEXT && !consumeOptionally(window, SyntaxKind.OFF))
							{
								consumeOptionally(window, SyntaxKind.MORE);
								consumeAnyOptionally(window, List.of(SyntaxKind.LEFT, SyntaxKind.RIGHT));
							}
					}
				}
			}
		}

		return window;
	}

	private StatementNode closePrinter() throws ParseError
	{
		var closePrinter = new ClosePrinterNode();
		consumeMandatory(closePrinter, SyntaxKind.CLOSE);
		consumeMandatory(closePrinter, SyntaxKind.PRINTER);
		consumeMandatory(closePrinter, SyntaxKind.LPAREN);

		if (peekKind(SyntaxKind.NUMBER_LITERAL))
		{
			var literal = consumeNonConcatLiteralNode(closePrinter, SyntaxKind.NUMBER_LITERAL);
			closePrinter.setPrinter(literal.token());
		}
		else
		{
			if (peek().kind().canBeIdentifier())
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
		if (peek().kind().canBeIdentifier())
		{
			var name = consumeMandatoryIdentifier(printer);
			printer.setName(name);
			consumeMandatory(printer, SyntaxKind.EQUALS_SIGN);
		}
		var printerNumber = consumeNonConcatLiteralNode(printer, SyntaxKind.NUMBER_LITERAL);
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
				}
				else
				{
					report(ParserErrors.unexpectedToken(List.of(SyntaxKind.IDENTIFIER, SyntaxKind.STRING_LITERAL), tokens));
					tokens.advance();
				}
			}
		}

		while (peekAny(List.of(SyntaxKind.PROFILE, SyntaxKind.DISP, SyntaxKind.COPIES)))
		{
			if (consumeOptionally(printer, SyntaxKind.PROFILE))
			{
				var literal = consumeNonConcatLiteralNode(printer, SyntaxKind.STRING_LITERAL);
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

	private boolean checkLiteralType(ILiteralNode literal, SyntaxKind... allowedKinds)
	{
		for (var allowedKind : allowedKinds)
		{
			if (literal.token().kind() == allowedKind)
			{
				return true;
			}
		}

		report(ParserErrors.invalidLiteralType(literal, allowedKinds));
		return false;
	}

	private void checkLiteralTypeIfLiteral(IOperandNode operand, SyntaxKind... allowedKinds)
	{
		if (operand instanceof ILiteralNode literalNode)
		{
			checkLiteralType(literalNode, allowedKinds);
		}
	}

	private void checkNumericRange(ILiteralNode node, int lowestValue, int highestValue)
	{
		if (node.token().kind() != SyntaxKind.NUMBER_LITERAL)
		{
			return;
		}

		int actualValue = node.token().intValue();
		if (actualValue < lowestValue || actualValue > highestValue)
		{
			report(ParserErrors.invalidNumericRange(node, actualValue, lowestValue, highestValue));
		}
	}

	private void checkStringLength(SyntaxToken token, String stringValue, int maxLength)
	{
		if (stringValue.length() > maxLength)
		{
			report(ParserErrors.invalidLengthForLiteral(token, maxLength));
		}
	}

	private static final Set<SyntaxKind> REPEAT_TERMINATION = Set.of(SyntaxKind.UNTIL, SyntaxKind.WHILE, SyntaxKind.END_REPEAT);

	private StatementNode repeatLoop() throws ParseError
	{
		var loopNode = new RepeatLoopNode();
		var opening = consumeMandatory(loopNode, SyntaxKind.REPEAT);
		if (consumeEitherOptionally(loopNode, SyntaxKind.UNTIL, SyntaxKind.WHILE))
		{
			loopNode.setCondition(conditionNode());
			loopNode.setBody(statementList(SyntaxKind.END_REPEAT));
		}
		else
		{
			loopNode.setBody(statementList(REPEAT_TERMINATION));
			if (consumeEitherOptionally(loopNode, SyntaxKind.UNTIL, SyntaxKind.WHILE))
			{
				loopNode.setCondition(conditionNode());
			}
		}

		checkForEmptyBody(loopNode);
		consumeMandatoryClosing(loopNode, SyntaxKind.END_REPEAT, opening);

		return loopNode;
	}

	private StatementNode forLoop() throws ParseError
	{
		var loopNode = new ForLoopNode();

		var opening = consumeMandatory(loopNode, SyntaxKind.FOR);
		loopNode.setLoopControl(consumeVariableReferenceNode(loopNode));
		consumeAnyOptionally(loopNode, List.of(SyntaxKind.COLON_EQUALS_SIGN, SyntaxKind.EQUALS_SIGN, SyntaxKind.EQ, SyntaxKind.FROM));
		consumeArithmeticExpression(loopNode);
		consumeAnyOptionally(loopNode, List.of(SyntaxKind.TO, SyntaxKind.THRU)); // According to the documentation, either TO or THRU is mandatory. However, FOR #I 1 10 also just works :)
		var upperBound = consumeArithmeticExpression(loopNode);
		loopNode.setUpperBound(upperBound);
		if (consumeOptionally(loopNode, SyntaxKind.STEP))
		{
			consumeOperandNode(loopNode);
		}

		loopNode.setBody(statementList(SyntaxKind.END_FOR));
		checkForEmptyBody(loopNode);
		consumeMandatoryClosing(loopNode, SyntaxKind.END_FOR, opening);

		return loopNode;
	}

	private static final List<SyntaxKind> END_OF_SORT_KINDS = List.of(SyntaxKind.USING, SyntaxKind.GIVE, SyntaxKind.GIVING, SyntaxKind.END_SORT);
	private static final List<SyntaxKind> SORT_DIRECTIONS_KINDS = List.of(SyntaxKind.ASC, SyntaxKind.ASCENDING, SyntaxKind.DESC, SyntaxKind.DESCENDING);
	private static final List<SyntaxKind> ALLOWED_SYSTEMFUNCTIONS = List.of(SyntaxKind.MAX, SyntaxKind.MIN, SyntaxKind.NMIN, SyntaxKind.COUNT, SyntaxKind.NCOUNT, SyntaxKind.OLD, SyntaxKind.AVER, SyntaxKind.NAVER, SyntaxKind.SUM, SyntaxKind.TOTAL);

	private StatementNode sortStatement() throws ParseError
	{
		var sort = new SortStatementNode();
		consumeMandatory(sort, SyntaxKind.END_ALL);
		consumeOptionally(sort, SyntaxKind.AND);
		consumeOptionally(sort, SyntaxKind.LABEL_IDENTIFIER);
		var opening = consumeMandatory(sort, SyntaxKind.SORT);
		consumeAnyOptionally(sort, List.of(SyntaxKind.THEM, SyntaxKind.RECORDS, SyntaxKind.RECORD));
		consumeOptionally(sort, SyntaxKind.BY);

		while ((isOperand() || peekAny(SORT_DIRECTIONS_KINDS)) && !peekAny(END_OF_SORT_KINDS))
		{
			var operand = consumeOperandNode(sort);
			var sortDirection = SortDirection.fromSyntaxKind(SyntaxKind.ASCENDING);
			if (consumeAnyOptionally(sort, SORT_DIRECTIONS_KINDS))
			{
				sortDirection = SortDirection.fromSyntaxKind(previousToken().kind());
			}

			sort.addSortBy(new SortedOperand(operand, sortDirection));
		}

		if (consumeOptionally(sort, SyntaxKind.USING) && !consumeEitherOptionally(sort, SyntaxKind.KEYS, SyntaxKind.KEY))
		{
			while (isOperand() && !peekAny(END_OF_SORT_KINDS))
			{
				var node = consumeOperandNode(sort);
				sort.addUsing(node);
			}
		}

		if (consumeEitherOptionally(sort, SyntaxKind.GIVE, SyntaxKind.GIVING))
		{
			while (!peekKind(SyntaxKind.END_SORT))
			{
				consumeAnyMandatory(sort, ALLOWED_SYSTEMFUNCTIONS);
				while (consumeAnyOptionally(sort, ALLOWED_SYSTEMFUNCTIONS))
				{
					// It's fine, don't worry
				}
				consumeOptionally(sort, SyntaxKind.OF);
				var lparen = consumeOptionally(sort, SyntaxKind.LPAREN);
				consumeOperandNode(sort);
				if (lparen)
				{
					consumeMandatory(sort, SyntaxKind.RPAREN);
				}
				if (peekKind(SyntaxKind.LPAREN))
				{
					consumeAttributeDefinition(sort);
				}
			}
		}

		sort.setBody(statementList(SyntaxKind.END_SORT));
		consumeMandatoryClosing(sort, SyntaxKind.END_SORT, opening);

		return sort;
	}

	private StatementNode perform() throws ParseError
	{
		var internalPerform = new InternalPerformNode();

		consumeMandatory(internalPerform, SyntaxKind.PERFORM);
		var symbolName = consumeIdentifierTokenOnly();
		var referenceNode = new SymbolReferenceNode(symbolName);
		internalPerform.setReferenceNode(referenceNode);
		internalPerform.addNode(referenceNode);

		if (!isStatementStart() && isModuleParameter())
		{
			var externalPerform = new ExternalPerformNode(internalPerform);
			while (!isAtEnd() && !isStatementStart() && isModuleParameter())
			{
				var operand = consumeModuleParameter(externalPerform);
				externalPerform.addParameter(operand);
			}
			var foundModule = sideloadModule(externalPerform.referencingToken().trimmedSymbolName(32), externalPerform.referencingToken());
			if (foundModule != null)
			{
				externalPerform.setReference(foundModule);
			}

			return externalPerform;
		}

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
		checkForEmptyBody(subroutine);

		referencableNodes.add(subroutine);

		return subroutine;
	}

	private void checkForEmptyBody(IStatementWithBodyNode statement)
	{
		if (statement.body().statements().isEmpty())
		{
			report(ParserErrors.emptyBodyDisallowed(statement));
		}
	}

	private void checkForEmptyBody(IStatementListNode statementList, SyntaxToken reportingToken)
	{
		if (statementList.statements().isEmpty())
		{
			report(ParserErrors.emptyBodyDisallowed(reportingToken));
		}
	}

	private StatementNode end() throws ParseError
	{
		var endNode = new EndNode();
		consumeMandatory(endNode, SyntaxKind.END);
		return endNode;
	}

	private CallFileNode callFile() throws ParseError
	{
		var call = new CallFileNode();
		var opening = consumeMandatory(call, SyntaxKind.CALL);
		consumeMandatory(call, SyntaxKind.FILE);
		call.setCalling(consumeNonConcatLiteralNode(call, SyntaxKind.STRING_LITERAL));
		call.setControlField(consumeOperandNode(call));
		call.setRecordArea(consumeOperandNode(call));

		call.setBody(statementList(SyntaxKind.END_FILE));
		consumeMandatoryClosing(call, SyntaxKind.END_FILE, opening);

		return call;
	}

	private CallLoopNode callLoop() throws ParseError
	{
		var call = new CallLoopNode();
		var opening = consumeMandatory(call, SyntaxKind.CALL);
		consumeMandatory(call, SyntaxKind.LOOP);

		var name = consumeOperandNode(call);
		checkOperand(name, "The program to be called can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
		checkLiteralTypeIfLiteral(name, SyntaxKind.STRING_LITERAL);
		call.setCalling(name);

		while (isOperand() && !isStatementStart())
		{
			call.addOperand(consumeOperandNode(call));
		}

		call.setBody(statementList(SyntaxKind.END_LOOP));
		consumeMandatoryClosing(call, SyntaxKind.END_LOOP, opening);

		return call;
	}

	private CallNode callStatement() throws ParseError
	{
		var call = new CallNode();
		consumeMandatory(call, SyntaxKind.CALL);
		consumeOptionally(call, SyntaxKind.INTERFACE4);

		var name = consumeOperandNode(call);
		checkOperand(name, "The program to be called can only be a constant string or a variable reference.", AllowedOperand.LITERAL, AllowedOperand.VARIABLE_REFERENCE);
		checkLiteralTypeIfLiteral(name, SyntaxKind.STRING_LITERAL);
		call.setCalling(name);

		// If USING specified, there must be at least one operand following
		if (consumeOptionally(call, SyntaxKind.USING))
		{
			call.addOperand(consumeOperandNode(call));
		}

		while (isOperand() && !isStatementStart())
		{
			call.addOperand(consumeOperandNode(call));
		}

		return call;
	}

	private CallnatNode callnat() throws ParseError
	{
		var callnat = new CallnatNode();

		consumeMandatory(callnat, SyntaxKind.CALLNAT);

		if (!isStringLiteralOrIdentifier())
		{
			report(ParserErrors.unexpectedToken(List.of(SyntaxKind.STRING_LITERAL, SyntaxKind.IDENTIFIER), tokens));
		}

		if (consumeOptionally(callnat, SyntaxKind.IDENTIFIER))
		{
			callnat.setReferencingToken(previousToken());
		}
		else
			if (consumeOptionally(callnat, SyntaxKind.STRING_LITERAL))
			{
				callnat.setReferencingToken(previousToken());
				var referencedModule = sideloadModule(callnat.referencingToken().stringValue().toUpperCase().trim(), previousTokenNode().token());
				callnat.setReferencedModule((NaturalModule) referencedModule);
				if (referencedModule != null
					&& referencedModule.file() != null && referencedModule.file().getFiletype() != null
					&& referencedModule.file().getFiletype() != NaturalFileType.SUBPROGRAM)
				{
					report(
						ParserErrors.invalidModuleType(
							"Only SUBPROGRAMs can be called with CALLNAT",
							callnat.referencingToken()
						)
					);
				}
			}

		consumeOptionally(callnat, SyntaxKind.USING);

		while (!isAtEnd() && !isStatementStart() && isModuleParameter())
		{
			var operand = consumeModuleParameter(callnat);
			callnat.addParameter(operand);
			if (peekKind(SyntaxKind.LPAREN) && peekKind(1, SyntaxKind.AD))
			{
				consumeAttributeDefinition((BaseSyntaxNode) operand);
			}
		}

		return callnat;
	}

	private IncludeNode include() throws ParseError
	{
		var include = new IncludeNode();

		consumeMandatory(include, SyntaxKind.INCLUDE);

		var referencingToken = consumeMandatoryIdentifier(include);
		include.setReferencingToken(referencingToken);

		while (!isAtEnd() && peekKind(SyntaxKind.STRING_LITERAL))
		{
			var parameter = consumeLiteralNode(include, SyntaxKind.STRING_LITERAL);
			include.addParameter(parameter);
		}

		var referencedModule = sideloadModule(referencingToken.symbolName(), previousTokenNode().token());
		include.setReferencedModule((NaturalModule) referencedModule);

		if (referencedModule != null && currentModuleCallStack.add(referencingToken.symbolName()))
		{
			try
			{
				if (referencedModule.file().getFiletype() != NaturalFileType.COPYCODE)
				{
					report(ParserErrors.invalidModuleType("Only copycodes can be INCLUDEd", include.referencingToken()));
				}

				var includedSource = Files.readString(referencedModule.file().getPath());
				var normalizedParameter = new ArrayList<String>(include.providedParameter().size());
				for (var parameter : include.providedParameter())
				{
					var value = parameter instanceof LiteralNode literal
						? literal.token().stringValue()
						: ((StringConcatOperandNode) parameter).stringValue();
					normalizedParameter.add(value);
				}
				var lexer = new Lexer(normalizedParameter);
				lexer.relocateDiagnosticPosition(shouldRelocateDiagnostics() ? relocatedDiagnosticPosition : referencingToken);
				var tokens = lexer.lex(includedSource, referencedModule.file().getPath());

				for (var diagnostic : tokens.diagnostics())
				{
					report(diagnostic);
				}

				var nestedParser = new StatementListParser(moduleProvider);
				nestedParser.currentModuleCallStack.addAll(this.currentModuleCallStack);
				nestedParser.relocateDiagnosticPosition(
					shouldRelocateDiagnostics()
						? relocatedDiagnosticPosition
						: referencingToken
				);
				var statementList = nestedParser.parse(tokens);

				for (var diagnostic : statementList.diagnostics())
				{
					if (!ParserError.isUnresolvedError(diagnostic.id()))
					{
						// Unresolved references will be resolved by the module including the copycode.
						report(diagnostic);
					}
				}
				unresolvedReferences.addAll(nestedParser.unresolvedReferences);
				referencableNodes.addAll(nestedParser.referencableNodes);
				include.setBody(
					statementList.result(),
					shouldRelocateDiagnostics()
						? relocatedDiagnosticPosition
						: referencingToken
				);
				currentModuleCallStack.remove(referencingToken.symbolName());
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}
		else
		{
			if (currentModuleCallStack.contains(referencingToken.symbolName()))
			{
				report(ParserErrors.cyclomaticInclude(referencingToken));
			}

			var unresolvedBody = new StatementListNode();
			unresolvedBody.setParent(include);
			include.setBody(
				unresolvedBody,
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

		if (!isStringLiteralOrIdentifier())
		{
			report(ParserErrors.unexpectedToken(List.of(SyntaxKind.STRING_LITERAL, SyntaxKind.IDENTIFIER), tokens));
		}

		if (consumeOptionally(fetch, SyntaxKind.IDENTIFIER))
		{
			fetch.setReferencingToken(previousToken());
		}
		else
			if (consumeOptionally(fetch, SyntaxKind.STRING_LITERAL))
			{
				fetch.setReferencingToken(previousToken());
				var referencedModule = sideloadModule(fetch.referencingToken().stringValue().toUpperCase().trim(), previousTokenNode().token());
				if (referencedModule != null
					&& referencedModule.file() != null
					&& referencedModule.file().getFiletype() != NaturalFileType.PROGRAM)
				{
					report(
						ParserErrors.invalidModuleType(
							"Only PROGRAMs can be called with FETCH",
							previousToken()
						)
					);
				}

				fetch.setReferencedModule((NaturalModule) referencedModule);
			}

		return fetch;
	}

	private StatementNode acceptOrReject() throws ParseError
	{
		var acceptReject = new AcceptRejectNode();
		consumeAnyMandatory(acceptReject, List.of(SyntaxKind.ACCEPT, SyntaxKind.REJECT));
		consumeOptionally(acceptReject, SyntaxKind.IF);
		acceptReject.setCondition(conditionNode());
		return acceptReject;
	}

	private static final Set<SyntaxKind> IF_STATEMENT_STOP_KINDS = Set.of(SyntaxKind.END_IF, SyntaxKind.ELSE);

	private StatementNode ifStatement() throws ParseError
	{
		if (peekKind(1, SyntaxKind.NO))
		{
			return ifNoRecord();
		}
		if (peekKind(1, SyntaxKind.SELECTION))
		{
			return ifSelection();
		}

		var ifStatement = new IfStatementNode();

		var opening = consumeMandatory(ifStatement, SyntaxKind.IF);

		ifStatement.setCondition(conditionNode());

		consumeOptionally(ifStatement, SyntaxKind.THEN);

		ifStatement.setBody(statementList(IF_STATEMENT_STOP_KINDS));
		checkForEmptyBody(ifStatement);
		if (peekKind(SyntaxKind.ELSE))
		{
			var elseKeyword = consumeMandatory(ifStatement, SyntaxKind.ELSE);
			ifStatement.setElseBranch(statementList(SyntaxKind.END_IF));
			checkForEmptyBody(ifStatement.elseBranch(), elseKeyword);
		}

		consumeMandatoryClosing(ifStatement, SyntaxKind.END_IF, opening);

		return ifStatement;
	}

	private ConditionNode conditionNode() throws ParseError
	{
		var conditionNode = new ConditionNode();
		conditionNode.setCriteria(chainedCriteria());
		return conditionNode;
	}

	private ILogicalConditionCriteriaNode chainedCriteria() throws ParseError
	{
		var left = conditionCriteria();
		if (peekKind(SyntaxKind.AND) || peekKind(SyntaxKind.OR) && !peekKind(1, SyntaxKind.COUPLED))
		{
			var chainedCriteria = new ChainedCriteriaNode();
			chainedCriteria.setLeft(left);
			consumeAnyMandatory(chainedCriteria, List.of(SyntaxKind.AND, SyntaxKind.OR));
			chainedCriteria.setOperator(ChainedCriteriaOperator.fromSyntax(previousToken().kind()));
			chainedCriteria.setRight(conditionCriteria());

			while (peekKind(SyntaxKind.AND) || peekKind(SyntaxKind.OR))
			{
				chainedCriteria = nestedChainedCriteria(chainedCriteria);
			}
			return chainedCriteria;
		}
		else
		{
			return left;
		}
	}

	private ChainedCriteriaNode nestedChainedCriteria(ChainedCriteriaNode previousChain) throws ParseError
	{
		var chain = new ChainedCriteriaNode();
		chain.setLeft(previousChain);
		consumeAnyMandatory(chain, List.of(SyntaxKind.AND, SyntaxKind.OR));
		chain.setOperator(ChainedCriteriaOperator.fromSyntax(previousToken().kind()));
		chain.setRight(chainedCriteria());
		return chain;
	}

	private static final Set<SyntaxKind> CONDITIONAL_OPERATOR_START = Set
		.of(SyntaxKind.EQUALS_SIGN, SyntaxKind.EQ, SyntaxKind.EQUAL, SyntaxKind.LESSER_GREATER, SyntaxKind.NE, SyntaxKind.NOT, SyntaxKind.CIRCUMFLEX_EQUAL, SyntaxKind.NOTEQUAL, SyntaxKind.LESSER_SIGN, SyntaxKind.LT, SyntaxKind.LESS, SyntaxKind.LESSER_EQUALS_SIGN, SyntaxKind.LE, SyntaxKind.GREATER_SIGN, SyntaxKind.GT, SyntaxKind.GREATER, SyntaxKind.GREATER_EQUALS_SIGN, SyntaxKind.GE);

	private boolean parensEncapsulatesCondition()
	{
		var parensCount = 1;
		int offset = 1; // skip first LPAREN
		while (parensCount > 0 && !isAtEnd(offset))
		{
			if (peekKind(offset, SyntaxKind.LPAREN))
			{
				parensCount++;
			}

			if (peekKind(offset, SyntaxKind.RPAREN))
			{
				parensCount--;
			}

			if (parensCount == 1 && CONDITIONAL_OPERATOR_START.contains(peek(offset).kind()))
			{
				return true;
			}

			offset++;
		}

		return peekKind(offset, SyntaxKind.RPAREN);
	}

	private ILogicalConditionCriteriaNode conditionCriteria() throws ParseError
	{
		if (peekKind(SyntaxKind.LPAREN) && parensEncapsulatesCondition())// && containsNoArithmeticUntilClosingParensOrComparingOperator(SyntaxKind.RPAREN)) // we're not bamboozled by grouping arithmetics or nested comparisons
		{
			return groupedConditionCriteria();
		}

		if (peekKind(SyntaxKind.LPAREN) && containsNoArithmeticUntilClosingParensOrComparingOperator(SyntaxKind.RPAREN))
		{
			return groupedConditionCriteria();
		}

		if (peekKind(SyntaxKind.NOT))
		{
			return negatedConditionCriteria();
		}

		IOperandNode lhs = null;
		if (peekKind(SyntaxKind.BREAK))
		{
			return breakConditionCriteria();
		}
		else
		{
			var tmpNode = new BaseSyntaxNode();
			lhs = consumeSubstringOrOperand(tmpNode);
		}

		if (peekKind(SyntaxKind.IS))
		{
			return isConditionCriteria(lhs);
		}

		if (peekKind(SyntaxKind.MODIFIED) || peekKind(1, SyntaxKind.MODIFIED))
		{
			return modifiedCriteria(lhs);
		}

		if (peekKind(SyntaxKind.SPECIFIED) || peekKind(1, SyntaxKind.SPECIFIED))
		{
			return specifiedCriteria(lhs);
		}

		if (lhs instanceof ILiteralNode literalNode && (literalNode.token().kind() == SyntaxKind.TRUE || literalNode.token().kind() == SyntaxKind.FALSE))
		{
			var unary = new UnaryLogicalCriteriaNode();
			unary.setNode(lhs);
			return unary;
		}

		if (CONDITIONAL_OPERATOR_START.contains(peek().kind()))
		{
			return relationalCriteria(lhs);
		}

		if (lhs instanceof IFunctionCallNode || lhs instanceof IVariableReferenceNode)
		{
			var unary = new UnaryLogicalCriteriaNode();
			unary.setNode(lhs);
			return unary;
		}

		report(ParserErrors.unexpectedToken(List.of(SyntaxKind.TRUE, SyntaxKind.FALSE, SyntaxKind.IDENTIFIER), tokens));
		throw new ParseError(peek());
	}

	protected boolean containsNoArithmeticUntilClosingParensOrComparingOperator(SyntaxKind stopKind)
	{
		var offset = 1;
		var nestedParens = 0;
		while (!isAtEnd(offset) && !peekKind(offset, stopKind) && !CONDITIONAL_OPERATOR_START.contains(peek(offset).kind()))
		{
			var currentKind = peek(offset).kind();
			if (currentKind == SyntaxKind.LPAREN)
			{
				nestedParens++;
			}

			offset++;

			// skip nested parens
			while (nestedParens > 0 && !isAtEnd(offset))
			{
				currentKind = peek(offset).kind();
				if (currentKind == SyntaxKind.LPAREN)
				{
					nestedParens++;
				}
				if (currentKind == SyntaxKind.RPAREN)
				{
					nestedParens--;
				}

				offset++;
			}

			if (ARITHMETIC_OPERATOR_KINDS.contains(currentKind))
			{
				return false;
			}
		}

		return true;
	}

	private ILogicalConditionCriteriaNode breakConditionCriteria() throws ParseError
	{
		var breakCriteria = new IfBreakCriteriaNode();
		consumeMandatory(breakCriteria, SyntaxKind.BREAK);
		consumeOptionally(breakCriteria, SyntaxKind.OF);
		var lhs = consumeOperandNode(breakCriteria);
		breakCriteria.setOperand(lhs);
		checkOperand(lhs, "IF BREAK can only be specified for variable references", AllowedOperand.VARIABLE_REFERENCE);
		if (consumeOptionally(breakCriteria, SyntaxKind.SLASH))
		{
			consumeLiteralNode(breakCriteria, SyntaxKind.NUMBER_LITERAL);
			consumeMandatory(breakCriteria, SyntaxKind.SLASH);
		}
		return breakCriteria;
	}

	private ILogicalConditionCriteriaNode modifiedCriteria(IOperandNode lhs) throws ParseError
	{
		var modifiedCriteria = new ModifiedCriteriaNode();
		modifiedCriteria.addNode((BaseSyntaxNode) lhs);
		modifiedCriteria.setOperand(lhs);
		checkOperand(lhs, "MODIFIED can only be checked on variable references", AllowedOperand.VARIABLE_REFERENCE);
		modifiedCriteria.setIsNotModified(consumeOptionally(modifiedCriteria, SyntaxKind.NOT));
		consumeMandatory(modifiedCriteria, SyntaxKind.MODIFIED);
		return modifiedCriteria;
	}

	private ILogicalConditionCriteriaNode specifiedCriteria(IOperandNode lhs) throws ParseError
	{
		var specifiedCriteria = new SpecifiedCriteriaNode();
		specifiedCriteria.addNode((BaseSyntaxNode) lhs);
		specifiedCriteria.setOperand(lhs);
		checkOperand(lhs, "SPECIFIED can only be checked on variable references", AllowedOperand.VARIABLE_REFERENCE);
		specifiedCriteria.setIsNotSpecified(consumeOptionally(specifiedCriteria, SyntaxKind.NOT));
		consumeMandatory(specifiedCriteria, SyntaxKind.SPECIFIED);
		return specifiedCriteria;
	}

	private ILogicalConditionCriteriaNode isConditionCriteria(IOperandNode lhs) throws ParseError
	{
		var isCriteria = new IsConditionCriteriaNode();
		isCriteria.addNode((BaseSyntaxNode) lhs);
		isCriteria.setLeft(lhs);
		consumeMandatory(isCriteria, SyntaxKind.IS);
		consumeMandatory(isCriteria, SyntaxKind.LPAREN);
		if (!peekKind(SyntaxKind.IDENTIFIER))
		{
			report(ParserErrors.unexpectedToken(peek(), "Expected a data type notation"));
			throw new ParseError(peek());
		}

		var type = peek();
		discard();
		if (peekKind(SyntaxKind.COMMA) || peekKind(SyntaxKind.DOT)) // TODO (lexermode): This should be done in the lexer when lexing data types (in this case after IS)
		{
			type = type.combine(peek(), type.kind());
			discard();
			type = type.combine(peek(), type.kind());
			discard();
		}

		isCriteria.addNode(new TokenNode(type));
		isCriteria.setCheckedType(type);
		consumeMandatory(isCriteria, SyntaxKind.RPAREN);
		return isCriteria;
	}

	private ILogicalConditionCriteriaNode negatedConditionCriteria() throws ParseError
	{
		var negated = new NegatedConditionalCriteria();
		consumeMandatory(negated, SyntaxKind.NOT);
		negated.setCriteria(chainedCriteria());
		return negated;
	}

	private ILogicalConditionCriteriaNode groupedConditionCriteria() throws ParseError
	{
		var groupedCriteria = new GroupedConditionCriteriaNode();
		consumeMandatory(groupedCriteria, SyntaxKind.LPAREN);
		groupedCriteria.setCriteria(chainedCriteria());
		consumeMandatory(groupedCriteria, SyntaxKind.RPAREN);
		return groupedCriteria;
	}

	private ILogicalConditionCriteriaNode relationalCriteria(IOperandNode lhs) throws ParseError
	{
		var expression = new RelationalCriteriaNode();
		expression.addNode((BaseSyntaxNode) lhs);
		expression.setLeft(lhs);

		var originalOperator = peek();
		var operator = parseRelationalOperator(expression); // we did the check of supported values beforehand as lookahead, don't check again
		expression.setOperator(operator);

		var rhs = consumeRelationalCriteriaRightHandSide(expression);
		expression.setRight(rhs);

		if (peekKind(SyntaxKind.OR) && (peekAny(1, List.of(SyntaxKind.EQUALS_SIGN, SyntaxKind.EQ, SyntaxKind.EQUAL))))
		{
			if (expression.operator() != ComparisonOperator.EQUAL)
			{
				report(ParserErrors.extendedRelationalExpressionCanOnlyBeUsedWithEquals(originalOperator));
			}
			return extendedRelationalCriteria(expression);
		}

		if (peekKind(SyntaxKind.THRU))
		{
			if (expression.operator() != ComparisonOperator.EQUAL)
			{
				report(ParserErrors.extendedRelationalExpressionCanOnlyBeUsedWithEquals(originalOperator));
			}
			return rangedExtendedRelationalCriteria(expression);
		}

		return expression;
	}

	private <T extends BaseSyntaxNode & IHasComparisonOperator> IOperandNode consumeRelationalCriteriaRightHandSide(T expression) throws ParseError
	{
		if (peekKind(SyntaxKind.MASK))
		{
			return consumeMask(expression);
		}

		if (peekKind(SyntaxKind.SCAN))
		{
			return consumeScan(expression);
		}

		return consumeSubstringOrOperand(expression);
	}

	private <T extends BaseSyntaxNode & IHasComparisonOperator> IOperandNode consumeScan(T node) throws ParseError
	{
		var scan = new ScanOperandNode();
		node.addNode(scan);
		consumeMandatory(scan, SyntaxKind.SCAN);
		if (consumeOptionally(scan, SyntaxKind.LPAREN))
		{
			scan.setOperand(consumeOperandNode(scan));
			consumeMandatory(scan, SyntaxKind.RPAREN);
		}
		else
		{
			scan.setOperand(consumeOperandNode(scan));
		}

		if (node.operator() != ComparisonOperator.EQUAL && node.operator() != ComparisonOperator.NOT_EQUAL)
		{
			report(ParserErrors.invalidMaskOrScanComparisonOperator(Objects.requireNonNull(scan.findDescendantToken(SyntaxKind.SCAN)).token()));
		}

		return scan;
	}

	private <T extends BaseSyntaxNode & IHasComparisonOperator> IOperandNode consumeMask(T expression) throws ParseError
	{
		var isConstant = peekKind(1, SyntaxKind.LPAREN);
		var mask = isConstant ? consumeConstantMask(expression) : consumeVariableMask(expression);

		if (expression.operator() != ComparisonOperator.EQUAL && expression.operator() != ComparisonOperator.NOT_EQUAL)
		{
			report(ParserErrors.invalidMaskOrScanComparisonOperator(Objects.requireNonNull(mask.findDescendantToken(SyntaxKind.MASK)).token()));
		}

		return mask;
	}

	private IMaskOperandNode consumeConstantMask(BaseSyntaxNode node) throws ParseError
	{
		var mask = new ConstantMaskOperandNode();
		node.addNode(mask);
		consumeMandatory(mask, SyntaxKind.MASK);
		consumeMandatory(mask, SyntaxKind.LPAREN);
		while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
		{
			var token = consume(mask);
			mask.addContent(token);
		}
		consumeMandatory(mask, SyntaxKind.RPAREN);

		if (isOperand() && !isStatementStart() && !peekKind(SyntaxKind.OR) && !peekKind(SyntaxKind.AND) && !peekKind(SyntaxKind.THEN))
		{
			mask.setCheckedOperand(consumeOperandNode(mask));
		}

		return mask;
	}

	private IMaskOperandNode consumeVariableMask(BaseSyntaxNode node) throws ParseError
	{
		var mask = new VariableMaskOperandNode();
		node.addNode(mask);
		consumeMandatory(mask, SyntaxKind.MASK);
		mask.setVariableMask(consumeVariableReferenceNode(mask));
		return mask;
	}

	private ILogicalConditionCriteriaNode rangedExtendedRelationalCriteria(RelationalCriteriaNode expression) throws ParseError
	{
		var rangedCriteria = new RangedExtendedRelationalCriteriaNode(expression);
		consumeMandatory(rangedCriteria, SyntaxKind.THRU);
		rangedCriteria.setUpperBound(consumeArithmeticExpression(rangedCriteria));
		if (consumeOptionally(rangedCriteria, SyntaxKind.BUT))
		{
			consumeMandatory(rangedCriteria, SyntaxKind.NOT);
			rangedCriteria.setExcludedLowerBound(consumeArithmeticExpression(rangedCriteria));
			if (consumeOptionally(rangedCriteria, SyntaxKind.THRU))
			{
				rangedCriteria.setExcludedUpperBound(consumeArithmeticExpression(rangedCriteria));
			}
		}
		return rangedCriteria;
	}

	private ExtendedRelationalCriteriaNode extendedRelationalCriteria(RelationalCriteriaNode expression) throws ParseError
	{
		var extendedCriteria = new ExtendedRelationalCriteriaNode(expression);
		while (peekKind(SyntaxKind.OR) && peekAny(1, List.of(SyntaxKind.EQUALS_SIGN, SyntaxKind.EQ, SyntaxKind.EQUAL)))
		{
			var part = new ExtendedRelationalCriteriaPartNode();
			consumeMandatory(part, SyntaxKind.OR);
			var operatorToken = consumeAnyMandatory(part, List.of(SyntaxKind.EQUALS_SIGN, SyntaxKind.EQ, SyntaxKind.EQUAL));
			if (previousToken().kind() == SyntaxKind.EQUAL)
			{
				consumeOptionally(part, SyntaxKind.TO);
			}

			part.setComparisonToken(operatorToken);
			var rhs = consumeRelationalCriteriaRightHandSide(part);
			part.setRhs(rhs);
			extendedCriteria.addRight(part);
		}

		return extendedCriteria;
	}

	private ComparisonOperator parseRelationalOperator(RelationalCriteriaNode node) throws ParseError
	{
		var kind = peek().kind();
		node.setComparisonToken(peek());
		var maybeOperator = ComparisonOperator.ofSyntaxKind(kind);
		if (maybeOperator != null)
		{
			consume(node);
			return maybeOperator;
		}

		return switch (kind)
		{
			case EQUAL ->
			{
				consume(node);
				consumeOptionally(node, SyntaxKind.TO);
				yield ComparisonOperator.EQUAL;
			}
			case NOT ->
			{
				consume(node);
				consumeAnyMandatory(node, List.of(SyntaxKind.EQUALS_SIGN, SyntaxKind.EQ, SyntaxKind.EQUAL, SyntaxKind.LESSER_SIGN, SyntaxKind.LT, SyntaxKind.GREATER_SIGN, SyntaxKind.GT));
				yield switch (previousToken().kind())
				{
					case LT, LESSER_SIGN -> ComparisonOperator.GREATER_OR_EQUAL;
					case GT, GREATER_SIGN -> ComparisonOperator.LESS_OR_EQUAL;
					default -> //EQUAL
					{
						consumeOptionally(node, SyntaxKind.TO);
						yield ComparisonOperator.NOT_EQUAL;
					}
				};
			}
			case LESS ->
			{
				consume(node);
				consumeAnyMandatory(node, List.of(SyntaxKind.THAN, SyntaxKind.EQUAL));
				yield previousToken().kind() == SyntaxKind.THAN ? ComparisonOperator.LESS_THAN : ComparisonOperator.LESS_OR_EQUAL;
			}
			case GREATER ->
			{
				consume(node);
				consumeAnyMandatory(node, List.of(SyntaxKind.THAN, SyntaxKind.EQUAL));
				yield previousToken().kind() == SyntaxKind.THAN ? ComparisonOperator.GREATER_THAN : ComparisonOperator.GREATER_OR_EQUAL;
			}
			default -> throw new RuntimeException("unreachable: All SyntaxKinds should have been checked beforehand");
		};
	}

	private IfNoRecordNode ifNoRecord() throws ParseError
	{
		var statement = new IfNoRecordNode();

		var opening = consumeMandatory(statement, SyntaxKind.IF);
		consumeMandatory(statement, SyntaxKind.NO);
		consumeEitherOptionally(statement, SyntaxKind.RECORD, SyntaxKind.RECORDS);
		consumeOptionally(statement, SyntaxKind.FOUND);

		statement.setBody(statementList(SyntaxKind.END_NOREC));

		consumeMandatoryClosing(statement, SyntaxKind.END_NOREC, opening);

		return statement;
	}

	private IfSelectionNode ifSelection() throws ParseError
	{
		var statement = new IfSelectionNode();

		var opening = consumeMandatory(statement, SyntaxKind.IF);
		consumeMandatory(statement, SyntaxKind.SELECTION);

		if (consumeOptionally(statement, SyntaxKind.NOT))
		{
			consumeOptionally(statement, SyntaxKind.UNIQUE);
			if (consumeOptionally(statement, SyntaxKind.IN))
			{
				consumeOptionally(statement, SyntaxKind.FIELDS);
			}
		}

		statement.setCondition(conditionNode());
		consumeOptionally(statement, SyntaxKind.THEN);
		statement.setBody(statementList(SyntaxKind.END_IF));

		consumeMandatoryClosing(statement, SyntaxKind.END_IF, opening);

		return statement;
	}

	private static final Set<SyntaxKind> DECIDE_ON_STOP_KINDS = Set.of(SyntaxKind.END_DECIDE, SyntaxKind.NONE, SyntaxKind.ANY, SyntaxKind.ALL, SyntaxKind.VALUE, SyntaxKind.VALUES);
	private static final List<SyntaxKind> DECIDE_ON_VALUE_KEYWORDS = List.of(SyntaxKind.VALUE, SyntaxKind.VALUES);

	private DecideOnNode decideOn() throws ParseError
	{
		var decideOn = new DecideOnNode();
		var opening = consumeMandatory(decideOn, SyntaxKind.DECIDE);
		consumeMandatory(decideOn, SyntaxKind.ON);
		consumeAnyMandatory(decideOn, List.of(SyntaxKind.FIRST, SyntaxKind.EVERY));
		consumeAnyOptionally(decideOn, DECIDE_ON_VALUE_KEYWORDS);
		consumeOptionally(decideOn, SyntaxKind.OF);

		decideOn.setOperand(consumeSubstringOrOperand(decideOn));

		while (!isAtEnd() && !peekKind(SyntaxKind.END_DECIDE))
		{
			if (peekKind(SyntaxKind.NONE))
			{
				var none = consumeMandatory(decideOn, SyntaxKind.NONE);
				consumeAnyOptionally(decideOn, DECIDE_ON_VALUE_KEYWORDS);
				var noneValue = statementList(DECIDE_ON_STOP_KINDS);
				decideOn.setNoneValue(noneValue);
				checkForEmptyBody(noneValue, none);
				continue;
			}

			if (peekKind(SyntaxKind.ANY))
			{
				var any = consumeMandatory(decideOn, SyntaxKind.ANY);
				consumeAnyOptionally(decideOn, DECIDE_ON_VALUE_KEYWORDS);
				var anyValue = statementList(DECIDE_ON_STOP_KINDS);
				decideOn.setAnyValue(anyValue);
				checkForEmptyBody(anyValue, any);
				continue;
			}

			if (peekKind(SyntaxKind.ALL))
			{
				var all = consumeMandatory(decideOn, SyntaxKind.ALL);
				consumeAnyOptionally(decideOn, DECIDE_ON_VALUE_KEYWORDS);
				var allValues = statementList(DECIDE_ON_STOP_KINDS);
				decideOn.setAllValues(allValues);
				checkForEmptyBody(allValues, all);
				continue;
			}

			decideOn.addBranch(decideOnBranch());
		}

		consumeMandatoryClosing(decideOn, SyntaxKind.END_DECIDE, opening);

		if (decideOn.noneValue() == null)
		{
			report(ParserErrors.missingNoneBranch(decideOn));
		}

		return decideOn;
	}

	private DecideOnBranchNode decideOnBranch() throws ParseError
	{
		var branch = new DecideOnBranchNode();
		var branchStart = consumeAnyMandatory(branch, List.of(SyntaxKind.VALUE, SyntaxKind.VALUES));
		branch.addOperand(consumeSubstringOrOperand(branch));

		if (consumeOptionally(branch, SyntaxKind.COLON))
		{
			branch.setHasValueRange();
			branch.addOperand(consumeSubstringOrOperand(branch));
		}
		else
		{
			while (!isAtEnd() && peekKind(SyntaxKind.COMMA))
			{
				consumeMandatory(branch, SyntaxKind.COMMA);
				branch.addOperand(consumeSubstringOrOperand(branch));
			}
		}

		branch.setBody(statementList(DECIDE_ON_STOP_KINDS));
		checkForEmptyBody(branch.body(), branchStart);
		return branch;
	}

	private static final Set<SyntaxKind> DECIDE_FOR_STOP_KINDS = Set.of(SyntaxKind.END_DECIDE, SyntaxKind.WHEN);

	private DecideForConditionNode decideFor() throws ParseError
	{
		var decide = new DecideForConditionNode();
		var opening = consumeMandatory(decide, SyntaxKind.DECIDE);
		consumeMandatory(decide, SyntaxKind.FOR);
		consumeEitherOptionally(decide, SyntaxKind.FIRST, SyntaxKind.EVERY);
		consumeMandatory(decide, SyntaxKind.CONDITION);

		while (!isAtEnd() && peekKind(SyntaxKind.WHEN))
		{
			var whenBranch = consumeMandatory(decide, SyntaxKind.WHEN);

			if (peekKind(SyntaxKind.ANY))
			{
				consumeMandatory(decide, SyntaxKind.ANY);
				var whenAny = statementList(DECIDE_FOR_STOP_KINDS);
				decide.setWhenAny(whenAny);
				checkForEmptyBody(whenAny, whenBranch);
				continue;
			}

			if (peekKind(SyntaxKind.ALL))
			{
				consumeMandatory(decide, SyntaxKind.ALL);
				var whenAll = statementList(DECIDE_FOR_STOP_KINDS);
				decide.setWhenAll(whenAll);
				checkForEmptyBody(whenAll, whenBranch);
				continue;
			}

			if (peekKind(SyntaxKind.NONE))
			{
				consumeMandatory(decide, SyntaxKind.NONE);
				var whenNone = statementList(SyntaxKind.END_DECIDE);
				decide.setWhenNone(whenNone);
				checkForEmptyBody(whenNone, whenBranch);
				continue;
			}

			var branch = new DecideForConditionBranchNode();
			var criteria = conditionNode();
			branch.setCriteria(criteria);
			var branchStatements = statementList(DECIDE_FOR_STOP_KINDS);
			branch.setBody(branchStatements);
			checkForEmptyBody(branchStatements, whenBranch);
			decide.addBranch(branch);
		}

		consumeMandatoryClosing(decide, SyntaxKind.END_DECIDE, opening);

		if (decide.whenNone() == null)
		{
			report(ParserErrors.missingNoneBranch(decide));
		}

		return decide;
	}

	private SetKeyStatementNode setKey() throws ParseError
	{
		var statement = new SetKeyStatementNode();

		consumeMandatory(statement, SyntaxKind.SET);
		consumeMandatory(statement, SyntaxKind.KEY);

		if (consumeAnyOptionally(statement, List.of(SyntaxKind.ALL, SyntaxKind.ON, SyntaxKind.OFF)))
		{
			return statement;
		}
		if (consumeOptionally(statement, SyntaxKind.NAMED))
		{
			consumeMandatory(statement, SyntaxKind.OFF);
			return statement;
		}
		if (consumeOptionally(statement, SyntaxKind.COMMAND))
		{
			consumeAnyMandatory(statement, List.of(SyntaxKind.ON, SyntaxKind.OFF));
			return statement;
		}

		while ((peekKind(SyntaxKind.IDENTIFIER) && !isStatementStart()) || peekKind(SyntaxKind.DYNAMIC))
		{
			var entrSpecified = false;
			if (peekKind(SyntaxKind.DYNAMIC))
			{
				consumeMandatory(statement, SyntaxKind.DYNAMIC);
				consumeOperandNode(statement);
			}
			else
			{
				var pfKeyToken = consumeMandatoryIdentifierTokenNode(statement);
				var name = pfKeyToken.token().symbolName();
				entrSpecified = (name.equals("ENTR")) ? true : false;
				var matcher = SETKEY_PATTERN.matcher(name);
				if (!matcher.find())
				{
					report(ParserErrors.unexpectedToken(pfKeyToken.token(), "Unexpected token %s, expected one of PFnn, PAn, CLR, ENTR".formatted(name)));
				}
			}

			if (!entrSpecified && consumeOptionally(statement, SyntaxKind.EQUALS_SIGN))
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
						var consumed = consumeAnyMandatory(statement, List.of(SyntaxKind.HELP, SyntaxKind.PROGRAM, SyntaxKind.PGM, SyntaxKind.ON, SyntaxKind.OFF, SyntaxKind.STRING_LITERAL, SyntaxKind.COMMAND, SyntaxKind.DISABLED));
						if (consumed.kind() == SyntaxKind.COMMAND)
						{
							consumeAnyMandatory(statement, List.of(SyntaxKind.ON, SyntaxKind.OFF));
						}
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

	private static final Set<SyntaxKind> DBMS_CONDITIONAL_OPERATORS = Set
		.of(SyntaxKind.LESSER_GREATER, SyntaxKind.LESSER_SIGN, SyntaxKind.LT, SyntaxKind.LESS, SyntaxKind.LESSER_EQUALS_SIGN, SyntaxKind.LE, SyntaxKind.GREATER_SIGN, SyntaxKind.GT, SyntaxKind.GREATER, SyntaxKind.GREATER_EQUALS_SIGN, SyntaxKind.GE);

	private StatementNode histogram() throws ParseError
	{
		var histogram = new HistogramNode();
		var opening = consumeMandatory(histogram, SyntaxKind.HISTOGRAM);
		consumeDbmsStart(histogram);
		histogram.setView(consumeVariableReferenceNode(histogram));
		consumePasswordAndCipher(histogram);

		if (consumeAnyOptionally(histogram, List.of(SyntaxKind.IN, SyntaxKind.ASC, SyntaxKind.ASCENDING, SyntaxKind.DESC, SyntaxKind.DESCENDING, SyntaxKind.VARIABLE, SyntaxKind.DYNAMIC)))
		{
			if (previousToken().kind() == SyntaxKind.IN)
			{
				consumeAnyMandatory(histogram, List.of(SyntaxKind.ASC, SyntaxKind.ASCENDING, SyntaxKind.DESC, SyntaxKind.DESCENDING, SyntaxKind.VARIABLE, SyntaxKind.DYNAMIC));
			}

			if (previousToken().kind() == SyntaxKind.VARIABLE || previousToken().kind() == SyntaxKind.DYNAMIC)
			{
				consumeOperandNode(histogram);
			}

			consumeOptionally(histogram, SyntaxKind.SEQUENCE);
		}

		consumeOptionally(histogram, SyntaxKind.VALUE);
		consumeOptionally(histogram, SyntaxKind.FOR);
		consumeOptionally(histogram, SyntaxKind.FIELD);
		histogram.setDescriptor(consumeMandatoryIdentifier(histogram));

		if (consumeAnyOptionally(histogram, List.of(SyntaxKind.STARTING, SyntaxKind.ENDING)))
		{
			if (previousToken().kind() == SyntaxKind.STARTING)
			{
				consumeAnyOptionally(histogram, List.of(SyntaxKind.WITH, SyntaxKind.FROM));
				consumeAnyOptionally(histogram, List.of(SyntaxKind.VALUE, SyntaxKind.VALUES));
				consumeOperandNode(histogram);
			}

			if (consumeAnyOptionally(histogram, List.of(SyntaxKind.THRU, SyntaxKind.ENDING)))
			{
				if (previousToken().kind() == SyntaxKind.ENDING)
				{
					consumeMandatory(histogram, SyntaxKind.AT);
					consumeOperandNode(histogram);
				}
			}
			else
			{
				if (consumeOptionally(histogram, SyntaxKind.TO))
				{
					consumeOperandNode(histogram);
				}
			}
		}
		else
		{
			if (DBMS_CONDITIONAL_OPERATORS.contains(peek().kind()))
			{
				var consumed = consume(histogram);
				if (consumed.kind() == SyntaxKind.LESS || consumed.kind() == SyntaxKind.GREATER)
				{
					consumeAnyMandatory(histogram, List.of(SyntaxKind.THAN, SyntaxKind.EQUAL));
				}
				consumeOperandNode(histogram);
			}
		}

		if (consumeOptionally(histogram, SyntaxKind.WHERE))
		{
			histogram.setCondition(conditionNode());
		}
		histogram.setBody(statementList(SyntaxKind.END_HISTOGRAM));
		consumeMandatoryClosing(histogram, SyntaxKind.END_HISTOGRAM, opening);

		return histogram;
	}

	private FindNode find() throws ParseError
	{
		var find = new FindNode();

		var opening = consumeMandatory(find, SyntaxKind.FIND);
		var hasNoBody = consumeOptionally(find, SyntaxKind.FIRST) || consumeOptionally(find, SyntaxKind.KW_NUMBER) || consumeOptionally(find, SyntaxKind.UNIQUE);
		consumeDbmsStart(find);
		find.setView(consumeVariableReferenceNode(find));
		consumePasswordAndCipher(find);

		// WITH is not required, however the descriptor and logical criteria is
		consumeOptionally(find, SyntaxKind.WITH);

		if (consumeOptionally(find, SyntaxKind.LIMIT))
		{
			consumeLiteralToken(find);
		}

		if (peekKind(SyntaxKind.STRING_LITERAL))
		{
			consumeLiteralNode(find);
		}
		else
		{
			find.addNode(conditionNode());
		}

		//Coupled Clause:
		if (consumeEitherOptionally(find, SyntaxKind.AND, SyntaxKind.OR))
		{
			consumeMandatory(find, SyntaxKind.COUPLED);
			consumeOptionally(find, SyntaxKind.TO);
			consumeOptionally(find, SyntaxKind.FILE);
			consumeOperandNode(find);
			if (consumeOptionally(find, SyntaxKind.VIA))
			{
				consumeOperandNode(find);
				consumeAnyMandatory(find, List.of(SyntaxKind.EQ, SyntaxKind.EQUALS_SIGN, SyntaxKind.EQUAL));
				if (previousToken().kind() == SyntaxKind.EQUAL)
				{
					consumeOptionally(find, SyntaxKind.TO);
				}
				consumeOperandNode(find);
			}
			consumeOptionally(find, SyntaxKind.WITH);
			find.addNode(conditionNode());
		}

		if (!hasNoBody)
		{
			consumeStartingWithIsn(find);
		}

		//Sorted-By Clause OR Retain-As Clause (you can't have both)
		if (consumeAnyOptionally(find, List.of(SyntaxKind.SORTED, SyntaxKind.RETAIN)))
		{
			if (previousToken().kind() == SyntaxKind.SORTED)
			{
				consumeOptionally(find, SyntaxKind.BY);
				while (!(peekAny(List.of(SyntaxKind.DESCENDING, SyntaxKind.DESC))) && isOperand() && !isStatementStart())
				{
					consumeOperandNode(find);
				}
				consumeAnyOptionally(find, List.of(SyntaxKind.DESCENDING, SyntaxKind.DESC));
			}
			else
			{
				consumeMandatory(find, SyntaxKind.AS);
				consumeOperandNode(find);
			}
		}

		if (!hasNoBody)
		{
			consumeSharedHold(find);
			consumeSkipRecordsInHold(find);
		}

		if (consumeOptionally(find, SyntaxKind.WHERE))
		{
			find.addNode(conditionNode());
		}

		if (!hasNoBody)
		{
			find.setBody(statementList(SyntaxKind.END_FIND));
			consumeMandatoryClosing(find, SyntaxKind.END_FIND, opening);
		}

		return find;
	}

	private ReadWorkNode readWork() throws ParseError
	{
		var work = new ReadWorkNode();

		var opening = consumeMandatory(work, SyntaxKind.READ);
		consumeMandatory(work, SyntaxKind.WORK);
		consumeOptionally(work, SyntaxKind.FILE);
		var number = consumeNonConcatLiteralNode(work, SyntaxKind.NUMBER_LITERAL);
		checkNumericRange(number, 1, 32);
		work.setWorkFileNumber(number);
		var hasNoBody = consumeOptionally(work, SyntaxKind.ONCE);
		var operandLoop = true;

		if (consumeOptionally(work, SyntaxKind.RECORD))
		{
			operandLoop = false;
			consumeOperandNode(work);
		}

		if (peekAny(List.of(SyntaxKind.AND, SyntaxKind.SELECT, SyntaxKind.OFFSET, SyntaxKind.FILLER)))
		{
			operandLoop = false;
			consumeOptionally(work, SyntaxKind.AND);
			consumeOptionally(work, SyntaxKind.SELECT);

			while (consumeEitherOptionally(work, SyntaxKind.OFFSET, SyntaxKind.FILLER) && !peekSmart(2, SyntaxKind.ADJUST))
			{
				if (previousToken().kind() == SyntaxKind.OFFSET)
				{
					consumeLiteralNode(work, SyntaxKind.NUMBER_LITERAL);
				}
				else
				{
					consumeMandatory(work, SyntaxKind.OPERAND_SKIP);
				}

				if (!peekSmart(2, SyntaxKind.ADJUST))
				{
					consumeOperandNode(work);
				}
			}
		}

		if (operandLoop)
		{
			while (isOperand() && !isStatementStart() && !peekSmart(2, SyntaxKind.ADJUST))
			{
				consumeOperandNode(work);
			}
		}

		if (isOperand() && peekSmart(2, SyntaxKind.ADJUST))
		{
			consumeVariableReferenceNode(work);
			consumeOptionally(work, SyntaxKind.AND);
			consumeMandatory(work, SyntaxKind.ADJUST);
			consumeOptionally(work, SyntaxKind.OCCURRENCES);
		}

		if (hasNoBody)
		{
			if (peekKind(SyntaxKind.AT))
			{
				atEndOfFile();
			}
		}
		else
		{
			work.setBody(statementList(SyntaxKind.END_WORK));
			consumeMandatoryClosing(work, SyntaxKind.END_WORK, opening);
		}

		return work;
	}

	private AtEndOfFileNode atEndOfFile() throws ParseError
	{
		var statement = new AtEndOfFileNode();

		var opening = consumeMandatory(statement, SyntaxKind.AT);
		consumeOptionally(statement, SyntaxKind.END);
		consumeOptionally(statement, SyntaxKind.OF);
		consumeOptionally(statement, SyntaxKind.FILE);

		statement.setBody(statementList(SyntaxKind.END_ENDFILE));
		consumeMandatoryClosing(statement, SyntaxKind.END_ENDFILE, opening);

		return statement;
	}

	private static final Set<SyntaxKind> READ_SYNTAXES = Set
		.of(SyntaxKind.BY, SyntaxKind.WITH, SyntaxKind.KW_ISN, SyntaxKind.IN, SyntaxKind.PHYSICAL, SyntaxKind.LOGICAL, SyntaxKind.SEQUENCE);
	private static final List<SyntaxKind> READ_SEQUENCES = List
		.of(SyntaxKind.ASCENDING, SyntaxKind.ASC, SyntaxKind.DESCENDING, SyntaxKind.DESC, SyntaxKind.VARIABLE, SyntaxKind.DYNAMIC);

	private ReadNode readStatement() throws ParseError
	{
		var read = new ReadNode();

		var opening = consumeAnyMandatory(read, List.of(SyntaxKind.READ, SyntaxKind.BROWSE));
		consumeDbmsStart(read);
		read.setView(consumeVariableReferenceNode(read));
		consumePasswordAndCipher(read);

		// WITH can be part of search specification, therefore:
		if (peekKind(SyntaxKind.WITH) && peekKind(1, SyntaxKind.REPOSITION))
		{
			consumeMandatory(read, SyntaxKind.WITH);
			consumeMandatory(read, SyntaxKind.REPOSITION);
		}

		var numConsumed = 0;
		var readSeq = ReadSequence.PHYSICAL;
		while (!isAtEnd() && READ_SYNTAXES.contains(peek().kind()))
		{
			var consumed = consume(read);
			numConsumed++;
			readSeq = ReadSequence.fromSyntaxKind(consumed.kind());
		}
		// Rollback the token pointer, now that the type of READ is known
		rollback(numConsumed);

		read.setReadSequence(readSeq);
		switch (readSeq)
		{
			case PHYSICAL:
				consumeAnyOptionally(read, List.of(SyntaxKind.IN, SyntaxKind.PHYSICAL));
				if (consumeAnyOptionally(read, READ_SEQUENCES) && (previousToken().kind() == SyntaxKind.VARIABLE || previousToken().kind() == SyntaxKind.DYNAMIC))
				{
					consumeOperandNode(read);
				}
				consumeOptionally(read, SyntaxKind.SEQUENCE);
				break;
			case ISN:
				consumeAnyMandatory(read, List.of(SyntaxKind.BY, SyntaxKind.WITH));
				consumeMandatory(read, SyntaxKind.KW_ISN);
				consumeReadCondition(read, readSeq);
				break;
			case LOGICAL:
				consumeOptionally(read, SyntaxKind.IN);
				consumeOptionally(read, SyntaxKind.LOGICAL);
				if (consumeAnyOptionally(read, READ_SEQUENCES) && (previousToken().kind() == SyntaxKind.VARIABLE || previousToken().kind() == SyntaxKind.DYNAMIC))
				{
					consumeOperandNode(read);
				}
				consumeOptionally(read, SyntaxKind.SEQUENCE);
				/* Actually, Natural seems to support not specifying a descriptor (even though syntax diagram does not show it).
				That's dirty! We want to enforce it. */
				consumeAnyMandatory(read, List.of(SyntaxKind.BY, SyntaxKind.WITH));
				consumeOperandNode(read);
				consumeReadCondition(read, readSeq);
				break;
			default:
				break;
		}

		consumeStartingWithIsn(read);
		consumeSharedHold(read);
		consumeSkipRecordsInHold(read);
		if (consumeOptionally(read, SyntaxKind.WHERE))
		{
			read.addNode(conditionNode());
		}

		read.setBody(statementList(SyntaxKind.END_READ));
		consumeMandatoryClosing(read, SyntaxKind.END_READ, opening);
		return read;
	}

	private void consumeDbmsStart(BaseSyntaxNode node) throws ParseError
	{
		consumeAnyOptionally(node, List.of(SyntaxKind.ALL, SyntaxKind.LPAREN));
		if (previousToken().kind() == SyntaxKind.LPAREN)
		{
			consumeOperandNode(node);
			consumeMandatory(node, SyntaxKind.RPAREN);
		}
		consumeMultiFetch(node);
		consumeEitherOptionally(node, SyntaxKind.RECORDS, SyntaxKind.RECORD);
		consumeOptionally(node, SyntaxKind.IN);
		consumeOptionally(node, SyntaxKind.FILE);
	}

	private void consumeMultiFetch(BaseSyntaxNode node) throws ParseError
	{
		if (consumeOptionally(node, SyntaxKind.MULTI_FETCH) && !consumeAnyOptionally(node, List.of(SyntaxKind.ON, SyntaxKind.OFF)))
		{
			consumeOptionally(node, SyntaxKind.OF);
			consumeOperandNode(node); // number to fetch
		}
	}

	private void consumePasswordAndCipher(BaseSyntaxNode node) throws ParseError
	{
		if (consumeOptionally(node, SyntaxKind.PASSWORD))
		{
			consumeMandatory(node, SyntaxKind.EQUALS_SIGN);
			consumeOperandNode(node);
		}
		if (consumeOptionally(node, SyntaxKind.CIPHER))
		{
			consumeMandatory(node, SyntaxKind.EQUALS_SIGN);
			consumeOperandNode(node);
		}
	}

	private void consumeStartingWithIsn(BaseSyntaxNode node) throws ParseError
	{
		if (consumeOptionally(node, SyntaxKind.STARTING))
		{
			consumeMandatory(node, SyntaxKind.WITH);
			consumeMandatory(node, SyntaxKind.KW_ISN);
			consumeMandatory(node, SyntaxKind.EQUALS_SIGN);
			consumeOperandNode(node);
		}
	}

	private void consumeSharedHold(BaseSyntaxNode node) throws ParseError
	{
		if (consumeAnyOptionally(node, List.of(SyntaxKind.IN, SyntaxKind.SHARED)))
		{
			if (previousToken().kind() == SyntaxKind.IN)
			{
				consumeMandatory(node, SyntaxKind.SHARED);
			}
			consumeMandatory(node, SyntaxKind.HOLD);
			if (consumeOptionally(node, SyntaxKind.MODE))
			{
				consumeMandatory(node, SyntaxKind.EQUALS_SIGN);
				consumeOperandNode(node);
			}
		}
	}

	private void consumeSkipRecordsInHold(BaseSyntaxNode node) throws ParseError
	{
		if (consumeOptionally(node, SyntaxKind.SKIP))
		{
			consumeAnyOptionally(node, List.of(SyntaxKind.RECORDS, SyntaxKind.RECORD));
			consumeMandatory(node, SyntaxKind.IN);
			consumeMandatory(node, SyntaxKind.HOLD);
		}
	}

	private void consumeReadCondition(BaseSyntaxNode node, ReadSequence readSeq) throws ParseError
	{

		if (consumeAnyOptionally(node, List.of(SyntaxKind.STARTING, SyntaxKind.FROM)))
		{
			if (previousToken().kind() == SyntaxKind.STARTING)
			{
				consumeMandatory(node, SyntaxKind.FROM);
			}
		}
		else
		{
			try
			{
				var expression = new RelationalCriteriaNode();
				parseRelationalOperator(expression);
				expression.destroy();
			}
			catch (Exception e)
			{
				return;
			}

		}

		consumeOperandNode(node);

		if (readSeq == ReadSequence.LOGICAL && consumeOptionally(node, SyntaxKind.TO))
		{
			consumeOperandNode(node);
		}
		else
		{
			if (consumeAnyOptionally(node, List.of(SyntaxKind.THRU, SyntaxKind.ENDING)))
			{
				if (previousToken().kind() == SyntaxKind.ENDING)
				{
					consumeMandatory(node, SyntaxKind.AT);
				}
				consumeOperandNode(node);
			}
		}
	}

	private GetTransactionNode getTransaction() throws ParseError
	{
		var get = new GetTransactionNode();
		consumeMandatory(get, SyntaxKind.GET);
		consumeMandatory(get, SyntaxKind.TRANSACTION);
		consumeOptionally(get, SyntaxKind.DATA);

		while (isOperand() && !isStatementStart())
		{
			get.addOperand(consumeVariableReferenceNode(get));
		}

		return get;
	}

	private GetSameNode getSame() throws ParseError
	{
		var get = new GetSameNode();
		consumeMandatory(get, SyntaxKind.GET);
		consumeMandatory(get, SyntaxKind.SAME);

		if (consumeOptionally(get, SyntaxKind.LPAREN))
		{
			SyntaxToken label;
			if (peekKind(SyntaxKind.LABEL_IDENTIFIER))
			{
				label = consumeMandatory(get, SyntaxKind.LABEL_IDENTIFIER);
			}
			else
			{
				label = consumeNonConcatLiteralNode(get, SyntaxKind.NUMBER_LITERAL).token();
			}

			get.setLabel(label);
			consumeMandatory(get, SyntaxKind.RPAREN);
		}

		return get;
	}

	private GetNode getStatement() throws ParseError
	{
		var get = new GetNode();
		consumeMandatory(get, SyntaxKind.GET);
		consumeOptionally(get, SyntaxKind.IN);
		consumeOptionally(get, SyntaxKind.FILE);

		get.setView(consumeVariableReferenceNode(get));
		consumePasswordAndCipher(get);
		consumeAnyOptionally(get, List.of(SyntaxKind.RECORDS, SyntaxKind.RECORD));
		consumeOperandNode(get);

		return get;
	}

	private List<StatementNode> assignmentsOrIdentifierReference() throws ParseError
	{
		// TODO: this whole lookahead can be simplified when we understand more statements
		if (!peekKind(1, SyntaxKind.COLON_EQUALS_SIGN)
			&& !(peekKind(1, SyntaxKind.LPAREN) && isKindAfterKindInSameLine(SyntaxKind.COLON_EQUALS_SIGN, SyntaxKind.RPAREN)))
		{
			return List.of(identifierReference());
		}

		var assignment = new AssignmentStatementNode();
		assignment.setTarget(consumeOperandNode(assignment));
		consumeMandatory(assignment, SyntaxKind.COLON_EQUALS_SIGN);
		assignment.setOperand(consumeControlLiteralOrSubstringOrOperand(assignment));

		if (peekKind(SyntaxKind.COLON_EQUALS_SIGN))
		{
			var assignments = new ArrayList<StatementNode>();
			assignments.add(assignment);
			var lastAssignment = assignment;

			while (peekKind(SyntaxKind.COLON_EQUALS_SIGN))
			{
				assignment = new AssignmentStatementNode();
				assignment.setTarget(lastAssignment.operand());
				consumeMandatory(assignment, SyntaxKind.COLON_EQUALS_SIGN);
				assignment.setOperand(consumeControlLiteralOrSubstringOrOperand(assignment));
				assignments.add(assignment);
				lastAssignment = assignment;
			}

			return assignments;
		}
		return List.of(assignment);
	}

	private static final List<SyntaxKind> ASSIGN_COMPUTE_EQUALS_SIGNS = List.of(SyntaxKind.EQUALS_SIGN, SyntaxKind.COLON_EQUALS_SIGN);

	private List<StatementNode> assignOrCompute(SyntaxKind kind) throws ParseError
	{
		var statements = new ArrayList<StatementNode>();
		var statement = new AssignOrComputeStatementNode();
		statements.add(statement);
		consumeMandatory(statement, kind);
		statement.setRounded(consumeOptionally(statement, SyntaxKind.ROUNDED));
		statement.setTarget(consumeOperandNode(statement));
		consumeAnyMandatory(statement, ASSIGN_COMPUTE_EQUALS_SIGNS);
		statement.setOperand(consumeControlLiteralOrSubstringOrOperand(statement));

		if (peekAny(ASSIGN_COMPUTE_EQUALS_SIGNS))
		{
			var lastStatement = statement;
			while (peekAny(ASSIGN_COMPUTE_EQUALS_SIGNS))
			{
				statement = new AssignOrComputeStatementNode();
				statements.add(statement);
				statement.setRounded(lastStatement.isRounded());
				statement.setTarget(lastStatement.operand());
				consumeAnyMandatory(statement, ASSIGN_COMPUTE_EQUALS_SIGNS);
				statement.setOperand(consumeControlLiteralOrSubstringOrOperand(statement));
				lastStatement = statement;
			}
		}

		return statements;
	}

	private static final List<SyntaxKind> SPECIAL_MOVE_KINDS = List.of(SyntaxKind.ROUNDED, SyntaxKind.BY, SyntaxKind.EDITED, SyntaxKind.LEFT, SyntaxKind.RIGHT, SyntaxKind.NORMALIZED, SyntaxKind.ENCODED, SyntaxKind.ALL);
	private static final List<SyntaxKind> MOVE_BY_KINDS = List.of(SyntaxKind.NAME, SyntaxKind.POSITION);

	private MoveStatementNode moveStatement() throws ParseError
	{
		var move = new MoveStatementNode();
		boolean usingEditMask = false;
		consumeMandatory(move, SyntaxKind.MOVE);

		if (peekAny(SPECIAL_MOVE_KINDS))
		{
			move.setMoveKind(consumeAnyMandatory(move, SPECIAL_MOVE_KINDS).kind());
			switch (move.moveKind())
			{
				// Syntax 1
				case ROUNDED:
					move.setRounded(true);
					move.setOperand(consumeSubstringOrOperand(move));
					consumeMoveAttributes(move);
					break;
				// Syntax 3
				case BY:
					if (peekAny(MOVE_BY_KINDS))
					{
						move.setByKind(consumeAnyMandatory(move, MOVE_BY_KINDS).kind());
					}
					move.setOperand(consumeOperandNode(move));
					break;
				// Syntax 4+5
				case EDITED:
					move.setEdited(true);
					move.setOperand(consumeOperandNode(move));
					// Syntax 5
					if (!peekKind(SyntaxKind.TO))
					{
						consumeAttributeDefinition(move);
						break;
					}
					usingEditMask = true;
					break;
				// Syntax 6
				case LEFT, RIGHT:
					move.setDirection(move.moveKind());
					consumeOptionally(move, SyntaxKind.JUSTIFIED);
					move.setOperand(consumeOperandNode(move));
					consumeMoveAttributes(move);
					break;
				// Syntax 7
				case NORMALIZED:
					move.setNormalized(true);
					move.setOperand(consumeOperandNode(move));
					break;
				// Syntax 8
				case ENCODED:
					move.setEncoded(true);
					move.setOperand(consumeOperandNode(move));
					if (peekAny(List.of(SyntaxKind.IN, SyntaxKind.CODEPAGE)))
					{
						consumeOptionally(move, SyntaxKind.IN);
						consumeMandatory(move, SyntaxKind.CODEPAGE);
						consumeOperandNode(move);
					}
					break;
				// Syntax 9
				case ALL:
					move.setAll(true);
					move.setOperand(consumeSubstringOrOperand(move));
					break;
				default:
					break;
			}
		}
		else
		{
			// Syntax 1 (not ROUNDED) + Syntax 2
			move.setMoveKind(SyntaxKind.MOVE);
			if (peekKind(SyntaxKind.LPAREN) && getKind(1).isAttribute())
			{
				consumeAttributeDefinition(move);
			}
			else
			{
				move.setOperand(consumeSubstringOrOperand(move));
				consumeMoveAttributes(move);
			}
		}

		consumeAnyMandatory(move, TO_INTO);

		switch (move.moveKind())
		{
			case ROUNDED:
				while (isOperand())
				{
					move.addTarget(consumeOperandNode(move));
				}
				break;
			case BY, LEFT, RIGHT, NORMALIZED:
				move.addTarget(consumeOperandNode(move));
				break;
			case EDITED:
				move.addTarget(consumeOperandNode(move));
				if (usingEditMask)
				{
					consumeAttributeDefinition(move);
				}
				break;
			case ENCODED:
				move.addTarget(consumeOperandNode(move));
				if (peekAny(List.of(SyntaxKind.IN, SyntaxKind.CODEPAGE)))
				{
					consumeOptionally(move, SyntaxKind.IN);
					consumeMandatory(move, SyntaxKind.CODEPAGE);
					consumeOperandNode(move);
				}
				if (consumeOptionally(move, SyntaxKind.GIVING))
				{
					consumeOperandNode(move);
				}
				break;
			case ALL:
				move.addTarget(consumeSubstringOrOperand(move));
				if (consumeOptionally(move, SyntaxKind.UNTIL))
				{
					consumeOperandNode(move);
				}
				break;
			case MOVE:
				while (isOperand() || peekAny(SUBSTRINGS))
				{
					move.addTarget(consumeSubstringOrOperand(move));
				}
				break;
			default:
				break;
		}

		return move;
	}

	private boolean consumeMoveAttributes(BaseSyntaxNode node) throws ParseError
	{
		if (peekKind(SyntaxKind.LPAREN))
		{
			consumeAttributeDefinition(node);
			return true;
		}
		return false;
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

		var lookahead = isAtEnd(1) ? null : peek(1).kind();

		return (peekKind(SyntaxKind.IDENTIFIER) && !peekKindInLine(SyntaxKind.COLON_EQUALS_SIGN))
			|| peek().kind().isSystemFunction()
			|| (peek().kind().isSystemVariable() && lookahead != SyntaxKind.COLON_EQUALS_SIGN)
			|| peek().kind().isLiteralOrConst()
			|| peekKind(SyntaxKind.VAL)
			|| peekKind(SyntaxKind.INT)
			|| peekKind(SyntaxKind.ABS)
			|| peekKind(SyntaxKind.OLD)
			|| peekKind(SyntaxKind.POS)
			|| peekKind(SyntaxKind.FRAC)
			|| (peekKind(SyntaxKind.MINUS) && lookahead == SyntaxKind.NUMBER_LITERAL)
			|| (peekKind(SyntaxKind.LPAREN) && lookahead != null && lookahead.isAttribute())
			|| (peek().kind().canBeIdentifier() && !peekKindInLine(SyntaxKind.COLON_EQUALS_SIGN)); // hopefully this fixes `#ARR(10) :=` being recognized as operand and has no side effects :)
	}

	private boolean isModuleParameter()
	{
		return isOperand() || peekKind(SyntaxKind.OPERAND_SKIP);
	}

	private boolean isStringLiteralOrIdentifier()
	{
		return peekKind(SyntaxKind.STRING_LITERAL) || peekKind(SyntaxKind.IDENTIFIER);
	}

	private void resolveUnresolvedExternalPerforms()
	{
		var resolvedReferences = new ArrayList<ISymbolReferenceNode>();

		for (var unresolvedReference : unresolvedReferences)
		{

			// external subroutines which don't pass parameter couldn't be distinguished from local subroutines up to this point
			if (unresolvedReference instanceof InternalPerformNode internalPerformNode)
			{
				var foundModule = sideloadModule(unresolvedReference.token().trimmedSymbolName(32), internalPerformNode.tokenNode().token());
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

	@SuppressWarnings(
		{
			"unused"
		}
	) // TODO: use this for error recovery
	private boolean isStatementStart()
	{
		if (tokens.isAtEnd())
		{
			return false;
		}

		var currentKind = tokens.peek().kind();
		if (isAssignmentStart())
		{
			return true;
		}

		return switch (currentKind)
		{
			case ACCEPT, ADD, ASSIGN, BEFORE, BACKOUT, CALL, CALLNAT, CLOSE, COMMIT, COMPOSE, COMPRESS, COMPUTE, DECIDE, DEFINE, DELETE, DISPLAY, DIVIDE, DO, DOEND, DOWNLOAD, EJECT, END, ESCAPE, EXAMINE, EXPAND, FETCH, FIND, FOR, FORMAT, GET, HISTOGRAM, IF, IGNORE, INCLUDE, INPUT, INSERT, INTERFACE, LOOP, METHOD, MOVE, MULTIPLY, NEWPAGE, OBTAIN, OPTIONS, PASSW, PERFORM, PRINT, PROCESS, PROPERTY, READ, REDEFINE, REDUCE, REINPUT, REJECT, RELEASE, REPEAT, RESET, RESIZE, RETRY, ROLLBACK, RUN, SELECT, SEPARATE, SET, SKIP, SORT, STACK, STOP, STORE, SUBTRACT, TERMINATE, UPDATE, WRITE -> true;
			case ON -> peekKind(1, SyntaxKind.ERROR);
			case OPEN -> peekKind(1, SyntaxKind.CONVERSATION);
			case PARSE -> peekKind(1, SyntaxKind.XML);
			case REQUEST -> peekKind(1, SyntaxKind.DOCUMENT);
			case SEND -> peekKind(1, SyntaxKind.METHOD);
			case LIMIT -> peekKind(1, SyntaxKind.NUMBER_LITERAL);
			case SUSPEND -> peekKind(1, SyntaxKind.IDENTICAL) && peekKind(2, SyntaxKind.SUPPRESS);
			case UPLOAD -> peekKind(1, SyntaxKind.PC) && peekKind(2, SyntaxKind.FILE);
			default -> false;
		};
	}

	private boolean isStatementEndOrBranch()
	{
		if (tokens.isAtEnd())
		{
			return false;
		}

		var currentKind = tokens.peek().kind();
		return switch (currentKind)
		{
			case ELSE, VALUE, VALUES, WHEN, NONE -> true; // branching
			case END_IF, END_ALL, END_BEFORE, END_BREAK, END_BROWSE, END_CLASS, END_DECIDE, END_ENDDATA, END_ENDFILE, END_ENDPAGE, END_ERROR, END_FILE, END_FIND, END_FOR, END_FUNCTION, END_HISTOGRAM, END_INTERFACE, END_LOOP, END_METHOD, END_NOREC, END_PARAMETERS, END_PARSE, END_PROCESS, END_PROPERTY, END_PROTOTYPE, END_READ, END_REPEAT, END_RESULT, END_SELECT, END_SORT, END_START, END_SUBROUTINE, END_TOPPAGE, END_WORK -> true;
			default -> false;
		};
	}

	private void checkOperand(IOperandNode operand, String message, AllowedOperand... allowedOperands)
	{
		var operands = Arrays.asList(allowedOperands);
		if ((operand instanceof IVariableReferenceNode) && operands.contains(AllowedOperand.VARIABLE_REFERENCE)
			|| (operand instanceof ILiteralNode) && operands.contains(AllowedOperand.LITERAL))
		{
			return;
		}

		report(ParserErrors.invalidOperand(operand, message, allowedOperands));
	}

	private void checkConstantStringValue(IOperandNode node, String actualValue, List<String> allowedValues)
	{
		if (!allowedValues.contains(actualValue))
		{
			report(ParserErrors.invalidStringLiteral(node, actualValue, allowedValues));
		}
	}

	private void checkStringLiteralValue(IOperandNode node, List<String> allowedValues)
	{
		if (!(node instanceof ILiteralNode literalNode) || literalNode.token().kind() != SyntaxKind.STRING_LITERAL)
		{
			return;
		}

		if (!allowedValues.contains(literalNode.token().stringValue()))
		{
			report(ParserErrors.invalidStringLiteral(literalNode, literalNode.token().stringValue(), allowedValues));
		}
	}

	private void checkIntLiteralValue(IOperandNode node, int allowedValue)
	{
		if (!(node instanceof ILiteralNode literalNode) || literalNode.token().kind() != SyntaxKind.NUMBER_LITERAL)
		{
			return;
		}

		if (literalNode.token().intValue() != allowedValue)
		{
			report(ParserErrors.invalidNumericValue(literalNode, literalNode.token().intValue(), allowedValue));
		}
	}

	enum AllowedOperand
	{
		LITERAL,
		VARIABLE_REFERENCE
	}

	private boolean isAssignmentStart()
	{
		return !isAtEnd()
			&& (peek().kind().canBeIdentifier() || peek().kind().isSystemVariable())
			&& (peekKind(1, SyntaxKind.COLON_EQUALS_SIGN)
				|| (peekKind(1, SyntaxKind.LPAREN) && isKindAfterKindInSameLine(SyntaxKind.COLON_EQUALS_SIGN, SyntaxKind.RPAREN)));
	}

	private boolean isInputStatementAttribute(SyntaxKind kind)
	{
		return switch (kind)
		{
			case AD, AL, CD, CV, DF, DL, DY, EM, FL, HE, IP, LS, MC, MS, NL, PC, PM, PS, SG, ZP -> true;
			default -> false;
		};
	}

	private boolean isInputElementAttribute(SyntaxKind kind)
	{
		return switch (kind)
		{
			case AD, AL, CD, CV, DF, DL, DY, EM, EMU, FL, HE, IP, NL, PM, SB, SG, ZP -> true;
			default -> false;
		};
	}
}
