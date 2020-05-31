package org.amshove.natlint.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import org.amshove.natlint.natparse.NaturalParseException;
import org.amshove.natlint.natparse.natural.ddm.*;

import java.util.ArrayList;
import java.util.List;

class GroupField extends DdmField implements IGroupField
{
	private final List<IDdmField> memberList = new ArrayList<>();

	GroupField(DdmField field)
	{
		super(field);
		if (field.fieldType() != FieldType.GROUP)
		{
			throw new NaturalParseException(String.format("Can't promote Field of Type %s to GroupField!", field.fieldType()));
		}
	}

	void addChildField(IDdmField field)
	{
		memberList.add(field);
	}

	@Override
	public ImmutableList<IDdmField> members()
	{
		// TODO(PERF): Copy once finished
		return ImmutableList.copyOf(memberList);
	}

}
