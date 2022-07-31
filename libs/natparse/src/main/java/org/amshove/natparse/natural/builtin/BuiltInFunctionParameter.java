package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IDataType;

public record BuiltInFunctionParameter(String name, IDataType type, boolean mandatory)
{

}

