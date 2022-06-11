package org.amshove.natls.snippets;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.IUsingNode;
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
		if(!fileTypeConstraints.isEmpty() && !fileTypeConstraints.contains(file.getType()))
		{
			return null;
		}

		if(!fileConstraints.isEmpty() && !fileConstraints.stream().allMatch(filter -> filter.test(file)))
		{
			return null;
		}

		if((!neededLocalUsings.isEmpty() || !neededParameterUsings.isEmpty())&& file.module() instanceof IHasDefineData hasDefineData && hasDefineData.defineData() == null)
		{
			throw new ResponseErrorException(new ResponseError(1, "Can't complete snippet because no DEFINE DATA was found", null));
		}

		var item = new CompletionItem(label);
		item.setKind(CompletionItemKind.Snippet);
		item.setInsertText(textToInsert);
		item.setInsertTextFormat(textToInsert.contains("${") ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);

		// TODO: Use IMarkupContentBuilder once hoverprovider is merged
		StringBuilder documentationBuilder = new StringBuilder("```natural");
		if(file.getType().canHaveDefineData())
		{
			var additionalEdits = new ArrayList<TextEdit>();

			additionalEdits.addAll(addUsings(file, neededParameterUsings, documentationBuilder)); // this order is important. parameter first
			additionalEdits.addAll(addUsings(file, neededLocalUsings, documentationBuilder));

			if(!additionalEdits.isEmpty())
			{
				item.setAdditionalTextEdits(additionalEdits);
				documentationBuilder.append("%n```%n---%n```natural".formatted());
			}
		}

		documentationBuilder.append("%n%s%n```%n".formatted(textToInsert));
		item.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, documentationBuilder.toString()));

		return item;
	}

	private static List<TextEdit> addUsings(LanguageServerFile file, List<UsingToAdd> usings, StringBuilder documentationBuilder)
	{
		var additionalEdits = new ArrayList<TextEdit>();
		for (var neededUsing : usings)
		{
			if(alreadyHasUsing(neededUsing.name, file))
			{
				continue;
			}

			var usingInsert = createUsingInsert(neededUsing, file);
			additionalEdits.add(usingInsert);
			documentationBuilder.append("%n%s".formatted(usingInsert.getNewText()));
		}

		return additionalEdits;
	}

	private static TextEdit createUsingInsert(UsingToAdd using, LanguageServerFile file)
	{
		var edit = new TextEdit();
		var range = findRangeToInsertUsing(file, using.scope);

		edit.setRange(range);
		edit.setNewText("%s USING %s%n".formatted(using.scope, using.name));
		return edit;
	}

	private static Range findRangeToInsertUsing(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		ReadOnlyList<IUsingNode> usings = scope == VariableScope.PARAMETER
			? defineData.parameterUsings()
			: defineData.localUsings();

		if(usings.hasItems())
		{
			var firstUsing = usings.first();
			return LspUtil.toSingleRange(firstUsing.position().line(), 0);
		}

		if(defineData.variables().hasItems() && defineData.variables().stream().anyMatch(v -> v.scope() == scope && v.position().filePath().equals(file.getPath())))
		{
			return defineData.variables().stream().filter(v -> v.scope() == scope)
				.findFirst()
				.map(v -> LspUtil.toSingleRange(v.position().line(), 0))
				.orElseThrow(() -> new RuntimeException("Could not deduce position to insert using by looking for the first variable with scope %s".formatted(scope)));
		}

		return LspUtil.toSingleRange(defineData.descendants().get(0).position().line() + 1, 0);
	}

	private static boolean alreadyHasUsing(String using, LanguageServerFile file)
	{
		return ((IHasDefineData) file.module()).defineData().usings().stream().anyMatch(u -> u.target().symbolName().equals(using));
	}

	public NaturalSnippet applicableInFiletypes(NaturalFileType... fileTypes)
	{
		fileTypeConstraints.addAll(Arrays.asList(fileTypes));
		return this;
	}

	private record UsingToAdd(String name, VariableScope scope){}
}
