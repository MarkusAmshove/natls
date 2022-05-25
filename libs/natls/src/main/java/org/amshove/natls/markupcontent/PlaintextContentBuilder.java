package org.amshove.natls.markupcontent;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

public class PlaintextContentBuilder implements IMarkupContentBuilder
{
	private final StringBuilder builder = new StringBuilder();

	@Override
	public IMarkupContentBuilder appendParagraph(String content)
	{
		builder.append(content);
		return appendNewline();
	}

	@Override
	public IMarkupContentBuilder appendCode(String content)
	{
		return appendParagraph(content);
	}

	@Override
	public IMarkupContentBuilder appendInlineCode(String content)
	{
		return append(content);
	}

	@Override
	public IMarkupContentBuilder appendNewline()
	{
		return append(System.lineSeparator());
	}

	@Override
	public IMarkupContentBuilder append(String content)
	{
		builder.append(content);
		return this;
	}

	@Override
	public IMarkupContentBuilder append(String format, Object... objects)
	{
		builder.append(format.formatted(objects));
		return this;
	}

	@Override
	public IMarkupContentBuilder appendStrong(String content)
	{
		return append(content);
	}

	@Override
	public MarkupContent build()
	{
		return new MarkupContent(
			MarkupKind.PLAINTEXT,
			builder.toString());
	}
}
