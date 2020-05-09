package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.NaturalParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DdmMetadataParser
{
	private static final Pattern METADATA_PATTERN = Pattern.compile("DB:\\s+(?<DBNR>\\d+).*FILE:\\s+(?<FILENR>\\d+).*-\\s+(?<DBNAME>[^\\s]+).*DEFAULT SEQUENCE:\\s+(?<SEQ>\\w+)");

	DdmMetadata parseMetadataLine(String line)
	{
		Matcher matcher = METADATA_PATTERN.matcher(line);
		if (!matcher.find())
		{
			throw new NaturalParseException(String.format("DDM Metadataline could not be parsed: \"%s\"", line));
		}

		DdmMetadata metadata = new DdmMetadata();
		metadata.setName(matcher.group("DBNAME"));
		metadata.setDatabaseNumber(matcher.group("DBNR"));
		metadata.setFileNumber(matcher.group("FILENR"));
		metadata.setDefaultSequence(matcher.group("SEQ"));

		return metadata;
	}
}
