package org.amshove.natparse.lexing.text;

import java.util.List;

public class SourceTextScanner
{
	public static final char END_CHARACTER = Character.MAX_VALUE;

	private final char[] source;
	private int currentOffset;
	private int currentLexemeStart;

	public SourceTextScanner(String source)
	{
		this(source, List.of());
	}

	public SourceTextScanner(String source, List<String> copyCodeSubstitution)
	{
		if (!copyCodeSubstitution.isEmpty())
		{
			var newSource = source;
			for (int i = 0; i < copyCodeSubstitution.size(); i++)
			{
				newSource = newSource.replace("&%d&".formatted(i + 1), copyCodeSubstitution.get(i));
			}
			this.source = newSource.toCharArray();
		}
		else
		{
			this.source = source.toCharArray();
		}
		currentOffset = 0;
		reset();
	}

	/**
	 * Returns the current position within the source text.
	 *
	 * @return offset from the start
	 */
	public int position()
	{
		return currentOffset;
	}

	/**
	 * Start parsing a lexeme.
	 */
	public void start()
	{
		currentLexemeStart = currentOffset;
	}

	/**
	 * Advance the current position by one without doing boundary checks.
	 */
	public void advance()
	{
		currentOffset++;
	}

	/**
	 * Advance the position by the given offset without doing boundary checks.
	 *
	 * @param offset the amount of characters to advance over
	 */
	public void advance(int offset)
	{
		currentOffset += offset;
	}

	public char peek()
	{
		if (isAtEnd())
		{
			return END_CHARACTER;
		}
		return source[currentOffset];
	}

	public char peek(int offset)
	{
		if (willPassEnd(offset))
		{
			return END_CHARACTER;
		}
		return source[currentOffset + offset];
	}

	/**
	 * Advances the position if the given text is matched. If not matched it will remain on the original position.
	 *
	 * @param expectedText the text to be matched
	 * @return true if matched, otherwise false
	 */
	public boolean advanceIf(String expectedText)
	{
		var expectedLength = expectedText.length();

		for (var i = 0; i < expectedLength; i++)
		{
			if (peek(i) != expectedText.charAt(i))
			{
				return false;
			}
		}

		advance(expectedLength);
		return true;
	}

	/**
	 * Advances the position if the given text is matched. If not matched it will remain on the original position.<br/>
	 *
	 * Comparison will be made case-insensitive.
	 *
	 * @param expectedText the text to be matched
	 * @return true if matched, otherwise false
	 */
	public boolean advanceIfIgnoreCase(String expectedText)
	{
		var expectedLength = expectedText.length();

		for (var i = 0; i < expectedLength; i++)
		{
			// Could be a problem with non ASCII letters
			if (Character.toLowerCase(peek(i)) != Character.toLowerCase(expectedText.charAt(i)))
			{
				return false;
			}
		}

		advance(expectedLength);
		return true;
	}

	public int lexemeStart()
	{
		return currentLexemeStart;
	}

	public int lexemeLength()
	{
		return currentOffset - currentLexemeStart;
	}

	public String lexemeText()
	{
		return StringPool.intern(new String(source, currentLexemeStart, currentOffset - currentLexemeStart));
	}

	public void rollbackCurrentLexeme()
	{
		if (currentLexemeStart == -1)
		{
			throw new UnsupportedOperationException("Can't reset offset if currentLexeme wasn't started");
		}
		currentOffset = currentLexemeStart;
		reset();
	}

	public void reset()
	{
		currentLexemeStart = -1;
	}

	public boolean isAtEnd()
	{
		return currentOffset >= source.length;
	}

	private boolean willPassEnd(int offset)
	{
		return currentOffset + offset >= source.length;
	}

	public boolean peekText(String text)
	{
		for (var i = 0; i < text.length(); i++)
		{
			var expected = text.charAt(i);
			var gotten = source[currentOffset + i];
			if (willPassEnd(i) || expected != gotten)
			{
				return false;
			}
		}

		return true;
	}

	public String peekText(int length)
	{
		if (willPassEnd(length))
		{
			return null;
		}

		return new String(source, currentOffset, length);
	}
}
