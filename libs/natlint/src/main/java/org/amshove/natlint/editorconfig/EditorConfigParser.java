package org.amshove.natlint.editorconfig;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.parsing.text.LinewiseTextScanner;

import java.util.ArrayList;

public class EditorConfigParser
{
	public EditorConfig parse(String content)
	{
		var scanner = new LinewiseTextScanner(content.split(System.lineSeparator()));
		var config = new EditorConfig();

		while (!scanner.isAtEnd())
		{
			var line = scanner.peek();
			if (isSectionStart(line))
			{
				config.addSection(parseSection(scanner));
			}
			else
			{
				scanner.advance();
			}
		}

		return config;
	}

	private EditorConfigSection parseSection(LinewiseTextScanner scanner)
	{
		var line = scanner.peek();
		var start = line.indexOf("[");
		var end = line.lastIndexOf("]");
		var filePattern = line.substring(start + 1, end);
		scanner.advance();

		var properties = new ArrayList<EditorConfigProperty>();

		while (!scanner.isAtEnd() && !isSectionStart(scanner.peek()))
		{
			line = scanner.peek();
			if (hasProperty(line))
			{
				var assignmentIndex = line.indexOf("=");
				var property = line.substring(0, assignmentIndex).trim();
				var value = line.substring(assignmentIndex + 1).trim();

				properties.add(new EditorConfigProperty(property, value));
			}

			scanner.advance();
		}

		return new EditorConfigSection(filePattern, ReadOnlyList.from(properties));
	}

	private boolean isSectionStart(String line)
	{
		return line.trim().startsWith("[");
	}

	private boolean hasProperty(String line)
	{
		return !line.startsWith("#") && line.contains("=");
	}
}
