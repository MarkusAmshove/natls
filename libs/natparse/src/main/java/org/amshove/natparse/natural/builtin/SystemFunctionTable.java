package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.lexing.SyntaxKind;

import java.util.HashMap;
import java.util.Map;

public class SystemFunctionTable
{
	private static final Map<SyntaxKind, IBuiltinFunctionDefinition> TABLE = new HashMap<>();

	public static IBuiltinFunctionDefinition getDefinition(SyntaxKind kind)
	{
		return TABLE.get(kind);
	}
}
