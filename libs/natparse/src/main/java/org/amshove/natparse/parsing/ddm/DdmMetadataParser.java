package org.amshove.natparse.parsing.ddm;

import org.amshove.natparse.NaturalParseException;

import java.util.regex.Pattern;

class DdmMetadataParser
{
	private static final Pattern METADATA_PATTERN = Pattern.compile("DB:\\s+(?<DBNR>\\d+).*FILE:\\s+(?<FILENR>\\d+).*-\\s+(?<DBNAME>[^\\s]+).*DEFAULT SEQUENCE:\\s?(?<SEQ>[\\w\\s$]*)");

	DdmMetadata parseMetadataLine(String line)
	{
		var matcher = METADATA_PATTERN.matcher(line);
		if (!matcher.find())
		{
			throw new NaturalParseException(String.format("DDM Metadata line could not be parsed: \"%s\"", line));
		}

		var metadata = new DdmMetadata();
		metadata.setName(matcher.group("DBNAME"));
		metadata.setDatabaseNumber(matcher.group("DBNR"));
		metadata.setFileNumber(matcher.group("FILENR"));
		metadata.setDefaultSequence(matcher.group("SEQ"));

		return metadata;
	}
}
