package org.amshove.natls.completion;

import org.amshove.natls.config.LSConfiguration;
import org.amshove.natls.hover.HoverContext;
import org.amshove.natls.hover.HoverProvider;
import org.amshove.natls.languageserver.UnresolvedCompletionInfo;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerLibrary;
import org.amshove.natls.snippets.SnippetEngine;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;
import org.amshove.natparse.natural.builtin.SystemFunctionDefinition;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class CompletionProvider
{
	private static final Logger log = Logger.getAnonymousLogger();

	private final SnippetEngine snippetEngine;
	private final HoverProvider hoverProvider;
	private LSConfiguration config;

	public CompletionProvider(SnippetEngine snippetEngine, HoverProvider hoverProvider)
	{
		this.snippetEngine = snippetEngine;
		this.hoverProvider = hoverProvider;
	}

	public List<CompletionItem> prepareCompletion(LanguageServerFile file, CompletionParams params, LSConfiguration config)
	{
		this.config = config;
		if (!file.getType().canHaveBody())
		{
			return List.of();
		}
		var module = file.module();
		var completionContext = CodeCompletionContext.create(file, params.getPosition());

		var completionItems = new ArrayList<CompletionItem>();

		if (completionContext.completesDataArea())
		{
			return dataAreaCompletions(file.getLibrary());
		}

		var isFilteredOnQualifiedNames = params.getContext().getTriggerKind() == CompletionTriggerKind.TriggerCharacter && ".".equals(params.getContext().getTriggerCharacter());
		if (isFilteredOnQualifiedNames || completionContext.isCurrentTokenKind(SyntaxKind.LABEL_IDENTIFIER)) // Label identifier is what's lexed for incomplete qualification, e.g. GRP.
		{
			assert completionContext.currentToken() != null;
			var qualifiedNameFilter = completionContext.currentToken().symbolName();
			return findVariablesToComplete(module)
				.filter(v -> v.qualifiedName().startsWith(qualifiedNameFilter))
				.map(v -> toVariableCompletion(v, module, file, qualifiedNameFilter))
				.filter(Objects::nonNull)
				.toList();
		}

		if (completionContext.completesPerform())
		{
			completionItems.addAll(externalSubroutineCompletions(file.getLibrary(), completionContext));
			completionItems.addAll(localSubroutineCompletions(module, completionContext));
			return completionItems;
		}

		if (completionContext.completesCallnat())
		{
			completionItems.addAll(subprogramCompletions(file.getLibrary(), completionContext));
			return completionItems;
		}

		completionItems.addAll(snippetEngine.provideSnippets(file));

		completionItems.addAll(
			findVariablesToComplete(module)
				.map(v -> toVariableCompletion(v, module, file, ""))
				.filter(Objects::nonNull)
				.toList()
		);

		completionItems.addAll(localSubroutineCompletions(module, completionContext));

		completionItems.addAll(functionCompletions(file.getLibrary()));
		completionItems.addAll(externalSubroutineCompletions(file.getLibrary(), completionContext));
		completionItems.addAll(subprogramCompletions(file.getLibrary(), completionContext));

		completionItems.addAll(completeSystemVars(completionContext));

		return completionItems;
	}

	private List<CompletionItem> localSubroutineCompletions(INaturalModule module, CodeCompletionContext context)
	{
		return module.referencableNodes().stream()
			.filter(ISubroutineNode.class::isInstance)
			.map(ISubroutineNode.class::cast)
			.map(n -> this.createLocalSubroutineCompletionItem(n, context))
			.toList();
	}

	private List<CompletionItem> dataAreaCompletions(LanguageServerLibrary library)
	{
		var dataAreas = new ArrayList<>(library.getModulesOfType(NaturalFileType.LDA, true));
		List<LanguageServerFile> pdas = library.getModulesOfType(NaturalFileType.PDA, true);
		dataAreas.addAll(pdas);
		dataAreas.addAll(library.getModulesOfType(NaturalFileType.GDA, true));
		return dataAreas.stream()
			.map(f ->
			{
				var item = new CompletionItem(f.getReferableName());
				item.setInsertText(f.getReferableName());
				item.setKind(CompletionItemKind.Struct);
				return item;
			})
			.toList();
	}

	public CompletionItem resolveComplete(CompletionItem item, LanguageServerFile calledModulesFile, UnresolvedCompletionInfo info, LSConfiguration config)
	{
		this.config = config;

		if (item.getData() == null)
		{
			return item;
		}

		var module = calledModulesFile.module();

		return switch (item.getKind())
		{
			case Variable ->
			{
				if (!(module instanceof IHasDefineData hasDefineData))
				{
					yield item;
				}

				var variableNode = hasDefineData.defineData().variables().stream().filter(v -> v.qualifiedName().equals(info.getQualifiedName())).findFirst().orElse(null);
				if (variableNode == null)
				{
					yield item;
				}

				item.setDocumentation(
					new MarkupContent(
						MarkupKind.MARKDOWN,
						hoverProvider.createHover(new HoverContext(variableNode, variableNode.declaration(), calledModulesFile)).getContents().getRight().getValue()
					)
				);
				yield item;
			}
			case Function ->
			{
				item.setInsertTextFormat(InsertTextFormat.Snippet);
				item.setDocumentation(
					new MarkupContent(
						MarkupKind.MARKDOWN,
						hoverProvider.hoverModule(module).getContents().getRight().getValue()
					)
				);
				item.setInsertText("%s(<%s>)$0".formatted(calledModulesFile.getReferableName(), functionParameterListAsSnippet(calledModulesFile)));
				yield item;
			}
			case Event ->
			{
				item.setInsertTextFormat(InsertTextFormat.Snippet);
				item.setDocumentation(
					new MarkupContent(
						MarkupKind.MARKDOWN,
						hoverProvider.hoverModule(module).getContents().getRight().getValue()
					)
				);
				var perform = info.hasPreviousText("PERFORM") ? "" : "PERFORM ";
				item.setInsertText("%s%s%s%n$0".formatted(perform, calledModulesFile.getReferableName(), externalModuleParameterListAsSnippet(calledModulesFile)));
				yield item;
			}
			case Class ->
			{
				item.setInsertTextFormat(InsertTextFormat.Snippet);
				item.setDocumentation(
					new MarkupContent(
						MarkupKind.MARKDOWN,
						hoverProvider.hoverModule(module).getContents().getRight().getValue()
					)
				);
				var callnat = info.hasPreviousText("CALLNAT") ? "" : "CALLNAT ";
				item.setInsertText("%s'%s'%s%n$0".formatted(callnat, calledModulesFile.getReferableName(), externalModuleParameterListAsSnippet(calledModulesFile)));
				yield item;
			}
			default -> item;
		};
	}

	private CompletionItem toVariableCompletion(IVariableNode variableNode, INaturalModule module, LanguageServerFile file, String alreadyPresentText)
	{
		try
		{
			var item = createCompletionItem(variableNode, file, module.referencableNodes(), !alreadyPresentText.isEmpty());
			item.setInsertText(item.getInsertText().substring(alreadyPresentText.length()));
			if (item.getKind() == CompletionItemKind.Variable)
			{
				item.setData(new UnresolvedCompletionInfo((String) item.getData(), file.getPath().toUri().toString()));
			}
			return item;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Variable completion threw an exception", e);
			return null;
		}
	}

	private Stream<IVariableNode> findVariablesToComplete(INaturalModule module)
	{
		return module.referencableNodes().stream()
			.filter(IVariableNode.class::isInstance)
			.map(IVariableNode.class::cast)
			.filter(v -> !(v instanceof IRedefinitionNode)); // this is the `REDEFINE #VAR`, which results in the variable being doubled in completion
	}

	private Collection<? extends CompletionItem> functionCompletions(LanguageServerLibrary library)
	{
		return library.getModulesOfType(NaturalFileType.FUNCTION, true)
			.stream()
			.map(f ->
			{
				try
				{
					var item = new CompletionItem(f.getReferableName());
					item.setData(new UnresolvedCompletionInfo(f.getReferableName(), f.getUri()));
					item.setKind(CompletionItemKind.Function);
					return item;
				}
				catch (Exception e)
				{
					log.log(Level.SEVERE, "Function completion threw an exception", e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.toList();
	}

	private String functionParameterListAsSnippet(LanguageServerFile function)
	{
		if (!(function.module()instanceof IHasDefineData hasDefineData) || hasDefineData.defineData() == null)
		{
			return "";
		}

		var builder = new StringBuilder();
		var index = 1;
		for (var parameter : hasDefineData.defineData().parameterInOrder())
		{
			if (index > 1)
			{
				builder.append(", ");
			}
			var parameterName = parameter instanceof IUsingNode using ? using.target().symbolName() : ((IVariableNode) parameter).name();
			builder.append("${%d:%s}".formatted(index, parameterName));
			index++;
		}

		return builder.toString();
	}

	private String externalModuleParameterListAsSnippet(LanguageServerFile module)
	{
		if (!(module.module()instanceof IHasDefineData hasDefineData) || hasDefineData.defineData() == null)
		{
			return "";
		}

		var builder = new StringBuilder();
		var index = 1;
		for (var parameter : hasDefineData.defineData().parameterInOrder())
		{
			builder.append(" ");
			var parameterName = parameter instanceof IUsingNode using ? using.target().symbolName() : ((IVariableNode) parameter).name();
			builder.append("${%d:%s}".formatted(index, parameterName));
			index++;
		}

		return builder.toString();
	}

	private Collection<? extends CompletionItem> subprogramCompletions(LanguageServerLibrary library, CodeCompletionContext context)
	{
		return library.getModulesOfType(NaturalFileType.SUBPROGRAM, true)
			.stream()
			.map(f ->
			{
				try
				{
					var item = new CompletionItem(f.getReferableName());
					item.setData(createUnresolvedInfo(f, context));
					item.setKind(CompletionItemKind.Class);
					return item;
				}
				catch (Exception e)
				{
					log.log(Level.SEVERE, "Subprogram completion threw an exception", e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.toList();
	}

	private Collection<? extends CompletionItem> externalSubroutineCompletions(LanguageServerLibrary library, CodeCompletionContext context)
	{
		return library.getModulesOfType(NaturalFileType.SUBROUTINE, true)
			.stream()
			.map(f ->
			{
				try
				{
					var item = new CompletionItem(f.getReferableName());
					item.setData(createUnresolvedInfo(f, context));
					item.setKind(CompletionItemKind.Event);
					return item;
				}
				catch (Exception e)
				{
					log.log(Level.SEVERE, "External Subroutine completion threw an exception", e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.toList();
	}

	private CompletionItem createCompletionItem(IVariableNode variableNode, LanguageServerFile openFile, ReadOnlyList<IReferencableNode> referencableNodes, boolean forceQualification)
	{
		var item = new CompletionItem();
		var variableName = variableNode.name();

		if (forceQualification || config.getCompletion().isQualify() || referencableNodes.stream().filter(n -> n.declaration().symbolName().equals(variableNode.name())).count() > 1)
		{
			variableName = variableNode.qualifiedName();
		}

		var insertText = variableNode.dimensions().hasItems()
			? "%s($1)$0".formatted(variableName)
			: variableName;
		item.setInsertText(insertText);
		item.setInsertTextFormat(InsertTextFormat.Snippet);

		item.setKind(CompletionItemKind.Variable);
		item.setLabel(variableName);
		var label = "";
		if (variableNode instanceof ITypedVariableNode typedNode)
		{
			label = variableName + " :" + typedNode.formatTypeForDisplay();
		}
		if (variableNode instanceof IGroupNode)
		{
			label = variableName + " : Group";
		}

		var isImported = variableNode.position().filePath().equals(openFile.getPath());

		if (isImported)
		{
			label += " (%s)".formatted(variableNode.position().fileNameWithoutExtension());
		}

		item.setSortText(
			isImported
				? "2"
				: "3"
		);

		item.setLabel(label);
		item.setData(variableNode.qualifiedName());

		return item;
	}

	private CompletionItem createLocalSubroutineCompletionItem(ISubroutineNode subroutineNode, CodeCompletionContext context)
	{
		var item = new CompletionItem();
		item.setKind(CompletionItemKind.Method);
		var perform = context.isCurrentTokenKind(SyntaxKind.PERFORM) || context.isPreviousTokenKind(SyntaxKind.PERFORM)
			? ""
			: "PERFORM ";
		item.setInsertText(perform + subroutineNode.declaration().trimmedSymbolName(32));
		item.setLabel(subroutineNode.declaration().symbolName());
		item.setSortText("1");

		return item;
	}

	private List<CompletionItem> completeSystemVars(CodeCompletionContext context)
	{
		var alreadyContainsAsterisk = context.isCurrentTokenKind(SyntaxKind.ASTERISK) || context.isPreviousTokenKind(SyntaxKind.ASTERISK);
		return Arrays.stream(SyntaxKind.values())
			.filter(sk -> sk.isSystemVariable() || sk.isSystemFunction())
			.map(sk ->
			{
				var callableName = sk.toString().replace("SV_", "").replace("_", "-");
				var completionItem = new CompletionItem();
				var definition = BuiltInFunctionTable.getDefinition(sk);
				var label = "*" + callableName;
				var insertion = alreadyContainsAsterisk ? callableName : label;
				completionItem.setDetail(definition.documentation());
				completionItem.setKind(definition instanceof SystemFunctionDefinition ? CompletionItemKind.Function : CompletionItemKind.Variable);
				completionItem.setLabel(label + " :%s".formatted(definition.type().toShortString()));
				completionItem.setSortText(
					(alreadyContainsAsterisk ? "0" : "9") + completionItem.getLabel()
				); // if alreadyContainsAsterisk, bring them to the front. else to the end.

				completionItem.setInsertText(insertion);
				if (definition instanceof SystemFunctionDefinition functionDefinition && !functionDefinition.parameter().isEmpty())
				{
					completionItem.setInsertText(insertion + "($1)$0");
					completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
				}

				return completionItem;
			})
			.toList();

	}

	private UnresolvedCompletionInfo createUnresolvedInfo(LanguageServerFile file, CodeCompletionContext context)
	{
		var info = new UnresolvedCompletionInfo(file.getReferableName(), file.getUri());
		info.setPreviousText(context.previousTexts());
		return info;
	}

}
