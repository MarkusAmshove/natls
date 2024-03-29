package org.amshove.natparse.parsing.ddm;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.ddm.DescriptorType;
import org.amshove.natparse.natural.ddm.FieldType;
import org.amshove.natparse.natural.ddm.IDdmField;
import org.amshove.natparse.natural.ddm.NullValueSuppression;

class DdmField implements IDdmField
{
	private final FieldType fieldType;
	private final int level;
	private final String shortname;
	private final String name;
	private final DataFormat format;
	private final double length;
	private final NullValueSuppression suppression;
	private final DescriptorType descriptor;
	private final String remark;

	DdmField(
		FieldType fieldType,
		int level,
		String shortname,
		String name,
		DataFormat format,
		double length,
		NullValueSuppression suppression,
		DescriptorType descriptor,
		String remark
	)
	{
		this.fieldType = fieldType;
		this.level = level;
		this.shortname = shortname;
		this.name = name;
		this.format = format;
		this.length = length;
		this.suppression = suppression;
		this.descriptor = descriptor;
		this.remark = remark;
	}

	DdmField(DdmField field)
	{
		this.fieldType = field.fieldType;
		this.level = field.level;
		this.shortname = field.shortname;
		this.name = field.name;
		this.format = field.format;
		this.length = field.length;
		this.suppression = field.suppression;
		this.descriptor = field.descriptor;
		this.remark = field.remark;
	}

	public FieldType fieldType()
	{
		return fieldType;
	}

	public int level()
	{
		return level;
	}

	public String shortname()
	{
		return shortname;
	}

	public String name()
	{
		return name;
	}

	public DataFormat format()
	{
		return format;
	}

	public double length()
	{
		return length;
	}

	public NullValueSuppression suppression()
	{
		return this.suppression;
	}

	public DescriptorType descriptor()
	{
		return this.descriptor;
	}

	public String remark()
	{
		return this.remark;
	}
}
