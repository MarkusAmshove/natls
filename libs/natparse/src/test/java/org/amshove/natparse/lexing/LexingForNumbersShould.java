package org.amshove.natparse.lexing;

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

	@TestFactory
	Iterable<DynamicTest> lexAllSingleDigitNumbers()
	{
		return Arrays.asList(
			dynamicTest("1", () -> assertTokens("1", token(SyntaxKind.NUMBER, "1"))),
			dynamicTest("2", () -> assertTokens("2", token(SyntaxKind.NUMBER, "2"))),
			dynamicTest("3", () -> assertTokens("3", token(SyntaxKind.NUMBER, "3"))),
			dynamicTest("4", () -> assertTokens("4", token(SyntaxKind.NUMBER, "4"))),
			dynamicTest("5", () -> assertTokens("5", token(SyntaxKind.NUMBER, "5"))),
			dynamicTest("6", () -> assertTokens("6", token(SyntaxKind.NUMBER, "6"))),
			dynamicTest("7", () -> assertTokens("7", token(SyntaxKind.NUMBER, "7"))),
			dynamicTest("8", () -> assertTokens("8", token(SyntaxKind.NUMBER, "8"))),
			dynamicTest("9", () -> assertTokens("9", token(SyntaxKind.NUMBER, "9"))),
			dynamicTest("0", () -> assertTokens("0", token(SyntaxKind.NUMBER, "0"))));
	}
}
