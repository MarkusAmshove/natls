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
	 * Constructs a {@link SourceWithCursor} from a single source string containing the cursor
	 * or selection denoted by ${}$. <br/>
	 * Examples:<br/>
	 * - DEFINE DA${TA}$: Means TA is selected<br/>
	 * - DEFINE DA${}$TA: Means the cursor is between A and T<br/>
	 */
	public static SourceWithCursor fromSourceWithCursor(String annotatedSource)
	{
		var resultingSource = new StringBuilder();
		var lines = annotatedSource.split("[\\r\\n]+");
		Range cursorPosition = null; // TODO: Currently no multiline selection is handled
		for (int i = 0, linesLength = lines.length; i < linesLength; i++)
		{
			String line = lines[i];
			var startIndex = line.indexOf(CURSOR_START);
			if (startIndex != -1)
			{
				if (cursorPosition != null)
				{
					throw new RuntimeException("Multiple cursor positions found!");
				}

				var endIndex = line.indexOf(CURSOR_END);
				if (endIndex == -1)
				{
					throw new RuntimeException("No end of cursor found in current line. Multiline selection is not handled yet.");
				}

				resultingSource
					.append(line, 0, startIndex);
				resultingSource
					.append(line.substring(endIndex + CURSOR_END.length()))
					.append("\n");
				cursorPosition = new Range(
					new Position(i, startIndex),
					new Position(i, endIndex)
				);
			}
			else
			{
				resultingSource.append(line).append("\n");
			}
		}

		if(cursorPosition == null)
		{
			throw new RuntimeException("No cursor position found");
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
