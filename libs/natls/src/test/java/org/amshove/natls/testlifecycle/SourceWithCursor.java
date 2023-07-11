package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * Contains Natural source code and a cursor position which will be passed to LSP methods.
 */
public record SourceWithCursor(String source, Range cursorPosition)
{
	private static final String CURSOR_START = "${";
	private static final String CURSOR_END = "}$";

	/**
	 * Constructs a {@link SourceWithCursor} from a single source string containing the cursor or selection denoted by
	 * ${}$. <br/>
	 * Examples:<br/>
	 * - DEFINE DA${TA}$: Means TA is selected<br/>
	 * - DEFINE DA${}$TA: Means the cursor is between A and T<br/>
	 */
	public static SourceWithCursor fromSourceWithCursor(String annotatedSource)
	{
		var resultingSource = new StringBuilder();
		annotatedSource = annotatedSource.replaceAll("\\r\\n?", "\n"); // Test files have linux line ending in the repository. Java text blocks use the source line endings, not platform
		var lines = annotatedSource.split("\\n");
		Range cursorPosition = null;
		for (int i = 0, linesLength = lines.length; i < linesLength; i++)
		{
			if (i != 0)
			{
				resultingSource.append("\n");
			}

			String line = lines[i];
			var startIndex = line.indexOf(CURSOR_START);
			var endIndex = line.indexOf(CURSOR_END);
			if (startIndex != -1)
			{
				if (cursorPosition != null || line.lastIndexOf(CURSOR_START) != startIndex)
				{
					throw new SourceWithCursorExtractionException("Multiple cursor start positions found");
				}
				cursorPosition = new Range();
				cursorPosition.setStart(new Position(i, startIndex));

				if (endIndex != -1)
				{
					if (line.lastIndexOf(CURSOR_END) != endIndex)
					{
						throw new SourceWithCursorExtractionException("Multiple cursor end positions found");
					}

					resultingSource
						.append(line, 0, startIndex);
					resultingSource
						.append(line, startIndex + CURSOR_START.length(), endIndex);
					resultingSource
						.append(line.substring(endIndex + CURSOR_END.length()));

					cursorPosition.setEnd(new Position(i, endIndex - CURSOR_END.length()));
				}
				else
				{
					resultingSource
						.append(line.substring(startIndex + CURSOR_START.length()));
				}

			}
			else
				if (endIndex != -1)
				{
					if (cursorPosition == null)
					{
						throw new SourceWithCursorExtractionException("End position of cursor encountered before encountering start position");
					}

					if (cursorPosition.getEnd() != null || line.lastIndexOf(CURSOR_END) != endIndex)
					{
						throw new SourceWithCursorExtractionException("Multiple cursor end positions found");
					}

					resultingSource
						.append(line, 0, endIndex)
						.append(line.substring(endIndex + CURSOR_END.length()));

					cursorPosition.setEnd(new Position(i, endIndex));
				}
				else
				{
					resultingSource.append(line);
				}
		}

		if (cursorPosition == null)
		{
			throw new SourceWithCursorExtractionException("No cursor position found");
		}

		if (cursorPosition.getEnd() == null)
		{
			throw new SourceWithCursorExtractionException("No end position of cursor found");
		}

		return new SourceWithCursor(resultingSource.toString(), cursorPosition);
	}

	/**
	 * Returns the Position of the beginning of the cursor.
	 */
	public Position toSinglePosition()
	{
		return new Position(cursorPosition.getStart().getLine(), cursorPosition.getStart().getCharacter());
	}
}
