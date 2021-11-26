package org.amshove.natparse.natural;

public interface IArrayDimension extends ISyntaxNode
{
	/**
	 * Specifies the lower bound of the array.
	 * If the array is unbound, it returns -1.
	 * Use `isLowerUnbound()` to check if it is unbound.
	 * @return the lower bound.
	 */
	int lowerBound();

	/**
	 * Specifies the upper bound of the array.
	 * If the array is unbound, it returns -1.
	 * Use `isUpperUnbound()` to check if it is unbound.
	 * @return the upper bound.
	 */
	int upperBound();

	default boolean isLowerUnbound()
	{
		return lowerBound() < 0;
	}

	default boolean isUpperUnbound()
	{
		return upperBound() < 0;
	}
}
