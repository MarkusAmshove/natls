package org.amshove.natlint.natparse.natural.ddm;

import com.google.common.collect.ImmutableList;

public interface IDataDefinitionModule
{
	String name();

	String fileNumber();

	String databaseNumber();

	String defaultSequence();

	/**
	 * Returns a list of all fields on level 1
	 *
	 * @return List of {@link IDdmField} on level 1
	 */
	ImmutableList<IDdmField> fields();
}
