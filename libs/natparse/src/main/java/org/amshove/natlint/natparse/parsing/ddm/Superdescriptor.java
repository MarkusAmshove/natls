package org.amshove.natlint.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import org.amshove.natlint.natparse.NaturalParseException;
import org.amshove.natlint.natparse.natural.ddm.DescriptorType;
import org.amshove.natlint.natparse.natural.ddm.ISuperdescriptor;
import org.amshove.natlint.natparse.natural.ddm.ISuperdescriptorChild;

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
