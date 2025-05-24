package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ILocalDataArea;
import org.amshove.natparse.natural.project.NaturalFile;

class LocalDataArea extends NaturalModule implements ILocalDataArea
{
	public LocalDataArea(NaturalFile file)
	{
		super(file);
	}
}
