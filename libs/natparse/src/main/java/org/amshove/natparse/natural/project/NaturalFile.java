package org.amshove.natparse.natural.project;

import java.nio.file.Path;

public class NaturalFile
{
	private final String referableName;
	private final Path path;
	private final NaturalFileType filetype;
	private NaturalLibrary library;

	public NaturalFile(String referableName, Path path, NaturalFileType filetype)
	{
		this.referableName = referableName;
		this.path = path;
		this.filetype = filetype;
	}

	public String getReferableName()
	{
		return referableName;
	}

	public Path getPath()
	{
		return path;
	}

	/* package */ void setLibrary(NaturalLibrary library)
	{
		this.library = library;
	}

	public NaturalLibrary getLibrary()
	{
		return library;
	}
}
