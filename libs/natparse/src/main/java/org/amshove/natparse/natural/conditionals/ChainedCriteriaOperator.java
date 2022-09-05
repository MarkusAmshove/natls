package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.lexing.SyntaxKind;

public enum ChainedCriteriaOperator
{
	AND,
	OR;

	public static ChainedCriteriaOperator fromSyntax(SyntaxKind kind)
	{
		return switch(kind) {
			case AND -> AND;
			case OR -> OR;
			default -> throw new RuntimeException("unreachable: ChainedCriteriaOperator can't be converted from %s".formatted(kind));
		};
	}
}
