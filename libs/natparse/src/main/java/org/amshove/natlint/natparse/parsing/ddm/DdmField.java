package org.amshove.natlint.natparse.parsing.ddm;

public class DdmField
{
	private final FieldType fieldType;

	DdmField(FieldType fieldType)
	{
		this.fieldType = fieldType;
	}

	public FieldType fieldType()
	{
		return fieldType;
	}
}
