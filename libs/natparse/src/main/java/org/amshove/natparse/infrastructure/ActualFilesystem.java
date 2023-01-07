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
	private static final String[] PROJECT_FILE_NAMES = new String[]
	{
		".natural", "_naturalBuild"
	};

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
		try (var files = Files.list(path))
		{
			return files
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
		try (var files = Files.list(root))
		{
			return files
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
				try (var filteredFiles = Files.list(root)
					.filter(f -> f.getFileName().toString().equalsIgnoreCase(projectFileName)))
				{
					var foundFile = filteredFiles.findFirst();

					if (foundFile.isPresent())
					{
						return foundFile;
					}
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
