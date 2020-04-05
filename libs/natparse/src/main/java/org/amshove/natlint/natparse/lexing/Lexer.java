package org.amshove.natlint.natparse.linting;

import org.amshove.natlint.natparse.linting.text.SourceTextScanner;

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
			if (consumeWhitespace() || consumeNewLine())
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
					createAndAddWithPotentialFollowup(SyntaxKind.COLON, '=', SyntaxKind.COLON_EQUALS);
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
					createAndAddWithPotentialFollowup(SyntaxKind.GREATER, '=', SyntaxKind.GREATER_EQUALS);
					continue;
				case '<':
					if (!tryCreateIfFollowedBy('=', SyntaxKind.LESSER_EQUALS)
						&& !tryCreateIfFollowedBy('>', SyntaxKind.LESSER_GREATER))
					{
						createAndAddCurrentSingleToken(SyntaxKind.LESSER);
					}
					continue;

				case 'G':
				case 'g':
				{
					char next = scanner.peek(1);
					switch (next)
					{
						case 'E':
						case 'e':
							scanner.start();
							scanner.advance(2);
							createAndAdd(SyntaxKind.GE);
							continue;
						case 'T':
						case 't':
							scanner.start();
							scanner.advance(2);
							createAndAdd(SyntaxKind.GT);
							continue;
					}
				}

				case 'L':
				case 'l':
				{
					char next = scanner.peek(1);
					switch (next)
					{
						case 'E':
						case 'e':
							scanner.start();
							scanner.advance(2);
							createAndAdd(SyntaxKind.LE);
							continue;
						case 'T':
						case 't':
							scanner.start();
							scanner.advance(2);
							createAndAdd(SyntaxKind.LT);
							continue;
					}
				}

				case 'N':
				case 'n':
				{
					char next = scanner.peek(1);
					switch (next)
					{
						case 'E':
						case 'e':
							scanner.start();
							scanner.advance(2);
							createAndAdd(SyntaxKind.NE);
							continue;
					}
				}

				case 'E':
				case 'e':
				{
					char next = scanner.peek(1);
					switch (next)
					{
						case 'Q':
						case 'q':
							scanner.start();
							scanner.advance(2);
							createAndAdd(SyntaxKind.EQ);
							continue;
					}
				}

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

	private void createAndAddCurrentSingleToken(SyntaxKind kind)
	{
		scanner.start();
		scanner.advance();
		createAndAdd(kind);
	}

	private void createAndAddWithPotentialFollowup(SyntaxKind withoutFollowup, char followUpCharacter, SyntaxKind withFollowup)
	{
		scanner.start();
		scanner.advance();
		if (scanner.peek() == followUpCharacter)
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
