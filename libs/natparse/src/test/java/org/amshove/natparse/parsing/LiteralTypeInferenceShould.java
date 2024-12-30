package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.DataType;
import org.amshove.natparse.natural.ILiteralNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class LiteralTypeInferenceShould
{
	@ParameterizedTest
	@CsvSource(
		{
			"-126,I1",
			"127,I1",
			"200,I2",
			"12345,I2",
			"2147483647,I4",
			"2147483648,N10",
			"1,I4"
		}
	)
	void inferTheCorrectTypeBasedOnTargetTypeForIntegers(String source, String targetType)
	{
		assertCompatibleType(targetType, source);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"-126,B1",
			"127,B1",
			"200,B2",
			"12345,B2",
			"2147483647,B4",
			"2147483648,P10",
		}
	)
	void inferTheCorrectTypeBasedOnTargetTypeForBinary(String source, String targetType)
	{
		assertCompatibleType(targetType, source);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"-126",
			"127",
			"200",
			"12345",
			"2147483647"
		}
	)
	// TODO: What about actual decimal numbers?
	void inferTheCorrectTypeBasedOnTargetTypeForFloats(String source)
	{
		assertCompatibleType("F8", source);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"-126,P3",
			"127,P3",
			"200,P3",
			"12345,P5",
			"2147483647,P10",
			"2147483648,P10",
			"2147483648.123,P10.3",
		}
	)
	void inferTheCorrectTypeBasedOnTargetTypeForPacked(String source, String targetType)
	{
		assertCompatibleType(targetType, source);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"TRUE", "FALSE"
	})
	void inferBooleans(String bool)
	{
		assertInferredType("L", bool, "L");
	}

	@ParameterizedTest
	@CsvSource(
		{
			"12345",
			"2147483647",
			"2147483648",
			"2147483648.123",
		}
	)
	void inferTheCorrectTypeBasedOnTargetTypeForNumerics(String source)
	{
		assertCompatibleType("N10.3", source);
	}

	@Test
	void ignoreLeadingZeroesWhenInferringNumerics()
	{
		assertReInferredType("N1", "007", "N1");
	}

	@ParameterizedTest
	@CsvSource(
		{
			"12345,I2",
			"2147483647,I4",
			"2147483648,N10",
			"2147483648.123,N10.3",
			"1,N8"
		}
	)
	void inferTheCorrectTypeForNumericLiterals(String source, String targetType)
	{
		assertCompatibleType(targetType, source);
	}

	@Test
	void reInferTheTypeOfNumericLiteralsToTheBiggerTargetType()
	{
		assertReInferredType("N8", "1", "N8");
	}

	@Test
	void reInferTheTypeOfUntrimmedStringLiteralsWhenTargetTypeIsBiggerThanTrimmedSize()
	{
		// The literal has a possible length of 1-10, depending on if it gets
		// trimmed by the runtime. If we take the target type A8 into account,
		// then it has to be seen as the biggest possible type.
		assertReInferredType("A8", "'A         '", "A10");
	}

	private void assertInferredType(String targetType, String source, String expectedInferredType)
	{
		var typedTarget = createType(targetType);
		var expectedType = createType(expectedInferredType);

		var literal = literal(source);
		var inferredType = literal.inferType();

		assertThat(inferredType.format())
			.as("Expected the inferred DataFormat to match")
			.isEqualTo(expectedType.format());

		if (typedTarget.length() > 0.0)
		{
			assertThat(inferredType.length())
				.as("Expected the inferred length to match")
				.isEqualTo(expectedType.length());
		}
	}

	private void assertReInferredType(String targetType, String source, String expectedInferredType)
	{
		var typedTarget = createType(targetType);
		var expectedType = createType(expectedInferredType);

		var literal = literal(source);
		var reInferredType = literal.reInferType(typedTarget);

		assertThat(reInferredType.format())
			.as("Expected the inferred DataFormat to match")
			.isEqualTo(expectedType.format());

		if (typedTarget.length() > 0.0)
		{
			assertThat(reInferredType.length())
				.as("Expected the inferred length to match")
				.isEqualTo(expectedType.length());
		}
	}

	private void assertCompatibleType(String targetType, String source)
	{
		var typedTarget = createType(targetType);

		var literal = literal(source);
		var inferredType = literal.inferType();
		assertThat(inferredType.fitsInto(typedTarget))
			.as("Expected the inferred type %s to fit into %s (e.g. having implicit conversion)".formatted(inferredType, typedTarget))
			.isTrue();
	}

	private DataType createType(String typeSource)
	{
		var length = typeSource.length() > 1
			? Double.parseDouble(typeSource.substring(1))
			: 0;
		return new DataType(DataFormat.fromSource(typeSource.charAt(0)), length);
	}

	private ILiteralNode literal(String source)
	{
		var lexer = new Lexer();
		var tokens = lexer.lex(source, Path.of("dummy"));
		assertThat(tokens.diagnostics()).as("Expected to lex without diagnostics").isEmpty();
		var token = tokens.peek();
		if (token.kind() == SyntaxKind.MINUS)
		{
			token = tokens.peek(1);
		}
		return new LiteralNode(token);
	}
}
