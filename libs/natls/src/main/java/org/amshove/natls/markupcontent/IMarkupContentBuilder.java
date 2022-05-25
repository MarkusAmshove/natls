package org.amshove.natls.markupcontent;

import org.eclipse.lsp4j.MarkupContent;

public interface IMarkupContentBuilder
{
	IMarkupContentBuilder appendParagraph(String content);
	IMarkupContentBuilder appendCode(String content);
	IMarkupContentBuilder appendInlineCode(String content);
	IMarkupContentBuilder appendNewline();
	IMarkupContentBuilder append(String content);
	IMarkupContentBuilder append(String format, Object... objects);
	IMarkupContentBuilder appendStrong(String content);
	IMarkupContentBuilder appendItalic(String content);

	MarkupContent build();
}
