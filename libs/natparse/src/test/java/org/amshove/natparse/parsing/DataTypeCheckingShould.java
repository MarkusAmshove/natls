package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IDataType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DataTypeCheckingShould
{
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
			"N,N", "N,I", "N,F", "N,P", "N,B",
			"A,U", "A,B", "A,A"
		}
	)
	void recognizeDataFormatsAsTheSameFamily(String firstFormat, String secondFormat)
	{
		assertSameFamily(firstFormat, secondFormat);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"N,A", "N,U", "N,L", "N,C", "N,D", "N,T",
			"A,L", "A,C", "A,D", "A,F", "A,I", "A,P", "A,T",
			"I,A", "I,U", "I,L", "I,C", "I,D", "I,T",
			"P,A", "P,U", "P,L", "P,C", "P,D", "P,T",
			"F,A", "F,U", "F,L", "F,C", "F,D", "F,T",
			"L,B", "L,D", "L,T", "L,U", "L,A", "L,P"
		}
	)
	void recognizeDataFormatsAsDifferentFamily(String firstFormat, String secondFormat)
	{
		assertDifferentFamily(firstFormat, secondFormat);
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
		return new DataType(format, length);
	}

	private IDataType dynamicType(DataFormat format)
	{
		return new DataType(format, IDataType.ONE_GIGABYTE);
	}

	record DataType(DataFormat format, double length) implements IDataType
	{
		@Override
		public boolean hasDynamicLength()
		{
			return length == IDataType.ONE_GIGABYTE;
		}
	}
}
