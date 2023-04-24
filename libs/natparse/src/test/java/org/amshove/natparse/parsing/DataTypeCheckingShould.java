package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IDataType;
import org.junit.jupiter.params.ParameterizedTest;
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
