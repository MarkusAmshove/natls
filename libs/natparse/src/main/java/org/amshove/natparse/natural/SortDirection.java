package org.amshove.natparse.natural;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.lexing.SyntaxKind;

public enum SortDirection
{
	ASCENDING,
	DESCENDING;

	public static SortDirection fromSyntaxKind(SyntaxKind kind)
	{
		return switch (kind)
		{
			case ASC, ASCENDING -> ASCENDING;
			case DESC, DESCENDING -> DESCENDING;
			default -> throw new NaturalParseException("Could not determine SortDirection from SyntaxKind");
		};
	}
}
