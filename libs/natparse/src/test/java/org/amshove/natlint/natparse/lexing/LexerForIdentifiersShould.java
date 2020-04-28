package org.amshove.natlint.natparse.lexing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;

import static org.junit.jupiter.api.DynamicTest.*;

public class LexerForIdentifiersShould extends AbstractLexerTest
{
	@Test
	void recognizeIdentifiersStartingWithHash()
	{
		assertTokens("#NAME", token(SyntaxKind.IDENTIFIER, "#NAME"));
	}

	@Test
	void recognizeIdentifiersContainHashes()
	{
		assertTokens("#SUR#NAME", token(SyntaxKind.IDENTIFIER, "#SUR#NAME"));
	}

	@Test
	void recognizeIdentifiersStartingWithAmpersand()
	{
		assertTokens("&NAME", token(SyntaxKind.IDENTIFIER, "&NAME"));
	}

	@Test
	void recognizeIdentifiersContainingAmpersands()
	{
		assertTokens("&GLOBAL&VARIABLE", token(SyntaxKind.IDENTIFIER, "&GLOBAL&VARIABLE"));
	}

	@Test
	void recognizeAivVariables()
	{
		assertTokens("+MY-AIV", token(SyntaxKind.IDENTIFIER, "+MY-AIV"));
	}

	@Test
	void recognizeHyphensInNames()
	{
		assertTokens("MY-VAR", token(SyntaxKind.IDENTIFIER_OR_KEYWORD, "MY-VAR"));
	}

	@Test
	void recognizeUnderscoreInNames()
	{
		assertTokens("SPECIAL_SNAKE", token(SyntaxKind.IDENTIFIER, "SPECIAL_SNAKE"));
	}

	@Test
	void recognizeDollarSignsInNames()
	{
		assertTokens("MUCH$MONEY", token(SyntaxKind.IDENTIFIER, "MUCH$MONEY"));
	}

	@Test
	void recognizeVariablesWithSlash()
	{
		assertTokens("SOME/PATH", token(SyntaxKind.IDENTIFIER, "SOME/PATH"));
	}

	@Test
	void recognizeVariablesWithCommercialAt()
	{
		assertTokens("message-me@mail", token(SyntaxKind.IDENTIFIER, "message-me@mail"));
	}

	@TestFactory
	Iterable<DynamicTest> recognizeQualifiedVariables()
	{
		return Arrays.asList(
			dynamicTest("plain qualified variable", () -> assertTokens("QUALIFIER.VARIABLE", token(SyntaxKind.IDENTIFIER, "QUALIFIER.VARIABLE"))),
			dynamicTest("with hyphen in qualifier", () -> assertTokens("QUALIFIED-VARIABLE.VARI", token(SyntaxKind.IDENTIFIER, "QUALIFIED-VARIABLE.VARI"))),
			dynamicTest("with hyphen in variable", () -> assertTokens("QUALIFIER.VARI-ABLE", token(SyntaxKind.IDENTIFIER, "QUALIFIER.VARI-ABLE"))),
			dynamicTest("with hypen in both", () -> assertTokens("QUALI-FIER.VARI-ABLE", token(SyntaxKind.IDENTIFIER, "QUALI-FIER.VARI-ABLE"))));
	}
}
