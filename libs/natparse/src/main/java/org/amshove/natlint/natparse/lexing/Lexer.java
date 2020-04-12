package org.amshove.natlint.natparse.lexing;

import org.amshove.natlint.natparse.lexing.text.SourceTextScanner;

import java.util.ArrayList;
import java.util.List;

public class Lexer
{
	private SourceTextScanner scanner;
	private List<SyntaxToken> tokens;
	private int line;
	private int currentLineStartOffset;

	public List<SyntaxToken> lex(String source)
	{
		tokens = new ArrayList<>();
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
				case '=':
					createAndAddCurrentSingleToken(SyntaxKind.EQUALS);
					continue;
				case ':':
					createAndAddFollowupEquals(SyntaxKind.COLON, SyntaxKind.COLON_EQUALS);
					continue;
				case '+':
					createAndAddCurrentSingleToken(SyntaxKind.PLUS);
					continue;
				case '-':
					createAndAddCurrentSingleToken(SyntaxKind.MINUS);
					continue;
				case '*':
					createAndAddCurrentSingleToken(SyntaxKind.ASTERISK);
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

				case '\'':
					consumeString('\'');
					continue;
				case '"':
					consumeString('"');
					continue;

				case 'e':
				case 'E':
				case 'g':
				case 'G':
				case 'l':
				case 'L':
				case 'n':
				case 'N':
				case 'a':
				case 'A':
				case 'b':
				case 'B':
				case 'c':
				case 'C':
				case 'd':
				case 'D':
				case 'f':
				case 'F':
				case 'h':
				case 'H':
				case 'i':
				case 'I':
				case 'j':
				case 'J':
				case 'k':
				case 'K':
				case 'm':
				case 'M':
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
					//case '+':
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
					break;
			}

			// Temporary
			if (scanner.peek() == SourceTextScanner.END_CHARACTER)
			{
				break;
			}
			scanner.start();
			while (scanner.peek() != '!' && scanner.peek() != '$')
			{
				scanner.advance();
			}
			createAndAdd(SyntaxKind.DUMMY);
			scanner.advance();
		}
		return tokens;
	}

	private void consumeIdentifier()
	{
		scanner.start();
		while (!isLineEnd() && !isNoWhitespace() && !scanner.isAtEnd())
		{
			scanner.advance();
		}
		createAndAdd(SyntaxKind.IDENTIFIER);
	}

	private void consumeIdentifierOrKeyword()
	{

		// NOTE: Could it be an identifier if it contains a dot? If it is a qualified identifier, it might be
		// 100% not a keyword
		scanner.start();
		while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd())
		{
			scanner.advance();
		}

		String lexeme = scanner.lexemeText();
		SyntaxKind kind = KeywordTable.getKeyword(lexeme);
		if (kind != null)
		{
			createAndAdd(kind);
		}
		else
		{
			createAndAdd(SyntaxKind.IDENTIFIER_OR_KEYWORD);
		}
	}

	private boolean isNoWhitespace()
	{
		return scanner.peek() != ' ' && scanner.peek() != '\t';
	}

	private boolean consumeComment()
	{
		boolean isSingleAsteriskComment = scanner.position() - currentLineStartOffset == 0 && scanner.peek() == '*';
		boolean isInlineComment = scanner.peek() == '/' && scanner.peek(1) == '*';

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

	private boolean tryCreateBooleanOperatorKeyword()
	{
		char current = scanner.peek();
		char next = scanner.peek(1);
		String both = "" + current + next;
		scanner.start();
		scanner.advance(2);

		if (both.equalsIgnoreCase("eq"))
		{
			createAndAdd(SyntaxKind.EQ);
			return true;
		}

		if (both.equalsIgnoreCase("ge"))
		{
			createAndAdd(SyntaxKind.GE);
			return true;
		}

		if (both.equalsIgnoreCase("gt"))
		{
			createAndAdd(SyntaxKind.GT);
			return true;
		}

		if (both.equalsIgnoreCase("le"))
		{
			createAndAdd(SyntaxKind.LE);
			return true;
		}

		if (both.equalsIgnoreCase("lt"))
		{
			createAndAdd(SyntaxKind.LT);
			return true;
		}

		if (both.equalsIgnoreCase("ne"))
		{
			createAndAdd(SyntaxKind.NE);
			return true;
		}

		scanner.rollbackCurrentLexeme();
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
		while (scanner.peek() != c && !scanner.isAtEnd())
		{
			scanner.advance();
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
		SyntaxToken token = SyntaxTokenFactory.create(kind,
			scanner.lexemeStart(),
			getOffsetInLine(),
			line,
			scanner.lexemeText());
		tokens.add(token);
		scanner.reset();
	}

	private int getOffsetInLine()
	{
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
}
