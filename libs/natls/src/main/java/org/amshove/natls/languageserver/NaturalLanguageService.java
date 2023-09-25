package org.amshove.natls.languageserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.amshove.natlint.editorconfig.EditorConfigParser;
import org.amshove.natlint.linter.LinterContext;
import org.amshove.natls.DiagnosticTool;
import org.amshove.natls.LanguageServerException;
import org.amshove.natls.codeactions.CodeActionRegistry;
import org.amshove.natls.codeactions.RefactoringContext;
import org.amshove.natls.codeactions.RenameSymbolAction;
import org.amshove.natls.codelens.CodeLensService;
import org.amshove.natls.completion.CompletionProvider;
import org.amshove.natls.config.LSConfiguration;
import org.amshove.natls.documentsymbol.DocumentSymbolProvider;
import org.amshove.natls.hover.HoverContext;
import org.amshove.natls.hover.HoverProvider;
import org.amshove.natls.inlayhints.InlayHintProvider;
import org.amshove.natls.progress.IProgressMonitor;
import org.amshove.natls.progress.ProgressTasks;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natls.project.ModuleReferenceParser;
import org.amshove.natls.project.ParseStrategy;
import org.amshove.natls.referencing.ReferenceFinder;
import org.amshove.natls.signaturehelp.SignatureHelpProvider;
import org.amshove.natls.snippets.SnippetEngine;
import org.amshove.natls.workspace.RenameFileHandler;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class NaturalLanguageService implements LanguageClientAware
{
	private static final CodeActionRegistry codeActionRegistry = CodeActionRegistry.INSTANCE;
	private NaturalProject project; // TODO: Replace
	private LanguageServerProject languageServerProject;
	private LanguageClient client;
	private boolean initialized;
	private Path workspaceRoot;

	private final InlayHintProvider inlayHintProvider = new InlayHintProvider();
	private HoverProvider hoverProvider;
	private final RenameSymbolAction renameComputer = new RenameSymbolAction();
	private final RenameFileHandler renameFileHandler = new RenameFileHandler();
	private final CodeLensService codeLensService = new CodeLensService();

	private static LSConfiguration config = LSConfiguration.createDefault();
	private final ReferenceFinder referenceFinder = new ReferenceFinder();
	private final SignatureHelpProvider signatureHelp = new SignatureHelpProvider();
	private CompletionProvider completionProvider;

	public void indexProject(Path workspaceRoot, IProgressMonitor progressMonitor)
	{
		this.workspaceRoot = workspaceRoot;
		var projectFile = new ActualFilesystem().findNaturalProjectFile(workspaceRoot);
		if (projectFile.isEmpty())
		{
			throw new LanguageServerException("Could not load Natural project. .natural or _naturalBuild not found");
		}
		var project = new BuildFileProjectReader().getNaturalProject(projectFile.get());
		var editorconfigPath = projectFile.get().getParent().resolve(".editorconfig");
		if (editorconfigPath.toFile().exists())
		{
			loadEditorConfig(editorconfigPath);
		}

		var indexer = new NaturalProjectFileIndexer();
		indexer.indexProject(project);
		this.project = project;
		languageServerProject = LanguageServerProject.fromProject(project);
		parseFileReferences(progressMonitor);
		preParseDataAreas(progressMonitor);
		initialized = true;
		hoverProvider = new HoverProvider();
		completionProvider = new CompletionProvider(new SnippetEngine(languageServerProject), hoverProvider);
	}

	public void loadEditorConfig(Path path)
	{
		try
		{
			var content = Files.readString(path);
			var newConfig = new EditorConfigParser().parse(content);
			LinterContext.INSTANCE.updateEditorConfig(newConfig);
		}
		catch (Exception e)
		{
			if (client != null)
			{
				client.logMessage(ClientMessage.warn("Error reading editorconfig: " + e.getMessage()));
			}
		}
	}

	public List<DocumentSymbol> findSymbolsInFile(TextDocumentIdentifier textDocument)
	{
		var filepath = LspUtil.uriToPath(textDocument.getUri());
		var module = findNaturalFile(filepath).module();
		return new DocumentSymbolProvider().provideSymbols(module);
	}

	public void createdFile(String uri)
	{
		var path = LspUtil.uriToPath(uri);
		var lspFile = languageServerProject.addFile(path);
		lspFile.parse();
	}

	public static void setConfiguration(LSConfiguration configuration)
	{
		config = configuration;
	}

	public List<? extends SymbolInformation> findWorkspaceSymbols(String query, CancelChecker cancelChecker)
	{
		return project.getLibraries().stream()
			.flatMap(l ->
			{
				cancelChecker.checkCanceled();
				return l.files().stream();
			})
			.filter(f -> f.getReferableName().toLowerCase().contains(query.toLowerCase()))
			.limit(100)
			.map(f ->
			{
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

	public Hover hoverSymbol(TextDocumentIdentifier textDocument, Position position)
	{
		var filepath = LspUtil.uriToPath(textDocument.getUri());
		var file = findNaturalFile(filepath);
		if (file.getType() == NaturalFileType.COPYCODE)
		{
			return HoverProvider.EMPTY_HOVER;
		}

		var module = file.module();

		// special case for the callnat string containing the called module
		var node = NodeUtil.findTokenNodeAtPosition(filepath, position.getLine(), position.getCharacter(), module.syntaxTree());

		var symbolToSearchFor = findTokenAtPosition(file, position); // TODO: Actually look for a node, could be ISymbolReferenceNode
		var providedHover = hoverProvider.createHover(new HoverContext(node, symbolToSearchFor, file));
		return providedHover != null ? providedHover : HoverProvider.EMPTY_HOVER;
	}

	private SyntaxToken findTokenAtPosition(LanguageServerFile file, Position position)
	{
		var tokenNodeAtPosition = NodeUtil.findTokenNodeAtPosition(file.getPath(), position.getLine(), position.getCharacter(), file.module().syntaxTree());
		return tokenNodeAtPosition != null ? tokenNodeAtPosition.token() : null;
	}

	public static LSConfiguration getConfig()
	{
		return config;
	}

	public List<Location> gotoDefinition(DefinitionParams params)
	{
		var fileUri = params.getTextDocument().getUri();
		var filePath = LspUtil.uriToPath(fileUri);
		var file = findNaturalFile(filePath);
		var position = params.getPosition();

		var node = NodeUtil.findTokenNodeAtPosition(filePath, position.getLine(), position.getCharacter(), file.module().syntaxTree());
		if (node == null)
		{
			return List.of();
		}

		if (node instanceof IVariableReferenceNode variableReferenceNode)
		{
			return List.of(LspUtil.toLocation(variableReferenceNode.reference()));
		}

		if (node.parent()instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			return List.of(LspUtil.toLocation(symbolReferenceNode.reference()));
		}

		if (node.parent()instanceof IModuleReferencingNode moduleReferencingNode)
		{
			return List.of(LspUtil.toLocation(moduleReferencingNode.reference()));
		}

		return List.of();
	}

	public CompletableFuture<List<? extends Location>> findReferences(ReferenceParams params)
	{
		return ProgressTasks.startNew("Finding references", client, monitor ->
		{
			var fileUri = params.getTextDocument().getUri();
			var filePath = LspUtil.uriToPath(fileUri);
			var file = findNaturalFile(filePath);
			return referenceFinder.findReferences(params, file, monitor);
		});
	}

	public SignatureHelp signatureHelp(TextDocumentIdentifier textDocument, Position position)
	{
		var filePath = LspUtil.uriToPath(textDocument.getUri());
		var file = findNaturalFile(filePath);
		var module = file.module();
		return signatureHelp.provideSignatureHelp(module, position);
	}

	public List<CompletionItem> complete(CompletionParams completionParams)
	{
		try
		{
			return completionProvider.prepareCompletion(findNaturalFile(completionParams.getTextDocument()), completionParams, config);
		}
		catch (Exception e)
		{
			client.logMessage(ClientMessage.error(e.getMessage()));
			return List.of();
		}
	}

	public CompletionItem resolveComplete(CompletionItem item)
	{
		if (item.getData() == null)
		{
			return item;
		}

		var info = extractJsonObject(item.getData(), UnresolvedCompletionInfo.class);
		var file = findNaturalFile(LspUtil.uriToPath(info.getUri()));
		return completionProvider.resolveComplete(item, file, info, config);
	}

	public LanguageServerFile findNaturalFile(String library, String name)
	{
		var naturalFile = project.findModule(library, name);
		return languageServerProject.findFile(naturalFile);
	}

	public LanguageServerFile findNaturalFile(TextDocumentIdentifier identifier)
	{
		var naturalFile = project.findModule(LspUtil.uriToPath(identifier.getUri()));
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
		var shouldIncludeLinterDiagnostics = switch (file.getType())
		{
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
		client.refreshCodeLenses();
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

		file.changed(newSource);
		publishDiagnostics(file);
		client.refreshCodeLenses();
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
		return ProgressTasks.startNewVoid("Parsing file references", client, this::parseFileReferences);
	}

	private void preParseDataAreas(IProgressMonitor monitor)
	{
		monitor.progress("Preparsing data areas", 0);
		languageServerProject.libraries().stream().flatMap(l -> l.files().stream().filter(f -> f.getType() == NaturalFileType.LDA || f.getType() == NaturalFileType.PDA))
			.parallel()
			.peek(f -> monitor.progress(f.getReferableName(), 0))
			.forEach(f -> f.parse(ParseStrategy.WITHOUT_CALLERS));
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
					case PROGRAM, SUBPROGRAM, SUBROUTINE, FUNCTION, COPYCODE -> parser.parseReferences(file);
					default ->
					{}
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
			.map(r ->
			{
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
			.map(r ->
			{
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
		var token = findTokenAtPosition(file, params.getRange().getStart());
		var nodeAtStart = NodeUtil.findNodeAtPosition(params.getRange().getStart().getLine(), params.getRange().getStart().getCharacter(), file.module());
		if (nodeAtStart == null && params.getRange().getStart().equals(params.getRange().getEnd()))
		{
			return List.of();
		}
		var nodeAtEnd = NodeUtil.findNodeAtPosition(params.getRange().getEnd().getLine(), params.getRange().getEnd().getCharacter(), file.module());

		var diagnosticsAtPosition = file.diagnosticsInRange(params.getRange());
		var context = new RefactoringContext(params.getTextDocument().getUri(), file.module(), file, token, params.getRange(), nodeAtStart, nodeAtEnd, diagnosticsAtPosition);

		return codeActionRegistry.createCodeActions(context);
	}

	public PrepareRenameResult prepareRename(PrepareRenameParams params)
	{
		var path = LspUtil.uriToPath(params.getTextDocument().getUri());
		var file = findNaturalFile(path);

		var node = NodeUtil.findTokenNodeAtPosition(path, params.getPosition().getLine(), params.getPosition().getCharacter(), file.module().syntaxTree());
		if (node == null)
		{
			throw new ResponseErrorException(new ResponseError(1, "Nothing renamable found", null));
		}

		String placeholder = null;

		if (node instanceof IVariableReferenceNode variableReferenceNode)
		{
			placeholder = variableReferenceNode.token().source();
		}

		if (node instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			placeholder = symbolReferenceNode.reference().declaration().symbolName();
		}

		if (node.parent()instanceof IReferencableNode rNode)
		{
			placeholder = rNode.declaration().symbolName();
		}

		if (placeholder == null)
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
		var fileUri = params.getTextDocument().getUri();
		var path = LspUtil.uriToPath(fileUri);
		var file = findNaturalFile(path);

		var module = file.module();
		var node = NodeUtil.findTokenNodeAtPosition(path, params.getPosition().getLine(), params.getPosition().getCharacter(), module.syntaxTree());
		if (node instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			if (file.getType() == NaturalFileType.FUNCTION && symbolReferenceNode.reference().declaration() == ((IFunction) module).functionName())
			{
				return renameFunctionAndItsFile(params, path, fileUri);
			}

			return renameComputer.rename(symbolReferenceNode, params.getNewName());
		}

		if (node instanceof IReferencableNode referencableNode)
		{
			return renameComputer.rename(referencableNode, params.getNewName());
		}

		if (node != null && node.parent()instanceof IReferencableNode referencableNode)
		{
			if (file.getType() == NaturalFileType.SUBROUTINE && referencableNode instanceof ISubroutineNode subroutine && subroutine.declaration().symbolName().equals(module.name()))
			{
				var edits = renameComputer.renameExternalSubroutine(params.getNewName(), subroutine, module, file);
				languageServerProject.renameReferableModule(params.getTextDocument().getUri(), params.getNewName());
				return edits;
			}
			return renameComputer.rename(referencableNode, params.getNewName());
		}

		return null;
	}

	private WorkspaceEdit renameFunctionAndItsFile(RenameParams params, Path oldPath, String oldUri)
	{
		var workspaceEdit = new WorkspaceEdit();
		var newUri = oldPath.getParent().resolve("%s.%s".formatted(params.getNewName(), "NS7")).toUri().toString();

		var changes = new ArrayList<Either<TextDocumentEdit, ResourceOperation>>();
		workspaceEdit.setDocumentChanges(changes);

		// Act like the function has been renamed by file rename and get all the changes that have to be done
		var renameFileChanges = willRenameFiles(List.of(new FileRename(params.getTextDocument().getUri(), newUri)));
		for (Map.Entry<String, List<TextEdit>> stringListEntry : renameFileChanges.getChanges().entrySet())
		{
			var txtDocEdit = new TextDocumentEdit(new VersionedTextDocumentIdentifier(stringListEntry.getKey(), 0), stringListEntry.getValue());
			changes.add(Either.forLeft(txtDocEdit));
		}

		// Rename the file as last change
		var operation = new RenameFile();
		operation.setNewUri(newUri);
		operation.setOldUri(oldUri);
		operation.setOptions(new RenameFileOptions(false, true));
		changes.add(Either.forRight(operation));

		return workspaceEdit;
	}

	private void assertCanRenameInFile(LanguageServerFile file)
	{
		var referenceLimit = 300; // Some arbitrary tested value
		if (file.getIncomingReferences().size() > referenceLimit)
		{
			client.showMessage(ClientMessage.error("Won't rename inside %s because it has more than %d referrers (%d)".formatted(file.getReferableName(), referenceLimit, file.getIncomingReferences().size())));
			throw new ResponseErrorException(new ResponseError(1, "Won't rename inside %s because it has more than %d referrers (%d)".formatted(file.getReferableName(), referenceLimit, file.getIncomingReferences().size()), null));
		}
	}

	public void invalidateStowCache(LanguageServerFile file)
	{
		var cacheFile = workspaceRoot.resolve("cache_deploy_Incr_VERSIS.properties");
		try (var lines = Files.lines(cacheFile))
		{
			var newLines = lines.map(l ->
			{
				if (l.startsWith(file.getPath().toString()))
				{
					return file.getPath().toString() + "=";
				}

				return l;
			})
				.collect(Collectors.joining(System.lineSeparator()));

			Files.writeString(cacheFile, newLines);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public List<CodeLens> codeLens(CodeLensParams params)
	{
		var path = LspUtil.uriToPath(params.getTextDocument().getUri());
		var file = findNaturalFile(path);
		return codeLensService.provideCodeLens(file);
	}

	public List<InlayHint> inlayHints(InlayHintParams params)
	{
		var module = findNaturalFile(LspUtil.uriToPath(params.getTextDocument().getUri())).module();
		return inlayHintProvider.provideInlayHints(module, params.getRange());
	}

	public LanguageServerFile findReferableName(String libraryName, String referableName)
	{
		for (var library : languageServerProject.libraries())
		{
			if (library.name().equalsIgnoreCase(libraryName))
			{
				return library.findFile(referableName);
			}
		}

		return null;
	}

	public List<TextEdit> format(DocumentFormattingParams params)
	{
		var file = findNaturalFile(LspUtil.uriToPath(params.getTextDocument().getUri()));
		var edits = new ArrayList<TextEdit>();
		file.tokens().forEach(t ->
		{
			if (t.kind() == SyntaxKind.STRING_LITERAL)
			{
				return;
			}

			var upper = t.source().toUpperCase();
			if (!upper.equals(t.source()))
			{
				var edit = new TextEdit(LspUtil.toRange(t), upper);
				edits.add(edit);
			}
		});

		return edits;
	}

	public WorkspaceEdit willRenameFiles(List<FileRename> renames)
	{
		return renameFileHandler.handleFileRename(renames, languageServerProject);
	}

	public LanguageServerProject getProject()
	{
		return languageServerProject;
	}

	private static <T> T extractJsonObject(Object obj, Class<T> clazz)
	{
		if (clazz.isInstance(obj))
		{
			return clazz.cast(obj);
		}

		var jsonData = (JsonObject) obj;
		return new Gson().fromJson(jsonData, clazz);
	}
}
