package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class VariableScopeShould
{
	private static Stream<Arguments> validMappings()
	{
		return Stream.of(
			Arguments.of(SyntaxKind.LOCAL, VariableScope.LOCAL),
			Arguments.of(SyntaxKind.GLOBAL, VariableScope.GLOBAL),
			Arguments.of(SyntaxKind.PARAMETER, VariableScope.PARAMETER)
			//			Arguments.of(SyntaxKind.INDEPENDENT, VariableScope.INDEPENDENT)
		);
	}

	@ParameterizedTest
	@MethodSource("validMappings")
	void mapTheCorrectScope(SyntaxKind syntaxKind, VariableScope expectedScope)
	{
		assertThat(VariableScope.fromSyntaxKind(syntaxKind)).isEqualTo(expectedScope);
	}

	@Test
	void throwAnExceptionOnInvalidSyntaxKinds()
	{
		assertThatThrownBy(() -> VariableScope.fromSyntaxKind(SyntaxKind.ADD))
			.hasMessage("Could not determine VariableScope from SyntaxKind");
	}
}
