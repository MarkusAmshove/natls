package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.lexing.SyntaxKind;

import java.util.HashMap;
import java.util.Map;

public class SystemFunctionTable
{
	private static final Map<SyntaxKind, ISystemDefinition> TABLE = new HashMap<>();

	public static ISystemDefinition getDefinition(SyntaxKind kind)
	{
		return TABLE.get(kind);
	}
}
