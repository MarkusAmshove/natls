package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ISubprogram;
import org.amshove.natparse.natural.project.NaturalFile;

class Subprogram extends NaturalModule implements ISubprogram
{
	public Subprogram(NaturalFile file)
	{
		super(file);
	}

	@Override
	public boolean isTestCase()
	{
		return (file.getReferableName().startsWith("TC") || file.getReferableName().startsWith("TS"));
	}
}
