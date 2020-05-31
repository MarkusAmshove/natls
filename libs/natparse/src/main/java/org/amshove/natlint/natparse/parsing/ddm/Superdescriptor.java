package org.amshove.natlint.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import org.amshove.natlint.natparse.NaturalParseException;
import org.amshove.natlint.natparse.natural.ddm.*;

import java.util.ArrayList;
import java.util.List;

public class Superdescriptor extends DdmField implements ISuperdescriptor
{
	private final List<SuperdescriptorChild> childfields = new ArrayList<>();

	Superdescriptor(DdmField field)
	{
		super(field);
		if (field.descriptor() != DescriptorType.SUPERDESCRIPTOR)
		{
			throw new NaturalParseException(String.format("Can't promote Field with DescriptorType %s to superdescriptor", field.descriptor()));
		}
	}

	void addChildField(SuperdescriptorChild child)
	{
		childfields.add(child);
	}

	@Override
	public ImmutableList<ISuperdescriptorChild> fields()
	{
		// TODO(PERF): Copy when finished constructing superdescriptor
		return ImmutableList.copyOf(childfields);
	}
}
