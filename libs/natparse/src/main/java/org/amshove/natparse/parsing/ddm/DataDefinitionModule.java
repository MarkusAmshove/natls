package org.amshove.natparse.parsing.ddm;

import com.google.common.collect.ImmutableList;
import org.amshove.natparse.natural.ddm.*;

import java.util.Collection;

class DataDefinitionModule implements IDataDefinitionModule
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

	@Override
	public IDdmField findField(String name)
	{
		return findField(name, fields);
	}

	private IDdmField findField(String name, Collection<IDdmField> fields)
	{
		for (var field : fields)
		{
			if(field.name().equalsIgnoreCase(name))
			{
				return field;
			}

			if(field instanceof IGroupField groupField)
			{
				var foundInGroup = findField(name, groupField.members());
				if (foundInGroup != null)
				{
					return foundInGroup;
				}
			}
		}

		return null;
	}
}
