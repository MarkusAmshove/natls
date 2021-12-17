package org.amshove.natparse.natural;

public interface IArrayDimension extends ISyntaxNode
{
	int UNBOUND_VALUE = Integer.MAX_VALUE;

	/**
	 * Specifies the lower bound of the array.
	 * If the array is unbound, it returns int.MAX_VALUE.
	 * Use `isLowerUnbound()` to check if it is unbound.
	 * @return the lower bound.
	 */
	int lowerBound();

	/**
	 * Specifies the upper bound of the array.
	 * If the array is unbound, it returns int.MAX_VALUE.
	 * Use `isUpperUnbound()` to check if it is unbound.
	 * @return the upper bound.
	 */
	int upperBound();

	default boolean isLowerUnbound()
	{
		return lowerBound() == UNBOUND_VALUE;
	}

	default boolean isUpperUnbound()
	{
		return upperBound() == UNBOUND_VALUE;
	}

	default String displayFormat()
	{
		return "%s:%s".formatted(
			isLowerUnbound() ? "*" : lowerBound(),
			isUpperUnbound() ? "*" : upperBound()
		);
	}
}
