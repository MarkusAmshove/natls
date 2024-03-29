package org.amshove.natls.markupcontent;

import org.eclipse.lsp4j.MarkupKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MarkdownContentBuilderShould extends MarkupContentBuilderTest
{
	private MarkdownContentBuilder sut;

	@BeforeEach
	void setUp()
	{
		sut = new MarkdownContentBuilder();
	}

	@Test
	void appendParagraphs()
	{
		assertContent(
			sut.appendParagraph("This is a paragraph"),
			"""
				This is a paragraph"""
		);
	}

	@Test
	void appendASection()
	{
		assertContent(
			sut.appendSection(
				"the heading", nested -> nested
					.appendCode("Code")
					.append("Text")
			),
			"""
				*the heading:*
				```natural
				Code
				```
				
				Text"""
		);
	}

	@Test
	void appendFormattedText()
	{
		assertContent(
			sut.append("Hello %s, this is %s", "World", "Markdown"),
			"""
				Hello World, this is Markdown"""
		);
	}

	@Test
	void buildACompleteText()
	{
		assertContent(
			sut.appendParagraph("Lorem Ipsum")
				.appendItalic("This is some italic text")
				.appendNewline()
				.appendStrong("This is some strong text")
				.appendNewline()
				.append("%d + %d = %d", 5, 5, 10)
				.appendNewline()
				.appendCode("INCLUDE MYCCODE")
				.append("Here is the code: ")
				.appendInlineCode("DEFINE DATA"),
			"""
				Lorem Ipsum

				*This is some italic text*

				**This is some strong text**

				5 + 5 = 10

				```natural
				INCLUDE MYCCODE
				```

				Here is the code: `DEFINE DATA`"""
		);
	}

	@Override
	protected String getExpectedKind()
	{
		return MarkupKind.MARKDOWN;
	}
}
