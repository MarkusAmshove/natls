package org.amshove.natls.markupcontent;

import org.eclipse.lsp4j.MarkupKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlaintextContentBuilderShould extends MarkupContentBuilderTest
{
	private PlaintextContentBuilder sut;

	@BeforeEach
	void setUp()
	{
		sut = new PlaintextContentBuilder();
	}

	@Test
	void appendParagraphs()
	{
		assertContent(
			sut.appendParagraph("This is a paragraph"),
			"""
				This is a paragraph
				"""
		);
	}

	@Test
	void appendFormattedText()
	{
		assertContent(
			sut.append("Hello %s, this is %s", "World", "Plaintext"),
			"""
				Hello World, this is Plaintext"""
		);
	}

	@Test
	void buildACompleteText()
	{
		assertContent(
			sut.appendParagraph("Lorem Ipsum")
				.appendStrong("This is some strong text")
				.appendNewline()
				.append("%d + %d = %d", 5, 5, 10)
				.appendNewline()
				.appendCode("INCLUDE MYCCODE")
				.append("Here is the code: ")
				.appendInlineCode("DEFINE DATA"),
			"""
				Lorem Ipsum
				This is some strong text
				5 + 5 = 10
				INCLUDE MYCCODE
				Here is the code: DEFINE DATA"""
		);
	}

	@Override
	protected String getExpectedKind()
	{
		return MarkupKind.PLAINTEXT;
	}
}
