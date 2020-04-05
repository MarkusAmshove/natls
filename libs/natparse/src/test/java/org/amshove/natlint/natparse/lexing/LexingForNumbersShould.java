package org.amshove.natlint.natparse.lexing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class LexingForNumbersShould extends AbstractLexerTest
{
	@TestFactory
	Iterable<DynamicTest> lexNumbersWithoutDecimalDelimiter()
	{
		return Arrays.asList(
			dynamicTest("1", () -> assertTokens("1", token(SyntaxKind.NUMBER, "1"))),
			dynamicTest("10", () -> assertTokens("10", token(SyntaxKind.NUMBER, "10"))),
			dynamicTest("100", () -> assertTokens("100", token(SyntaxKind.NUMBER, "100"))),
			dynamicTest("12324567890", () -> assertTokens("1234567890", token(SyntaxKind.NUMBER, "1234567890"))));
	}

	@TestFactory
	Iterable<DynamicTest> lexNumbersWithDecimalPoint()
	{
		return Arrays.asList(
			dynamicTest("1.0", () -> assertTokens("1.0", token(SyntaxKind.NUMBER, "1.0"))),
			dynamicTest("1.0001", () -> assertTokens("1.0001", token(SyntaxKind.NUMBER, "1.0001"))),
			dynamicTest("1.10101", () -> assertTokens("1.10101", token(SyntaxKind.NUMBER, "1.10101"))),
			dynamicTest("1.001010", () -> assertTokens("1.001010", token(SyntaxKind.NUMBER, "1.001010"))),
			dynamicTest("1.10101", () -> assertTokens("1.10101", token(SyntaxKind.NUMBER, "1.10101"))),
			dynamicTest("1.001010", () -> assertTokens("1.001010", token(SyntaxKind.NUMBER, "1.001010"))),
			dynamicTest("1002002.001010",
				() -> assertTokens("1002002.001010", token(SyntaxKind.NUMBER, "1002002.001010"))),
			dynamicTest("05121.001010",
				() -> assertTokens("05121.001010", token(SyntaxKind.NUMBER, "05121.001010"))));
	}

	@TestFactory
	Iterable<DynamicTest> lexNumbersWithDecimalComma()
	{
		return Arrays.asList(
			dynamicTest("1", () -> assertTokens("1", token(SyntaxKind.NUMBER, "1"))),
			dynamicTest("1,0", () -> assertTokens("1,0", token(SyntaxKind.NUMBER, "1,0"))),
			dynamicTest("1,0001", () -> assertTokens("1,0001", token(SyntaxKind.NUMBER, "1,0001"))),
			dynamicTest("1,10101", () -> assertTokens("1,10101", token(SyntaxKind.NUMBER, "1,10101"))),
			dynamicTest("1,001010", () -> assertTokens("1,001010", token(SyntaxKind.NUMBER, "1,001010"))),
			dynamicTest("1002002,001010",
				() -> assertTokens("1002002,001010", token(SyntaxKind.NUMBER, "1002002,001010"))),
			dynamicTest("05121,001010",
				() -> assertTokens("05121,001010", token(SyntaxKind.NUMBER, "05121,001010"))));
	}
}
