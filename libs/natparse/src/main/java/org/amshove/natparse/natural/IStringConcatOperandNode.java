package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IStringConcatOperandNode extends IOperandNode, ITypeInferable
{
	ReadOnlyList<ILiteralNode> literals();

	String stringValue();
}
