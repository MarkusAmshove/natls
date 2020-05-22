package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.natural.DataFormat;
import org.amshove.natlint.natparse.natural.DescriptorType;
import org.amshove.natlint.natparse.natural.NullValueSupression;

public class DdmField
{
	private final FieldType fieldType;
	private final int level;
	private final String shortname;
	private final String name;
	private final DataFormat format;
	private final double length;
	private final NullValueSupression supression;
	private final DescriptorType descriptor;

	DdmField(FieldType fieldType, int level, String shortname, String name, DataFormat format, double length, NullValueSupression supression, DescriptorType descriptorType)
	{
		this.fieldType = fieldType;
		this.level = level;
		this.shortname = shortname;
		this.name = name;
		this.format = format;
		this.length = length;
		this.supression = supression;
		this.descriptor = descriptorType;
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

	public NullValueSupression supression()
	{
		return this.supression;
	}

	public DescriptorType descriptor()
	{
		return this.descriptor;
	}
}
