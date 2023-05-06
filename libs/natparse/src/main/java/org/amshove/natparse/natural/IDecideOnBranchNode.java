package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IDecideOnBranchNode
{
	/**
	 * Contains only two operands if {@code hasValueRange()} is true.
	 */
	ReadOnlyList<IOperandNode> values();

	/**
	 * Returns true if the values condition is a range condition like {@code 1:10}.
	 */
	boolean hasValueRange();

	IStatementListNode body();
}
