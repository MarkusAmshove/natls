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

	// NOTE: Only for debugging purposes.
	private List<Character> unknownCharacters;

	public List<Character> getUnknownCharacters()
	{
		return unknownCharacters;
	}

	public List<SyntaxToken> lex(String source)
	{
		tokens = new ArrayList<>();
		unknownCharacters = new ArrayList<>();
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
					unknownCharacters.add(scanner.peek());
					scanner.advance();
			}
		}
		return tokens;
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

	private void consumeIdentifierOrKeyword()
	{
		SyntaxKind kindHint = null;
		scanner.start();
		while (!isLineEnd() && isNoWhitespace() && !scanner.isAtEnd())
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

		String lexeme = scanner.lexemeText();

		if (kindHint != null)
		{
			createAndAdd(kindHint);
			return;
		}

		SyntaxKind kind = KeywordTable.getKeyword(lexeme);
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
