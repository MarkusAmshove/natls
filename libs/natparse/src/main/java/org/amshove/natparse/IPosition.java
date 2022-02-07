package org.amshove.natparse;

import java.nio.file.Path;

public interface IPosition
{
	int offset();
	int offsetInLine();
	int line();
	int length();
	Path filePath();

	default String fileNameWithoutExtension()
	{
		var fileName = filePath().getFileName().toString();
		if(!fileName.contains("."))
		{
			return fileName;
		}

		var extensionIndex = fileName.lastIndexOf('.');
		return fileName.substring(0, extensionIndex);
	}

	default int endOffset()
	{
		return offsetInLine() + length();
	}
}
