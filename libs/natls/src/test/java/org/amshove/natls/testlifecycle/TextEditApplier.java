package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.TextEdit;

import java.util.List;

public class TextEditApplier
{
	private int deletedLines = 0;

	public String apply(TextEdit edit, String source)
	{
		source = source.replaceAll("\\r\\n?", "\n"); // Test files have linux line ending in the repository. Java text blocks use the source line endings, not platform
		var lines = source.split("\n");
		var resultingSource = new StringBuilder();

		var startLine = edit.getRange().getStart().getLine() - deletedLines;
		var startLineOffset = edit.getRange().getStart().getCharacter();
		var endLine = edit.getRange().getEnd().getLine() - deletedLines;
		var endLineOffset = edit.getRange().getEnd().getCharacter();

		for (var lineNumber = 0; lineNumber < lines.length; lineNumber++)
		{
			if (lineNumber < startLine || lineNumber > endLine)
			{
				resultingSource.append("%s\n".formatted(lines[lineNumber]));
				continue;
			}

			var line = lines[lineNumber];

			// appended inline
			for (var charIndex = 0; charIndex < line.length(); charIndex++)
			{
				if (lineNumber == startLine && charIndex < startLineOffset)
				{
					resultingSource.append(line.charAt(charIndex));
					continue;
				}

				if (lineNumber == startLine && charIndex == startLineOffset)
				{
					if (edit.getNewText().isEmpty() && endLine > lineNumber)
					{
						deletedLines++;
					}
					if (edit.getNewText().contains("\n"))
					{
						deletedLines--;
					}

					resultingSource.append(edit.getNewText());
				}

				if (lineNumber == endLine && charIndex >= endLineOffset)
				{
					resultingSource.append(line.charAt(charIndex));
				}
			}

			// appended to end
			if (startLineOffset == line.length())
			{
				resultingSource.append(edit.getNewText());
			}

			if (lineNumber >= endLine)
			{
				resultingSource.append("\n");
			}
		}

		// A line that needs to be changed isn't present anymore.
		// Put the new text before the last line
		if (edit.getRange().getStart().getLine() >= lines.length)
		{
			var resultedLines = resultingSource.toString().split("\n");
			var last = resultedLines[resultedLines.length - 1];
			resultingSource = new StringBuilder();
			for (int i = 0; i < resultedLines.length - 1; i++)
			{
				resultingSource
					.append(resultedLines[i])
					.append("\n");
			}
			resultingSource.append(edit.getNewText());
			resultingSource.append(last).append("\n");
		}

		return resultingSource.toString();
	}

	public String applyAll(List<? extends TextEdit> edits, String source)
	{
		var editedSource = source;
		for (var edit : edits)
		{
			editedSource = apply(edit, editedSource);
		}

		return editedSource;
	}
}
