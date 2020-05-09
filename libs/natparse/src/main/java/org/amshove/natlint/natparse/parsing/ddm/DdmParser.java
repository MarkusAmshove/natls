package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.natural.DataDefinitionModule;

public class DdmParser
{
	private final DdmMetadataParser metadataParser = new DdmMetadataParser();

	DdmMetadata metadata;

	public DataDefinitionModule parseDdm(String content)
	{
		String[] lines = content.split("[\\r\\n]+");

		for (String line : lines)
		{
			if (line.startsWith("DB:"))
			{
				metadata = metadataParser.parseMetadataLine(line);
			}
		}

		return new DataDefinitionModule(metadata.getDatabaseNumber(), metadata.getFileNumber(), metadata.getName(), metadata.getDefaultSequence());
	}
}
