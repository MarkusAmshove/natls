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
	void recognizeAivInParens()
	{
		assertTokens("(+THEAIV)", token(SyntaxKind.LPAREN), token(SyntaxKind.IDENTIFIER), token(SyntaxKind.RPAREN));
	}

	@Test
	void recognizeAivInArrayBounds()
	{
		assertTokens("(1:+THEAIV)", token(SyntaxKind.LPAREN), token(SyntaxKind.NUMBER_LITERAL), token(SyntaxKind.COLON), token(SyntaxKind.IDENTIFIER), token(SyntaxKind.RPAREN));
	}

	@Test
	void recognizeAivAsFunctionParameter()
	{
		assertTokens("(<+THEAIV>)", token(SyntaxKind.LPAREN), token(SyntaxKind.LESSER_SIGN), token(SyntaxKind.IDENTIFIER), token(SyntaxKind.GREATER_SIGN));
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
		assertTokens(
			"CN*AMES",
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
	@ValueSource(strings =
	{
		"R1.", "R.", "R-1.", "#like-a-var. ", "another-var.", "A.", "X.", "A123.", "#WAT.)"
	})
	void recognizeJumpLabelsAsLabelIdentifiers(String source)
	{
		assertTokens(source, SyntaxKind.LABEL_IDENTIFIER);
	}

	@Test
	void recognizeLabelUsingSlashAndIdentifier()
	{
		assertTokens(
			"(R1./IX)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.LABEL_IDENTIFIER),
			token(SyntaxKind.SLASH),
			token(SyntaxKind.IDENTIFIER),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void recognizeLabelUsingSlashAndAsterix()
	{
		assertTokens(
			"(R1./*)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.LABEL_IDENTIFIER),
			token(SyntaxKind.SLASH),
			token(SyntaxKind.ASTERISK),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void recognizeLabelsUsingSlashAndVariable()
	{
		assertTokens(
			"(F1./VAR)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.LABEL_IDENTIFIER, "F1."),
			token(SyntaxKind.SLASH),
			token(SyntaxKind.IDENTIFIER, "VAR"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void recognizeLabelsUsingSlashAndLiteral()
	{
		assertTokens(
			"(F1./1)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.LABEL_IDENTIFIER, "F1."),
			token(SyntaxKind.SLASH),
			token(SyntaxKind.NUMBER_LITERAL, "1"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void recognizeLabelsMixedWithIdentifiersAndNumbers()
	{
		assertTokens(
			"(F1./DIX1.2)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.LABEL_IDENTIFIER, "F1."),
			token(SyntaxKind.SLASH),
			token(SyntaxKind.IDENTIFIER, "DIX1"),
			token(SyntaxKind.DOT),
			token(SyntaxKind.NUMBER_LITERAL, "2")
		);
	}

	@Test
	void recognizeLabelUsingNone()
	{
		assertTokens(
			"(R1.)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.LABEL_IDENTIFIER),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void addADiagnosticForRecognizedIdentifiersThatEndWithADot()
	{
		assertDiagnostic("C*WHODOESTHIS.", assertedDiagnostic(0, 0, 0, 14, LexerError.INVALID_IDENTIFIER));
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
		assertTokens(
			"LAPDA.LAVARIABLE(I-INDEX-VAR,*)",
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
			dynamicTest("with hypen in both", () -> assertTokens("QUALI-FIER.VARI-ABLE", token(SyntaxKind.IDENTIFIER, "QUALI-FIER.VARI-ABLE")))
		);
	}

	@Test
	void recognizeAVariableStartingWithPfAsIdentifier()
	{
		assertTokens("PFAD", token(SyntaxKind.IDENTIFIER));
	}

	@Test
	void notConfuseArithmeticWithAiv()
	{
		assertTokens("5+NUM", token(SyntaxKind.NUMBER_LITERAL), token(SyntaxKind.PLUS), token(SyntaxKind.IDENTIFIER));
	}

	@Test
	void notConfuseArithmeticWithTwoIdentifiersWithAiv()
	{
		assertTokens("NUM+NUM2", token(SyntaxKind.IDENTIFIER), token(SyntaxKind.PLUS), token(SyntaxKind.IDENTIFIER));
	}

	@Test
	void notMistakeAQualifiedIdentifierWithAColorAttribute()
	{
		assertTokensInOrder(
			lexSource("(#VAR EQ ' ' AND RED.V EQ 0)"), // because we're in parens, it contained ' and RE it thought it was a color attribute
			SyntaxKind.LPAREN,
			SyntaxKind.IDENTIFIER,
			SyntaxKind.EQ,
			SyntaxKind.STRING_LITERAL,
			SyntaxKind.AND,
			SyntaxKind.IDENTIFIER,
			SyntaxKind.EQ,
			SyntaxKind.NUMBER_LITERAL,
			SyntaxKind.RPAREN
		);
	}

	@Test
	void notTreatSomethingStartingWithANumberAsIdentifier()
	{
		assertTokens(
			"1AA",
			token(SyntaxKind.NUMBER_LITERAL, "1"),
			token(SyntaxKind.IDENTIFIER, "AA")
		);
	}

	@Test
	void notTreatAnIdentifierColonNumberIdentifierAsQualifiedVariable()
	{
		assertTokens(
			"#VAR.1AA",
			token(SyntaxKind.IDENTIFIER, "#VAR"),
			token(SyntaxKind.DOT),
			token(SyntaxKind.NUMBER_LITERAL, "1"),
			token(SyntaxKind.IDENTIFIER, "AA")
		);
	}

	@Test
	void treatUtf8CharactersAsValidIdentifierStartForLeftSideOfQualification()
	{
		assertTokens(
			"ÆONE.VAR",
			token(SyntaxKind.IDENTIFIER, "ÆONE.VAR")
		);
	}

	@Test
	void treatUtf8CharactersAsValidIdentifierStartForRightSideOfQualification()
	{
		assertTokens(
			"VAR.ÆONE",
			token(SyntaxKind.IDENTIFIER, "VAR.ÆONE")
		);
	}

	@Test
	void treatUtf8CharactersAsValidIdentifierStartForBothSidesOfQualification()
	{
		assertTokens(
			"ÆONE.ÆTWO",
			token(SyntaxKind.IDENTIFIER, "ÆONE.ÆTWO")
		);
	}

	@Test
	void treatUtf8CharactersAsValidIdentifierStartForLeftSideOfQualificationInAiv()
	{
		assertTokens(
			"+ÆONE.VAR",
			token(SyntaxKind.IDENTIFIER, "+ÆONE.VAR")
		);
	}

	@Test
	void treatUtf8CharactersAsValidIdentifierStartForRightSideOfQualificationInAiv()
	{
		assertTokens(
			"+VAR.ÆONE",
			token(SyntaxKind.IDENTIFIER, "+VAR.ÆONE")
		);
	}

	@Test
	void treatUtf8CharactersAsValidIdentifierStartForBothSidesOfQualificationInAiv()
	{
		assertTokens(
			"+ÆONE.ÆTWO",
			token(SyntaxKind.IDENTIFIER, "+ÆONE.ÆTWO")
		);
	}

	@Test
	void notTreatAnIdentifierColonNumberIdentifierAsQualifiedVariableWithIdentifierThatMightBeKeyword()
	{
		assertTokens(
			"VAR.1",
			token(SyntaxKind.IDENTIFIER, "VAR"),
			token(SyntaxKind.DOT),
			token(SyntaxKind.NUMBER_LITERAL, "1")
		);
	}

	@Test
	void recognizeQualifiedCounterVariables()
	{
		assertTokens(
			"#VAR.C*VAR2",
			token(SyntaxKind.IDENTIFIER, "#VAR.C*VAR2")
		);
	}

	@Test
	void recognizeQualifiedCounterVariablesWhenQualifierMightBeKeyword()
	{
		assertTokens(
			"VAR.C*VAR2",
			token(SyntaxKind.IDENTIFIER, "VAR.C*VAR2")
		);
	}
}
