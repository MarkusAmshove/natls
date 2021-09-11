package org.amshove.natlint.natparse.natural.ddm;

public interface ISuperdescriptorChild
{
	String name();

	IDdmField field();

	int rangeFrom();

	int rangeTo();
}
