package org.amshove.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.natural.ddm.DescriptorType;
import org.amshove.natparse.natural.ddm.ISuperdescriptor;
import org.amshove.natparse.natural.ddm.ISDescriptorChild;

class SDescriptor extends DdmField implements ISuperdescriptor
{
	private ImmutableList<ISDescriptorChild> children;
	private DescriptorType resolvedDescriptorType;

	SDescriptor(DdmField field)
	{
		super(field);
		if (field.descriptor() != DescriptorType._S_DESCRIPTOR)
		{
			throw new NaturalParseException(String.format("Can't promote Field with DescriptorType %s to superdescriptor", field.descriptor()));
		}
	}
	
	@Override
	public DescriptorType descriptor() {
		return resolvedDescriptorType == null ? DescriptorType._S_DESCRIPTOR : resolvedDescriptorType;
	}
	
	public void resolveDescriptorType(DescriptorType resolved) {
		this.resolvedDescriptorType = resolved;
	}

	@Override
	public ImmutableList<ISDescriptorChild> fields()
	{
		return children;
	}

	void setChildren(ImmutableList<ISDescriptorChild> children)
	{
		this.children = children;
	}
}
