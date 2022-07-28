package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.natural.DataFormat;

public sealed interface ISystemDefinition permits SystemFunctionDefinition, SystemVariableDefinition
{
	String documentation();
	DataFormat format();
}
