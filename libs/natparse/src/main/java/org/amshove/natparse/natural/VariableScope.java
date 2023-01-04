package org.amshove.natparse.natural;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.lexing.SyntaxKind;

public enum VariableScope
{
	LOCAL,
	PARAMETER,
	GLOBAL,
	INDEPENDENT;

	public static VariableScope fromSyntaxKind(SyntaxKind kind)
	{
		return switch (kind)
		{
			case LOCAL -> LOCAL;
			case PARAMETER -> PARAMETER;
			case GLOBAL -> GLOBAL;
			case INDEPENDENT -> INDEPENDENT;
			default -> throw new NaturalParseException("Could not determine VariableScope from SyntaxKind");
		};
	}

	public boolean isLocal()
	{
		return this == LOCAL;
	}

	public boolean isParameter()
	{
		return this == PARAMETER;
	}

	public boolean isGlobal()
	{
		return this == GLOBAL;
	}

	public boolean isIndependent()
	{
		return this == INDEPENDENT;
	}
}
