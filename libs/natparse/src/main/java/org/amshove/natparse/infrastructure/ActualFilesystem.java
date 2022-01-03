package org.amshove.natparse.infrastructure;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ActualFilesystem implements IFilesystem
{
	private static final String[] PROJECT_FILE_NAMES = new String[] { ".natural", "_naturalBuild" };

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

	public Optional<Path> findFile(String name, Path root)
	{
		try
		{
			return Files.list(root)
				.filter(f -> f.getFileName().toString().equalsIgnoreCase(name))
				.findFirst();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	// TODO: Move somewhere else where the priority can be tested
	public Optional<Path> findNaturalProjectFile(Path root)
	{
		try
		{
			for (var projectFileName : PROJECT_FILE_NAMES)
			{
				var foundFile = Files.list(root)
					.filter(f -> f.getFileName().toString().equalsIgnoreCase(projectFileName))
					.findFirst();

				if(foundFile.isPresent())
				{
					return foundFile;
				}
			}

			return Optional.empty();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
