package org.amshove.natlint.linter;

import java.nio.file.Path;

import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.project.NaturalHeader;
import org.amshove.natparse.natural.project.NaturalLibrary;
import org.amshove.natparse.natural.project.NaturalProgrammingMode;

public class SyntheticNaturalFile extends NaturalFile
{
	public SyntheticNaturalFile(String referableName, Path path, NaturalFileType filetype)
	{
		super(referableName, path, filetype, new NaturalHeader(NaturalProgrammingMode.STRUCTURED, 10));
	}

	@Override
	public NaturalLibrary getLibrary()
	{
		return new NaturalLibrary(getPath().getParent());
	}
}
