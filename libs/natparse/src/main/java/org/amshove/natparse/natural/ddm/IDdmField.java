package org.amshove.natparse.natural.ddm;

import org.amshove.natparse.natural.DataFormat;

public interface IDdmField
{
	FieldType fieldType();

	int level();

	String shortname();

	String name();

	DataFormat format();

	double length();

	NullValueSuppression suppression();

	DescriptorType descriptor();

	String remark();
}
