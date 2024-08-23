package org.amshove.natparse.natural.ddm;

import com.google.common.collect.ImmutableList;

public interface ISuperdescriptor extends IDdmField
{
	ImmutableList<ISDescriptorChild> fields();
}
