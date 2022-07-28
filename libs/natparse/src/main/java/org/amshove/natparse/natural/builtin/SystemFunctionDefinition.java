package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.natural.IDataType;

public record SystemFunctionDefinition(String name, String documentation, IDataType type) implements IBuiltinFunctionDefinition
{
}
