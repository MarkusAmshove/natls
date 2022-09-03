package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.lexing.SyntaxKind;

import javax.annotation.Nullable;

public enum ComparisonOperator
{
	EQUAL,
	NOT_EQUAL,
	GREATER_THAN,
	LESS_THAN,
	GREATER_OR_EQUAL,
	LESS_OR_EQUAL;

	/**
	 * Maps a single SyntaxKind to a comparison operator.<br/>
	 * If multiple SyntaxKinds can result in the same comparison (e.g. GREATER vs GREATER THAN), this returns null.</br>
	 * Nullable because it's used in the hot path.
	 */
	public static @Nullable ComparisonOperator ofSyntaxKind(SyntaxKind kind)
	{
		return switch (kind) {
			case EQUALS_SIGN, EQ -> EQUAL;
			case LESSER_GREATER, NE -> NOT_EQUAL;
			case LESSER_SIGN, LT -> LESS_THAN;
			case LESSER_EQUALS_SIGN, LE -> LESS_OR_EQUAL;
			case GREATER_SIGN, GT -> GREATER_THAN;
			case GREATER_EQUALS_SIGN, GE -> GREATER_OR_EQUAL;
			default -> null;
		};
	}
}
