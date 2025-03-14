package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.DataType;
import org.amshove.natparse.natural.IDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DataTypeCheckingShould
{
	@ParameterizedTest
	@ValueSource(strings =
	{
		"F", "I", "N", "P"
	})
	void seeNumericFamily(String type)
	{
		var format = type(DataFormat.fromSource(type), 8);

		assertThat(format.isNumericFamily())
			.as("%s is numeric family".formatted(format.toShortString()))
			.isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"A", "B", "C", "D", "L", "T", "U"
	})
	void seeNotNumericFamily(String type)
	{
		var format = type(DataFormat.fromSource(type), 8);

		assertThat(format.isNumericFamily())
			.as("%s is numeric family".formatted(format.toShortString()))
			.isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"A", "B", "U"
	})
	void seeAlphanumericFamily(String type)
	{
		var format = type(DataFormat.fromSource(type), 8);

		assertThat(format.isAlphaNumericFamily())
			.as("%s is alphanumeric family".formatted(format.toShortString()))
			.isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"C", "D", "F", "I", "L", "N", "P"
	})
	void seeNotAlphanumericFamily(String type)
	{
		var format = type(DataFormat.fromSource(type), 8);

		assertThat(format.isAlphaNumericFamily())
			.as("%s is alphanumeric family".formatted(format.toShortString()))
			.isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"A", "B", "F", "I", "N", "P"
	})
	void seeSameDataTypeAndLengthAsCompatible(String type)
	{
		assertCompatible(
			type(DataFormat.fromSource(type), 8),
			type(DataFormat.fromSource(type), 8)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"A", "B", "F", "I", "N", "P"
	})
	void seeSameDataTypeWithSmallerLengthAsCompatible(String type)
	{
		assertCompatible(
			type(DataFormat.fromSource(type), 4),
			type(DataFormat.fromSource(type), 8)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"A", "B", "F", "I", "N", "P"
	})
	void seeSameDataTypeWithBiggerLengthAsIncompatible(String type)
	{
		assertNotCompatible(
			type(DataFormat.fromSource(type), 8),
			type(DataFormat.fromSource(type), 4)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"C", "D", "L", "T", "U"
	})
	void seeSameDataTypeWithoutExplicitLengthAsCompatible(String type)
	{
		assertCompatible(
			type(DataFormat.fromSource(type), 0),
			type(DataFormat.fromSource(type), 0)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"N", "P", "I"
	})
	void seeImplicitlyCompatibleDataTypesAsCompatibleWithSameLength(String numericType)
	{
		assertCompatible(
			type(DataFormat.fromSource(numericType), 8),
			type(DataFormat.ALPHANUMERIC, 8)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"N", "P", "I"
	})
	void seeImplicitlyCompatibleDataTypesAsCompatibleWithSmallerLength(String numericType)
	{
		assertCompatible(
			type(DataFormat.fromSource(numericType), 4),
			type(DataFormat.ALPHANUMERIC, 8)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"N", "P", "I"
	})
	void seeImplicitlyCompatibleDataTypesAsCompatibleIfTargetIsDynamic(String numericType)
	{
		assertCompatible(
			type(DataFormat.fromSource(numericType), 4),
			dynamicType(DataFormat.ALPHANUMERIC)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"N", "P", "I"
	})
	void seeImplicitlyCompatibleDataTypesAsIncompatibleWithBiggerLength(String numericType)
	{
		assertNotCompatible(
			type(DataFormat.fromSource(numericType), 8),
			type(DataFormat.ALPHANUMERIC, 4)
		);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"N,N", "N,I", "N,F", "N,P", "N,T",
			"P,N", "P,I", "P,F", "P,P", "P,T",
			"T,N", "T,I", "T,F", "T,P", "T,T",
			"A,A", "A,U", "A,B", "B,B",
			"U,A", "U,B", "U,U"
		}
	)
	void recognizeDataFormatsAsTheSameFamily(String firstFormat, String secondFormat)
	{
		assertSameFamily(firstFormat, secondFormat);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"N,A", "N,U", "N,L", "N,C", "N,D",
			"A,L", "A,C", "A,D", "A,F", "A,I", "A,P", "A,T",
			"I,A", "I,U", "I,L", "I,C", "I,D",
			"P,A", "P,U", "P,L", "P,C", "P,D",
			"F,A", "F,U", "F,L", "F,C", "F,D",
			"L,B", "L,D", "L,T", "L,U", "L,A", "L,P"
		}
	)
	void recognizeDataFormatsAsDifferentFamily(String firstFormat, String secondFormat)
	{
		assertDifferentFamily(firstFormat, secondFormat);
	}

	@Test
	void recognizeIntegersNotFittingIntoNumerics()
	{
		assertNotCompatible(
			type(DataFormat.INTEGER, 2),
			type(DataFormat.NUMERIC, 1)
		);
	}

	@Test
	void recognizeTimeCompatibleWithAlphanumeric()
	{
		assertCompatible(
			type(DataFormat.TIME, 0),
			type(DataFormat.ALPHANUMERIC, 8)
		);
	}

	@Test
	void recognizeDateCompatibleWithAlphanumeric()
	{
		assertCompatible(
			type(DataFormat.DATE, 0),
			type(DataFormat.ALPHANUMERIC, 8)
		);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"N6.2,N8",
			"N8,N6.2",
			"N4.1,N12"
		}
	)
	void notSeeNumericWithAndWithoutFloatingPrecisionAsCompatible(String firstType, String secondType)
	{
		assertNotCompatible(
			DataType.fromString(firstType),
			DataType.fromString(secondType)
		);
	}

	private void assertSameFamily(String firstFormat, String secondFormat)
	{
		assertThat(type(DataFormat.fromSource(firstFormat), IDataType.ONE_GIGABYTE).hasSameFamily(type(DataFormat.fromSource(secondFormat), IDataType.ONE_GIGABYTE)))
			.as("Expected %s and %s to be the same format family".formatted(firstFormat, secondFormat))
			.isTrue();
	}

	private void assertDifferentFamily(String firstFormat, String secondFormat)
	{
		assertThat(type(DataFormat.fromSource(firstFormat), IDataType.ONE_GIGABYTE).hasSameFamily(type(DataFormat.fromSource(secondFormat), IDataType.ONE_GIGABYTE)))
			.as("Expected %s and %s to not be the same format family".formatted(firstFormat, secondFormat))
			.isFalse();
	}

	private void assertCompatible(IDataType firstType, IDataType targetType)
	{
		assertThat(firstType.fitsInto(targetType))
			.as("Expected %s to fit into %s".formatted(firstType.toShortString(), targetType.toShortString()))
			.isTrue();
	}

	private void assertNotCompatible(IDataType firstType, IDataType targetType)
	{
		assertThat(firstType.fitsInto(targetType))
			.as("Expected %s to NOT fit into %s".formatted(firstType.toShortString(), targetType.toShortString()))
			.isFalse();
	}

	private IDataType type(DataFormat format, double length)
	{
		return new CheckedDataType(format, length);
	}

	private IDataType dynamicType(DataFormat format)
	{
		return new CheckedDataType(format, IDataType.ONE_GIGABYTE);
	}

	record CheckedDataType(DataFormat format, double length) implements IDataType
	{
		@Override
		public boolean hasDynamicLength()
		{
			return length == IDataType.ONE_GIGABYTE;
		}
	}
}
