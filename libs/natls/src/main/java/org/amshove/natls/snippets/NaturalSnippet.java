package org.amshove.natls.snippets;

import org.amshove.natls.codemutation.TextEdits;
import org.amshove.natls.codemutation.UsingToAdd;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;

import java.util.*;
import java.util.function.Predicate;

public class NaturalSnippet
{
	private final String label;
	private final List<UsingToAdd> neededLocalUsings = new ArrayList<>();
	private final List<UsingToAdd> neededParameterUsings = new ArrayList<>();
	private final List<Predicate<LanguageServerFile>> fileConstraints = new ArrayList<>();
	private final List<NaturalFileType> fileTypeConstraints = new ArrayList<>();
	private String textToInsert = null;

	public NaturalSnippet(String label)
	{
		if (label.contains(" "))
		{
			throw new SnippetInitializationException("Label should not contain whitespace");
		}

		this.label = label;
	}

	public NaturalSnippet insertsText(String text)
	{
		textToInsert = text;
		return this;
	}

	public NaturalSnippet needsLocalUsing(String using)
	{
		neededLocalUsings.add(new UsingToAdd(using, VariableScope.LOCAL));
		return this;
	}

	public NaturalSnippet needsParameterUsing(String using)
	{
		neededParameterUsings.add(new UsingToAdd(using, VariableScope.PARAMETER));
		return this;
	}

	public NaturalSnippet applicableWhen(Predicate<LanguageServerFile> predicate)
	{
		fileConstraints.add(predicate);
		return this;
	}

	public CompletionItem createCompletion(LanguageServerFile file)
	{
		if (!fileTypeConstraints.isEmpty() && !fileTypeConstraints.contains(file.getType()))
		{
			return null;
		}

		if (!fileConstraints.isEmpty() && !fileConstraints.stream().allMatch(filter -> filter.test(file)))
		{
			return null;
		}

		if ((!neededLocalUsings.isEmpty() || !neededParameterUsings.isEmpty()) && file.module()instanceof IHasDefineData hasDefineData && hasDefineData.defineData() == null)
		{
			throw new ResponseErrorException(new ResponseError(1, "Can't complete snippet because no DEFINE DATA was found", null));
		}

		var item = new CompletionItem(label);
		item.setKind(CompletionItemKind.Snippet);
		item.setInsertText(textToInsert);
		item.setInsertTextFormat(textToInsert.contains("${") ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);

		// TODO: Use IMarkupContentBuilder once hoverprovider is merged
		StringBuilder documentationBuilder = new StringBuilder("```natural");
		if (file.getType().canHaveDefineData())
		{
			var additionalEdits = new ArrayList<TextEdit>();

			// this order is important. parameter first
			additionalEdits.addAll(addUsings(file, neededParameterUsings, documentationBuilder));
			additionalEdits.addAll(addUsings(file, neededLocalUsings, documentationBuilder));

			if (!additionalEdits.isEmpty())
			{
				item.setAdditionalTextEdits(additionalEdits);
				documentationBuilder.append("%n```%n---%n```natural".formatted());
			}
		}

		documentationBuilder.append("%n%s%n```%n".formatted(textToInsert));
		var documentation = documentationBuilder.toString()
			.replace("${", "") // replace placeholders
			.replace("}", "");
		item.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, documentation));

		return item;
	}

	private List<TextEdit> addUsings(LanguageServerFile file, List<UsingToAdd> usings, StringBuilder documentationBuilder)
	{
		var textEdits = new ArrayList<TextEdit>();
		for (var using : usings)
		{
			documentationBuilder.append("%n%s USING %s".formatted(using.scope(), using.name()));
			var edit = TextEdits.addUsing(file, using);
			if (edit != null)
			{
				textEdits.add(edit);
			}
		}

		return textEdits;
	}

	public NaturalSnippet applicableInFiletypes(NaturalFileType... fileTypes)
	{
		fileTypeConstraints.addAll(Arrays.asList(fileTypes));
		return this;
	}

}
