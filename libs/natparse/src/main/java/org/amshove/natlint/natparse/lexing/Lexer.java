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

				case 'e':
				case 'E':
				case 'g':
				case 'G':
				case 'l':
				case 'L':
				case 'n':
				case 'N':
				{
					if (tryCreateBooleanOperatorKeyword())
					{
						break;
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
