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

	private final List<IDdmField> fields = new ArrayList<>();

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

	void addField(IDdmField field)
	{
		fields.add(field);
	}

	public DdmType type()
	{
		return ddmType;
	}

	void setDdmType(DdmType type)
	{
		ddmType = type;
	}

	void finish()
	{
		// TODO: Set references for superdescriptor children, copy lists once to immutable, etc.
	}

	public ImmutableList<IDdmField> fields()
	{
		// TODO(PERF):
		return ImmutableList.copyOf(fields);
	}
}
