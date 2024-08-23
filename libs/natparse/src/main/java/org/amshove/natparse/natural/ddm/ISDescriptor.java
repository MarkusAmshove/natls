package org.amshove.natparse.natural.ddm;

import com.google.common.collect.ImmutableList;

public interface ISDescriptor extends IDdmField
{
	ImmutableList<ISDescriptorChild> fields();
}
