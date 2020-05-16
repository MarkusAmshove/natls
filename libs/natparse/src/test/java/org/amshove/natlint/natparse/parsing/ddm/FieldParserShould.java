package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.NaturalParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FieldParserShould
{
	private FieldParser sut;

	//012345...
	//T L DB Name                              F Leng  S D Remark
	@BeforeEach
	void setup()
	{
		sut = new FieldParser();
	}

	@ParameterizedTest
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
		private String length = "";
		private String format = "";
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
