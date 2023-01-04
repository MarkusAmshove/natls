package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.natural.IDataType;

public record SystemVariableDefinition(String name, String documentation, IDataType type, boolean isModifiable) implements IBuiltinFunctionDefinition
{}
