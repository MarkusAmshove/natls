package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.DataType;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

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
