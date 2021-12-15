package org.amshove.natparse.lexing;

import org.amshove.natparse.lexing.text.SourceTextScanner;

import java.util.ArrayList;
import java.util.List;

public class Lexer
{
	private SourceTextScanner scanner;
	private List<SyntaxToken> tokens;
	private List<SyntaxToken> comments;
	private int line;
	private int currentLineStartOffset;

	private List<LexerDiagnostic> diagnostics;

	public TokenList lex(String source)
	{
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
					createAndAddCurrentSingleToken(SyntaxKind.LPAREN);
					continue;
				case ')':
					createAndAddCurrentSingleToken(SyntaxKind.RPAREN);
					continue;
				case '[':
					createAndAddCurrentSingleToken(SyntaxKind.LBRACKET);
					continue;
				case ']':
					createAndAddCurrentSingleToken(SyntaxKind.RBRACKET);
					continue;
				case '=':
					createAndAddCurrentSingleToken(SyntaxKind.EQUALS);
					continue;
				case ':':
					createAndAddFollowupEquals(SyntaxKind.COLON, SyntaxKind.COLON_EQUALS);
					continue;
				case '+':
					if (isValidIdentifierCharacter(scanner.peek(1)))
					{
						consumeIdentifier();
					}
					else
					{
						createAndAddCurrentSingleToken(SyntaxKind.PLUS);
					}
					continue;
				case '-':
					consumeMinusOrNumber();
					continue;
				case '*':
					consumeAsteriskOrFunction();
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
					createAndAddFollowupEquals(SyntaxKind.GREATER, SyntaxKind.GREATER_EQUALS);
					continue;
				case '<':
					if (tryCreateIfFollowedBy('=', SyntaxKind.LESSER_EQUALS)
						|| tryCreateIfFollowedBy('>', SyntaxKind.LESSER_GREATER))
					{
						continue;
					}
					createAndAddCurrentSingleToken(SyntaxKind.LESSER);
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
				case 'h':
				case 'H':
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
						LexerError.UNKNOWN_CHARACTER));
					scanner.advance();
			}
		}
		return TokenList.fromTokensAndDiagnostics(tokens, diagnostics, comments);
	}

	private void consumeMinusOrNumber()
	{
		scanner.start();
		scanner.advance(); // the minus
		if (Character.isDigit(scanner.peek()))
		{
			while (Character.isDigit(scanner.peek()) || scanner.peek() == ',' || scanner.peek() == '.')
			{
				scanner.advance();
			}
			createAndAdd(SyntaxKind.NUMBER);
		}
		else
		{
			createAndAdd(SyntaxKind.MINUS);
		}
	}

	private void consumeAsteriskOrFunction()
	{
		var lookahead = scanner.peek(1);
		if (lookahead != 'T' && lookahead != 'D' && lookahead != 'L')
		{
			createAndAddCurrentSingleToken(SyntaxKind.ASTERISK);
			return;
		}

		scanner.start();
		scanner.advance();
		if (scanner.advanceIf("TIMX"))
		{
			createAndAdd(SyntaxKind.TIMX);
		}
		if (scanner.advanceIf("DATX"))
		{
			createAndAdd(SyntaxKind.DATX);
		}
		if (scanner.advanceIf("DATN"))
		{
			createAndAdd(SyntaxKind.DATN);
		}
		if (scanner.advanceIf("LANGUAGE"))
		{
			createAndAdd(SyntaxKind.LANGUAGE);
		}
	}

	private void consumeIdentifier()
	{
		scanner.start();
		while (!scanner.isAtEnd() && !isLineEnd() && isNoWhitespace() && isValidIdentifierCharacter(scanner.peek()))
		{
			scanner.advance();
		}
		createAndAdd(SyntaxKind.IDENTIFIER);
	}

	private boolean isValidIdentifierCharacter(char character)
	{
		return Character.isAlphabetic(character) || Character.isDigit(character) || character == '-' || character == '/' || character == '@' || character == '$' || character == '&' || character == '#' || character == '+' || character == '.' || character == '_';
	}

	private void consumeIdentifierOrKeyword()
	{
		SyntaxKind kindHint = null;
		scanner.start();
		while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && isValidIdentifierCharacter(scanner.peek()))
		{

			// Characters from which we can be sure that we're dealing with an identifier
			switch (scanner.peek())
			{
				case '/':
				case '@':
				case '$':
				case '&':
				case '#':
				case '+':
				case '.':
					kindHint = SyntaxKind.IDENTIFIER;
			}

			scanner.advance();
		}

		if (scanner.peek() == ',' || scanner.peek() == '.')
		{
			// TODO(lexermode): This is only needed because the Define Data Parser relies on DataFormats to be identifiers currently.
			//		With a fitting lexer mode we can build this better.
			while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && Character.isDigit(scanner.peek()))
			{
				scanner.advance();
			}
			if (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && scanner.peek() == '.' || scanner.peek() == ',')
			{
				scanner.advance();
			}
			while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && Character.isDigit(scanner.peek()))
			{
				scanner.advance();
			}
			if (mightBeDataFormat(scanner.lexemeText()))
			{
				kindHint = SyntaxKind.IDENTIFIER;
			}
		}

		// Handling for C* count variables
		if (scanner.lexemeLength() == 1 && scanner.peek() == '*')
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
			// WITH_CTE is the only Keyword that contains an underscore, if we're
			// this far, and it contains an underscore, it's an identifier
			if (lexeme.contains("_"))
			{
				createAndAdd(SyntaxKind.IDENTIFIER);
			}
			else
			{
				createAndAdd(SyntaxKind.IDENTIFIER_OR_KEYWORD);
			}
		}
	}

	private boolean isNoWhitespace()
	{
		return scanner.peek() != ' ' && scanner.peek() != '\t';
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
				scanner.lexemeText());
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
			scanner.advance();
		}
		createAndAdd(SyntaxKind.NUMBER);
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
			createAndAdd(SyntaxKind.STRING);
			return;
		}

		// The current character is the terminating string literal (' or "), therefore it needs to be consumed
		// to be included.
		scanner.advance();
		createAndAdd(SyntaxKind.STRING);
	}

	private void createAndAdd(SyntaxKind kind)
	{
		var token = SyntaxTokenFactory.create(kind,
			scanner.lexemeStart(),
			getOffsetInLine(),
			line,
			scanner.lexemeText());
		tokens.add(token);
		scanner.reset();
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
		diagnostics.add(LexerDiagnostic.create(
			message,
			scanner.lexemeStart(),
			getOffsetInLine(),
			line,
			scanner.lexemeLength(),
			error));
	}
}
