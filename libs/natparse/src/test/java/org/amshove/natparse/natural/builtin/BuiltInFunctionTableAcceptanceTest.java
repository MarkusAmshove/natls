package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.lexing.SyntaxKind;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class BuiltInFunctionTableAcceptanceTest
{
	@TestFactory
	Stream<DynamicContainer> everySystemFunctionAndVariableShouldBeInTheTable()
	{
		return Arrays.stream(SyntaxKind.values())
			.filter(sk -> sk.isSystemFunction() || sk.isSystemVariable())
			.map(
				k -> dynamicContainer(
					k.toString(), Stream.of(
						dynamicTest("%s should be represented".formatted(k), () -> assertThat(BuiltInFunctionTable.getDefinition(k)).isNotNull()),
						dynamicTest("%s should have a name".formatted(k), () -> assertThat(BuiltInFunctionTable.getDefinition(k).name()).isNotNull()),
						dynamicTest("%s should have documentation".formatted(k), () -> assertThat(BuiltInFunctionTable.getDefinition(k).documentation()).isNotNull()),
						dynamicTest("%s should have a data format".formatted(k), () -> assertThat(BuiltInFunctionTable.getDefinition(k).type()).isNotNull())
					)
				)
			);
	}
}
