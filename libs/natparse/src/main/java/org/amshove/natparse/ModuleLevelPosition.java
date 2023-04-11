package org.amshove.natparse;

import java.nio.file.Path;

public class ModuleLevelPosition implements IPosition
{
	private final Path filePath;

	public ModuleLevelPosition(Path filePath)
	{
		this.filePath = filePath;
	}

	@Override
	public int offset()
	{
		return 0;
	}

	@Override
	public int offsetInLine()
	{
		return 0;
	}

	@Override
	public int line()
	{
		return 0;
	}

	@Override
	public int length()
	{
		return 0;
	}

	@Override
	public Path filePath()
	{
		return filePath;
	}
}
