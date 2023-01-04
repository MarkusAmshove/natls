package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.natural.IDataType;

public sealed interface IBuiltinFunctionDefinition permits SystemFunctionDefinition,SystemVariableDefinition
{
	String name();

	String documentation();

	IDataType type();
}
