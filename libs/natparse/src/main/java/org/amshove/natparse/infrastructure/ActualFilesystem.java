package org.amshove.natparse.infrastructure;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ActualFilesystem implements IFilesystem
{
	public String readFile(Path path)
	{
		try
		{
			return Files.readString(path);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public List<Path> listDirectories(Path path)
	{
		try
		{
			return Files.list(path)
				.filter(p -> p.toFile().isDirectory())
				.toList();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public Stream<Path> streamFilesRecursively(Path startPath)
	{
		try
		{
			return Files.walk(startPath, Integer.MAX_VALUE).filter(p -> p.toFile().isFile());
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
