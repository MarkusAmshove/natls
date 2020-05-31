package org.amshove.natlint.natparse.natural.ddm;

import org.amshove.natlint.natparse.natural.DataFormat;

public interface IDdmField
{
	FieldType fieldType();

	int level();

	String shortname();

	String name();

	DataFormat format();

	double length();

	NullValueSupression suppression();

	DescriptorType descriptor();

	String remark();
}
