package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.natural.DataFormat;
import org.amshove.natlint.natparse.natural.ddm.DescriptorType;
import org.amshove.natlint.natparse.natural.ddm.FieldType;
import org.amshove.natlint.natparse.natural.ddm.IDdmField;
import org.amshove.natlint.natparse.natural.ddm.NullValueSupression;

public class DdmField implements IDdmField
{
	private final FieldType fieldType;
	private final int level;
	private final String shortname;
	private final String name;
	private final DataFormat format;
	private final double length;
	private final NullValueSupression suppression;
	private final DescriptorType descriptor;
	private final String remark;

	DdmField(FieldType fieldType,
		int level,
		String shortname,
		String name,
		DataFormat format,
		double length,
		NullValueSupression suppression,
		DescriptorType descriptor,
		String remark)
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

	public NullValueSupression suppression()
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
