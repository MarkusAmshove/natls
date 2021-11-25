package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IVariableType
{
	DataFormat format();
	double length();
	boolean hasDynamicLength();
	SyntaxToken initialValue();
}
