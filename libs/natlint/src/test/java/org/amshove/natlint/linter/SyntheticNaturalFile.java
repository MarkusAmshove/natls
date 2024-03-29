package org.amshove.natlint.linter;

import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.project.NaturalLibrary;

import java.nio.file.Path;

public class SyntheticNaturalFile extends NaturalFile
{
	public SyntheticNaturalFile(String referableName, Path path, NaturalFileType filetype)
	{
		super(referableName, path, filetype);
	}

	@Override
	public NaturalLibrary getLibrary()
	{
		return new NaturalLibrary(getPath().getParent());
	}
}
