package org.amshove.natparse.lexing;

import org.amshove.natparse.AdditionalDiagnosticInfo;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.text.SourceTextScanner;
import org.amshove.natparse.natural.project.NaturalHeader;
import org.amshove.natparse.natural.project.NaturalProgrammingMode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lexer
{
	private final List<String> copyCodeParameter;
	private SourceTextScanner scanner;
	private List<SyntaxToken> tokens;
	private List<SyntaxToken> comments;
	private int line;
	private int currentLineStartOffset;
	private Path filePath;
	private NaturalHeader sourceHeader;
	private IPosition relocatedDiagnosticPosition;

	private int parensLevel;
	private boolean inSourceHeader;
	private boolean sourceHeaderDone;
	private SyntaxToken lastBeforeOpenParens;

	private NaturalProgrammingMode programmingMode = NaturalProgrammingMode.UNKNOWN;
	private int lineIncrement = 10;
	private List<LexerDiagnostic> diagnostics;

	private enum LexerMode
	{
		DEFAULT,
		IN_DEFINE_DATA,
		IN_DATA_TYPE
	}

	private LexerMode lexerMode = LexerMode.DEFAULT;

	private static final List<String> NO_PARAMETER = List.of();

	public Lexer()
	{
		this(NO_PARAMETER);
	}

	public Lexer(List<String> copyCodeParameter)
	{
		this.copyCodeParameter = copyCodeParameter;
	}

	public TokenList lex(String source, Path filePath)
	{
		this.filePath = filePath;
		tokens = new ArrayList<>();
		diagnostics = new ArrayList<>();
		comments = new ArrayList<>();
		scanner = new SourceTextScanner(source, copyCodeParameter);
		sourceHeader = new NaturalHeader(NaturalProgrammingMode.UNKNOWN, 0);
		line = 0;
		currentLineStartOffset = 0;

		while (!scanner.isAtEnd())
		{
			if (!sourceHeaderDone && consumeNaturalHeader())
			{
				continue;
			}

			if (lexerMode != LexerMode.IN_DATA_TYPE && consumeComment())
			{
				continue;
			}

			switch (scanner.peek())
			{
				case ' ':
				case '\t':
				case '\r':
					scanner.advance();
					continue;
				case '\n':
					consumeNewLine();
					continue;
				case '(':
					if (consumeNumberedLabel())
					{
						continue;
					}
					else
					{
						parensLevel++;
						lastBeforeOpenParens = previous();
						if (lexerMode == LexerMode.IN_DEFINE_DATA && lastBeforeOpenParens != null && lastBeforeOpenParens.kind() != SyntaxKind.LESSER_SIGN)
						{
							lexerMode = LexerMode.IN_DATA_TYPE;
						}
						createAndAddCurrentSingleToken(SyntaxKind.LPAREN);
						continue;
					}
				case ')':
					parensLevel--;
					if (lexerMode == LexerMode.IN_DATA_TYPE)
					{
						lexerMode = LexerMode.IN_DEFINE_DATA;
					}
					lastBeforeOpenParens = null;
					createAndAddCurrentSingleToken(SyntaxKind.RPAREN);
					continue;
				case '[':
					createAndAddCurrentSingleToken(SyntaxKind.LBRACKET);
					continue;
				case ']':
					createAndAddCurrentSingleToken(SyntaxKind.RBRACKET);
					continue;
				case '=':
					createAndAddCurrentSingleToken(SyntaxKind.EQUALS_SIGN);
					continue;
				case ':':
					createAndAddFollowupEquals(SyntaxKind.COLON, SyntaxKind.COLON_EQUALS_SIGN);
					continue;
				case '+':
					if (isValidAivStartAfterPlus(scanner.peek(1))
						&& (hasSpaceBetweenThisAndLast()
							|| previousWasNoLiteralOrIdentifier()))
					{
						consumeIdentifier();
					}
					else
					{
						createAndAddCurrentSingleToken(SyntaxKind.PLUS);
					}
					continue;
				case '-':
					createAndAddCurrentSingleToken(SyntaxKind.MINUS);
					continue;
				case '*':
					consumeAsteriskOrSystemVariable();
					continue;
				case '/':
					createAndAddCurrentSingleToken(SyntaxKind.SLASH);
					continue;
				case '\\':
					createAndAddCurrentSingleToken(SyntaxKind.BACKSLASH);
					continue;
				case ';':
					createAndAddCurrentSingleToken(SyntaxKind.SEMICOLON);
					continue;
				case '>':
					createAndAddFollowupEquals(SyntaxKind.GREATER_SIGN, SyntaxKind.GREATER_EQUALS_SIGN);
					continue;
				case '<':
					if (tryCreateIfFollowedBy('=', SyntaxKind.LESSER_EQUALS_SIGN)
						|| tryCreateIfFollowedBy('>', SyntaxKind.LESSER_GREATER))
					{
						continue;
					}
					createAndAddCurrentSingleToken(SyntaxKind.LESSER_SIGN);
					continue;
				case '.':
					createAndAddCurrentSingleToken(SyntaxKind.DOT);
					continue;
				case ',':
					createAndAddCurrentSingleToken(SyntaxKind.COMMA);
					continue;
				case '\'':
					consumeString('\'');
					continue;
				case '"':
					consumeString('"');
					continue;
				case '^':
					if (tryCreateIfFollowedBy('=', SyntaxKind.CIRCUMFLEX_EQUAL))
					{
						continue;
					}
					createAndAddCurrentSingleToken(SyntaxKind.CARET);
					continue;
				case '!':
					if (tryCreateIfFollowedBy('!', SyntaxKind.SQL_CONCAT))
					{
						continue;
					}
					// Single ! is likely to be the ID (Input delimiter char, in most cases this could be comma instead)
					createAndAddCurrentSingleToken(SyntaxKind.COMMA);
					continue;
				case '%':
					createAndAddCurrentSingleToken(SyntaxKind.PERCENT);
					continue;
				case '_':
					createAndAddCurrentSingleToken(SyntaxKind.UNDERSCORE);
					continue;
				case '\u00A7': // §
					createAndAddCurrentSingleToken(SyntaxKind.SECTION_SYMBOL);
					continue;
				case '?':
					createAndAddCurrentSingleToken(SyntaxKind.QUESTIONMARK);
					continue;
				case 'h':
				case 'H':
					if (scanner.peek(1) == '\'')
					{
						consumeHexLiteral();
					}
					else
					{
						consumeIdentifierOrKeyword();
					}
					continue;
				case 't':
				case 'T':
					if (scanner.peek(1) == '\'')
					{
						consumeTimeLiteral();
					}
					else
					{
						consumeIdentifierOrKeyword();
					}
					continue;
				case 'a':
				case 'A':
				case 'b':
				case 'B':
				case 'c':
				case 'C':
					consumeIdentifierOrKeyword();
					continue;
				case 'd':
				case 'D':
					if (scanner.peek(1) == '\'')
					{
						consumeDateLiteral();
					}
					else
					{
						consumeIdentifierOrKeyword();
					}
					continue;
				case 'e':
				case 'E':
					if (scanner.peek(1) == '\'')
					{
						consumeExtendedTimeLiteral();
					}
					else
					{
						consumeIdentifierOrKeyword();
					}
					continue;
				case 'f':
				case 'F':
				case 'g':
				case 'G':
				case 'i':
				case 'I':
				case 'j':
				case 'J':
				case 'k':
				case 'K':
				case 'l':
				case 'L':
				case 'm':
				case 'M':
				case 'n':
				case 'N':
				case 'o':
				case 'O':
				case 'p':
				case 'P':
				case 'q':
				case 'Q':
				case 'r':
				case 'R':
				case 's':
				case 'S':
				case 'u':
				case 'U':
				case 'v':
				case 'V':
				case 'w':
				case 'W':
				case 'x':
				case 'X':
				case 'y':
				case 'Y':
				case 'z':
				case 'Z':
				case '\u00C5': // (char) 0xc385: // AA
				case '\u00C6': // (char) 0xc386: // AE
				case '\u00D8': // (char) 0xc398: // OE
				case '\u00E6': // (char) 0xc3a6: // ae
				case '\u00E5': // (char) 0xc3a5: // aa
				case '\u00F8': // (char) 0xc3b8: // oe
					consumeIdentifierOrKeyword();
					continue;
				case '#':
					consumeIdentifier();
					continue;
				case '&':
					consumeIdentifierOrCopyCodeParameter();
					continue;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					consumeNumber();
					continue;
				case '¬':
					if (tryCreateIfFollowedBy('=', SyntaxKind.NOT_SIGN_EQUAL))
					{
						continue;
					}
					// Fall through, `¬` is only valid with an `=` after
				default:
					diagnostics.add(
						LexerDiagnostic.create(
							"Unknown character [%c]".formatted(scanner.peek()),
							scanner.position(),
							getOffsetInLine(),
							line,
							1,
							filePath,
							LexerError.UNKNOWN_CHARACTER
						)
					);
					scanner.advance();
			}
		}
		return TokenList.fromTokensAndDiagnostics(filePath, tokens, diagnostics, comments, sourceHeader);
	}

	private void consumeIdentifierOrCopyCodeParameter()
	{
		// This checks for left over parameter, e.g. if a user didn't provide a parameter
		if (scanner.peek() == '&')
		{
			scanner.start();
			scanner.advance(); // &
			var offset = 0;
			while (!isWhitespace(offset) && Character.isDigit(scanner.peek(offset)))
			{
				offset++;
			}

			if (scanner.peek(offset) == '&') // all were digits and we end with ampersand, so this is a copycode parameter
			{
				scanner.advance(offset);
				var position = Integer.parseInt(scanner.lexemeText().substring(1));
				scanner.advance(); // to include the closing in error position

				// only raise the diagnostic for the including side
				if (relocatedDiagnosticPosition != null)
				{
					addDiagnostic("Copy code parameter with position %d not provided".formatted(position), "Parameter is used here", LexerError.MISSING_COPYCODE_PARAMETER);
				}
			}

			// We roll this all back, because this was just for better error messages.
			// The &n& will be added as an IDENTIFIER so that copy codes get analyzed correctly.
			scanner.rollbackCurrentLexeme();
		}

		consumeIdentifier();
	}

	private boolean previousWasNoLiteralOrIdentifier()
	{
		var previous = previous();
		return previous != null && !(previous.kind().isLiteralOrConst() || previous.kind().isIdentifier());
	}

	private boolean hasSpaceBetweenThisAndLast()
	{
		var previous = previous();
		return previous == null || previous.totalEndOffset() != scanner.position();
	}

	private void consumeNewLine()
	{
		line++;
		scanner.advance();
		currentLineStartOffset = scanner.position();
	}

	public void relocateDiagnosticPosition(IPosition diagnosticPosition)
	{
		this.relocatedDiagnosticPosition = diagnosticPosition;
	}

	private void consumeAsteriskOrSystemVariable()
	{
		if (scanner.peek(1) == '*')
		{
			var prev = previous();
			if (prev == null || prev.kind() != SyntaxKind.MARK)
			{
				scanner.start();
				scanner.advance(2); // "**"
				createAndAdd(SyntaxKind.EXPONENT_OPERATOR);
				return;
			}
		}

		var lookahead = scanner.peek(1);
		switch (lookahead)
		{
			case 'a':
			case 'A':
			case 'c':
			case 'C':
			case 'd':
			case 'D':
			case 'e':
			case 'E':
			case 'g':
			case 'G':
			case 'h':
			case 'H':
			case 'i':
			case 'I':
			case 'l':
			case 'L':
			case 'm':
			case 'M':
			case 'n':
			case 'N':
			case 't':
			case 'T':
			case 'o':
			case 'O':
			case 'p':
			case 'P':
			case 's':
			case 'S':
			case 'u':
			case 'U':
			case 'w':
			case 'W':
				break;
			default:
				createAndAddCurrentSingleToken(SyntaxKind.ASTERISK);
				return;
		}

		scanner.start();
		scanner.advance();
		if (scanner.advanceIfIgnoreCase("OCCURRENCE"))
		{
			createAndAdd(SyntaxKind.OCCURRENCE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("OCC"))
		{
			createAndAdd(SyntaxKind.OCC);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATA"))
		{
			createAndAdd(SyntaxKind.SV_DATA);
			return;
		}
		if (scanner.advanceIfIgnoreCase("LEVEL"))
		{
			createAndAdd(SyntaxKind.SV_LEVEL);
			return;
		}
		if (scanner.advanceIfIgnoreCase("LINEX"))
		{
			createAndAdd(SyntaxKind.LINEX);
			return;
		}
		if (scanner.advanceIfIgnoreCase("LINE-COUNT"))
		{
			createAndAdd(SyntaxKind.LINE_COUNT);
			return;
		}
		if (scanner.advanceIfIgnoreCase("LINESIZE"))
		{
			createAndAdd(SyntaxKind.LINESIZE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("PAGESIZE"))
		{
			createAndAdd(SyntaxKind.PAGESIZE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("TRIM"))
		{
			createAndAdd(SyntaxKind.TRIM);
			return;
		}
		if (scanner.advanceIfIgnoreCase("ERROR-NR"))
		{
			createAndAdd(SyntaxKind.ERROR_NR);
			return;
		}
		if (scanner.advanceIfIgnoreCase("ERROR-LINE"))
		{
			createAndAdd(SyntaxKind.ERROR_LINE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("ERROR-TA"))
		{
			createAndAdd(SyntaxKind.ERROR_TA);
			return;
		}
		if (scanner.advanceIfIgnoreCase("ERROR"))
		{
			createAndAdd(SyntaxKind.SV_ERROR);
			return;
		}
		if (scanner.advanceIfIgnoreCase("LINE"))
		{
			createAndAdd(SyntaxKind.LINE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("TIMX"))
		{
			createAndAdd(SyntaxKind.TIMX);
			return;
		}
		if (scanner.advanceIfIgnoreCase("TIMD"))
		{
			createAndAdd(SyntaxKind.TIMD);
			return;
		}
		if (scanner.advanceIfIgnoreCase("TIMN"))
		{
			createAndAdd(SyntaxKind.TIMN);
			return;
		}
		if (scanner.advanceIfIgnoreCase("TIME-OUT"))
		{
			createAndAdd(SyntaxKind.TIME_OUT);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DAT4E"))
		{
			createAndAdd(SyntaxKind.DAT4E);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATE"))
		{
			createAndAdd(SyntaxKind.SV_DATE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATX"))
		{
			createAndAdd(SyntaxKind.DATX);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATN"))
		{
			createAndAdd(SyntaxKind.DATN);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATD"))
		{
			createAndAdd(SyntaxKind.DATD);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DAT4D"))
		{
			createAndAdd(SyntaxKind.DAT4D);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DAT4I"))
		{
			createAndAdd(SyntaxKind.DAT4I);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATI"))
		{
			createAndAdd(SyntaxKind.DATI);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATG"))
		{
			createAndAdd(SyntaxKind.DATG);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DAT4J"))
		{
			createAndAdd(SyntaxKind.DAT4J);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATJ"))
		{
			createAndAdd(SyntaxKind.DATJ);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DAT4U"))
		{
			createAndAdd(SyntaxKind.DAT4U);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATU"))
		{
			createAndAdd(SyntaxKind.DATU);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATVS"))
		{
			createAndAdd(SyntaxKind.DATVS);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DATV"))
		{
			createAndAdd(SyntaxKind.DATV);
			return;
		}
		if (scanner.advanceIfIgnoreCase("LANGUAGE"))
		{
			createAndAdd(SyntaxKind.LANGUAGE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("LIBRARY-ID"))
		{
			createAndAdd(SyntaxKind.LIBRARY_ID);
			return;
		}
		if (scanner.advanceIfIgnoreCase("ISN"))
		{
			createAndAdd(SyntaxKind.SV_ISN);
			return;
		}
		if (scanner.advanceIfIgnoreCase("PROGRAM"))
		{
			createAndAdd(SyntaxKind.PROGRAM);
			return;
		}
		if (scanner.advanceIfIgnoreCase("CPU-TIME"))
		{
			createAndAdd(SyntaxKind.CPU_TIME);
			return;
		}
		if (scanner.advanceIfIgnoreCase("ETID"))
		{
			createAndAdd(SyntaxKind.ETID);
			return;
		}
		if (scanner.advanceIfIgnoreCase("INIT-PROGRAM"))
		{
			createAndAdd(SyntaxKind.INIT_PROGRAM);
			return;
		}
		if (scanner.advanceIfIgnoreCase("LBOUND"))
		{
			createAndAdd(SyntaxKind.LBOUND);
			return;
		}
		if (scanner.advanceIfIgnoreCase("UBOUND"))
		{
			createAndAdd(SyntaxKind.UBOUND);
			return;
		}
		if (scanner.advanceIfIgnoreCase("GROUP"))
		{
			createAndAdd(SyntaxKind.SV_GROUP);
			return;
		}
		if (scanner.advanceIfIgnoreCase("USER-NAME"))
		{
			createAndAdd(SyntaxKind.USER_NAME);
			return;
		}
		if (scanner.advanceIfIgnoreCase("USER"))
		{
			createAndAdd(SyntaxKind.SV_USER);
			return;
		}
		if (scanner.advanceIfIgnoreCase("NUMBER"))
		{
			createAndAdd(SyntaxKind.SV_NUMBER);
			return;
		}
		if (scanner.advanceIfIgnoreCase("LENGTH"))
		{
			createAndAdd(SyntaxKind.SV_LENGTH);
			return;
		}
		if (scanner.advanceIfIgnoreCase("CURRENT-UNIT"))
		{
			createAndAdd(SyntaxKind.CURRENT_UNIT);
			return;
		}
		if (scanner.advanceIfIgnoreCase("CURSOR"))
		{
			createAndAdd(SyntaxKind.CURSOR);
			return;
		}
		if (scanner.advanceIfIgnoreCase("CURS-COL"))
		{
			createAndAdd(SyntaxKind.CURS_COL);
			return;
		}
		if (scanner.advanceIfIgnoreCase("CURS-LINE"))
		{
			createAndAdd(SyntaxKind.CURS_LINE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("CURS-FIELD"))
		{
			createAndAdd(SyntaxKind.CURS_FIELD);
			return;
		}
		if (scanner.advanceIfIgnoreCase("PARSE-COL"))
		{
			createAndAdd(SyntaxKind.PARSE_COL);
			return;
		}
		if (scanner.advanceIfIgnoreCase("PARSE-ROW"))
		{
			createAndAdd(SyntaxKind.PARSE_ROW);
			return;
		}
		if (scanner.advanceIfIgnoreCase("INIT-USER"))
		{
			createAndAdd(SyntaxKind.INIT_USER);
			return;
		}
		if (scanner.advanceIfIgnoreCase("INIT-ID"))
		{
			createAndAdd(SyntaxKind.INIT_ID);
			return;
		}
		if (scanner.advanceIfIgnoreCase("COUNTER"))
		{
			createAndAdd(SyntaxKind.COUNTER);
			return;
		}
		if (scanner.advanceIfIgnoreCase("COM"))
		{
			createAndAdd(SyntaxKind.COM);
			return;
		}
		if (scanner.advanceIfIgnoreCase("PF-KEY"))
		{
			createAndAdd(SyntaxKind.PF_KEY);
			return;
		}
		if (scanner.advanceIfIgnoreCase("MAXVAL"))
		{
			createAndAdd(SyntaxKind.MAXVAL);
			return;
		}
		if (scanner.advanceIfIgnoreCase("MINVAL"))
		{
			createAndAdd(SyntaxKind.MINVAL);
			return;
		}
		if (scanner.advanceIfIgnoreCase("DEVICE"))
		{
			createAndAdd(SyntaxKind.DEVICE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("OPSYS"))
		{
			createAndAdd(SyntaxKind.OPSYS);
			return;
		}
		if (scanner.advanceIfIgnoreCase("TPSYS"))
		{
			createAndAdd(SyntaxKind.TPSYS);
			return;
		}
		if (scanner.advanceIfIgnoreCase("TIMESTMP"))
		{
			createAndAdd(SyntaxKind.TIMESTMP);
			return;
		}
		if (scanner.advanceIfIgnoreCase("APPLIC-ID"))
		{
			createAndAdd(SyntaxKind.APPLIC_ID);
			return;
		}
		if (scanner.advanceIfIgnoreCase("APPLIC-NAME"))
		{
			createAndAdd(SyntaxKind.APPLIC_NAME);
			return;
		}
		if (scanner.advanceIfIgnoreCase("SERVER-TYPE"))
		{
			createAndAdd(SyntaxKind.SERVER_TYPE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("STARTUP"))
		{
			createAndAdd(SyntaxKind.STARTUP);
			return;
		}
		if (scanner.advanceIfIgnoreCase("STEPLIB"))
		{
			createAndAdd(SyntaxKind.STEPLIB);
			return;
		}
		if (scanner.advanceIfIgnoreCase("PAGE-NUMBER"))
		{
			createAndAdd(SyntaxKind.PAGE_NUMBER);
			return;
		}
		if (scanner.advanceIfIgnoreCase("WINDOW-PS"))
		{
			createAndAdd(SyntaxKind.WINDOW_PS);
			return;
		}
		if (scanner.advanceIfIgnoreCase("WINDOW-POS"))
		{
			createAndAdd(SyntaxKind.WINDOW_POS);
			return;
		}
		if (scanner.advanceIfIgnoreCase("WINDOW-LS"))
		{
			createAndAdd(SyntaxKind.WINDOW_LS);
			return;
		}
		if (scanner.advanceIfIgnoreCase("TRANSLATE"))
		{
			createAndAdd(SyntaxKind.TRANSLATE);
			return;
		}
		if (scanner.advanceIfIgnoreCase("PID"))
		{
			createAndAdd(SyntaxKind.PID);
			return;
		}
		if (scanner.advanceIfIgnoreCase("NET-USER"))
		{
			createAndAdd(SyntaxKind.NET_USER);
			return;
		}
		if (scanner.advanceIfIgnoreCase("HOSTNAME"))
		{
			createAndAdd(SyntaxKind.HOSTNAME);
			return;
		}
		if (scanner.advanceIfIgnoreCase("MACHINE-CLASS"))
		{
			createAndAdd(SyntaxKind.MACHINE_CLASS);
			return;
		}
		if (scanner.advanceIfIgnoreCase("TIME"))
		{
			createAndAdd(SyntaxKind.SV_TIME);
			return;
		}
		if (scanner.advanceIfIgnoreCase("SUBROUTINE"))
		{
			createAndAdd(SyntaxKind.SV_SUBROUTINE);
			return;
		}
		scanner.rollbackCurrentLexeme();
		createAndAddCurrentSingleToken(SyntaxKind.ASTERISK);
	}

	private boolean consumeNumberedLabel()
	{
		var previous = previous();
		if (previous != null && previous.kind() == SyntaxKind.IDENTIFIER)
		{
			return false;
		}

		var i = 1; // Skip '(', then check if next 4 are digits
		while (i < 5 && !scanner.willPassEnd(i) && Character.isDigit(scanner.peek(i)))
		{
			i++;
		}

		if (i == 5 && scanner.peek(i) == ')')
		{
			createAndAddCurrentSingleToken(SyntaxKind.LPAREN);
			scanner.start();
			scanner.advance(4);
			createAndAdd(SyntaxKind.LABEL_IDENTIFIER);
			createAndAddCurrentSingleToken(SyntaxKind.RPAREN);
			return true;
		}

		scanner.reset();
		return false;
	}

	private void consumeIdentifier()
	{
		scanner.start();
		if (scanner.peek() == '+')
		{
			scanner.advance();
		}

		var isQualified = false;

		while (!scanner.isAtEnd() && !isLineEnd() && isNoWhitespace() && isValidIdentifierCharacter(scanner.peek()))
		{
			if (scanner.peek() == '.')
			{
				if (!isValidIdentifierStart(scanner.peek(1)))
				{
					break;
				}
				isQualified = true;
			}

			if (scanner.peek() == '/' && scanner.peek(1) == '*')
			{
				// Slash is a valid character for identifiers, but an asterisk is not.
				// If a variable is named #MYVAR/* we can safely assume it's a variable followed
				// by a comment.
				break;
			}
			scanner.advance();
		}

		var text = scanner.lexemeText();
		if (text.startsWith("+"))
		{
			// Special case. Starting with + could be an AIV, but +123 is meant
			// arithmetically
			var onlyDigits = true;
			for (int i = 1; i < text.length(); i++)
			{
				if (!Character.isDigit(text.charAt(i)))
				{
					onlyDigits = false;
					break;
				}
			}

			if (onlyDigits)
			{
				scanner.rollbackCurrentLexeme();
				createAndAddCurrentSingleToken(SyntaxKind.PLUS);
				return;
			}
		}

		// Handling for C*, T*, P*
		var cStarAtStart = scanner.lexemeLength() == 1 && scanner.peek() == '*';
		var cStarQualified = isQualified && scanner.peek() == '*' && scanner.peek(-2) == '.';
		if (cStarAtStart || cStarQualified)
		{
			scanner.advance();
			while (!scanner.isAtEnd() && !isLineEnd() && isNoWhitespace()
				&& isValidIdentifierCharacter(scanner.peek()))
			{
				scanner.advance();
			}
		}

		if (scanner.peek() == '.' && (scanner.peek(1) == ')' || scanner.peek(1) == ' ' || (isInParens() && scanner.peek(1) == '/')))
		{
			scanner.advance();
			createAndAdd(SyntaxKind.LABEL_IDENTIFIER);
			return;
		}

		createAndAdd(SyntaxKind.IDENTIFIER);
	}

	private boolean isValidIdentifierStart(char character)
	{
		return Character.isAlphabetic(character)
			|| character == '&'
			|| character == '#'
			|| character == '+';
	}

	private boolean isValidIdentifierCharacter(char character)
	{
		return Character.isAlphabetic(character) || Character.isDigit(character) || character == '-' || character == '/'
			|| character == '@' || character == '$' || character == '&' || character == '#' || character == '.'
			|| character == '_';
	}

	private void consumeIdentifierOrKeyword()
	{
		if (isInParens() && scanner.peek(2) == '=')
		{
			var attributeLookahead = scanner.peekText(3).toUpperCase();
			var previous = previous();
			switch (attributeLookahead)
			{
				case "AD=" -> attributeDefinition();
				case "AL=" -> alphanumericLengthAttribute();
				case "CD=" -> colorDefinition();
				case "CV=" -> controlVariableAttribute();
				case "DF=" -> dateFormatAttribute();
				case "DL=" -> operandAttribute(SyntaxKind.DL);
				case "FL=" -> operandAttribute(SyntaxKind.FL);
				case "DY=" -> dynamicAttribute();
				case "ES=" -> emptyLineSuppression();
				case "EM=" -> editMask();
				case "HE=" -> helproutine();
				case "MC=" -> operandAttribute(SyntaxKind.MC);
				case "MS=" -> valueAttribute(SyntaxKind.MS);
				case "IP=" -> inputPromptAttribute();
				case "IS=" -> identicalSuppressAttribute();
				case "NL=" -> valueAttribute(SyntaxKind.NL);
				case "PC=" -> operandAttribute(SyntaxKind.PC);
				case "PM=" -> valueAttribute(SyntaxKind.PM);
				case "PS=" -> operandAttribute(SyntaxKind.PS);
				case "SB=" -> selectionBoxAttribute();
				case "SG=" -> signPosition();
				case "ZP=" -> zeroPrintingAttribute();
				case "LS=" -> lineSize();
			}

			if (previous() != previous) // check that we consumed something
			{
				return;
			}
		}

		if (isInParens() && tokens.size() > 2)
		{
			var prevLastToken = tokens.get(tokens.size() - 2).kind();

			if (prevLastToken == SyntaxKind.STRING_LITERAL
				&& (scanner.peekText("TU") ||
					scanner.peekText("NE") ||
					scanner.peekText("RE") ||
					scanner.peekText("YE") ||
					scanner.peekText("BL") ||
					scanner.peekText("GR") ||
					scanner.peekText("PI"))
				&& (scanner.peek(3) == ')' || isWhitespace(3)))
			{
				colorAttribute();
				return;
			}
		}

		var isQualified = false;
		SyntaxKind kindHint = null;
		var dashCount = 0;
		scanner.start();

		while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && isValidIdentifierCharacter(scanner.peek()))
		{
			var maybeAdabasSpecialIndexNotation = false;
			// Characters from which we can be sure that we're dealing with an identifier
			switch (scanner.peek())
			{
				case '.':
					if (!isValidIdentifierStart(scanner.peek(1)))
					{
						maybeAdabasSpecialIndexNotation = true;
						break;
					}
					isQualified = true;
				case '@':
				case '$':
				case '&':
				case '#':
					kindHint = SyntaxKind.IDENTIFIER;
					break;
				case '-':
					dashCount++;
					if (dashCount > 1)
					{
						// This might be removed when IDENTIFIER_OR_KEYWORD is gone
						kindHint = SyntaxKind.IDENTIFIER;
					}
					break;
			}

			if (maybeAdabasSpecialIndexNotation && Character.isDigit(scanner.peek(1)))
			{
				break;
			}

			if (isInParens() && scanner.peek(-1) == '.' && (scanner.peek() == '/' || scanner.peek() == ')'))
			{
				createAndAdd(SyntaxKind.LABEL_IDENTIFIER);
				if (scanner.peekText("/*")) // Otherwise it'll be confused with a comment
				{
					createAndAddCurrentSingleToken(SyntaxKind.SLASH);
					createAndAddCurrentSingleToken(SyntaxKind.ASTERISK);
				}
				return;
			}

			if (scanner.peek() == '/' && lexerMode == LexerMode.IN_DATA_TYPE)
			{
				// Slash is a valid character for identifiers, if we're lexing a datatype we can be pretty confident about the slash being for array dimensions
				break;
			}

			if (scanner.peek() == '/')
			{
				var possibleKeyword = KeywordTable.getKeyword(scanner.lexemeText());
				var asteriskFollows = scanner.peek(1) == '*';
				if (possibleKeyword != null && asteriskFollows)
				{
					// We just assume that something like END-SUBROUTINE/* is meant as END-SUBROUTINE plus comment
					createAndAdd(possibleKeyword);
					return;
				}

				kindHint = SyntaxKind.IDENTIFIER;

				if (asteriskFollows && tokens.get(tokens.size() - 1).kind() == SyntaxKind.INCLUDE)
				{
					// The slash belongs to a comment, and we aren't parsing an array definition.
					// TODO(lexermode): This should no longer be needed when the array definition is
					// handled by a parser mode.
					break;
				}
			}

			scanner.advance();
		}

		if ((scanner.peek() == ',' || scanner.peek() == '.') && !isValidIdentifierCharacter(scanner.peek(1))
			&& !isWhitespace(1))
		{
			// TODO(lexermode): This is only needed because the Define Data Parser relies on
			// DataFormats to be identifiers currently.
			// With a fitting lexer mode we can build this better.
			var somethingAsideOfCommaOrDotConsumed = false;
			while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && Character.isDigit(scanner.peek()))
			{
				somethingAsideOfCommaOrDotConsumed = true;
				scanner.advance();
			}
			if (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && scanner.peek() == '.'
				|| scanner.peek() == ',')
			{
				scanner.advance();
			}
			while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && Character.isDigit(scanner.peek()))
			{
				somethingAsideOfCommaOrDotConsumed = true;
				scanner.advance();
			}
			if (mightBeDataFormat(scanner.lexemeText()))
			{
				kindHint = SyntaxKind.IDENTIFIER;
			}

			if (!somethingAsideOfCommaOrDotConsumed)
			{
				scanner.advance(-1); // If we didn't find anything that we need, roll back the ./,
			}
		}

		if (scanner.peek(-1) == '.')
		{
			kindHint = SyntaxKind.LABEL_IDENTIFIER;
		}

		// Handling for C*, T*, P*
		var cStarAtStart = scanner.lexemeLength() == 1 && scanner.peek() == '*';
		var cStarQualified = isQualified && scanner.peek() == '*' && scanner.peek(-2) == '.';
		if (cStarAtStart || cStarQualified)
		{
			scanner.advance();
			while (!scanner.isAtEnd() && !isLineEnd() && isNoWhitespace()
				&& isValidIdentifierCharacter(scanner.peek()))
			{
				scanner.advance();
			}
			kindHint = SyntaxKind.IDENTIFIER;
		}

		if (kindHint != null)
		{
			createAndAdd(kindHint);
			return;
		}

		var lexeme = scanner.lexemeText();
		var kind = KeywordTable.getKeyword(lexeme);
		createAndAdd(Objects.requireNonNullElse(kind, SyntaxKind.IDENTIFIER));
	}

	private void controlVariableAttribute()
	{
		scanner.start();
		scanner.advance(3); // CV=
		// we intentionally don't consume more, because the variable should be IDENTIFIER
		createAndAdd(SyntaxKind.CV);
	}

	private void editMask()
	{
		scanner.start();
		scanner.advance(3); // EM=
		var isInString = false;
		var nestedParens = 0;
		while (!scanner.isAtEnd() && !(scanner.peek() == ')' && nestedParens == 0) || isInString)
		{
			if (!isInString && scanner.peek() == '(')
			{
				nestedParens++;
			}

			if (!isInString && scanner.peek() == ')')
			{
				nestedParens--;
			}

			if (scanner.peek() == '\'')
			{
				isInString = !isInString;
			}

			if (isWhitespace(0) && !isInString)
			{
				break;
			}

			scanner.advance();
		}

		createAndAdd(SyntaxKind.EM);
	}

	private void helproutine()
	{
		scanner.start();
		scanner.advance(3); // HE=
		var isInString = false;
		while (!scanner.isAtEnd() && !scanner.peekText(")") || isInString)
		{
			if (scanner.peek() == '\'' || scanner.peek() == '"')
			{
				isInString = !isInString;
			}

			if (isWhitespace(0) && !isInString)
			{
				break;
			}

			scanner.advance();
		}

		createAndAdd(SyntaxKind.HE);
	}

	private void attributeDefinition()
	{
		scanner.start();
		scanner.advance(3); // AD=
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		// This handles an empty string for empty filler chars, e.g.
		// (AD=ODL' ')
		if (scanner.peek() == ' ' && scanner.peek(-1) == '\'' && scanner.peek(1) == '\'')
		{
			scanner.advance();
			scanner.advance();
		}

		createAndAdd(SyntaxKind.AD);
	}

	private void valueAttribute(SyntaxKind kind)
	{
		scanner.start();
		scanner.advance(kind.name().length() + 1); // e.g. "DL" + "="
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(kind);
	}

	private void operandAttribute(SyntaxKind kind)
	{
		scanner.start();
		scanner.advance(kind.name().length() + 1); // e.g. "DL" + "="
		createAndAdd(kind);
	}

	private void alphanumericLengthAttribute()
	{
		scanner.start();
		scanner.advance(3); // AL=
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.AL);
	}

	private void zeroPrintingAttribute()
	{
		scanner.start();
		scanner.advance(3); // ZP=
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.ZP);
	}

	private void dateFormatAttribute()
	{
		scanner.start();
		scanner.advance(3); // DF=
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.DF);
	}

	private void inputPromptAttribute()
	{
		scanner.start();
		scanner.advance(3); // IP=
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.IP);
	}

	private void identicalSuppressAttribute()
	{
		scanner.start();
		scanner.advance(3); // IS=
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.IS);
	}

	private void lineSize()
	{
		scanner.start();
		scanner.advance(3); // LS=
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.NL);
	}

	private void selectionBoxAttribute()
	{
		scanner.start();
		scanner.advance(3); // SB=
		// we intentionally don't consume more, because the variables should be a list of IDENTIFIERs
		createAndAdd(SyntaxKind.SB);
	}

	private void signPosition()
	{
		scanner.start();
		scanner.advance(3); // SG=
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.SG);
	}

	private void dynamicAttribute()
	{
		scanner.start();
		scanner.advance(3); // DY=

		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.DY);
	}

	private void emptyLineSuppression()
	{
		scanner.start();
		scanner.advance(3); // ES=

		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.ES);
	}

	private void colorDefinition()
	{
		scanner.start();
		scanner.advance(3); // CD=
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.CD);
	}

	private void colorAttribute()
	{
		scanner.start();
		scanner.advance(2); // YE, NE, TU etc
		while (!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.COLOR_ATTRIBUTE);
	}

	private boolean isNoWhitespace()
	{
		return !isWhitespace(0);
	}

	private boolean isWhitespace(int offset)
	{
		return scanner.peek(offset) == ' ' || scanner.peek(offset) == '\t' || scanner.peek(offset) == '\r'
			|| scanner.peek(offset) == '\n';
	}

	private boolean isAtLineStart()
	{
		return scanner.position() - currentLineStartOffset == 0;
	}

	private boolean mightBeDataFormat(String possibleDataFormat)
	{
		var chars = possibleDataFormat.toCharArray();
		if (!Character.isLetter(chars[0]))
		{
			return false;
		}

		var floatingPointCount = 0;
		for (var i = 1; i < chars.length; i++)
		{
			char c = chars[i];
			if (floatingPointCount > 1)
			{
				return false;
			}

			if (c == '.' || c == ',')
			{
				floatingPointCount++;
				continue;
			}

			if (!Character.isDigit(c))
			{
				return false;
			}
		}

		return floatingPointCount < 2;
	}

	private boolean consumeNaturalHeader()
	{
		if (!(isAtLineStart() && (isSingleAsteriskComment() || isInlineComment())))
		{
			return false;
		}

		scanner.start();
		while (!isLineEnd() && !scanner.isAtEnd())
		{
			scanner.advance();
		}
		String s = scanner.lexemeText().stripTrailing();

		if (inSourceHeader)
		{
			if (s.contains("* <Natural Source Header"))
			{
				sourceHeader = new NaturalHeader(programmingMode, lineIncrement);
				sourceHeaderDone = true;
			}
			if (s.contains("* :Mode"))
			{
				programmingMode = NaturalProgrammingMode.fromString(s.substring(s.length() - 1));
			}
			if (s.contains("* :LineIncrement"))
			{
				s = s.replaceAll("\\D", "");
				lineIncrement = (Integer.parseInt(s));
			}
		}
		else
		{
			inSourceHeader = s.contains("* >Natural Source Header");
		}
		if (!inSourceHeader)
			scanner.rollbackCurrentLexeme();

		return inSourceHeader;
	}

	private boolean consumeComment()
	{
		var isInlineComment = isInlineComment();

		if (isInlineComment && tokens.size() > 2 && lexerMode == LexerMode.IN_DEFINE_DATA)
		{
			// special case like (A5/*) which we might solve naively this way.
			// (A5/*) is a shortcut for (A5/1:*)
			var lastToken = tokens.get(tokens.size() - 1);
			var prevLastToken = tokens.get(tokens.size() - 2);
			if (lastToken.kind() == SyntaxKind.IDENTIFIER && prevLastToken.kind() == SyntaxKind.LPAREN)
			{
				return false;
			}
		}

		if (isSingleAsteriskComment() || isInlineComment)
		{
			scanner.start();
			while (!isLineEnd() && !scanner.isAtEnd())
			{
				scanner.advance();
			}

			var token = SyntaxTokenFactory.create(
				SyntaxKind.COMMENT,
				scanner.lexemeStart(),
				getOffsetInLine(),
				line,
				scanner.lexemeText(),
				filePath
			);
			comments.add(token);
			scanner.reset();

			return true;
		}
		return false;
	}

	private boolean isSingleAsteriskComment()
	{
		var lookahead = scanner.peek(1);
		return isAtLineStart()
			&& scanner.peek() == '*'
			&&
			(lookahead == ' '
				|| lookahead == '*'
				|| lookahead == '\t'
				|| lookahead == '\n'
				|| lookahead == '\r'
				|| lookahead == '/'
				|| lookahead == SourceTextScanner.END_CHARACTER);
	}

	private boolean isInlineComment()
	{
		return scanner.peek() == '/' && scanner.peek(1) == '*';
	}

	private void createAndAddCurrentSingleToken(SyntaxKind kind)
	{
		scanner.start();
		scanner.advance();
		createAndAdd(kind);
	}

	private void createAndAddFollowupEquals(SyntaxKind withoutFollowup, SyntaxKind withFollowup)
	{
		scanner.start();
		scanner.advance();
		if (scanner.peek() == '=')
		{
			scanner.advance();
			createAndAdd(withFollowup);
		}
		else
		{
			createAndAdd(withoutFollowup);
		}
	}

	private void consumeNumber()
	{
		scanner.start();
		while (Character.isDigit(scanner.peek())
			|| scanner.peek() == '.'
			|| (scanner.peek() == ',' && !(isInParens() && tokenBeforeLParenWas(SyntaxKind.IDENTIFIER))) // added to disambiguate between array access #ARR(1,1,1) and floating point numbers
		)
		{
			if (scanner.peek() == ',' && !Character.isDigit(scanner.peek(1)))
			{
				break;
			}

			var prev = previous();
			if (scanner.peek() == ',' && prev != null && prev.kind() == SyntaxKind.COLON)
			{
				// Case for (1:5,2:5) which are two dimensions and not a floating number
				break;
			}

			scanner.advance();
		}

		if (scanner.peek() == 'X' || scanner.peek() == 'x')
		{
			scanner.advance();
			createAndAdd(SyntaxKind.OPERAND_SKIP);
			return;
		}

		if (scanner.peek() == 'T')
		{
			scanner.advance();
			createAndAdd(SyntaxKind.TAB_SETTING);
			return;
		}

		if (scanner.peek() == 'E')
		{
			scanner.advance(); // E
			scanner.advance(); // + or -
			while (Character.isDigit(scanner.peek()))
			{
				scanner.advance();
			}
		}

		createAndAdd(SyntaxKind.NUMBER_LITERAL);
	}

	private boolean tokenBeforeLParenWas(SyntaxKind kind)
	{
		return isInParens() && lastBeforeOpenParens != null && lastBeforeOpenParens.kind() == kind;
	}

	private void consumeDateLiteral()
	{
		scanner.start();
		scanner.advance(2); // D'

		if (!consumeStringToEnd(SyntaxKind.DATE_LITERAL))
		{
			return;
		}

		createAndAdd(SyntaxKind.DATE_LITERAL);
		checkStringLiteralLength(previousUnsafe());
	}

	private void consumeExtendedTimeLiteral()
	{
		scanner.start();
		scanner.advance(2); // E'

		if (!consumeStringToEnd(SyntaxKind.EXTENDED_TIME_LITERAL))
		{
			return;
		}

		createAndAdd(SyntaxKind.EXTENDED_TIME_LITERAL);
		checkStringLiteralLength(previousUnsafe());
	}

	private void consumeTimeLiteral()
	{
		scanner.start();
		scanner.advance(2); // T'

		if (!consumeStringToEnd(SyntaxKind.TIME_LITERAL))
		{
			return;
		}

		createAndAdd(SyntaxKind.TIME_LITERAL);
		checkStringLiteralLength(previousUnsafe());
	}

	private void consumeHexLiteral()
	{
		scanner.start();
		scanner.advance(2); // H and '

		if (!consumeStringToEnd(SyntaxKind.HEX_LITERAL))
		{
			return;
		}

		createAndAdd(SyntaxKind.HEX_LITERAL);
		checkStringLiteralLength(previousUnsafe());
		var hexLiteralChars = previousUnsafe().source().length() - 3; // - H''
		if (hexLiteralChars % 2 != 0)
		{
			addDiagnostic("Invalid HEX literal. Number of characters must be even but was %d.".formatted(hexLiteralChars), "Literal defined here", LexerError.UNKNOWN_CHARACTER);
		}
	}

	private boolean consumeStringToEnd(SyntaxKind kindToCreate)
	{
		while (scanner.peek() != '\'' && !scanner.isAtEnd() && !isLineEnd())
		{
			scanner.advance();
		}

		if (scanner.peek() != '\'')
		{
			// Recovery
			while (!isLineEnd() && !scanner.isAtEnd())
			{
				scanner.advance();
			}

			addDiagnostic("Unterminated String literal, expecting closing [']", "Literal declared here", LexerError.UNTERMINATED_STRING);

			// We can still produce a valid token, although it is unterminated
			createAndAdd(kindToCreate);
			checkStringLiteralLength(previousUnsafe());
			return false;
		}

		scanner.advance(); // closing '
		return true;
	}

	private void consumeString(char c)
	{
		scanner.start();
		scanner.advance();
		while (!scanner.isAtEnd() && !isLineEnd())
		{
			if (scanner.peek() == c && scanner.peek(1) == c)
			{
				// escaped ' or "
				scanner.advance();
				scanner.advance();
				continue;
			}

			if (scanner.peek() == c)
			{
				break; // closing character will be consumed later
			}

			scanner.advance();
		}

		if (scanner.peek() != c)
		{
			// Recovery
			while (!isLineEnd() && !scanner.isAtEnd())
			{
				scanner.advance();
			}

			addDiagnostic(
				"Unterminated String literal, expecting closing [%c]".formatted(c),
				"Literal declared here",
				LexerError.UNTERMINATED_STRING
			);

			// We can still produce a valid token, although it is unterminated
			createAndAdd(SyntaxKind.STRING_LITERAL);
			return;
		}

		// The current character is the terminating string literal (' or "), therefore
		// it needs to be consumed
		// to be included.
		scanner.advance();
		createAndAdd(SyntaxKind.STRING_LITERAL);
		checkStringLiteralLength(previousUnsafe());
	}

	private void createAndAdd(SyntaxKind kind)
	{
		var token = SyntaxTokenFactory.create(
			kind,
			scanner.lexemeStart(),
			getOffsetInLine(),
			line,
			scanner.lexemeText(),
			filePath
		);
		addToken(token);
	}

	private SyntaxToken previous()
	{
		if (tokens.isEmpty())
		{
			return null;
		}
		return tokens.get(tokens.size() - 1);
	}

	/**
	 * Returns the previous consumed token. <strong>Does not do a boundary check</strong>
	 */
	private SyntaxToken previousUnsafe()
	{
		return previousUnsafe(1);
	}

	/**
	 * Returns the previous consumed token at the given relative offset. <strong>Does not do a boundary check</strong>
	 */
	private SyntaxToken previousUnsafe(int offset)
	{
		return tokens.get(tokens.size() - offset);
	}

	private int getOffsetInLine()
	{
		if (scanner.lexemeStart() == -1)
		{
			return scanner.position() - currentLineStartOffset;
		}

		return scanner.lexemeStart() - currentLineStartOffset;
	}

	private boolean isLineEnd()
	{
		return scanner.peek() == '\n' || scanner.peek() == '\r' && scanner.peek(1) == '\n';
	}

	private boolean tryCreateIfFollowedBy(char followup, SyntaxKind kind)
	{
		if (scanner.peek(1) == followup)
		{
			scanner.start();
			scanner.advance(2);
			createAndAdd(kind);
			return true;
		}
		return false;
	}

	private void addDiagnostic(String message, String additionalMessage, LexerError error)
	{
		if (relocatedDiagnosticPosition != null)
		{
			var diagnostic = LexerDiagnostic.create(
				message,
				relocatedDiagnosticPosition.offset(),
				relocatedDiagnosticPosition.offsetInLine(),
				relocatedDiagnosticPosition.line(),
				relocatedDiagnosticPosition.length(),
				relocatedDiagnosticPosition.filePath(),
				error
			);

			diagnostic.addAdditionalInfo(
				new AdditionalDiagnosticInfo(
					additionalMessage,
					new PlainPosition(
						scanner.lexemeStart(),
						getOffsetInLine(),
						line,
						scanner.lexemeLength(),
						filePath
					)
				)
			);

			diagnostics.add(diagnostic);
		}
		else
		{
			diagnostics.add(
				LexerDiagnostic.create(
					message,
					scanner.lexemeStart(),
					getOffsetInLine(),
					line,
					scanner.lexemeLength(),
					filePath,
					error
				)
			);
		}
	}

	private void addDiagnostic(String message, String additionalMessage, LexerError error, SyntaxToken where)
	{
		if (relocatedDiagnosticPosition != null)
		{
			var diagnostic = LexerDiagnostic.create(
				message,
				relocatedDiagnosticPosition.offset(),
				relocatedDiagnosticPosition.offsetInLine(),
				relocatedDiagnosticPosition.line(),
				relocatedDiagnosticPosition.length(),
				relocatedDiagnosticPosition.filePath(),
				error
			);

			diagnostic.addAdditionalInfo(
				new AdditionalDiagnosticInfo(
					additionalMessage,
					new PlainPosition(
						where.offset(),
						where.offsetInLine(),
						where.line(),
						where.length(),
						where.filePath()
					)
				)
			);

			diagnostics.add(diagnostic);
		}
		else
		{
			diagnostics.add(
				LexerDiagnostic.create(
					message,
					where.offset(),
					where.offsetInLine(),
					where.line(),
					where.length(),
					where.filePath(),
					error
				)
			);
		}
	}

	private void addToken(SyntaxToken token)
	{
		if (token.kind() == SyntaxKind.IDENTIFIER)
		{
			if (token.source().endsWith("."))
			{
				addDiagnostic("Identifiers can not end with '.'", "Identifier defined here", LexerError.INVALID_IDENTIFIER);
			}
		}

		var previous = previous();
		if (token.kind() == SyntaxKind.DATA && previous != null && previous.kind() == SyntaxKind.DEFINE)
		{
			lexerMode = LexerMode.IN_DEFINE_DATA;
		}
		else
			if (token.kind() == SyntaxKind.END_DEFINE && lexerMode == LexerMode.IN_DEFINE_DATA)
			{
				lexerMode = LexerMode.DEFAULT;
			}

		token.setDiagnosticPosition(relocatedDiagnosticPosition);
		tokens.add(token);
		scanner.reset();
	}

	private void checkStringLiteralLength(SyntaxToken token)
	{
		if (token.stringValue().isEmpty())
		{
			addDiagnostic(
				"String literals in Natural can't be empty. Add a blank.",
				"Literal is used here",
				LexerError.INVALID_STRING_LENGTH,
				token
			);
		}
	}

	private boolean isValidAivStartAfterPlus(char character)
	{
		// Every identifier name is allowed after the AIV plus, except for numbers
		return isValidIdentifierCharacter(character) && !Character.isDigit(character);
	}

	private boolean isInParens()
	{
		return parensLevel > 0;
	}
}
