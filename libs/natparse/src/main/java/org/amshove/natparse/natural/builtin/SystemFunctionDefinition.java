package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.natural.DataFormat;

public record SystemFunctionDefinition(String documentation, DataFormat format) implements IBuiltinFunctionDefinition
{
}
