package org.amshove.natlint.natparse.natural.ddm;

import com.google.common.collect.ImmutableList;

public interface ISuperdescriptor extends IDdmField
{
	ImmutableList<ISuperdescriptorChild> fields();
}
