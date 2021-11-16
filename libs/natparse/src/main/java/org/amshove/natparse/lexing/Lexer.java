package org.amshove.natparse.lexing;

import org.amshove.natparse.lexing.text.SourceTextScanner;

import java.util.ArrayList;
import java.util.List;

public class Lexer
{
	private SourceTextScanner scanner;
	private List<SyntaxToken> tokens;
	private int line;
	private int currentLineStartOffset;

	private List<LexerDiagnostic> diagnostics;

	public TokenList lex(String source)
	{
		tokens = new ArrayList<>();
		diagnostics = new ArrayList<>();
		scanner = new SourceTextScanner(source);
		line = 0;
		currentLineStartOffset = 0;

		while (!scanner.isAtEnd() && scanner.peek() != '$')
		{
			if (consumeWhitespace() || consumeNewLine() || consumeComment())
			{
				continue;
			}

			switch (scanner.peek())
			{
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
					if (Character.isAlphabetic(scanner.peek(1)))
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
		return TokenList.fromTokensAndDiagnostics(tokens, diagnostics);
	}

	private void consumeAsteriskOrFunction()
	{
		var lookahead = scanner.peek(1);
		if(lookahead != 'T' && lookahead != 'D')
		{
			createAndAddCurrentSingleToken(SyntaxKind.ASTERISK);
			return;
		}

		scanner.start();
		scanner.advance();
		if(scanner.advanceIf("TIMX"))
		{
			createAndAdd(SyntaxKind.TIMX);
		}
		if(scanner.advanceIf("DATX"))
		{
			createAndAdd(SyntaxKind.DATX);
		}
	}

	private void consumeIdentifier()
	{
		scanner.start();
		while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd())
		{
			scanner.advance();
		}
		createAndAdd(SyntaxKind.IDENTIFIER);
	}

	private boolean isValidIdentifierCharacter()
	{
		var character = scanner.peek();
		return Character.isAlphabetic(character) || Character.isDigit(character) || character == '-' || character == '/' || character == '@' || character == '$' || character == '&' || character == '#' || character == '+' || character == '.' || character == '_';
	}

	private void consumeIdentifierOrKeyword()
	{
		SyntaxKind kindHint = null;
		scanner.start();
		while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd() && isValidIdentifierCharacter())
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
				case '.': // Qualified Variable
					kindHint = SyntaxKind.IDENTIFIER;
			}

			scanner.advance();
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

	private boolean consumeComment()
	{
		var lookahead = scanner.peek(1);
		var isSingleAsteriskComment = isAtLineStart() && scanner.peek() == '*' && (lookahead == ' ' || lookahead == '\t');
		var isInlineComment = scanner.peek() == '/' && lookahead == '*';

		if (isSingleAsteriskComment || isInlineComment)
		{
			scanner.start();
			while (!isLineEnd() && !scanner.isAtEnd())
			{
				scanner.advance();
			}
			createAndAdd(SyntaxKind.COMMENT);
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

	private boolean consumeWhitespace()
	{
		return consumeMultiple(' ', SyntaxKind.WHITESPACE) || consumeMultiple('\t', SyntaxKind.TAB);
	}

	private boolean consumeNewLine()
	{
		if (isLineEnd())
		{
			scanner.start();
			if (scanner.peek() == '\n')
			{
				scanner.advance();
				createAndAdd(SyntaxKind.NEW_LINE);
				currentLineStartOffset = scanner.position();
				line++;
				return true;
			}
			if (scanner.advanceIf("\r\n"))
			{
				createAndAdd(SyntaxKind.NEW_LINE);
				currentLineStartOffset = scanner.position();
				line++;
				return true;
			}
		}
		return false;
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

	private boolean consumeMultiple(char c, SyntaxKind kind)
	{
		if (scanner.peek() == c)
		{
			scanner.start();
			scanner.advance();
			while (scanner.peek() == c)
			{
				scanner.advance();
			}
			createAndAdd(kind);
			return true;
		}
		return false;
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
		if(scanner.lexemeStart() ==  -1)
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
