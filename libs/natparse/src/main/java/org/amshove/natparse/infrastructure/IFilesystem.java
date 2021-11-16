package org.amshove.natparse.infrastructure;

import java.nio.file.Path;
import java.util.List;

public interface IFilesystem
{
	String readFile(Path path);

	List<Path> listDirectories(Path path);
}
