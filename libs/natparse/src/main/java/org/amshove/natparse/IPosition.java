package org.amshove.natparse;

import org.amshove.natparse.natural.project.NaturalFileType;

import java.nio.file.Path;

/**
 * Points to a position in a document. A position can be more than one character, given the {@code length} property.
 */
public interface IPosition
{
	/**
	 * 0-based offset of the start of this position in its file.
	 */
	int offset();

	/**
	 * 0-based offset of the start of this position in its line.
	 */
	int offsetInLine();

	/**
	 * 0-based line number.
	 */
	int line();

	/**
	 * length of the position.
	 */
	int length();

	/**
	 * path of the enclosing file.
	 */
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
