package org.amshove.natparse.lexing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.text.SourceTextScanner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Lexer
{
	private SourceTextScanner scanner;
	private List<SyntaxToken> tokens;
	private List<SyntaxToken> comments;
	private int line;
	private int currentLineStartOffset;
	private Path filePath;
	private IPosition relocatedDiagnosticPosition;

	private boolean inParens;

	private List<LexerDiagnostic> diagnostics;

	public TokenList lex(String source, Path filePath)
	{
		this.filePath = filePath;
		tokens = new ArrayList<>();
		diagnostics = new ArrayList<>();
		comments = new ArrayList<>();
		scanner = new SourceTextScanner(source);
		line = 0;
		currentLineStartOffset = 0;

		while (!scanner.isAtEnd() && scanner.peek() != '$')
		{
			if (consumeComment())
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
					line++;
					scanner.advance();
					currentLineStartOffset = scanner.position();
					continue;

				case '(':
					inParens = true;
					createAndAddCurrentSingleToken(SyntaxKind.LPAREN);
					continue;
				case ')':
					inParens = false;
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
					if (isValidAivStartAfterPlus(scanner.peek(1)))
					{
						consumeIdentifier();
					}
					else
					{
						createAndAddCurrentSingleToken(SyntaxKind.PLUS);
					}
					continue;
				case '-':
					consumeMinusOrNumberOrStringConcat();
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
					createAndAddCurrentSingleToken(SyntaxKind.CARET);
					continue;

				case '%':
					createAndAddCurrentSingleToken(SyntaxKind.PERCENT);
					continue;

				case '?':
					createAndAddCurrentSingleToken(SyntaxKind.QUESTIONMARK);
					continue;

				case 'h':
				case 'H':
					if (scanner.peek(1) == '\'')
					{
						consumeHexString();
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
				case 'd':
				case 'D':
				case 'e':
				case 'E':
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
				case 't':
				case 'T':
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
					consumeIdentifierOrKeyword();
					continue;

				case '#':
				case '&':
					consumeIdentifier();
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

				default:
					diagnostics.add(LexerDiagnostic.create(
						"Unknown character [%c]".formatted(scanner.peek()),
						scanner.position(),
						getOffsetInLine(),
						line,
						1,
						filePath,
						LexerError.UNKNOWN_CHARACTER));
					scanner.advance();
			}
		}
		return TokenList.fromTokensAndDiagnostics(filePath, tokens, diagnostics, comments);
	}

	private void consumeMinusOrNumberOrStringConcat()
	{
		var lookaheadIndex = findNextNonWhitespaceLookaheadOffset();
		var lookahead = scanner.peek(lookaheadIndex);
		var previousToken = previous();
		var isStringConcatenation = previousToken != null && previousToken.kind() == SyntaxKind.STRING_LITERAL && (lookahead == '\'' || lookahead == '"');
		if (isStringConcatenation)
		{
			var previousString = previousUnsafe();
			var previousStringIndex = tokens.size() - 1;
			scanner.advance(lookaheadIndex);
			consumeString(lookahead);
			var currentString = previousUnsafe();
			var currentStringIndex = tokens.size() - 1;
			if (currentStringIndex >= previousStringIndex)
			{
				tokens.subList(previousStringIndex, currentStringIndex + 1).clear();
			}
			addToken(SyntaxTokenFactory.create(
				SyntaxKind.STRING_LITERAL,
				previousString.offset(),
				previousString.offsetInLine(),
				previousString.line(),
				"'" + previousString.stringValue() + currentString.stringValue() + "'",
				filePath
			));
			return;
		}

		scanner.start();
		scanner.advance(); // the minus
		if (Character.isDigit(scanner.peek()))
		{
			while (Character.isDigit(scanner.peek()) || scanner.peek() == ',' || scanner.peek() == '.')
			{
				scanner.advance();
			}
			if(scanner.peek() == 'E')
			{
				scanner.advance(); // E
				scanner.advance(); // + or -
				while(Character.isDigit(scanner.peek()))
				{
					scanner.advance();
				}
			}
			createAndAdd(SyntaxKind.NUMBER_LITERAL);
		}
		else
		{
			createAndAdd(SyntaxKind.MINUS);
		}
	}

	private void consumeAsteriskOrSystemVariable()
	{
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
			case 'i':
			case 'I':
			case 'l':
			case 'L':
			case 'm':
			case 'M':
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
		if (scanner.advanceIf("OCCURRENCE"))
		{
			createAndAdd(SyntaxKind.OCCURRENCE);
			return;
		}
		if (scanner.advanceIf("OCC"))
		{
			createAndAdd(SyntaxKind.OCC);
			return;
		}
		if (scanner.advanceIf("LINEX"))
		{
			createAndAdd(SyntaxKind.LINEX);
			return;
		}
		if (scanner.advanceIf("TRIM"))
		{
			createAndAdd(SyntaxKind.TRIM);
			return;
		}
		if (scanner.advanceIf("ERROR-NR"))
		{
			createAndAdd(SyntaxKind.ERROR_NR);
			return;
		}
		if (scanner.advanceIf("ERROR-LINE"))
		{
			createAndAdd(SyntaxKind.ERROR_LINE);
			return;
		}
		if (scanner.advanceIf("LINE"))
		{
			createAndAdd(SyntaxKind.LINE);
			return;
		}
		if (scanner.advanceIf("TIMX"))
		{
			createAndAdd(SyntaxKind.TIMX);
			return;
		}
		if (scanner.advanceIf("TIMN"))
		{
			createAndAdd(SyntaxKind.TIMN);
			return;
		}
		if (scanner.advanceIf("DATX"))
		{
			createAndAdd(SyntaxKind.DATX);
			return;
		}
		if (scanner.advanceIf("DATN"))
		{
			createAndAdd(SyntaxKind.DATN);
			return;
		}
		if (scanner.advanceIf("DATD"))
		{
			createAndAdd(SyntaxKind.DATD);
			return;
		}
		if (scanner.advanceIf("LANGUAGE"))
		{
			createAndAdd(SyntaxKind.LANGUAGE);
			return;
		}
		if (scanner.advanceIf("LIBRARY-ID"))
		{
			createAndAdd(SyntaxKind.LIBRARY_ID);
			return;
		}
		if (scanner.advanceIf("PROGRAM"))
		{
			createAndAdd(SyntaxKind.PROGRAM);
			return;
		}
		if (scanner.advanceIf("USER"))
		{
			createAndAdd(SyntaxKind.USER);
			return;
		}
		if (scanner.advanceIf("CURRENT-UNIT"))
		{
			createAndAdd(SyntaxKind.CURRENT_UNIT);
			return;
		}
		if (scanner.advanceIf("CURS-LINE"))
		{
			createAndAdd(SyntaxKind.CURS_LINE);
			return;
		}
		if (scanner.advanceIf("ERROR-TA"))
		{
			createAndAdd(SyntaxKind.ERROR_TA);
			return;
		}
		if (scanner.advanceIf("INIT-USER"))
		{
			createAndAdd(SyntaxKind.INIT_USER);
			return;
		}
		if (scanner.advanceIf("INIT-ID"))
		{
			createAndAdd(SyntaxKind.INIT_ID);
			return;
		}
		if (scanner.advanceIf("COUNTER"))
		{
			createAndAdd(SyntaxKind.COUNTER);
			return;
		}
		if (scanner.advanceIf("COM"))
		{
			createAndAdd(SyntaxKind.COM);
			return;
		}
		if (scanner.advanceIf("PF-KEY"))
		{
			createAndAdd(SyntaxKind.PF_KEY);
			return;
		}
		if (scanner.advanceIf("MAXVAL"))
		{
			createAndAdd(SyntaxKind.MAXVAL);
			return;
		}
		if (scanner.advanceIf("MINVAL"))
		{
			createAndAdd(SyntaxKind.MINVAL);
			return;
		}
		if (scanner.advanceIf("DEVICE"))
		{
			createAndAdd(SyntaxKind.DEVICE);
			return;
		}
		if (scanner.advanceIf("OPSYS"))
		{
			createAndAdd(SyntaxKind.OPSYS);
			return;
		}
		if (scanner.advanceIf("TPSYS"))
		{
			createAndAdd(SyntaxKind.TPSYS);
			return;
		}
		if (scanner.advanceIf("APPLIC-ID"))
		{
			createAndAdd(SyntaxKind.APPLIC_ID);
			return;
		}
		if (scanner.advanceIf("STARTUP"))
		{
			createAndAdd(SyntaxKind.STARTUP);
			return;
		}
		if (scanner.advanceIf("STEPLIB"))
		{
			createAndAdd(SyntaxKind.STEPLIB);
			return;
		}
		if (scanner.advanceIf("PAGE-NUMBER"))
		{
			createAndAdd(SyntaxKind.PAGE_NUMBER);
			return;
		}
		if (scanner.advanceIf("WINDOW-PS"))
		{
			createAndAdd(SyntaxKind.WINDOW_PS);
			return;
		}
		scanner.rollbackCurrentLexeme();
		createAndAddCurrentSingleToken(SyntaxKind.ASTERISK);
	}

	private void consumeIdentifier()
	{
		scanner.start();
		if(scanner.peek() == '+')
		{
			scanner.advance();
		}

		var isQualified = false;

		while (!scanner.isAtEnd() && !isLineEnd() && isNoWhitespace() && isValidIdentifierCharacter(scanner.peek()))
		{
			if(scanner.peek() == '.')
			{
				isQualified = true;
			}

			if(scanner.peek() == '/' && scanner.peek(1) == '*')
			{
				// Slash is a valid character for identifiers, but an asterisk is not.
				// If a variable is named #MYVAR/* we can safely assume its a variable followed
				// by a comment.
				break;
			}
			scanner.advance();
		}

		var text = scanner.lexemeText();
		if(text.startsWith("+"))
		{
			// Special case. Starting with + could be an AIV, but +123 is meant arithmetically
			var onlyDigits = true;
			for (int i = 1; i < text.length(); i++)
			{
				if(!Character.isDigit(text.charAt(i)))
				{
					onlyDigits = false;
					break;
				}
			}

			if(onlyDigits)
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
			while (!scanner.isAtEnd() && !isLineEnd() && isNoWhitespace() && isValidIdentifierCharacter(scanner.peek()))
			{
				scanner.advance();
			}
		}

		if(scanner.peek(-1) == '.')
		{
			createAndAdd(SyntaxKind.LABEL_IDENTIFIER);
		}
		else
		{
			createAndAdd(SyntaxKind.IDENTIFIER);
		}
	}

	private boolean isValidIdentifierCharacter(char character)
	{
		return Character.isAlphabetic(character) || Character.isDigit(character) || character == '-' || character == '/' || character == '@' || character == '$' || character == '&' || character == '#' || character == '.' || character == '_';
	}

	private void consumeIdentifierOrKeyword()
	{
		if(inParens && scanner.peekText("EM="))
		{
			editorMask();
			return;
		}

		if(inParens && scanner.peekText("AD="))
		{
			attributeDefinition();
			return;
		}

		if(inParens && scanner.peekText("CD="))
		{
			colorDefinition();
			return;
		}

		var isQualified = false;
		SyntaxKind kindHint = null;
		scanner.start();

		if (scanner.advanceIf("PF") && Character.isDigit(scanner.peek()))
		{
			while(!scanner.isAtEnd() && Character.isDigit(scanner.peek()))
			{
				scanner.advance();
			}
			createAndAdd(SyntaxKind.PF);
			return;
		}

		var dashCount = 0;
		while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && isValidIdentifierCharacter(scanner.peek()))
		{

			// Characters from which we can be sure that we're dealing with an identifier
			switch (scanner.peek())
			{
				case '.':
					isQualified = true;
				case '@':
				case '$':
				case '&':
				case '#':
					kindHint = SyntaxKind.IDENTIFIER;
					break;
				case '-':
					dashCount++;
					if (dashCount >1)
					{
						// This might be removed when IDENTIFIER_OR_KEYWORD is gone
						kindHint = SyntaxKind.IDENTIFIER;
					}
					break;
			}

			if(scanner.peek() == '/')
			{
				kindHint = SyntaxKind.IDENTIFIER;

				if(scanner.peek(1) == '*' && tokens.get(tokens.size() - 1).kind() == SyntaxKind.INCLUDE)
				{
					// The slash belongs to a comment, and we aren't parsing an array definition.
					// TODO(lexermode): This should no longer be needed when the array definition is handled by a parser mode.
					break;
				}
			}

			scanner.advance();
		}

		if ((scanner.peek() == ',' || scanner.peek() == '.') && !isValidIdentifierCharacter(scanner.peek(1)) && !isWhitespace(1))
		{
			// TODO(lexermode): This is only needed because the Define Data Parser relies on DataFormats to be identifiers currently.
			//		With a fitting lexer mode we can build this better.
			var somethingAsideOfCommaOrDotConsumed = false;
			while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && Character.isDigit(scanner.peek()))
			{
				somethingAsideOfCommaOrDotConsumed = true;
				scanner.advance();
			}
			if (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && scanner.peek() == '.' || scanner.peek() == ',')
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

			if(!somethingAsideOfCommaOrDotConsumed)
			{
				scanner.advance(-1); // If we didn't find anything that we need, roll back the ./,
			}
		}

		if(scanner.peek(-1) == '.')
		{
			kindHint = SyntaxKind.LABEL_IDENTIFIER;
		}

		// Handling for C*, T*, P*
		var cStarAtStart = scanner.lexemeLength() == 1 && scanner.peek() == '*';
		var cStarQualified = isQualified && scanner.peek() == '*' && scanner.peek(-2) == '.';
		if (cStarAtStart || cStarQualified)
		{
			scanner.advance();
			while (!scanner.isAtEnd() && !isLineEnd() && isNoWhitespace() && isValidIdentifierCharacter(scanner.peek()))
			{
				scanner.advance();
			}
			kindHint = SyntaxKind.IDENTIFIER;
		}

		var lexeme = scanner.lexemeText();

		if (kindHint != null)
		{
			createAndAdd(kindHint);
			return;
		}

		var kind = KeywordTable.getKeyword(lexeme);
		if (kind != null)
		{
			createAndAdd(kind);
		}
		else
		{
			createAndAdd(SyntaxKind.IDENTIFIER);
		}
	}

	private void editorMask()
	{
		scanner.start();
		scanner.advance(3); // EM=
		var isInString = false;
		while(!scanner.isAtEnd() && scanner.peek() != ')')
		{
			if(scanner.peek() == '\'' || scanner.peek() == '"')
			{
				isInString = !isInString;
			}

			if(isWhitespace(0) && !isInString)
			{
				break;
			}

			scanner.advance();
		}

		createAndAdd(SyntaxKind.EM);
	}

	private void attributeDefinition()
	{
		scanner.start();
		scanner.advance(3); // AD=
		while(!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.AD);
	}

	private void colorDefinition()
	{
		scanner.start();
		scanner.advance(3); // CD=
		while(!scanner.isAtEnd() && isNoWhitespace() && scanner.peek() != ')')
		{
			scanner.advance();
		}

		createAndAdd(SyntaxKind.CD);
	}

	private boolean isNoWhitespace()
	{
		return !isWhitespace(0);
	}

	private boolean isWhitespace(int offset)
	{
		return scanner.peek(offset) == ' ' || scanner.peek(offset) == '\t' || scanner.peek(offset) == '\r' || scanner.peek(offset) == '\n';
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

	private boolean consumeComment()
	{
		var lookahead = scanner.peek(1);
		var isSingleAsteriskComment = isAtLineStart()
			&& scanner.peek() == '*'
			&&
			(
				lookahead == ' '
					|| lookahead == '*'
					|| lookahead == '\t'
					|| lookahead == '\n'
					|| lookahead == '\r'
					|| lookahead == '/'
					|| lookahead == SourceTextScanner.END_CHARACTER
			);
		var isInlineComment = scanner.peek() == '/' && lookahead == '*';

		if (isInlineComment && tokens.size() > 2)
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

		if (isSingleAsteriskComment || isInlineComment)
		{
			scanner.start();
			while (!isLineEnd() && !scanner.isAtEnd())
			{
				scanner.advance();
			}

			var token = SyntaxTokenFactory.create(SyntaxKind.COMMENT,
				scanner.lexemeStart(),
				getOffsetInLine(),
				line,
				scanner.lexemeText(),
				filePath);
			comments.add(token);
			scanner.reset();

			return true;
		}
		return false;
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
		while (Character.isDigit(scanner.peek()) || scanner.peek() == ',' || scanner.peek() == '.')
		{
			if(scanner.peek() == ',' && !Character.isDigit(scanner.peek(1)))
			{
				break;
			}
			scanner.advance();
		}

		if(scanner.peek() == 'X' || scanner.peek() == 'x')
		{
			scanner.advance();
			createAndAdd(SyntaxKind.OPERAND_SKIP);
			return;
		}

		if(scanner.peek() == 'T')
		{
			scanner.advance();
			createAndAdd(SyntaxKind.TAB_SETTING);
			return;
		}

		if(scanner.peek() == 'E')
		{
			scanner.advance(); // E
			scanner.advance(); // + or -
			while(Character.isDigit(scanner.peek()))
			{
				scanner.advance();
			}
		}

		createAndAdd(SyntaxKind.NUMBER_LITERAL);
	}

	private void consumeHexString()
	{
		scanner.start();
		scanner.advance(2); // H and '
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

			addDiagnostic("Unterminated String literal, expecting closing [']", LexerError.UNTERMINATED_STRING);

			// We can still produce a valid token, although it is unterminated
			createAndAdd(SyntaxKind.STRING_LITERAL);
			return;
		}

		// We don't evaluate the content. Is it worth it? We could convert it to the actual characters.

		scanner.advance();
		createAndAdd(SyntaxKind.STRING_LITERAL);
	}

	private void consumeString(char c)
	{
		scanner.start();
		scanner.advance();
		while (scanner.peek() != c && !scanner.isAtEnd() && !isLineEnd())
		{
			scanner.advance();
		}

		if (scanner.peek() != c)
		{
			// Recovery
			while (!isLineEnd() && !scanner.isAtEnd())
			{
				scanner.advance();
			}

			addDiagnostic("Unterminated String literal, expecting closing [%c]".formatted(c), LexerError.UNTERMINATED_STRING);

			// We can still produce a valid token, although it is unterminated
			createAndAdd(SyntaxKind.STRING_LITERAL);
			return;
		}

		// The current character is the terminating string literal (' or "), therefore it needs to be consumed
		// to be included.
		scanner.advance();
		createAndAdd(SyntaxKind.STRING_LITERAL);
	}

	private void createAndAdd(SyntaxKind kind)
	{
		var token = SyntaxTokenFactory.create(kind,
			scanner.lexemeStart(),
			getOffsetInLine(),
			line,
			scanner.lexemeText(),
			filePath);
		addToken(token);
	}

	private SyntaxToken previous()
	{
		if(tokens.isEmpty())
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
		return tokens.get(tokens.size() - 1);
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

	private void addDiagnostic(String message, LexerError error)
	{
		if(relocatedDiagnosticPosition != null)
		{
			diagnostics.add(LexerDiagnostic.create(
				message,
				scanner.lexemeStart(),
				getOffsetInLine(),
				line,
				scanner.lexemeLength(),
				filePath,
				relocatedDiagnosticPosition,
				error));
		}
		else
		{
			diagnostics.add(LexerDiagnostic.create(
				message,
				scanner.lexemeStart(),
				getOffsetInLine(),
				line,
				scanner.lexemeLength(),
				filePath,
				error));
		}
	}

	private int findNextNonWhitespaceLookaheadOffset()
	{
		var start = 1;
		while (!scanner.isAtEnd() && isWhitespace(start))
		{
			start++;
		}

		return start;
	}

	private void addToken(SyntaxToken token)
	{
		if(token.kind() == SyntaxKind.IDENTIFIER)
		{
			if(token.source().endsWith("."))
			{
				addDiagnostic("Identifiers can not end with '.'", LexerError.INVALID_IDENTIFIER);
			}
		}

		token.setDiagnosticPosition(relocatedDiagnosticPosition);
		tokens.add(token);
		scanner.reset();
	}

	public void relocateDiagnosticPosition(IPosition diagnosticPosition)
	{
		this.relocatedDiagnosticPosition = diagnosticPosition;
	}

	private boolean isValidAivStartAfterPlus(char character)
	{
		// Every identifier name is allowed after the AIV plus, except for numbers
		return isValidIdentifierCharacter(character) && !Character.isDigit(character);
	}
}
