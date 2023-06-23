package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public enum ReadSequence
{
	PHYSICAL,
	ISN,
	LOGICAL;

	public static ReadSequence fromSyntaxKind(SyntaxKind kind)
	{
		return switch (kind)
		{
			case KW_ISN -> ISN;
			case BY, WITH -> LOGICAL;
			default -> PHYSICAL;
		};
	}

	public boolean isPhysicalSequence()
	{
		return this == PHYSICAL;
	}

	public boolean isIsnSequence()
	{
		return this == ISN;
	}

	public boolean isLogicalSequence()
	{
		return this == LOGICAL;
	}
}
