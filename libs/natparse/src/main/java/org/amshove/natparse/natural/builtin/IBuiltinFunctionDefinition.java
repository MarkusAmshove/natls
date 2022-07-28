package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.natural.DataFormat;

public sealed interface IBuiltinFunctionDefinition permits SystemFunctionDefinition, SystemVariableDefinition
{
	String documentation();
	DataFormat format();
}
