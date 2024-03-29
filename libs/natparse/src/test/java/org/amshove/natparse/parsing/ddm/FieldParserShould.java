package org.amshove.natparse.parsing.ddm;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.ddm.DescriptorType;
import org.amshove.natparse.natural.ddm.FieldType;
import org.amshove.natparse.natural.ddm.NullValueSuppression;
import org.amshove.natparse.parsing.text.LinewiseTextScanner;
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
	{
		"G,GROUP", "M,MULTIPLE", "P,PERIODIC", ",NONE"
	})
	void parseTheFieldTypeOfTheField(String type, String expectedType)
	{
		// CsvSource passes null for empty
		if (type == null)
		{
			type = " ";
		}

		var expectedFieldType = FieldType.valueOf(expectedType);
		assertThat(parsedField(fieldBuilder().withType(type)).fieldType()).isEqualTo(expectedFieldType);
	}

	@Test
	void throwAnExceptionWhenPassingAnInvalidFieldType()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> parsedField(fieldBuilder().withType("L")))
			.withMessage("Can't determine FieldType from \"L\"");
	}

	@ParameterizedTest(name = "parseTheLevelOfFields [Level = {argumentsWithNames}]")
	@ValueSource(ints =
	{
		1, 2, 3, 4
	})
	void parseTheLevelOfFields(int level)
	{
		assertThat(parsedField(fieldBuilder().withLevel(level)).level()).isEqualTo(level);
	}

	@ParameterizedTest(name = "parseTheAdabasShortname [{argumentsWithNames}]")
	@ValueSource(strings =
	{
		"AA", "CB", "YX", "ZA"
	})
	void parseTheAdabasShortname(String shortname)
	{
		assertThat(parsedField(fieldBuilder().withDbShortname(shortname)).shortname()).isEqualTo(shortname);
	}

	@ParameterizedTest(name = "parseTheFieldname [{argumentsWithNames}]")
	@ValueSource(strings =
	{
		"SOME-FIELD", "FIELD", "SOME-LONG-FIELD-NAME"
	})
	void parseTheFieldname(String fieldname)
	{
		assertThat(parsedField(fieldBuilder().withName(fieldname)).name()).isEqualTo(fieldname);
	}

	@ParameterizedTest(name = "parseTheFormat [Type, ExpectedType = {argumentsWithNames}]")
	@CsvSource(value =
	{
		"A,ALPHANUMERIC", "B,BINARY", "C,CONTROL", "D,DATE", "F,FLOAT", "I,INTEGER", "L,LOGIC", "N,NUMERIC", "P,PACKED", "T,TIME", "U,UNICODE"
	})
	void parseTheFormat(String format, String expectedFormat)
	{
		var expectedTypedFormat = DataFormat.valueOf(expectedFormat);
		assertThat(parsedField(fieldBuilder().withFormat(format)).format()).isEqualTo(expectedTypedFormat);
	}

	@Test
	void throwAnExceptionWhenPassingAnInvalidFieldFormat()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> parsedField(fieldBuilder().withFormat("X")))
			.withMessage("Can't determine DataFormat from format \"X\"");
	}

	@ParameterizedTest(name = "parseTheLengthOfTheFormat [Length, ExpectedLength = {argumentsWithNames}]")
	@CsvSource(value =
	{
		"12;12", "1;1", "12,7;12.7", "12.7;12.7", "120;120"
	}, delimiterString = ";")
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

	@ParameterizedTest(name = "parseTheNullValueSuppresion [Supression, ExpectedSupression = {argumentsWithNames}]")
	@CsvSource(value =
	{
		"N,NULL_SUPRESSION", "F,FIXED_STORAGE", ",NONE"
	})
	void parseTheNullValueSuppresion(String source, String expectedSupression)
	{
		// CsvSource passes null for empty
		if (source == null)
		{
			source = " ";
		}

		var typedExpectedSupression = NullValueSuppression.valueOf(expectedSupression);

		assertThat(parsedField(fieldBuilder().withSupression(source)).suppression()).isEqualTo(typedExpectedSupression);
	}

	@Test
	void throwAnExceptionWhenPassingAnInvalidNullSupressionValue()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> parsedField(fieldBuilder().withSupression("A")))
			.withMessage("Can't determine NullValueSupression from \"A\"");
	}

	@ParameterizedTest(name = "parseTheDescriptorType [Descriptor, ExpectedType = {argumentsWithNames}]")
	@CsvSource(value =
	{
		"D,DESCRIPTOR", "S,SUPERDESCRIPTOR", "P,PHONETIC", "U,UNIQUE", ",NONE"
	})
	void parseTheDescriptorType(String descriptorLiteral, String expectedDescriptorType)
	{
		if (descriptorLiteral == null)
		{
			descriptorLiteral = " ";
		}

		var expectedType = DescriptorType.valueOf(expectedDescriptorType);

		assertThat(parsedField(fieldBuilder().withDescriptor(descriptorLiteral)).descriptor()).isEqualTo(expectedType);
	}

	@Test
	void parseEmptyDescriptors()
	{
		// this is the case when the ddm source has no remark and was saved without trailing whitespace
		var field = sut.parse(createScanner("  1 AA SOME-NUMBER                       N   12  N"));
		assertThat(field.descriptor()).isEqualTo(DescriptorType.NONE);
	}

	@Test
	void throwAnExceptionWhenPassingAnInvalidDescriptorType()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> parsedField(fieldBuilder().withDescriptor("F")))
			.withMessage("Can't determine DescriptorType from \"F\"");
	}

	@ParameterizedTest(name = "parseTheRemark [Remark = {argumentsWithNames}]")
	@CsvSource(value =
	{
		"Remark", "Long remark", "Super long remark", "Would this remark even fit?"
	})
	void parseTheRemark(String remark)
	{
		assertThat(parsedField(fieldBuilder().withRemark(remark)).remark()).isEqualTo(remark);
	}

	@Test
	void parseEmptyRemarks()
	{
		// this is the case when the ddm source has no remark and was saved without trailing whitespace
		var field = sut.parse(createScanner("  1 AD ALPHANUMERIC-DESCRIPTOR           A   18    D"));
		assertThat(field.remark()).isEmpty();
	}

	@Test
	void parseAPeriodicGroup()
	{
		var field = sut.parse(createScanner("P 1 AS PERIODIC-FIELD                                "));
		assertThat(field.fieldType()).isEqualTo(FieldType.PERIODIC);
		assertThat(field.name()).isEqualTo("PERIODIC-FIELD");
		assertThat(field.length()).isEqualTo(0);
		assertThat(field.format()).isEqualTo(DataFormat.NONE);
	}

	@Test
	void parseAGroup()
	{
		var field = sut.parse(createScanner("G 1 AS GROUP-FIELD                                   "));
		assertThat(field.fieldType()).isEqualTo(FieldType.GROUP);
		assertThat(field.name()).isEqualTo("GROUP-FIELD");
		assertThat(field.length()).isEqualTo(0);
		assertThat(field.format()).isEqualTo(DataFormat.NONE);
	}

	@Test
	void parseTheNameIfNothingElseIsComingAfter()
	{
		assertThat(sut.parse(createScanner("P 1 LF ERROR")).name())
			.isEqualTo("ERROR");
	}

	private LinewiseTextScanner createScanner(String text)
	{
		return new LinewiseTextScanner(new String[]
		{
			text
		});
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
		private String supression = "";
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

		DdmFieldBuilder withSupression(String supression)
		{
			this.supression = supression;
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

		LinewiseTextScanner build()
		{
			return new LinewiseTextScanner(new String[]
			{
				String.format(
					"%-1s %-1s %-2s %-32s  %-1s %4s  %-1s %-1s %-24s",
					type,
					level,
					shortname,
					name,
					format,
					length,
					supression,
					descriptor,
					remark
				)
			});
		}
	}
}
