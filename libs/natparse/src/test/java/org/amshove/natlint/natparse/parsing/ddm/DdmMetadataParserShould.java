package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.NaturalParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DdmMetadataParserShould
{
	private DdmMetadataParser sut;

	@BeforeEach
	void setup()
	{
		sut = new DdmMetadataParser();
	}

	@Test
	void throwAnExceptionWhenMetadataCantBeExtracted()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> sut.parseMetadataLine("This wouldn't match"))
			.withMessage("DDM Metadata line could not be parsed: \"This wouldn't match\"");
	}

	@ParameterizedTest(name = "parseDatabaseNumbers [{argumentsWithNames}]")
	@ValueSource(strings =
	{ "000", "001", "0", "1", "111", "9999" })
	void parseDbNumbers(String dbNumber)
	{
		assertThat(parsedMetadata(new DbNumber(dbNumber)).databaseNumber()).isEqualTo(dbNumber);
	}

	@ParameterizedTest(name = "parseFileNumers [{argumentsWithNames}]")
	@ValueSource(strings =
	{ "000", "001", "0", "1", "111", "9999" })
	void parseFileNumbers(String fileNumber)
	{
		assertThat(parsedMetadata(new FileNumber(fileNumber)).fileNumber()).isEqualTo(fileNumber);
	}

	@ParameterizedTest(name = "parseDdmNames [{argumentsWithNames}]")
	@ValueSource(strings =
	{ "NODASH", "SINGLE-DASH", "MULTI-PLE-DASHES", "A-LOT-OF-DASHES-FOR-A-NAME" })
	void parseAName(String name)
	{
		assertThat(parsedMetadata(new DbName(name)).name()).isEqualTo(name);
	}

	@ParameterizedTest(name = "parseDefaultSequences [{argumentsWithNames}]")
	@ValueSource(strings =
	{ "AA", "AC", "DC", "XX", "ABCD" })
	void parseDefaultSequences(String sequence)
	{
		assertThat(parsedMetadata(new DefaultSequence(sequence)).defaultSequence()).isEqualTo(sequence);
	}

	@Test
	void parseAnEmptyDefaultSequenceWithoutTrailingWhitespace()
	{
		assertThat(sut.parseMetadataLine("DB: 255 FILE: 227  - SOME-DDM-HERE                  DEFAULT SEQUENCE:").defaultSequence())
			.isEmpty();
	}

	DdmMetadata parsedMetadata(MetadataField field)
	{
		String dbNumber = "001";
		String fileNumber = "100";
		String name = "MYDDM";
		String sequence = "AA";

		if (field instanceof DbNumber)
		{
			dbNumber = field.value;
		}

		if (field instanceof FileNumber)
		{
			fileNumber = field.value;
		}

		if (field instanceof DbName)
		{
			name = field.value;
		}

		if (field instanceof DefaultSequence)
		{
			sequence = field.value;
		}

		String metadataLine = String.format(
			"DB: %s FILE: %s - %s DEFAULT SEQUENCE: %s",
			dbNumber,
			fileNumber,
			name,
			sequence);

		return sut.parseMetadataLine(metadataLine);
	}

	private static abstract class MetadataField
	{
		final String value;

		MetadataField(String value)
		{
			this.value = value;
		}
	}

	private static class DbNumber extends MetadataField
	{
		DbNumber(String number)
		{
			super(number);
		}
	}

	private static class FileNumber extends MetadataField
	{
		FileNumber(String number)
		{
			super(number);
		}
	}

	private static class DbName extends MetadataField
	{
		DbName(String name)
		{
			super(name);
		}
	}

	private static class DefaultSequence extends MetadataField
	{
		DefaultSequence(String sequence)
		{
			super(sequence);
		}
	}
}
