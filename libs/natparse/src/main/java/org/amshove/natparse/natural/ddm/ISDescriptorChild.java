package org.amshove.natparse.natural.ddm;

public interface ISDescriptorChild
{
	String name();

	IDdmField field();

	int rangeFrom();

	int rangeTo();
}
