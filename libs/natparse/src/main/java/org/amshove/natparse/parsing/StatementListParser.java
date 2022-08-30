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
					case BREAK:
						statementList.addStatement(breakOf());
						break;
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
					case EJECT:
						statementList.addStatement(eject());
						break;
					case ESCAPE:
						statementList.addStatement(escape());
						break;
					case FORMAT:
						statementList.addStatement(formatNode());
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
					case IDENTIFIER:
						statementList.addStatement(identifierReference());
						break;
					case EXAMINE:
						statementList.addStatement(examine());
						break;
					case WRITE:
						statementList.addStatement(write());
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
					case NEWPAGE:
						statementList.addStatement(newPage());
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
					case TOP:
						statementList.addStatement(parseAtPositionOf(SyntaxKind.TOP, SyntaxKind.PAGE, SyntaxKind.END_TOPPAGE, false, new TopOfPageNode()));
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
						// TODO: until we support EXAMINE, DECIDE, HISTOGRAM, ...
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

	private StatementNode escape() throws ParseError
	{
		var escape = new EscapeNode();
		consumeMandatory(escape, SyntaxKind.ESCAPE);
		consumeAnyMandatory(escape, List.of(SyntaxKind.TOP, SyntaxKind.BOTTOM, SyntaxKind.ROUTINE, SyntaxKind.MODULE));
		var direction = previousToken().kind();
		escape.setDirection(direction);
		if(direction == SyntaxKind.TOP)
		{
			if(consumeOptionally(escape, SyntaxKind.REPOSITION))
			{
				escape.setReposition();
			}
		}
		else
		{
			if(direction == SyntaxKind.BOTTOM && consumeOptionally(escape, SyntaxKind.LPAREN))
			{
				var label = consumeMandatory(escape, SyntaxKind.LABEL_IDENTIFIER);
				escape.setLabel(label);
				consumeMandatory(escape, SyntaxKind.RPAREN);
			}

			if(consumeOptionally(escape, SyntaxKind.IMMEDIATE))
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

		if(consumeAnyOptionally(eject, List.of(SyntaxKind.ON, SyntaxKind.OFF)))
		{
			consumeOptionalReportSpecification(eject);
		}
		else
		{
			consumeOptionalReportSpecification(eject);
			consumeAnyOptionally(eject, List.of(SyntaxKind.IF, SyntaxKind.WHEN));
			if(consumeOptionally(eject, SyntaxKind.LESS))
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

	private StatementNode breakOf() throws ParseError
	{
		var breakOf = new BreakOfNode();
		consumeOptionally(breakOf, SyntaxKind.AT);
		var openingToken = consumeMandatory(breakOf, SyntaxKind.BREAK);
		if(consumeOptionally(breakOf, SyntaxKind.LPAREN))
		{
			var identifier = consumeMandatory(breakOf, SyntaxKind.LABEL_IDENTIFIER);
			breakOf.setReportSpecification(identifier);
			consumeMandatory(breakOf, SyntaxKind.RPAREN);
		}

		consumeOptionally(breakOf, SyntaxKind.OF);
		consumeVariableReferenceNode(breakOf);

		if(consumeOptionally(breakOf, SyntaxKind.SLASH))
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
	 * @param location          the "location", e.g. START, TOP, END
	 * @param statementType     the type, e.g. PAGE, DATA
	 * @param statementEndToken the token which ends the body
	 * @param node              the resulting node
	 */
	private <T extends StatementWithBodyNode & ICanSetReportSpecification> StatementNode parseAtPositionOf(
		SyntaxKind location,
		SyntaxKind statementType,
		SyntaxKind statementEndToken,
		boolean canHaveLabelIdentifier,
		T node) throws ParseError
	{
		consumeOptionally(node, SyntaxKind.AT);
		var openingToken = consumeMandatory(node, location);
		consumeOptionally(node, SyntaxKind.OF);
		consumeMandatory(node, statementType);

		if (consumeOptionally(node, SyntaxKind.LPAREN))
		{
			if(canHaveLabelIdentifier)
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
			if (consumeAnyOptionally(newPage, List.of(SyntaxKind.IF, SyntaxKind.WHEN, SyntaxKind.LESS)))
			{
				if (previousToken().kind() != SyntaxKind.LESS)
				{
					consumeMandatory(newPage, SyntaxKind.LESS);
				}

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
			consumeOperandNode(newPage);
		}

		return newPage;
	}

	private StatementNode examine() throws ParseError
	{
		var examine = new ExamineNode();
		consumeMandatory(examine, SyntaxKind.EXAMINE);
		consumeAnyOptionally(examine, List.of(SyntaxKind.FORWARD, SyntaxKind.BACKWARD));
		if (consumeOptionally(examine, SyntaxKind.FULL))
		{
			if (consumeOptionally(examine, SyntaxKind.VALUE))
			{
				consumeOptionally(examine, SyntaxKind.OF);
			}
		}

		// TODO: Handle SUBSTRING
		var examined = consumeVariableReferenceNode(examine);
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
			consumeOptionally(examine, SyntaxKind.DELIMITERS);
			consumeOperandNode(examine);
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

		while (consumeOptionally(examine, SyntaxKind.GIVING))
		{
			if (consumeOptionally(examine, SyntaxKind.IN))
			{
				consumeOperandNode(examine);
			}
			else
				if (consumeOptionally(examine, SyntaxKind.KW_NUMBER))
				{
					consumeOptionally(examine, SyntaxKind.IN);
					consumeOperandNode(examine);
				}
				else
					if (consumeOptionally(examine, SyntaxKind.POSITION))
					{
						consumeOptionally(examine, SyntaxKind.IN);
						consumeOperandNode(examine);
					}
					else
						if (consumeOptionally(examine, SyntaxKind.LENGTH))
						{
							consumeOptionally(examine, SyntaxKind.IN);
							consumeOperandNode(examine);
						}
						else
							if (consumeOptionally(examine, SyntaxKind.INDEX))
							{
								consumeOptionally(examine, SyntaxKind.IN);
								while (isOperand())
								{
									consumeOperandNode(examine);
								}
							}
							else
							{
								consumeOperandNode(examine);
							}
		}

		return examine;
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

	private StatementNode write() throws ParseError
	{
		var write = new WriteNode();
		consumeMandatory(write, SyntaxKind.WRITE);
		if (consumeOptionally(write, SyntaxKind.LPAREN))
		{
			if (peekKind(SyntaxKind.IDENTIFIER) && peekKind(1, SyntaxKind.RPAREN))
			{
				var token = consumeMandatoryIdentifier(write);
				write.setReportSpecification(token);
			}
			else
			{
				// currently consume everything until closing parenthesis to consume things like attribute definition etc.
				while (!peekKind(SyntaxKind.RPAREN))
				{
					consume(write);
				}
			}
			consumeMandatory(write, SyntaxKind.RPAREN);
		}

		consumeOptionally(write, SyntaxKind.NOTITLE);
		consumeOptionally(write, SyntaxKind.NOHDR);

		return write;
	}

	private static final Set<SyntaxKind> FORMAT_MODIFIERS = Set.of(SyntaxKind.AD, SyntaxKind.AL, SyntaxKind.CD, SyntaxKind.DF, SyntaxKind.DL, SyntaxKind.EM, SyntaxKind.ES, SyntaxKind.FC, SyntaxKind.FL, SyntaxKind.GC, SyntaxKind.HC, SyntaxKind.HW, SyntaxKind.IC, SyntaxKind.IP, SyntaxKind.IS, SyntaxKind.KD, SyntaxKind.LC, SyntaxKind.LS, SyntaxKind.MC, SyntaxKind.MP, SyntaxKind.MS, SyntaxKind.NL,
		SyntaxKind.PC, SyntaxKind.PM, SyntaxKind.PS, SyntaxKind.SF, SyntaxKind.SG, SyntaxKind.TC, SyntaxKind.UC, SyntaxKind.ZP);

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
		consumeAnyOptionally(loopNode, List.of(SyntaxKind.TO, SyntaxKind.THRU)); // According to the documentation, either TO or THRU is mandatory. However, FOR #I 1 10 also just works :)
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
