package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IGlobalDataArea;
import org.amshove.natparse.natural.project.NaturalFile;

class GlobalDataArea extends NaturalModule implements IGlobalDataArea
{
	public GlobalDataArea(NaturalFile file)
	{
		super(file);
	}
}
