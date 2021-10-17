package org.amshove.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.natural.ddm.DescriptorType;
import org.amshove.natparse.natural.ddm.ISuperdescriptor;
import org.amshove.natparse.natural.ddm.ISuperdescriptorChild;

class Superdescriptor extends DdmField implements ISuperdescriptor
{
	private ImmutableList<ISuperdescriptorChild> children;

	Superdescriptor(DdmField field)
	{
		super(field);
		if (field.descriptor() != DescriptorType.SUPERDESCRIPTOR)
		{
			throw new NaturalParseException(String.format("Can't promote Field with DescriptorType %s to superdescriptor", field.descriptor()));
		}
	}

	@Override
	public ImmutableList<ISuperdescriptorChild> fields()
	{
		return children;
	}

	void setChildren(ImmutableList<ISuperdescriptorChild> children)
	{
		this.children = children;
	}
}
