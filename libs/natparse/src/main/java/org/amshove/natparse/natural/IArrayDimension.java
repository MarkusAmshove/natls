package org.amshove.natparse.natural;

public interface IArrayDimension extends ISyntaxNode
{
	int UNBOUND_VALUE = Integer.MAX_VALUE;
	int VARIABLE_BOUND = Integer.MAX_VALUE - 1;

	/**
	 * Specifies the lower bound of the array. If the array is unbound, it returns int.MAX_VALUE. Use `isLowerUnbound()`
	 * to check if it is unbound.
	 * 
	 * @return the lower bound.
	 */
	int lowerBound();

	/**
	 * Specifies the upper bound of the array. If the array is unbound, it returns int.MAX_VALUE. Use `isUpperUnbound()`
	 * to check if it is unbound.
	 * 
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

	/**
	 * This indicates that the upper bound is V for PDAs, not that the upper bound is a variable.<br/>
	 * <a href=
	 * "https://documentation.softwareag.com/natural/nat912win/sm/defineda_array.htm#Variable_Arrays_in_a_Parameter_Data_Area">Documentation</a>
	 */
	boolean isUpperVariable();

	default int occurrences()
	{
		if (isLowerUnbound() || isUpperUnbound())
		{
			return UNBOUND_VALUE;
		}

		return upperBound() - lowerBound() + 1;
	}

	default String displayFormat()
	{
		return "%s:%s".formatted(
			isLowerUnbound() ? "*" : lowerBound(),
			isUpperUnbound() ? "*" : upperBound()
		);
	}
}
