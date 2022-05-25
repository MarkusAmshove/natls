package org.amshove.natls.markupcontent;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

public class MarkdownContentBuilder implements IMarkupContentBuilder
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
		builder.append("```natural");
		builder.append(System.lineSeparator());
		builder.append(content);
		builder.append(System.lineSeparator());
		builder.append("```");
		return appendNewline();
	}

	@Override
	public IMarkupContentBuilder appendInlineCode(String content)
	{
		builder.append("`").append(content).append("`");
		return this;
	}

	@Override
	public IMarkupContentBuilder appendNewline()
	{
		builder.append(System.lineSeparator()).append(System.lineSeparator());
		return this;
	}

	@Override
	public IMarkupContentBuilder append(String content)
	{
		builder.append(content);
		return this;
	}

	@Override
	public IMarkupContentBuilder append(String format, Object... args)
	{
		builder.append(format.formatted(args));
		return this;
	}

	@Override
	public IMarkupContentBuilder appendStrong(String content)
	{
		return append("**%s**", content);
	}

	@Override
	public IMarkupContentBuilder appendItalic(String content)
	{
		return append("*%s*", content);
	}

	@Override
	public MarkupContent build()
	{
		return new MarkupContent(
			MarkupKind.MARKDOWN,
			builder.toString()
		);
	}
}
