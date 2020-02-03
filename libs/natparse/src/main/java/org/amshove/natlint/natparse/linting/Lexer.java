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

			// Temporary
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
}
