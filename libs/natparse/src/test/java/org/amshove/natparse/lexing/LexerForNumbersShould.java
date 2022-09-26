package org.amshove.natparse.lexing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class LexerForNumbersShould extends AbstractLexerTest
{
	@TestFactory
	Iterable<DynamicTest> lexNumbersWithoutDecimalDelimiter()
	{
		return Arrays.asList(
			dynamicTest("1", () -> assertTokens("1", token(SyntaxKind.NUMBER_LITERAL, "1"))),
			dynamicTest("10", () -> assertTokens("10", token(SyntaxKind.NUMBER_LITERAL, "10"))),
			dynamicTest("100", () -> assertTokens("100", token(SyntaxKind.NUMBER_LITERAL, "100"))),
			dynamicTest("12324567890", () -> assertTokens("1234567890", token(SyntaxKind.NUMBER_LITERAL, "1234567890"))));
	}

	@TestFactory
	Iterable<DynamicTest> lexNumbersWithDecimalPoint()
	{
		return Arrays.asList(
			dynamicTest("1.0", () -> assertTokens("1.0", token(SyntaxKind.NUMBER_LITERAL, "1.0"))),
			dynamicTest("1.0001", () -> assertTokens("1.0001", token(SyntaxKind.NUMBER_LITERAL, "1.0001"))),
			dynamicTest("1.10101", () -> assertTokens("1.10101", token(SyntaxKind.NUMBER_LITERAL, "1.10101"))),
			dynamicTest("1.001010", () -> assertTokens("1.001010", token(SyntaxKind.NUMBER_LITERAL, "1.001010"))),
			dynamicTest("1.10101", () -> assertTokens("1.10101", token(SyntaxKind.NUMBER_LITERAL, "1.10101"))),
			dynamicTest("1.001010", () -> assertTokens("1.001010", token(SyntaxKind.NUMBER_LITERAL, "1.001010"))),
			dynamicTest("1002002.001010",
				() -> assertTokens("1002002.001010", token(SyntaxKind.NUMBER_LITERAL, "1002002.001010"))),
			dynamicTest("05121.001010",
				() -> assertTokens("05121.001010", token(SyntaxKind.NUMBER_LITERAL, "05121.001010"))));
	}

	@TestFactory
	Iterable<DynamicTest> lexNumbersWithDecimalComma()
	{
		return Arrays.asList(
			dynamicTest("1", () -> assertTokens("1", token(SyntaxKind.NUMBER_LITERAL, "1"))),
			dynamicTest("1,0", () -> assertTokens("1,0", token(SyntaxKind.NUMBER_LITERAL, "1,0"))),
			dynamicTest("1,0001", () -> assertTokens("1,0001", token(SyntaxKind.NUMBER_LITERAL, "1,0001"))),
			dynamicTest("1,10101", () -> assertTokens("1,10101", token(SyntaxKind.NUMBER_LITERAL, "1,10101"))),
			dynamicTest("1,001010", () -> assertTokens("1,001010", token(SyntaxKind.NUMBER_LITERAL, "1,001010"))),
			dynamicTest("1002002,001010",
				() -> assertTokens("1002002,001010", token(SyntaxKind.NUMBER_LITERAL, "1002002,001010"))),
			dynamicTest("05121,001010",
				() -> assertTokens("05121,001010", token(SyntaxKind.NUMBER_LITERAL, "05121,001010"))));
	}

	@TestFactory
	Iterable<DynamicTest> lexAllSingleDigitNumbers()
	{
		return Arrays.asList(
			dynamicTest("1", () -> assertTokens("1", token(SyntaxKind.NUMBER_LITERAL, "1"))),
			dynamicTest("2", () -> assertTokens("2", token(SyntaxKind.NUMBER_LITERAL, "2"))),
			dynamicTest("3", () -> assertTokens("3", token(SyntaxKind.NUMBER_LITERAL, "3"))),
			dynamicTest("4", () -> assertTokens("4", token(SyntaxKind.NUMBER_LITERAL, "4"))),
			dynamicTest("5", () -> assertTokens("5", token(SyntaxKind.NUMBER_LITERAL, "5"))),
			dynamicTest("6", () -> assertTokens("6", token(SyntaxKind.NUMBER_LITERAL, "6"))),
			dynamicTest("7", () -> assertTokens("7", token(SyntaxKind.NUMBER_LITERAL, "7"))),
			dynamicTest("8", () -> assertTokens("8", token(SyntaxKind.NUMBER_LITERAL, "8"))),
			dynamicTest("9", () -> assertTokens("9", token(SyntaxKind.NUMBER_LITERAL, "9"))),
			dynamicTest("0", () -> assertTokens("0", token(SyntaxKind.NUMBER_LITERAL, "0"))));
	}

	@TestFactory
	Iterable<DynamicTest> lexNegativeNumbersAsMultipleTokens()
	{
		return Arrays.asList(
			dynamicTest("-1", () -> assertTokens("-1", token(SyntaxKind.MINUS, "-"), token(SyntaxKind.NUMBER_LITERAL, "1"))),
			dynamicTest("-2,3", () -> assertTokens("-2,3", token(SyntaxKind.MINUS, "-"), token(SyntaxKind.NUMBER_LITERAL, "2,3"))),
			dynamicTest("-15", () -> assertTokens("-15", token(SyntaxKind.MINUS, "-"), token(SyntaxKind.NUMBER_LITERAL, "15"))),
			dynamicTest("-1.2", () -> assertTokens("-1.2", token(SyntaxKind.MINUS, "-"), token(SyntaxKind.NUMBER_LITERAL, "1.2")))
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"5X", "2x"})
	void recognizeSkipPatterns(String skip)
	{
		assertTokens(skip, token(SyntaxKind.OPERAND_SKIP, skip));
	}

	@ParameterizedTest
	@ValueSource(strings = {"5T", "25T"})
	void recognizeTabSettings(String setting)
	{
		assertTokens(setting, token(SyntaxKind.TAB_SETTING, setting));
	}

	@ParameterizedTest
	@ValueSource(strings = {"1.5E+5", "0.2E-15", "0.22E+1"})
	void recognizeFloatingFormats(String format)
	{
		assertTokens(format, token(SyntaxKind.NUMBER_LITERAL, format));
	}

	@Test
	void notAddACommaIfNoNumberFollows()
	{
		assertTokens("5,#HI", token(SyntaxKind.NUMBER_LITERAL), token(SyntaxKind.COMMA), token(SyntaxKind.IDENTIFIER));
	}
}
