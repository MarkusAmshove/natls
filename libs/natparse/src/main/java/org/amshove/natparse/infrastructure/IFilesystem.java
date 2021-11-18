package org.amshove.natparse.infrastructure;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface IFilesystem
{
	String readFile(Path path);

	List<Path> listDirectories(Path path);

	Stream<Path> streamFilesRecursively(Path startPath);
}
