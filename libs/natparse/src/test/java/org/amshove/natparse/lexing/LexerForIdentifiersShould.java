package org.amshove.natparse.lexing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
	void recognizeAivVariablesWithMultipleSymbols()
	{
		assertTokens("+#MY-AIV", token(SyntaxKind.IDENTIFIER, "+#MY-AIV"));
	}

	@Test
	void recognizeHyphensInNames()
	{
		assertTokens("MY-VAR", token(SyntaxKind.IDENTIFIER, "MY-VAR"));
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
	void recognizeCountVariables()
	{
		assertTokens("C*NAMES", token(SyntaxKind.IDENTIFIER, "C*NAMES"));
	}

	@Test
	void notAllowCountAsteriskToBeAtAnotherLocationThan2()
	{
		assertTokens("CN*AMES",
			token(SyntaxKind.IDENTIFIER, "CN"),
			token(SyntaxKind.ASTERISK),
			token(SyntaxKind.IDENTIFIER, "AMES")
		);
	}

	@Test
	void recognizeVariablesWithCommercialAt()
	{
		assertTokens("message-me@mail", token(SyntaxKind.IDENTIFIER, "message-me@mail"));
	}

	@Test
	void stopIdentifiersWhenANonIdentifierCharacterIsFound()
	{
		assertTokens("#DATE'test'", token(SyntaxKind.IDENTIFIER, "#DATE"), token(SyntaxKind.STRING_LITERAL));
	}

	@Test
	void stopIdentifiersWhenACommentIsFollowing()
	{
		// Slash is a valid character for identifiers, but * is not.
		assertTokens("#MYVAR/*Asd", token(SyntaxKind.IDENTIFIER, "#MYVAR"));
	}

	@Test
	void stopIdentifierWhenACommentIsFollowingWithWhitespace()
	{
		assertTokens("INCLUDE IDEN/*", token(SyntaxKind.INCLUDE), token(SyntaxKind.IDENTIFIER, "IDEN"));
	}

	@Test
	void safelyAssumeIdentifiersIfTheTokenHasMultipleDashes()
	{
		assertTokens("END-DEFINE-DEFINE", token(SyntaxKind.IDENTIFIER));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"R1.", "R.", "R-1.", "#like-a-var.", "another-var.", "A.", "X.", "A123.", "#WAT."
	})
	void recognizeJumpLabelsAsLabelIdentifiers(String source)
	{
		assertTokens(source, SyntaxKind.LABEL_IDENTIFIER);
	}

	@Test
	void addADiagnosticForRecognizedIdentifiersThatEndWithADot()
	{
		assertDiagnostic("C*WHODOESTHIS.", assertedDiagnostic(0,0,0,14, LexerError.INVALID_IDENTIFIER));
	}

	@Test
	void notRecognizeArithmeticAsVariable()
	{
		assertTokens("+123", token(SyntaxKind.PLUS), token(SyntaxKind.NUMBER_LITERAL));
	}

	@Test
	void recognizeTheStartOfAnArithmeticExpressionWithinAVariableAndBreakIt()
	{
		assertTokens("MYVAR+123", token(SyntaxKind.IDENTIFIER), token(SyntaxKind.PLUS), token(SyntaxKind.NUMBER_LITERAL));
	}

	@Test
	void notAddCommasToIdentifiers()
	{
		assertTokens(
			"FIRST-IDENTIFIER-HERE,SECOND-IDENTIFIER-HERE",
			token(SyntaxKind.IDENTIFIER),
			token(SyntaxKind.COMMA),
			token(SyntaxKind.IDENTIFIER)
		);
	}

	@Test
	void notAddCommasToIdentifiersWithSpaceAfterComma()
	{
		assertTokens(
			"FIRST-IDENTIFIER-HERE, SECOND-IDENTIFIER-HERE",
			token(SyntaxKind.IDENTIFIER),
			token(SyntaxKind.COMMA),
			token(SyntaxKind.IDENTIFIER)
		);
	}

	@Test
	void allowCStarInQualifiedVariables()
	{
		assertTokens(
			"#FIRST-IDENTIFIER-HERE.C*SECOND-IDENTIFIER-HERE END",
			token(SyntaxKind.IDENTIFIER),
			token(SyntaxKind.END)
		);
	}

	@Test
	void notIncludeTheCommaInArrayIndexNotation()
	{
		assertTokens("LAPDA.LAVARIABLE(I-INDEX-VAR,*)",
			token(SyntaxKind.IDENTIFIER),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.IDENTIFIER, "I-INDEX-VAR"),
			token(SyntaxKind.COMMA),
			token(SyntaxKind.ASTERISK),
			token(SyntaxKind.RPAREN)
		);
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
