package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IDefineData;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class AllowedIdentifierTest extends AbstractParserTest<IDefineData>
{
	protected AllowedIdentifierTest()
	{
		super(DefineDataParser::new);
	}

	@TestFactory
	Stream<DynamicTest> usingAKeywordThatCanBeIdentifierShouldntRaisesADiagnostic()
	{
		return Arrays.stream(SyntaxKind.values())
			.filter(SyntaxKind::canBeIdentifier)
			.filter(sk -> sk != SyntaxKind.IDENTIFIER)
			.map(sk -> dynamicTest("%s should be discouraged as identifier".formatted(sk), () -> {
				assertDiagnostic("""
					DEFINE DATA LOCAL
					1 %s (A10)
					END-DEFINE
					""".formatted(sk.toString().replace("KW_", "").replace("_", "-").replace("WITH-CTE", "WITH_CTE")),
					ParserError.KEYWORD_USED_AS_IDENTIFIER);
			}));
	}
}
