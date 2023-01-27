package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.conditionals.ChainedCriteriaOperator;
import org.amshove.natparse.natural.conditionals.ComparisonOperator;
import org.amshove.natparse.natural.conditionals.IHasComparisonOperator;
import org.amshove.natparse.natural.conditionals.ILogicalConditionCriteriaNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
				if (endTokenKind != null
					&& (peekKind(endTokenKind)
						|| (peekKind(SyntaxKind.END_ALL) && END_KINDS_THAT_END_ALL_ENDS.contains(endTokenKind))))
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
					case BEFORE:
						statementList.addStatement(beforeBreak());
						break;
					case BREAK:
						statementList.addStatement(breakOf());
						break;
					case CALLNAT:
						statementList.addStatement(callnat());
						break;
					case RESIZE:
						statementList.addStatement(resize());
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
					case SKIP:
						statementList.addStatement(skip());
						break;
					case ESCAPE:
						statementList.addStatement(escape());
						break;
					case FORMAT:
						statementList.addStatement(formatNode());
						break;
					case HISTOGRAM:
						statementList.addStatement(histogram());
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
					case DEFINE:
						switch (peek(1).kind())
						{
							case SUBROUTINE, IDENTIFIER -> statementList.addStatement(subroutine());
							case PRINTER -> statementList.addStatement(definePrinter());
							case WINDOW -> statementList.addStatement(defineWindow());
							case WORK, PROTOTYPE, DATA ->
							{ // not implemented statements. DATA needs to be handled when parsing functions and external subroutines
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
					case STACK:
						statementList.addStatement(stack());
						break;
					case TOP:
						statementList.addStatement(parseAtPositionOf(SyntaxKind.TOP, SyntaxKind.PAGE, SyntaxKind.END_TOPPAGE, false, new TopOfPageNode()));
						break;
					case RESET:
						statementList.addStatement(resetStatement());
						break;
					case DECIDE:
						if (peekKind(1, SyntaxKind.FOR))
						{
							statementList.addStatement(decideFor());
							break;
						}
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
		while (!isAtEnd() && !peekKind(SyntaxKind.RPAREN))
		{
			consume(resize);
		}

		consumeMandatory(resize, SyntaxKind.RPAREN);
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
		var newSize = consumeLiteralNode(resize, SyntaxKind.NUMBER_LITERAL);
		resize.setSizeToResizeTo(newSize.token().intValue());

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

	private StatementNode histogram() throws ParseError
	{
		var histogram = new HistogramNode();
		consumeMandatory(histogram, SyntaxKind.HISTOGRAM);
		var start = previousToken();

		consumeAnyOptionally(histogram, List.of(SyntaxKind.ALL, SyntaxKind.LPAREN));
		if (previousToken().kind() == SyntaxKind.LPAREN)
		{
			consumeOperandNode(histogram); // limit
			consumeMandatory(histogram, SyntaxKind.RPAREN);
		}

		if (consumeOptionally(histogram, SyntaxKind.MULTI_FETCH) && !consumeAnyOptionally(histogram, List.of(SyntaxKind.ON, SyntaxKind.OFF)))
		{
			consumeOptionally(histogram, SyntaxKind.OF);
			consumeOperandNode(histogram); // number to fetch
		}

		consumeOptionally(histogram, SyntaxKind.IN);
		consumeOptionally(histogram, SyntaxKind.FILE);

		histogram.setView(consumeVariableReferenceNode(histogram));
		if (consumeOptionally(histogram, SyntaxKind.PASSWORD))
		{
			consumeMandatory(histogram, SyntaxKind.EQUALS_SIGN);
			consumeOperandNode(histogram);
		}

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
					consumeOptionally(histogram, SyntaxKind.AT);
				}
			}
			else
			{
				consumeOptionally(histogram, SyntaxKind.TO);
			}

			if (isOperand())
			{
				consumeOperandNode(histogram);
			}
		}

		histogram.setBody(statementList(SyntaxKind.END_HISTOGRAM));
		consumeMandatoryClosing(histogram, SyntaxKind.END_HISTOGRAM, start);

		return histogram;
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

	private StatementNode breakOf() throws ParseError
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
		consumeVariableReferenceNode(breakOf);

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
			consumeOperandNode(newPage);
		}

		return newPage;
	}

	private StatementNode examine() throws ParseError
	{
		var examine = new ExamineNode();
		consumeMandatory(examine, SyntaxKind.EXAMINE);
		if (consumeOptionally(examine, SyntaxKind.DIRECTION))
		{
			consumeAnyOptionally(examine, List.of(SyntaxKind.FORWARD, SyntaxKind.BACKWARD));
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
			if (peek().kind().isLiteralOrConst() || peek().kind().isIdentifier())
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

	private IOperandNode consumeSubstringOrOperand(BaseSyntaxNode node) throws ParseError
	{
		if (peekKind(SyntaxKind.SUBSTR) || peekKind(SyntaxKind.SUBSTRING))
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
		consumeAnyMandatory(node, List.of(SyntaxKind.SUBSTR, SyntaxKind.SUBSTRING));

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
			if (peekKind(SyntaxKind.IDENTIFIER) && peekKind(1, SyntaxKind.RPAREN))
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

		// TODO: Actual operands to WRITE not parsed
		return write;
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

		referencableNodes.add(subroutine);

		return subroutine;
	}

	private StatementNode end() throws ParseError
	{
		var endNode = new EndNode();
		consumeMandatory(endNode, SyntaxKind.END);
		return endNode;
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
			var referencedModule = sideloadModule(callnat.referencingToken().stringValue().toUpperCase().trim(), previousTokenNode().token());
			callnat.setReferencedModule((NaturalModule) referencedModule);
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
				include.setBody(
					statementList.result(),
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
			var referencedModule = sideloadModule(fetch.referencingToken().stringValue().toUpperCase().trim(), previousTokenNode().token());
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
		if (peek(1).kind() == SyntaxKind.BREAK)
		{
			return ifBreak();
		}
		if (peek(1).kind() == SyntaxKind.SELECTION)
		{
			return ifSelection();
		}

		var ifStatement = new IfStatementNode();

		var opening = consumeMandatory(ifStatement, SyntaxKind.IF);

		ifStatement.setCondition(conditionNode());

		consumeOptionally(ifStatement, SyntaxKind.THEN);

		ifStatement.setBody(statementList(SyntaxKind.END_IF));

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
		if (peekKind(SyntaxKind.AND) || peekKind(SyntaxKind.OR))
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

	private ILogicalConditionCriteriaNode conditionCriteria() throws ParseError
	{
		if (peekKind(SyntaxKind.LPAREN) && containsNoArithmeticUntilClosingParensOrComparingOperator(SyntaxKind.RPAREN)) // we're not bamboozled by grouping arithmetics or nested comparisons
		{
			return groupedConditionCriteria();
		}

		if (peekKind(SyntaxKind.NOT))
		{
			return negatedConditionCriteria();
		}

		var tmpNode = new BaseSyntaxNode();
		var lhs = consumeSubstringOrOperand(tmpNode);

		if (peekKind(SyntaxKind.IS))
		{
			return isConditionCriteria(lhs);
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

		if (lhs instanceof ILiteralNode literalNode && (literalNode.token().kind() == SyntaxKind.TRUE || literalNode.token().kind() == SyntaxKind.FALSE))
		{
			var unary = new UnaryLogicalCriteriaNode();
			unary.setNode(lhs);
			return unary;
		}

		report(ParserErrors.unexpectedToken(List.of(SyntaxKind.TRUE, SyntaxKind.FALSE, SyntaxKind.IDENTIFIER), peek()));
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
			// skip nested parens
			while (nestedParens > 0 && !isAtEnd(offset))
			{
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
			offset++;
		}

		return true;
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

		if (!isAtEnd() && peek().kind().isIdentifier() && !peekKind(SyntaxKind.OR) && !peekKind(SyntaxKind.AND))
		{
			mask.setCheckedOperand(consumeVariableReferenceNode(mask));
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
		rangedCriteria.setUpperBound(consumeOperandNode(rangedCriteria));
		if (consumeOptionally(rangedCriteria, SyntaxKind.BUT))
		{
			consumeMandatory(rangedCriteria, SyntaxKind.NOT);
			rangedCriteria.setExcludedLowerBound(consumeOperandNode(rangedCriteria));
			if (consumeOptionally(rangedCriteria, SyntaxKind.THRU))
			{
				rangedCriteria.setExcludedUpperBound(consumeOperandNode(rangedCriteria));
			}
		}
		return rangedCriteria;
	}

	private ExtendedRelationalCriteriaNode extendedRelationalCriteria(RelationalCriteriaNode expression) throws ParseError
	{
		var extendedCriteria = new ExtendedRelationalCriteriaNode(expression);
		while (peekKind(SyntaxKind.OR) && peekAny(1, List.of(SyntaxKind.EQUALS_SIGN, SyntaxKind.EQ, SyntaxKind.EQUAL)))
		{
			consumeMandatory(extendedCriteria, SyntaxKind.OR);
			consumeAnyMandatory(extendedCriteria, List.of(SyntaxKind.EQUALS_SIGN, SyntaxKind.EQ, SyntaxKind.EQUAL));
			if (previousToken().kind() == SyntaxKind.EQUAL)
			{
				consumeOptionally(extendedCriteria, SyntaxKind.TO);
			}

			extendedCriteria.addRight(consumeRelationalCriteriaRightHandSide(extendedCriteria));
		}

		return extendedCriteria;
	}

	private ComparisonOperator parseRelationalOperator(RelationalCriteriaNode node) throws ParseError
	{
		var kind = peek().kind();
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

	private IfBreakNode ifBreak() throws ParseError
	{
		var statement = new IfBreakNode();

		var opening = consumeMandatory(statement, SyntaxKind.IF);
		consumeMandatory(statement, SyntaxKind.BREAK);
		consumeOptionally(statement, SyntaxKind.OF);
		consumeMandatoryIdentifier(statement);
		if (consumeOptionally(statement, SyntaxKind.SLASH))
		{
			consumeLiteralNode(statement, SyntaxKind.NUMBER_LITERAL);
			consumeMandatory(statement, SyntaxKind.SLASH);
		}

		consumeOptionally(statement, SyntaxKind.THEN);
		statement.setBody(statementList(SyntaxKind.END_IF));

		consumeMandatoryClosing(statement, SyntaxKind.END_IF, opening);

		return statement;
	}

	private IfSelectionNode ifSelection() throws ParseError
	{
		var statement = new IfSelectionNode();

		var opening = consumeMandatory(statement, SyntaxKind.IF);
		consumeMandatory(statement, SyntaxKind.SELECTION);
		consumeAnyOptionally(statement, List.of(SyntaxKind.NOT, SyntaxKind.UNIQUE, SyntaxKind.IN, SyntaxKind.FIELDS));

		statement.setCondition(conditionNode());
		consumeOptionally(statement, SyntaxKind.THEN);
		statement.setBody(statementList(SyntaxKind.END_IF));

		consumeMandatoryClosing(statement, SyntaxKind.END_IF, opening);

		return statement;
	}

	private DecideForConditionNode decideFor() throws ParseError
	{
		var decide = new DecideForConditionNode();
		consumeMandatory(decide, SyntaxKind.DECIDE);
		consumeMandatory(decide, SyntaxKind.FOR);
		consumeEitherOptionally(decide, SyntaxKind.FIRST, SyntaxKind.EVERY);
		consumeMandatory(decide, SyntaxKind.CONDITION);

		while (!isAtEnd() && peekKind(SyntaxKind.WHEN))
		{
			consumeMandatory(decide, SyntaxKind.WHEN);

			if (peekKind(SyntaxKind.ANY))
			{
				consumeMandatory(decide, SyntaxKind.ANY);
				decide.setWhenAny(statementList(SyntaxKind.WHEN));
				continue;
			}

			if (peekKind(SyntaxKind.ALL))
			{
				consumeMandatory(decide, SyntaxKind.ALL);
				decide.setWhenAll(statementList(SyntaxKind.WHEN));
				continue;
			}

			if (peekKind(SyntaxKind.NONE))
			{
				consumeMandatory(decide, SyntaxKind.NONE);
				decide.setWhenNone(statementList(SyntaxKind.END_DECIDE));
				continue;
			}

			var branch = new DecideForConditionBranchNode();
			var criteria = conditionCriteria();
			branch.setCriteria(criteria);
			branch.setBody(statementList(SyntaxKind.WHEN));
			decide.addBranch(branch);
		}

		return decide;
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
		if (consumeOptionally(find, SyntaxKind.MULTI_FETCH) && !consumeAnyOptionally(find, List.of(SyntaxKind.ON, SyntaxKind.OFF)))
		{
			consumeOptionally(find, SyntaxKind.OF);
			consumeOperandNode(find); // number to fetch
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

		var lookahead = isAtEnd(1) ? null : peek(1).kind();

		return (peekKind(SyntaxKind.IDENTIFIER) && !peekKindInLine(SyntaxKind.COLON_EQUALS_SIGN))
			|| peek().kind().isSystemFunction()
			|| (peek().kind().isSystemVariable() && lookahead != SyntaxKind.COLON_EQUALS_SIGN)
			|| peek().kind().isLiteralOrConst()
			|| peekKind(SyntaxKind.VAL)
			|| peekKind(SyntaxKind.ABS)
			|| peekKind(SyntaxKind.POS)
			|| peekKind(SyntaxKind.FRAC)
			|| (peekKind(SyntaxKind.MINUS) && lookahead == SyntaxKind.NUMBER_LITERAL)
			|| (peek().kind().canBeIdentifier() && !peekKindInLine(SyntaxKind.COLON_EQUALS_SIGN)); // hopefully this fixes `#ARR(10) :=` being recognized as operand and has no side effects :)
	}

	private boolean isModuleParameter()
	{
		return isOperand() || peekKind(SyntaxKind.OPERAND_SKIP);
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
		if (currentKind.canBeIdentifier() && peekKind(1, SyntaxKind.COLON_EQUALS_SIGN))
		{
			return true;
		}

		return switch (currentKind)
		{
			case ACCEPT, ADD, ASSIGN, BEFORE, CALL, CALLNAT, CLOSE, COMMIT, COMPRESS, COMPUTE, DECIDE, DEFINE, DELETE, DISPLAY, DIVIDE, DO, DOEND, DOWNLOAD, EJECT, END, ESCAPE, EXAMINE, EXPAND, FETCH, FIND, FOR, FORMAT, GET, HISTOGRAM, IF, IGNORE, INCLUDE, INPUT, INSERT, INTERFACE, LIMIT, LOOP, METHOD, MOVE, MULTIPLY, NEWPAGE, OBTAIN, OPTIONS, PASSW, PERFORM, PRINT, PROCESS, PROPERTY, READ, REDEFINE, REDUCE, REINPUT, REJECT, RELEASE, REPEAT, RESET, RESIZE, RETRY, ROLLBACK, RUN, SELECT, SEPARATE, SET, SKIP, SORT, STACK, STOP, STORE, SUBTRACT, TERMINATE, UPDATE, WRITE -> true;
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
