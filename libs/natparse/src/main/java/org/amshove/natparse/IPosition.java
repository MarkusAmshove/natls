package org.amshove.natparse;

import org.amshove.natparse.natural.project.NaturalFileType;

import java.nio.file.Path;

public interface IPosition
{
	int offset();

	int offsetInLine();

	int line();

	int length();

	Path filePath();

	default NaturalFileType fileType()
	{
		return NaturalFileType.fromExtension(filePath().getFileName().toString().split("\\.")[1]);
	}

	default String fileNameWithoutExtension()
	{
		var fileName = filePath().getFileName().toString();
		if (!fileName.contains("."))
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

	default int totalEndOffset()
	{
		return offset() + length();
	}

	default boolean isSamePositionAs(IPosition other)
	{
		return offset() == other.offset() && offsetInLine() == other.offsetInLine() && line() == other.line() && length() == other.length() && filePath().equals(other.filePath());
	}

	default boolean isSameFileAs(IPosition other)
	{
		return filePath().equals(other.filePath());
	}
}
