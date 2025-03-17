package org.amshove.natqube;

import org.sonar.api.batch.fs.InputFile;

public class NaturalInputFile
{
	private final InputFile file;

	private NaturalInputFile(InputFile file)
	{
		this.file = file;
	}

	public static NaturalInputFile fromInputFile(InputFile file)
	{
		var language = file.language();
		if (language == null || !language.equals(Natural.KEY))
		{
			return null;
		}

		return new NaturalInputFile(file);
	}
}
