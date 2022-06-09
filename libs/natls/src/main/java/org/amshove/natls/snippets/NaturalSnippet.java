package org.amshove.natls.snippets;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class NaturalSnippet
{
	private final String label;
	private final List<String> neededUsings = new ArrayList<>();
	private final List<Predicate<LanguageServerFile>> fileConstraints = new ArrayList<>();
	private final List<NaturalFileType> fileTypeConstraints = new ArrayList<>();
	private String textToInsert = null;

	public NaturalSnippet(String label)
	{
		if(label.contains(" "))
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

	public NaturalSnippet needsUsing(String using)
	{
		neededUsings.add(using);
		return this;
	}

	public NaturalSnippet applicableWhen(Predicate<LanguageServerFile> predicate)
	{
		fileConstraints.add(predicate);
		return this;
	}

	public CompletionItem createCompletion(LanguageServerFile file)
	{
		if(!fileTypeConstraints.isEmpty() && !fileTypeConstraints.contains(file.getType()))
		{
			return null;
		}

		if(!fileConstraints.isEmpty() && !fileConstraints.stream().allMatch(filter -> filter.test(file)))
		{
			return null;
		}

		if(!neededUsings.isEmpty() && file.module() instanceof IHasDefineData hasDefineData && hasDefineData.defineData() == null)
		{
			throw new ResponseErrorException(new ResponseError(1, "Can't complete snippet because no DEFINE DATA was found", null));
		}

		var item = new CompletionItem(label);
		item.setKind(CompletionItemKind.Snippet);
		item.setInsertText(textToInsert);
		item.setInsertTextFormat(textToInsert.contains("${") ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);

		// TODO: Use IMarkupContentBuilder once hoverprovider is merged
		var documentation = "```natural";
		if(file.getType().canHaveDefineData())
		{
			var additionalEdits = new ArrayList<TextEdit>();
			for (var neededUsing : neededUsings)
			{
				if(alreadyHasUsing(neededUsing, file))
				{
					continue;
				}

				var usingInsert = createUsingInsert(neededUsing, file);
				additionalEdits.add(usingInsert);
				documentation += "%n%s".formatted(usingInsert.getNewText());
			}

			if(!additionalEdits.isEmpty())
			{
				item.setAdditionalTextEdits(additionalEdits);
				documentation += "%n```%n---%n```natural".formatted();
			}
		}

		documentation += "%n%s%n```%n".formatted(textToInsert);
		item.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, documentation));

		return item;
	}

	private static TextEdit createUsingInsert(String using, LanguageServerFile file)
	{
		var edit = new TextEdit();
		var range = findRangeToInsertUsing(file);

		edit.setRange(range);
		edit.setNewText("LOCAL USING %s%n".formatted(using));
		return edit;
	}

	private static Range findRangeToInsertUsing(LanguageServerFile file)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		if(defineData.localUsings().hasItems())
		{
			var firstUsing = defineData.localUsings().first();
			return LspUtil.toSingleRange(firstUsing.position().line(), 0);
		}

		if(defineData.variables().hasItems())
		{
			return defineData.variables().stream().filter(v -> v.scope() == VariableScope.LOCAL)
				.findFirst()
				.map(v -> LspUtil.toSingleRange(v.position().line(), 0))
				.orElseThrow(() -> new RuntimeException("Could not deduce position to insert using by looking for the first variable"));
		}

		throw new RuntimeException("Could deduce position to insert using");
	}

	private static boolean alreadyHasUsing(String using, LanguageServerFile file)
	{
		return ((IHasDefineData) file.module()).defineData().usings().stream().anyMatch(u -> u.target().symbolName().equals(using));
	}

	public NaturalSnippet applicableInFiletypes(NaturalFileType... fileTypes)
	{
		for (var fileType : fileTypes)
		{
			fileTypeConstraints.add(fileType);
		}
		return this;
	}
}
