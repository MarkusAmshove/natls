package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.NaturalParseException;
import org.amshove.natlint.natparse.natural.DataFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FieldParserShould
{
	private FieldParser sut;

	@BeforeEach
	void setup()
	{
		sut = new FieldParser();
	}

	@ParameterizedTest(name = "parseTheFieldType [DdmType, Expectation: {argumentsWithNames}]")
	@CsvSource(value =
	{ "G,GROUP", "M,MULTIPLE", "P,PERIODIC", ",NONE" })
	void parseTheFieldTypeOfTheField(String type, String expectedType)
	{
		// CsvSource passes null for empty
		if (type == null)
		{
			type = " ";
		}

		FieldType expectedFieldType = FieldType.valueOf(expectedType);
		assertThat(parsedField(fieldBuilder().withType(type)).fieldType()).isEqualTo(expectedFieldType);
	}

	@Test
	void throwAnExceptionWhenPassingAnInvalidFieldType()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> parsedField(fieldBuilder().withType("L")))
			.withMessage("Can't determine DDM FieldType from \"L\"");
	}

	@ParameterizedTest(name = "parseTheLevelOfFields [Level = {argumentsWithNames}]")
	@ValueSource(ints =
	{ 1, 2, 3, 4 })
	void parseTheLevelOfFields(int level)
	{
		assertThat(parsedField(fieldBuilder().withLevel(level)).level()).isEqualTo(level);
	}

	@ParameterizedTest(name = "parseTheAdabasShortname [{argumentsWithNames}]")
	@ValueSource(strings =
	{ "AA", "CB", "YX", "ZA" })
	void parseTheAdabasShortname(String shortname)
	{
		assertThat(parsedField(fieldBuilder().withDbShortname(shortname)).shortname()).isEqualTo(shortname);
	}

	@ParameterizedTest(name = "parseTheFieldname [{argumentsWithNames}]")
	@ValueSource(strings =
	{ "SOME-FIELD", "FIELD", "SOME-LONG-FIELD-NAME" })
	void parseTheFieldname(String fieldname)
	{
		assertThat(parsedField(fieldBuilder().withName(fieldname)).name()).isEqualTo(fieldname);
	}

	@ParameterizedTest(name = "parseTheFormat [Type, ExpectedType = {argumentsWithNames}]")
	@CsvSource(value =
	{ "A,ALPHANUMERIC", "B,BINARY", "C,CONTROL", "D,DATE", "F,FLOAT", "I,INTEGER", "L,LOGIC", "N,NUMERIC", "P,PACKED", "T,TIME", "U,UNICODE" })
	void parseTheFormat(String format, String expectedFormat)
	{
		DataFormat expectedTypedFormat = DataFormat.valueOf(expectedFormat);
		assertThat(parsedField(fieldBuilder().withFormat(format)).format()).isEqualTo(expectedTypedFormat);
	}

	@Test
	void throwAnExceptionWhenPassingAnInvalidFieldFormat()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> parsedField(fieldBuilder().withFormat(" ")))
			.withMessage("Can't determine DataFormat from format \" \"");
	}

	@ParameterizedTest(name = "parseTheLengthOfTheFormat [Length, ExpectedLength = {argumentsWithNames}]")
	@CsvSource(value =
	{ "12;12", "1;1", "12,7;12.7", "12.7;12.7", "120;120" }, delimiterString = ";")
	void parseTheLengthOfTheFormat(String ddmLength, double expectedLength)
	{
		assertThat(parsedField(fieldBuilder().withLength(ddmLength)).length()).isEqualTo(expectedLength);
	}

	@Test
	void throwAnExceptionWhenPassingAnUnparsableLength()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> parsedField(fieldBuilder().withLength(",2;3")))
			.withMessage("Can't parse format length \".2;3\"");
	}

	private DdmField parsedField(DdmFieldBuilder builder)
	{
		return sut.parse(builder.build());
	}

	private static DdmFieldBuilder fieldBuilder()
	{
		return new DdmFieldBuilder();
	}

	private static class DdmFieldBuilder
	{
		private String type = "";
		private int level = 1;
		private String shortname = "";
		private String name = "";
		private String format = "A";
		private String length = "1";
		private String remark = "";
		private String suppression = "";
		private String descriptor = "";

		DdmFieldBuilder withType(String type)
		{
			this.type = type;
			return this;
		}

		DdmFieldBuilder withLevel(int level)
		{
			this.level = level;
			return this;
		}

		DdmFieldBuilder withDbShortname(String shortname)
		{
			this.shortname = shortname;
			return this;
		}

		DdmFieldBuilder withName(String name)
		{
			this.name = name;
			return this;
		}

		DdmFieldBuilder withFormat(String format)
		{
			this.format = format;
			return this;
		}

		DdmFieldBuilder withLength(String length)
		{
			this.length = length;
			return this;
		}

		DdmFieldBuilder withSuppression(String suppression)
		{
			this.suppression = suppression;
			return this;
		}

		DdmFieldBuilder withDescriptor(String descriptor)
		{
			this.descriptor = descriptor;
			return this;
		}

		DdmFieldBuilder withRemark(String remark)
		{
			this.remark = remark;
			return this;
		}

		String build()
		{
			return String.format(
				"%-1s %-1s %-2s %-32s  %-1s %4s  %-1s %-1s %-24s",
				type,
				level,
				shortname,
				name,
				format,
				length,
				suppression,
				descriptor,
				remark);
		}
	}
}
