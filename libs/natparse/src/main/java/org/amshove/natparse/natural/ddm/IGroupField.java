package org.amshove.natparse.natural.ddm;

import com.google.common.collect.ImmutableList;

public interface IGroupField extends IDdmField
{
	ImmutableList<IDdmField> members();
}
