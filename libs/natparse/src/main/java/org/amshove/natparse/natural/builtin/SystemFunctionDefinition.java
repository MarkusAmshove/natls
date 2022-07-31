package org.amshove.natparse.natural.builtin;

import java.util.List;

import org.amshove.natparse.natural.IDataType;

public record SystemFunctionDefinition(String name, String documentation, IDataType type, List<BuiltInFunctionParameter> parameter) implements IBuiltinFunctionDefinition
{
	public boolean hasMandatoryParameter()
	{
		return parameter.stream().anyMatch(BuiltInFunctionParameter::mandatory);
	}
}
