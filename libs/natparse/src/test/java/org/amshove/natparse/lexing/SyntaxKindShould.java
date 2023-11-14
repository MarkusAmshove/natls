package org.amshove.natparse.lexing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.*;

class SyntaxKindShould
{
	@TestFactory
	Stream<DynamicTest> knowAboutKeywordsThatStartAStatementThatHasAnEndKeyword()
	{
		return Stream.of(
			SyntaxKind.IF,
			SyntaxKind.DEFINE,
			SyntaxKind.DECIDE,
			SyntaxKind.REPEAT,
			SyntaxKind.READ,
			SyntaxKind.FIND,
			SyntaxKind.FOR
		)
			.map(sk -> dynamicTest("%s should be recognized".formatted(sk), () -> assertThat(sk.opensStatementWithCloseKeyword()).isTrue()));
	}

	@TestFactory
	Stream<DynamicTest> knowAboutStatementClosingKeywords()
	{
		return Stream.of(
			SyntaxKind.END_IF,
			SyntaxKind.END_FOR,
			SyntaxKind.END_DECIDE,
			SyntaxKind.END_DEFINE,
			SyntaxKind.END_BREAK,
			SyntaxKind.END_ERROR,
			SyntaxKind.END_FIND,
			SyntaxKind.END_READ,
			SyntaxKind.END_SUBROUTINE,
			SyntaxKind.END_REPEAT,
			SyntaxKind.END_WORK
		)
			.map(sk -> dynamicTest("%s should be recognized".formatted(sk), () -> assertThat(sk.closesStatement()).isTrue()));
	}
}
