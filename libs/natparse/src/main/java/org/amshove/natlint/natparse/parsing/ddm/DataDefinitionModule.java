package org.amshove.natlint.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import org.amshove.natlint.natparse.natural.ddm.DdmType;
import org.amshove.natlint.natparse.natural.ddm.IDataDefinitionModule;
import org.amshove.natlint.natparse.natural.ddm.IDdmField;

import java.util.ArrayList;
import java.util.List;

public class DataDefinitionModule implements IDataDefinitionModule
{
	private final String databaseNumber;
	private final String fileNumber;
	private final String ddmName;
	private final String defaultSequence;
	private DdmType ddmType;

	private ImmutableList<IDdmField> fields;

	DataDefinitionModule(String databaseNumber, String fileNumber, String ddmName, String defaultSequence)
	{
		this.databaseNumber = databaseNumber;
		this.fileNumber = fileNumber;
		this.ddmName = ddmName;
		this.defaultSequence = defaultSequence;
	}

	public String name()
	{
		return ddmName;
	}

	public String fileNumber()
	{
		return fileNumber;
	}

	public String databaseNumber()
	{
		return databaseNumber;
	}

	public String defaultSequence()
	{
		return defaultSequence;
	}

	public DdmType type()
	{
		return ddmType;
	}

	void setDdmType(DdmType type)
	{
		ddmType = type;
	}

	void setFields(ImmutableList<IDdmField> fields)
	{
		this.fields = fields;
	}

	public ImmutableList<IDdmField> fields()
	{
		return fields;
	}
}
