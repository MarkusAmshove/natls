package org.amshove.natls.languageserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.amshove.natls.DiagnosticTool;
import org.amshove.natls.codeactions.CodeActionRegistry;
import org.amshove.natls.codeactions.RefactoringContext;
import org.amshove.natls.codeactions.RenameSymbolAction;
import org.amshove.natls.progress.IProgressMonitor;
import org.amshove.natls.progress.ProgressTasks;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natls.project.ModuleReferenceParser;
import org.amshove.natls.snippets.SnippetEngine;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.DefineDataParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NaturalLanguageService implements LanguageClientAware
{
	private static final Hover EMPTY_HOVER = null; // This should be done according to the spec
	private final CodeActionRegistry codeActionRegistry = CodeActionRegistry.INSTANCE;
	private NaturalProject project; // TODO: Replace
	private LanguageServerProject languageServerProject;
	private LanguageClient client;
	private boolean initialized;
	private RenameSymbolAction renameComputer = new RenameSymbolAction();
	private SnippetEngine snippetEngine;
	private Path workspaceRoot;

	public void indexProject(Path workspaceRoot, IProgressMonitor progressMonitor)
	{
		this.workspaceRoot = workspaceRoot;
		var projectFile = new ActualFilesystem().findNaturalProjectFile(workspaceRoot);
		if (projectFile.isEmpty())
		{
			throw new RuntimeException("Could not load Natural project. .natural or _naturalBuild not found");
		}
		var project = new BuildFileProjectReader().getNaturalProject(projectFile.get());
		var indexer = new NaturalProjectFileIndexer();
		indexer.indexProject(project);
		this.project = project;
		languageServerProject = LanguageServerProject.fromProject(project);
		parseFileReferences(progressMonitor);
		snippetEngine = new SnippetEngine(languageServerProject);
		initialized = true;
	}

	public List<SymbolInformation> findSymbolsInFile(TextDocumentIdentifier textDocument)
	{
		var filepath = LspUtil.uriToPath(textDocument.getUri());
		var module = findNaturalFile(filepath).module();
		var referencableNodes = module.referencableNodes();

		return referencableNodes
			.stream()
			.map(n -> new SymbolInformation(
				n.declaration().symbolName(),
				n instanceof IVariableNode ? SymbolKind.Variable : SymbolKind.Method,
				LspUtil.toLocation(n),
				n.position().fileNameWithoutExtension()
			))
			.toList();
	}

	private Stream<SyntaxToken> getVariableDeclarationTokens(TokenList tokens)
	{
		return tokens.tokensUntilNext(SyntaxKind.END_DEFINE).stream();
	}

	public void createdFile(String uri)
	{
		var path = LspUtil.uriToPath(uri);
		languageServerProject.addFile(path);
	}

	public List<? extends SymbolInformation> findWorkspaceSymbols(String query, CancelChecker cancelChecker)
	{
		return project.getLibraries().stream()
			.flatMap(l -> {
				cancelChecker.checkCanceled();
				return l.files().stream();
			})
			.filter(f -> f.getReferableName().toLowerCase().contains(query.toLowerCase()))
			.limit(100)
			.map(f -> {
				cancelChecker.checkCanceled();
				return convertToSymbolInformation(f);
			})
			.toList();
	}

	private SymbolInformation convertToSymbolInformation(NaturalFile file)
	{
		return new SymbolInformation(
			file.getReferableName(),
			SymbolKind.Class,
			new Location(
				file.getPath().toUri().toString(),
				new Range(
					new Position(0, 0),
					new Position(0, 0)
				)
			),
			file.getLibrary().getName()
		);
	}

	private SymbolInformation convertToSymbolInformation(SyntaxToken token, Path filepath)
	{
		return new SymbolInformation(
			token.source(),
			SymbolKind.Variable,
			new Location(
				filepath.toUri().toString(),
				new Range(
					new Position(token.line(), token.offsetInLine()),
					new Position(token.line(), token.offsetInLine() + token.length())
				)
			)
		);
	}

	public Hover hoverSymbol(TextDocumentIdentifier textDocument, Position position)
	{

		var filepath = LspUtil.uriToPath(textDocument.getUri());
		var file = findNaturalFile(filepath);
		if (file.getType() == NaturalFileType.COPYCODE)
		{
			return EMPTY_HOVER;
		}
		var symbolToSearchFor = findTokenAtPosition(filepath, position); // TODO: Actually look for a node, could be ISymbolReferenceNode
		if (symbolToSearchFor == null)
		{
			// No position found where we can provide hover for
			return EMPTY_HOVER;
		}

		if (symbolToSearchFor.kind() == SyntaxKind.STRING_LITERAL)
		{
			return hoverExternalModule(symbolToSearchFor);
		}

		var externalSubroutineHover = hoverExternalModule(symbolToSearchFor);
		if(externalSubroutineHover != EMPTY_HOVER)
		{
			return externalSubroutineHover;
		}

		var module = file.module();
		if (!(module instanceof IHasDefineData hasDefineData))
		{
			return EMPTY_HOVER;
		}

		Predicate<IVariableNode> variableFilter = symbolToSearchFor.symbolName().contains(".")
			? v -> v.qualifiedName().equals(symbolToSearchFor.symbolName())
			: v -> v.declaration().symbolName().equals(symbolToSearchFor.symbolName());
		return hasDefineData
			.defineData()
			.variables().stream()
			.filter(variableFilter)
			.map(v ->
				new Hover(
					new MarkupContent(
						MarkupKind.MARKDOWN,
						createHoverMarkdownText(v, module)
					)
				)
			)
			.findFirst()
			.orElse(EMPTY_HOVER);
	}

	private Hover hoverExternalModule(SyntaxToken symbolToSearchFor)
	{
		var module = project.findModule(symbolToSearchFor.kind().isIdentifier() ? symbolToSearchFor.symbolName() : symbolToSearchFor.stringValue());
		if (module == null)
		{
			return EMPTY_HOVER;
		}

		var tokens = lexPath(module.getPath());
		var defineData = parseDefineData(tokens);
		if (defineData == null)
		{
			return EMPTY_HOVER;
		}

		var hoverText = "**%s.%s**".formatted(module.getLibrary().getName(), module.getReferableName());
		if(!module.getFilenameWithoutExtension().equals(module.getReferableName()))
		{
			hoverText += " (%s)".formatted(module.getFilenameWithoutExtension());
		}

		var documentation = extractDocumentation(tokens.comments(), tokens.subrange(0, 0).first().line());
		if(documentation != null && !documentation.isEmpty())
		{
			hoverText += "\n```natural\n";
			hoverText += "\n" + documentation;
			hoverText += "\n```";
		}

		if(module.getFiletype() == NaturalFileType.SUBROUTINE
			|| module.getFiletype() == NaturalFileType.SUBPROGRAM
			|| module.getFiletype() == NaturalFileType.PROGRAM
			|| module.getFiletype() == NaturalFileType.FUNCTION)
		{
			// TODO: Hover level 1 variables for *DAs
			hoverText += "\n\nParameter:\n```natural\n";

			// TODO: Order is not correct. DefineData should have .parameter() which have USINGs and non-USINGS
			//  mixed in definition order
			hoverText += defineData.parameterUsings().stream()
				.map(using -> "PARAMETER USING %s %s".formatted(using.target().source(), extractLineComment(tokens.comments(), using.position().line())).trim())
				.collect(Collectors.joining("\n"));
			hoverText += "\n```\n";
			hoverText += defineData.variables().stream()
				.filter(v -> v.scope() == VariableScope.PARAMETER)
				.map(v -> "%s %s%n```".formatted(formatVariableHover(v, false), extractLineComment(tokens.comments(), v.position().line())).trim())
				.collect(Collectors.joining("\n"));
		}

		return new Hover(
			new MarkupContent(
				MarkupKind.MARKDOWN,
				hoverText
			)
		);
	}

	private String extractLineComment(ReadOnlyList<SyntaxToken> comments, int line)
	{
		return comments.stream().filter(t -> t.line() == line)
			.findFirst()
			.map(SyntaxToken::source)
			.orElse("");
	}

	private String extractDocumentation(ReadOnlyList<SyntaxToken> comments, int firstLineOfCode)
	{
		if(comments.isEmpty())
		{
			return null;
		}

		return comments.stream()
			.takeWhile(t -> t.line() < firstLineOfCode)
			.map(SyntaxToken::source)
			.filter(l -> !l.startsWith("* >") && !l.startsWith("* <") && !l.startsWith("* :"))
			.filter(l -> !l.trim().endsWith("*"))
			.collect(Collectors.joining(System.lineSeparator()));
	}

	private SyntaxToken findTokenAtPosition(Path filePath, Position position)
	{
		var tokens = lexPath(filePath);

		while (!tokens.isAtEnd())
		{
			var token = tokens.peek();
			if (token.line() != position.getLine())
			{
				tokens.advance();
				continue;
			}

			if (token.offsetInLine() <= position.getCharacter() && token.offsetInLine() + token.length() >= position.getCharacter())
			{
				break;
			}

			tokens.advance();
		}

		return tokens.peek();
	}

	private String getLineComment(int line, Path filePath)
	{
		return getLineComment(line, findNaturalFile(filePath));
	}

	private String getLineComment(int line, LanguageServerFile file)
	{
		return file.comments().stream()
			.filter(t -> t.line() == line)
			.map(SyntaxToken::source)
			.findFirst()
			.orElse(null);
	}

	private String createHoverMarkdownText(IVariableNode v, INaturalModule originalModule)
	{
		var hoverText = formatVariableHover(v);
		hoverText += "\n";

		var hasUsingComment = false;
		if(!originalModule.file().getPath().equals(v.position().filePath()))
		{
			// This is an imported variable
			var importedFile = findNaturalFile(v.position().filePath());
			var importedModule = importedFile.module();
			var using = ((IHasDefineData)originalModule).defineData().usings().stream().filter(u -> u.target().symbolName().equals(importedModule.name())).findFirst().orElse(null);
			if(using != null)
			{
				var usingComment = getLineComment(using.position().line(), using.position().filePath());
				if (usingComment != null)
				{
					hasUsingComment = true;
					hoverText += "\n*using comment:*\n ```natural\n " + usingComment + "\n```\n";
				}
			}
		}

		var originalPositionComment = getLineComment(v.position().line(), v.position().filePath());
		if (originalPositionComment != null)
		{
			var commentTitle = hasUsingComment ? "origin comment" : "comment";
			hoverText += "%n*%s*:%n```natural%n ".formatted(commentTitle) + originalPositionComment + "\n```\n";
		}

		if (v.level() > 1)
		{
			var groupOwner = v.parent();
			while (!(groupOwner instanceof IGroupNode group) || ((IGroupNode) groupOwner).level() > 1)
			{
				groupOwner = ((ISyntaxNode) groupOwner).parent();
			}

			hoverText += "\n\n*member of:*";
			hoverText += "%n ```natural%n %s %d %s%n```".formatted(group.scope().name(), group.level(), group.name());
		}

		hoverText += "\n\n*source:*";
		hoverText += "\n- %s".formatted(v.position().filePath().toFile().getName());

		return hoverText;
	}

	private String formatVariableHover(IVariableNode v)
	{
		return formatVariableHover(v, true);
	}

	private String formatVariableHover(IVariableNode v, boolean closeMarkdown)
	{
		var hoverText = "```natural%n%s %d %s".formatted(v.scope().name(), v.level(), v.name());
		if (v instanceof ITypedVariableNode typedVariable)
		{
			hoverText += " (%c".formatted(typedVariable.type().format().identifier());
			if (typedVariable.type().length() > 0.0)
			{
				hoverText += "%s)".formatted(DataFormat.formatLength(typedVariable.type().length()));
			}
			if (typedVariable.type().hasDynamicLength())
			{
				hoverText += ") DYNAMIC";
			}
			if (typedVariable.type().isConstant())
			{
				hoverText += " CONST<";
			}
			if (typedVariable.type().initialValue() != null)
			{
				if (!typedVariable.type().isConstant())
				{
					hoverText += " INIT<";
				}

				hoverText += "%s>".formatted(typedVariable.type().initialValue().source());
			}
		}

		if(v.findDescendantToken(SyntaxKind.OPTIONAL) != null)
		{
			hoverText += " OPTIONAL";
		}

		if(closeMarkdown)
		{
			hoverText += "\n```";
		}

		if (v.isArray())
		{
			hoverText += "\n\n*dimensions:*";
			hoverText += "%n ```%n%s%n```".formatted(v.dimensions().stream().map(IArrayDimension::displayFormat).collect(Collectors.joining(",")));
		}

		return hoverText;
	}

	private String readSource(Path path)
	{
		try
		{
			return Files.readString(path);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private TokenList lexSource(String source, Path path)
	{
		return new Lexer().lex(source, path);
	}

	private TokenList lexPath(Path path)
	{
		return lexSource(readSource(path), path);
	}

	// TODO: Remove
	private IDefineData parseDefineData(TokenList tokens)
	{
		var parser = new DefineDataParser(null);
		return parser.parse(tokens).result();
	}

	public List<Location> gotoDefinition(DefinitionParams params)
	{
		var fileUri = params.getTextDocument().getUri();
		var filePath = LspUtil.uriToPath(fileUri);
		var file = findNaturalFile(filePath);
		var position = params.getPosition();

		var node = NodeUtil.findNodeAtPosition(position.getLine(), position.getCharacter(), file.module());
		// TOOD: qualified variables

		if(node instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			return List.of(LspUtil.toLocation(symbolReferenceNode.reference()));
		}

		if(node instanceof IModuleReferencingNode moduleReferencingNode)
		{
			return List.of(LspUtil.toLocation(moduleReferencingNode.reference()));
		}

		if(node instanceof ITokenNode && node.parent() instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			return List.of(LspUtil.toLocation(symbolReferenceNode.reference()));
		}

		if(node instanceof ITokenNode && node.parent() instanceof IModuleReferencingNode moduleReferencingNode)
		{
			return List.of(LspUtil.toLocation(moduleReferencingNode.reference()));
		}

		return List.of();
	}

	public List<Location> findReferences(ReferenceParams params)
	{
		var fileUri = params.getTextDocument().getUri();
		var filePath = LspUtil.uriToPath(fileUri);
		var position = params.getPosition();
		var file = findNaturalFile(filePath);

		var node = NodeUtil.findNodeAtPosition(position.getLine(), position.getCharacter(), file.module());
		if(node instanceof ITokenNode && node.parent() instanceof ISubroutineNode)
		{
			node = (ISyntaxNode) node.parent();
		}

		var references = new ArrayList<Location>();

		if(node instanceof IReferencableNode referencableNode)
		{
			references.addAll(resolveReferences(params, referencableNode));
		}

		if(node instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			references.addAll(resolveReferences(params, symbolReferenceNode.reference()));
		}

		if(node instanceof IModuleReferencingNode moduleReferencingNode)
		{
			references.addAll(moduleReferencingNode.reference().callers().stream()
				.map(caller -> LspUtil.toLocation(caller.referencingToken()))
				.toList()
			);
		}

		if(references.isEmpty())
		{
			// If we didn't find any references, lets test the ergonomics of returning the references
			// of the current module.
			// However, this current approach is super expensive, therefor we limit it.
			// The correct way to do this will be getting the actual positions in ModuleReferenceParser.
			var thresholdForExpensiveLookup = 100;
			var thresholdToDenyLookup = thresholdForExpensiveLookup * 2;
			var amountOfIncomingReferences = file.getIncomingReferences().size();

			if(amountOfIncomingReferences > thresholdToDenyLookup)
			{
				return references;
			}

			if(amountOfIncomingReferences > thresholdForExpensiveLookup)
			{
				// Getting real positions would be too expensive currently.
				references.addAll(
					languageServerProject.provideAllFiles().filter(f -> f.getOutgoingReferences().contains(file)).map(f -> new Location(f.getUri(), LspUtil.toRange(new Position(0, 0)))).toList()
				);
			}
			else
			{
				languageServerProject.provideAllFiles().filter(f -> f.getOutgoingReferences().contains(file)).forEach(f -> f.parse());
				references.addAll(
					file.module().callers().stream().filter(n -> n != null).map(n -> LspUtil.toLocation(n.referencingToken())).toList()
				);
			}
		}

		return references;
	}

	private List<Location> resolveReferences(ReferenceParams params, IReferencableNode referencableNode)
	{
		var references = new ArrayList<Location>();
		referencableNode.references().stream()
			.map(r -> LspUtil.toLocation(r.referencingToken()))
			.forEach(references::add);

		if(params.getContext().isIncludeDeclaration())
		{
			references.add(LspUtil.toLocation(referencableNode.declaration()));
		}

		return references;
	}

	public SignatureHelp signatureHelp(TextDocumentIdentifier textDocument, Position position)
	{
		var filePath = LspUtil.uriToPath(textDocument.getUri());

		var token = findTokenAtPosition(filePath, position);
		if (token == null || token.kind() != SyntaxKind.STRING_LITERAL)
		{
			return null;
		}

		var calledModule = token.stringValue();
		var calledFile = languageServerProject.findFileByReferableName(calledModule);
		var module = (INaturalModule) calledFile.module();

		if (!(module instanceof IHasDefineData hasDefineData))
		{
			return null;
		}
		var defineData = hasDefineData.defineData();

		var parameter = defineData.variables().stream()
			.filter(v -> v.scope().isParameter())
			.filter(v -> v.level() == 1)
			.map(v -> (ITypedVariableNode) v)
			.toList();

		var help = new SignatureHelp();
		var signatureInformation = new SignatureInformation(module.name());
		var parameterInfos = new ArrayList<ParameterInformation>();
		for (var p : parameter)
		{
			var parameterInfo = new ParameterInformation();
			parameterInfo.setLabel(p.name());
			parameterInfo.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, formatVariableHover(p)));
			parameterInfos.add(parameterInfo);
		}
		signatureInformation.setParameters(parameterInfos);
		help.setSignatures(List.of(signatureInformation));
		// TODO: This needs to actually set the correct current parameter position, once we have a callnat statement
		return help;
	}

	public List<CompletionItem> complete(CompletionParams completionParams)
	{
		var fileUri = completionParams.getTextDocument().getUri();
		var filePath = LspUtil.uriToPath(fileUri);

		// TODO: Use position to filter what stuff to complete (variables, subroutines, ...)
		// var position = completionParams.getPosition();

		var file = findNaturalFile(filePath);
		if(!file.getType().canHaveBody())
		{
			return List.of();
		}
		var module = file.module();

		var completionItems = new ArrayList<CompletionItem>();

		completionItems.addAll(snippetEngine.provideSnippets(file));

		completionItems.addAll(module.referencableNodes().stream()
			.filter(v -> !(v instanceof IRedefinitionNode)) // this is the `REDEFINE #VAR`, which results in the variable being doubled in completion
			.map(n -> createCompletionItem(n, file, module.referencableNodes()))
			.filter(Objects::nonNull)
			.peek(i -> {
				if(i.getKind() == CompletionItemKind.Variable)
				{
					i.setData(new UnresolvedCompletionInfo((String) i.getData(), filePath.toUri().toString()));
				}
			})
			.toList());

		return completionItems;
	}

	public CompletionItem resolveComplete(CompletionItem item)
	{
		if(item.getKind() != CompletionItemKind.Variable)
		{
			return item;
		}

		var jsonData = (JsonObject)item.getData();
		var info = new Gson().fromJson(jsonData, UnresolvedCompletionInfo.class);
		var module = findNaturalFile(LspUtil.uriToPath(info.getUri())).module();
		if(!(module instanceof IHasDefineData hasDefineData))
		{
			return item;
		}

		var variableNode = hasDefineData.defineData().variables().stream().filter(v -> v.qualifiedName().equals(info.getQualifiedName())).findFirst().orElse(null);
		if(variableNode == null)
		{
			return item;
		}

		item.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, createHoverMarkdownText(variableNode, module)));
		return item;
	}

	private CompletionItem createCompletionItem(IReferencableNode referencableNode, LanguageServerFile openFile, ReadOnlyList<IReferencableNode> referencableNodes)
	{
		try
		{
			if (referencableNode instanceof IVariableNode variableNode)
			{
				return createCompletionItem(variableNode, openFile, referencableNodes);
			}

			if (referencableNode instanceof ISubroutineNode subroutineNode)
			{
				return createCompletionItem(subroutineNode);
			}
		}
		catch (Exception e)
		{
			client.logMessage(ClientMessage.error(e.getMessage()));
		}

		return null;
	}

	private CompletionItem createCompletionItem(IVariableNode variableNode, LanguageServerFile openFile, ReadOnlyList<IReferencableNode> referencableNodes)
	{
		var item = new CompletionItem();
		var variableName = variableNode.name();

		if(referencableNodes.stream().filter(n -> n.declaration().symbolName().equals(variableNode.name())).count() > 1)
		{
			variableName = variableNode.qualifiedName();
		}

		item.setKind(CompletionItemKind.Variable);
		item.setLabel(variableName);
		var label = "";
		if (variableNode instanceof ITypedVariableNode typedNode)
		{
			label = variableName + " :" + typedNode.type().toShortString();
			item.setInsertText(variableName);
		}
		if (variableNode instanceof IGroupNode)
		{
			label = variableName + " : Group";
			item.setInsertText(variableName);
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

	private CompletionItem createCompletionItem(ISubroutineNode subroutineNode)
	{
		var item = new CompletionItem();
		item.setKind(CompletionItemKind.Method);
		item.setInsertText(subroutineNode.declaration().trimmedSymbolName(32));
		item.setLabel(subroutineNode.declaration().symbolName());
		item.setSortText("1");

		return item;
	}

	public LanguageServerFile findNaturalFile(String library, String name)
	{
		var naturalFile = project.findModule(library, name);
		return languageServerProject.findFile(naturalFile);
	}

	public LanguageServerFile findNaturalFile(Path path)
	{
		var naturalFile = project.findModule(path);
		return languageServerProject.findFile(naturalFile);
	}

	public void publishDiagnostics(LanguageServerFile file)
	{
		publishDiagnosticsOfFile(file);
		file.getIncomingReferences().forEach(this::publishDiagnosticsOfFile);
		file.getOutgoingReferences().forEach(this::publishDiagnosticsOfFile);
	}

	private void publishDiagnosticsOfFile(LanguageServerFile file)
	{
		var allDiagnostics = file.allDiagnostics();
		var shouldIncludeLinterDiagnostics = switch (file.getType()) {
			case LDA, GDA, PDA, MAP, DDM -> false;
			default -> true;
		};

		var diagnosticsToReport = shouldIncludeLinterDiagnostics ? allDiagnostics
			: allDiagnostics.stream().filter(d -> !d.getSource().equals(DiagnosticTool.NATLINT.getId())).toList();
		client.publishDiagnostics(new PublishDiagnosticsParams(file.getUri(), diagnosticsToReport));
	}

	@Override
	public void connect(LanguageClient client)
	{
		this.client = client;
	}

	public void fileSaved(Path path)
	{
		var file = findNaturalFile(path);
		if (file == null)
		{
			// TODO: Handle new file
			return;
		}

		file.save();
		publishDiagnostics(file);
	}

	public void fileClosed(Path path)
	{
		var file = findNaturalFile(path);
		if (file == null)
		{
			return;
		}

		file.close();
		publishDiagnostics(file);
	}

	public void fileOpened(Path path)
	{
		var file = findNaturalFile(path);
		if (file == null)
		{
			return;
		}

		file.open();
		publishDiagnostics(file);
	}

	public void fileChanged(Path path, String newSource)
	{
		var file = findNaturalFile(path);
		if (file == null)
		{
			return;
		}

		var start = System.currentTimeMillis();
		file.changed(newSource);
		publishDiagnostics(file);
		var end = System.currentTimeMillis();
		System.err.printf("fileChanged took %dms%n", end - start);
	}

	public void parseAll(IProgressMonitor monitor)
	{
		var libraries = languageServerProject.libraries();
		var params = new WorkDoneProgressCreateParams();
		var token = UUID.randomUUID().toString();
		params.setToken(token);

		monitor.progress("Parse whole Natural Project", 0);

		var fileCount = libraries.stream().map(l -> (long) l.files().size()).mapToLong(l -> l).sum();
		var filesParsed = 0;
		for (var lib : libraries)
		{
			for (var file : lib.files())
			{
				if (!file.getType().canHaveDefineData())
				{
					filesParsed++;
					continue;
				}
				var qualifiedName = "%s.%s".formatted(lib.name(), file.getReferableName());

				var percentage = (int) (filesParsed * 100 / fileCount);
				monitor.progress(qualifiedName, percentage);
				file.parse();
				publishDiagnostics(file);
				filesParsed++;
			}
		}

		monitor.progress("Done", 100);
	}

	public CompletableFuture<Void> parseFileReferences()
	{
		return ProgressTasks.startNew("Parsing file references", client, this::parseFileReferences);
	}

	private void parseFileReferences(IProgressMonitor monitor)
	{
		monitor.progress("Clearing current references", 0);
		var parser = new ModuleReferenceParser();
		languageServerProject.provideAllFiles().forEach(LanguageServerFile::clearAllIncomingAndOutgoingReferences);
		var allFilesCount = languageServerProject.countAllFiles();
		var processedFiles = 0L;
		for (var library : languageServerProject.libraries())
		{
			if (monitor.isCancellationRequested())
			{
				break;
			}
			for (var file : library.files())
			{
				if (monitor.isCancellationRequested())
				{
					break;
				}
				var percentageDone = 100L * processedFiles / allFilesCount;
				monitor.progress("Indexing %s.%s".formatted(library.name(), file.getReferableName()), (int) percentageDone);
				switch (file.getType())
				{
					case PROGRAM, SUBPROGRAM, SUBROUTINE, FUNCTION -> parser.parseReferences(file);
				}
				processedFiles++;
			}
		}
	}

	public boolean isInitialized()
	{
		return initialized;
	}

	public List<CallHierarchyOutgoingCall> createCallHierarchyOutgoingCalls(CallHierarchyItem item)
	{
		var file = findNaturalFile(LspUtil.uriToPath(item.getUri()));
		return file.getOutgoingReferences().stream()
			.map(r -> {
				var call = new CallHierarchyOutgoingCall();
				call.setTo(callHierarchyItem(r));
				call.setFromRanges(List.of(item.getRange()));
				return call;
			})
			.toList();
	}

	public List<CallHierarchyIncomingCall> createCallHierarchyIncomingCalls(CallHierarchyItem item)
	{
		var file = findNaturalFile(LspUtil.uriToPath(item.getUri()));
		return file.module().callers().stream()
			.map(r -> {
				var call = new CallHierarchyIncomingCall();
				call.setFrom(callHierarchyItem(r, findNaturalFile(r.referencingToken().filePath()).getReferableName()));
				call.setFromRanges(List.of(new Range(new Position(0, 0), new Position(0, 0))));
				return call;
			})
			.toList();
	}

	public List<CallHierarchyItem> createCallHierarchyItems(CallHierarchyPrepareParams params)
	{
		// TODO: Use Position from params. If in DEFINE DATA or top level statement block, search for module references
		// 	If within local subroutine, get the local call hierarchy to that subroutine
		var file = findNaturalFile(LspUtil.uriToPath(params.getTextDocument().getUri()));
		var item = new CallHierarchyItem();
		item.setRange(new Range(new Position(0, 0), new Position(0, 0)));
		item.setSelectionRange(new Range(new Position(0, 0), new Position(0, 0)));
		item.setName(file.getReferableName());
		item.setDetail(file.getType().toString());
		item.setUri(params.getTextDocument().getUri());
		item.setKind(SymbolKind.Class);
		return List.of(item);
	}

	private CallHierarchyItem callHierarchyItem(LanguageServerFile file)
	{
		var item = new CallHierarchyItem();
		item.setRange(new Range(new Position(0, 0), new Position(0, 0)));
		item.setSelectionRange(new Range(new Position(0, 0), new Position(0, 0)));
		item.setName(file.getReferableName());
		item.setDetail(file.getType().toString());
		item.setUri(file.getPath().toUri().toString());
		item.setKind(SymbolKind.Class);
		return item;
	}

	private CallHierarchyItem callHierarchyItem(IModuleReferencingNode node, String referableModuleName)
	{
		var item = new CallHierarchyItem();
		item.setRange(LspUtil.toRange(node.referencingToken()));
		item.setSelectionRange(LspUtil.toRange(node));
		item.setName(referableModuleName);
		item.setDetail(node.getClass().getSimpleName());
		item.setUri(node.referencingToken().filePath().toUri().toString());
		item.setKind(SymbolKind.Class);
		return item;
	}

	public List<CodeAction> codeAction(CodeActionParams params)
	{
		var file = findNaturalFile(LspUtil.uriToPath(params.getTextDocument().getUri()));
		var token = findTokenAtPosition(file.getPath(), params.getRange().getStart());
		var node = NodeUtil.findNodeAtPosition(params.getRange().getStart().getLine(), params.getRange().getStart().getCharacter(), file.module());
		if(node == null)
		{
			return List.of();
		}

		var context = new RefactoringContext(params.getTextDocument().getUri(), file.module(), token, node, file.diagnosticsInRange(params.getRange()));

		return codeActionRegistry.createCodeActions(context);
	}

	public PrepareRenameResult prepareRename(PrepareRenameParams params)
	{
		var path = LspUtil.uriToPath(params.getTextDocument().getUri());
		var file = findNaturalFile(path);

		var node = NodeUtil.findNodeAtPosition(params.getPosition().getLine(), params.getPosition().getCharacter(), file.module());

		String placeholder = null;
		if(node instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			placeholder = symbolReferenceNode.reference().declaration().symbolName();
		}

		if(node instanceof IReferencableNode rNode)
		{
			placeholder = rNode.declaration().symbolName();
		}

		if(placeholder == null)
		{
			// Nothing we can rename
			throw new ResponseErrorException(new ResponseError(1, "Can't rename %s".formatted(node.getClass().getSimpleName()), null));
		}

		assertCanRenameInFile(file);

		file.reparseCallers(); // TODO: This should be some kind of "light" parse that doesn't add diagnostics

		var result = new PrepareRenameResult();
		result.setRange(LspUtil.toRange(node.position()));
		result.setPlaceholder(placeholder);
		return result;
	}

	public WorkspaceEdit rename(RenameParams params)
	{
		var path = LspUtil.uriToPath(params.getTextDocument().getUri());
		var file = findNaturalFile(path);

		var node = NodeUtil.findNodeAtPosition(params.getPosition().getLine(), params.getPosition().getCharacter(), file.module());
		if(node instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			return renameComputer.rename(symbolReferenceNode, params.getNewName());
		}

		if(node instanceof IReferencableNode referencableNode)
		{
			return renameComputer.rename(referencableNode, params.getNewName());
		}

		if(node instanceof ITokenNode && node.parent() instanceof IReferencableNode referencableNode)
		{
			return renameComputer.rename(referencableNode, params.getNewName());
		}

		return null;
	}

	private void assertCanRenameInFile(LanguageServerFile file)
	{
		var referenceLimit = 300; // Some arbitrary tested value
		if(file.getIncomingReferences().size() > referenceLimit)
		{
			throw new ResponseErrorException(new ResponseError(1, "Won't rename inside %s because it has more than %d referrers (%d)".formatted(file.getReferableName(), referenceLimit, file.getIncomingReferences().size()), null));
		}
	}

	public void invalidateStowCache(LanguageServerFile file)
	{
		var cacheFile = workspaceRoot.resolve("cache_deploy_Incr_VERSIS.properties");
		try(var lines = Files.lines(cacheFile))
		{
			var newLines = lines.map(l -> {
				System.err.println(file.getPath().toString());
				if(l.startsWith(file.getPath().toString()))
				{
					return file.getPath().toString() + "=";
				}

				return l;
			})
			.collect(Collectors.joining(System.lineSeparator()));

			Files.writeString(cacheFile, newLines);
		}
		catch(IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
