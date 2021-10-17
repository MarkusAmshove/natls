package org.amshove.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.natural.ddm.FieldType;
import org.amshove.natparse.natural.ddm.IDdmField;
import org.amshove.natparse.natural.ddm.IGroupField;

class GroupField extends DdmField implements IGroupField
{
	private ImmutableList<IDdmField> member;

	GroupField(DdmField field)
	{
		super(field);
		if (field.fieldType() != FieldType.GROUP)
		{
			throw new NaturalParseException(String.format("Cannot promote field of type %s to GroupField", field.fieldType()));
		}
	}

	void setChildren(ImmutableList<IDdmField> fields)
	{
		member = fields;
	}

	@Override
	public ImmutableList<IDdmField> members()
	{
		return member;
	}

}
