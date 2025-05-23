package org.amshove.natparse.infrastructure;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface IFilesystem
{
	String readFile(Path path);

	boolean exists(Path path);

	List<Path> listDirectories(Path path);

	Stream<Path> streamFilesRecursively(Path startPath);

	Optional<Path> findFile(String name, Path root);

	Stream<String> peekFile(Path path);

	static String filenameWithoutExtension(Path path)
	{
		var split = path.getFileName().toString().split("\\.");
		return split[0];
	}
}
