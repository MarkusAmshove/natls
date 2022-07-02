package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.TextEdit;

public class TextEditApplier
{

	public String apply(TextEdit edit, String source)
	{
		var lines = source.split(System.lineSeparator());
		var resultingSource = new StringBuilder();

		var startLine = edit.getRange().getStart().getLine();
		var startLineOffset = edit.getRange().getStart().getCharacter();
		var endLine = edit.getRange().getEnd().getLine();
		var endLineOffset = edit.getRange().getEnd().getCharacter();

		for(var lineNumber = 0; lineNumber < lines.length; lineNumber++)
		{
			if(lineNumber < startLine || lineNumber > endLine)
			{
				resultingSource.append("%s%n".formatted(lines[lineNumber]));
				continue;
			}

			var line = lines[lineNumber];
			for(var charIndex = 0; charIndex < line.length(); charIndex++)
			{
				if(lineNumber == startLine && charIndex < startLineOffset)
				{
					resultingSource.append(line.charAt(charIndex));
					continue;
				}

				if(lineNumber == startLine && charIndex == startLineOffset)
				{
					resultingSource.append(edit.getNewText());
					if(lineNumber == endLine && charIndex >= endLineOffset)
					{
						resultingSource.append(line.charAt(charIndex));
					}
				}

				if(lineNumber == endLine && charIndex > endLineOffset)
				{
					resultingSource.append(line.charAt(charIndex));
				}
			}

			if(lineNumber >= endLine)
			{
				resultingSource.append(System.lineSeparator());
			}
		}

		return resultingSource.toString();
	}
}
