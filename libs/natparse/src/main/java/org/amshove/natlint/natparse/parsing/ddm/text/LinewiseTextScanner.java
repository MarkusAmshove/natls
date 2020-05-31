package org.amshove.natlint.natparse.parsing.ddm.text;

public class LinewiseTextScanner
{

	private final String[] lines;
	private int currentIndex = 0;

	public LinewiseTextScanner(String[] lines)
	{
		if (lines.length == 0)
		{
			throw new IllegalArgumentException("Lines cannot be empty");
		}

		this.lines = lines;
	}

	/**
	 * Peeks the current line
	 *
	 * @return the current line or null when at end
	 */
	public String peek()
	{
		if (currentIndex >= lines.length)
		{
			return null;
		}

		return lines[currentIndex];
	}

	/**
	 * Peeks the line at the given offset
	 *
	 * @param offset the offset to look at
	 * @return the line at the given offset or null when at end
	 */
	public String peek(int offset)
	{
		if (currentIndex + offset >= lines.length)
		{
			return null;
		}

		return lines[currentIndex + offset];
	}

	public void advance()
	{
		currentIndex++;
	}

	public boolean isAtEnd()
	{
		return currentIndex >= lines.length;
	}
}
