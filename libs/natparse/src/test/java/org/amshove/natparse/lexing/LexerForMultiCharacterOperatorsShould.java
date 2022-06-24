package org.amshove.natparse.lexing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class LexerForMultiCharacterOperatorsShould extends AbstractLexerTest
{
	@Test
	void lexColonEqualsAssign()
	{
		assertTokens(":=", token(SyntaxKind.COLON_EQUALS, ":="));
	}

	@Test
	void lexGreaterEqualsWithSigns()
	{
		assertTokens(">=", token(SyntaxKind.GREATER_EQUALS_SIGN, ">="));
	}

	@TestFactory
	Iterable<DynamicTest> lexGreaterEqualsKeywords()
	{
		return Arrays.asList(
			dynamicTest("GE", () -> assertTokens("GE", token(SyntaxKind.GE, "GE"))),
			dynamicTest("ge", () -> assertTokens("ge", token(SyntaxKind.GE, "ge"))),
			dynamicTest("gE", () -> assertTokens("gE", token(SyntaxKind.GE, "gE"))),
			dynamicTest("Ge", () -> assertTokens("Ge", token(SyntaxKind.GE, "Ge"))));
	}

	@TestFactory
	Iterable<DynamicTest> lexGreaterThanKeywords()
	{
		return Arrays.asList(
			dynamicTest("GT", () -> assertTokens("GT", token(SyntaxKind.GT, "GT"))),
			dynamicTest("gt", () -> assertTokens("gt", token(SyntaxKind.GT, "gt"))),
			dynamicTest("gT", () -> assertTokens("gT", token(SyntaxKind.GT, "gT"))),
			dynamicTest("Gt", () -> assertTokens("Gt", token(SyntaxKind.GT, "Gt"))));
	}

	@Test
	void lexLessEqualsWithSigns()
	{
		assertTokens("<=", token(SyntaxKind.LESSER_EQUALS_SIGN, "<="));
	}

	@TestFactory
	Iterable<DynamicTest> lexLessEqualsKeywords()
	{
		return Arrays.asList(
			dynamicTest("LE", () -> assertTokens("LE", token(SyntaxKind.LE, "LE"))),
			dynamicTest("le", () -> assertTokens("le", token(SyntaxKind.LE, "le"))),
			dynamicTest("lE", () -> assertTokens("lE", token(SyntaxKind.LE, "lE"))),
			dynamicTest("Le", () -> assertTokens("Le", token(SyntaxKind.LE, "Le"))));
	}

	@TestFactory
	Iterable<DynamicTest> lexLessThanKeywords()
	{
		return Arrays.asList(
			dynamicTest("LT", () -> assertTokens("LT", token(SyntaxKind.LT, "LT"))),
			dynamicTest("lt", () -> assertTokens("lt", token(SyntaxKind.LT, "lt"))),
			dynamicTest("lT", () -> assertTokens("lT", token(SyntaxKind.LT, "lT"))),
			dynamicTest("Lt", () -> assertTokens("Lt", token(SyntaxKind.LT, "Lt"))));
	}

	@TestFactory
	Iterable<DynamicTest> lexEqualsKeywords()
	{
		return Arrays.asList(
			dynamicTest("EQ", () -> assertTokens("EQ", token(SyntaxKind.EQ, "EQ"))),
			dynamicTest("eq", () -> assertTokens("eq", token(SyntaxKind.EQ, "eq"))),
			dynamicTest("eQ", () -> assertTokens("eQ", token(SyntaxKind.EQ, "eQ"))),
			dynamicTest("Eq", () -> assertTokens("Eq", token(SyntaxKind.EQ, "Eq"))));
	}

	@Test
	void lexNotEqualsSigns()
	{
		assertTokens("<>", token(SyntaxKind.LESSER_GREATER, "<>"));
	}

	@TestFactory
	Iterable<DynamicTest> lexNotEqualsKeywords()
	{
		return Arrays.asList(
			dynamicTest("NE", () -> assertTokens("NE", token(SyntaxKind.NE, "NE"))),
			dynamicTest("ne", () -> assertTokens("ne", token(SyntaxKind.NE, "ne"))),
			dynamicTest("nE", () -> assertTokens("nE", token(SyntaxKind.NE, "nE"))),
			dynamicTest("Ne", () -> assertTokens("Ne", token(SyntaxKind.NE, "Ne"))));
	}
}
