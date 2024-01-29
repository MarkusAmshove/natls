package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.DataType;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DataTypeShould
{
	@TestFactory
	Iterable<DynamicTest> parseTypes()
	{
		return List.of(
			test("A10", DataFormat.ALPHANUMERIC, 10.0),
			test("A50", DataFormat.ALPHANUMERIC, 50.0),
			test("A", DataFormat.ALPHANUMERIC, 1),
			test("L", DataFormat.LOGIC, 1),
			test("N12,7", DataFormat.NUMERIC, 12.7),
			test("N12.7", DataFormat.NUMERIC, 12.7)
		);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"A10,10", "B10,10", "N12,12", "N12.2,15", "U8,8", "C,1", "L,1", "D,8", "T,8", "F4,13", "F8,22", "I1,3", "I2,5", "I4,10", "P8,5", "P12,7"
		}
	)
	void calculateTheAlphanumericLengthOfDataTypes(String datatype, int expectedLength)
	{
		var type = DataType.fromString(datatype);
		assertThat(type.alphanumericLength()).isEqualTo(expectedLength);
	}

	private DynamicTest test(String source, DataFormat expectedFormat, double expectedLength)
	{
		return DynamicTest.dynamicTest(source, () -> assertType(source, expectedFormat, expectedLength));
	}

	private void assertType(String source, DataFormat expectedFormat, double expectedLength)
	{
		var parsedType = DataType.fromString(source);
		assertThat(parsedType.format()).isEqualTo(expectedFormat);
		assertThat(parsedType.length()).isEqualTo(expectedLength);
	}

	private record TypeParseTest(String source, DataFormat expectedFormat, double expectedLength)
	{}
}
