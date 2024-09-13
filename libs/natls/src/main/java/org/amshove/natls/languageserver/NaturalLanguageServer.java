package org.amshove.natls.languageserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.amshove.natls.App;
import org.amshove.natls.codeactions.CodeActionRegistry;
import org.amshove.natls.config.LSConfiguration;
import org.amshove.natls.languageserver.constantfinding.FindConstantsParams;
import org.amshove.natls.languageserver.constantfinding.FindConstantsResponse;
import org.amshove.natls.languageserver.inputstructure.InputStructureParams;
import org.amshove.natls.languageserver.inputstructure.InputStructureResponse;
import org.amshove.natls.markupcontent.MarkdownContentBuilder;
import org.amshove.natls.markupcontent.MarkupContentBuilderFactory;
import org.amshove.natls.progress.*;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.*;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.amshove.natls.SafeWrap.wrapSafe;

public class NaturalLanguageServer implements LanguageServer, LanguageClientAware
{
	private static final Logger log = Logger.getAnonymousLogger();
	private final NaturalWorkspaceService workspaceService = new NaturalWorkspaceService();
	private final NaturalDocumentService documentService = new NaturalDocumentService();
	private final NaturalLanguageService languageService = new NaturalLanguageService();
	private LanguageClient client;

	@Override
	@SuppressWarnings("deprecation") // Further support old LSP versions
	public CompletableFuture<InitializeResult> initialize(InitializeParams params)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			var initStart = System.currentTimeMillis();
			log.info("Starting initialization");
			var capabilities = new ServerCapabilities();

			var config = params.getInitializationOptions() != null
				? new Gson().fromJson((JsonObject) params.getInitializationOptions(), LSConfiguration.class)
				: LSConfiguration.createDefault();
			NaturalLanguageService.setConfiguration(config);

			capabilities.setWorkspaceSymbolProvider(true);
			capabilities.setDocumentSymbolProvider(new DocumentSymbolOptions("NatLS"));
			var hoverOptions = new HoverOptions();
			hoverOptions.setWorkDoneProgress(true);
			capabilities.setHoverProvider(true);
			capabilities.setHoverProvider(hoverOptions);
			capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
			capabilities.setDefinitionProvider(true);
			capabilities.setReferencesProvider(true);
			capabilities.setFoldingRangeProvider(true);

			capabilities.setCompletionProvider(new CompletionOptions(true, List.of(".")));

			capabilities.setCodeLensProvider(new CodeLensOptions(false));
			capabilities.setCallHierarchyProvider(true);
			capabilities.setCodeActionProvider(CodeActionRegistry.INSTANCE.registeredCodeActionCount() > 0);
			capabilities.setRenameProvider(new RenameOptions(true));
			var inlayHintRegistrationOptions = new InlayHintRegistrationOptions();
			inlayHintRegistrationOptions.setId("natls");
			inlayHintRegistrationOptions.setResolveProvider(true);
			capabilities.setInlayHintProvider(inlayHintRegistrationOptions);

			capabilities.setSignatureHelpProvider(new SignatureHelpOptions());

			capabilities.setDocumentFormattingProvider(true);

			var workspace = new WorkspaceServerCapabilities();
			var fileOperations = new FileOperationsServerCapabilities();
			var naturalFileOperationOptions = new FileOperationOptions(
				Arrays.stream(NaturalFileType.values()).map(
					ft -> new FileOperationFilter(
						new FileOperationPattern(
							"**/Natural-Libraries/**/*.%s".formatted(ft.getExtension())
						)
					)
				)
					.toList()
			);
			fileOperations.setDidCreate(naturalFileOperationOptions);
			fileOperations.setWillRename(naturalFileOperationOptions);
			workspace.setFileOperations(fileOperations);
			var workspaceFoldersOptions = new WorkspaceFoldersOptions();
			workspaceFoldersOptions.setSupported(false);
			workspaceFoldersOptions.setChangeNotifications(true);
			workspace.setWorkspaceFolders(workspaceFoldersOptions);
			capabilities.setWorkspace(workspace);

			MarkupContentBuilderFactory.configureFactory(MarkdownContentBuilder::new);

			var progressMonitor = params.getWorkDoneToken() != null
				? new WorkDoneProgressMonitor(params.getWorkDoneToken().getLeft(), client)
				: new MessageProgressMonitor(client);

			if (params.getCapabilities().getWindow() != null && params.getCapabilities().getWindow().getWorkDoneProgress())
			{
				ProgressTasks.setClientProgressType(ClientProgressType.WORK_DONE);
			}

			if (client != null)
			{
				var watchFileMethod = "workspace/didChangeWatchedFiles";
				var sourceWatcher = new FileSystemWatcher(Either.forLeft("**/Natural-Libraries/**/*.*"));
				var watchChangesRegistrationOption = new DidChangeWatchedFilesRegistrationOptions(List.of(sourceWatcher));
				client.registerCapability(new RegistrationParams(List.of(new Registration(UUID.randomUUID().toString(), watchFileMethod, watchChangesRegistrationOption))));
			}

			if (params.getWorkDoneToken() != null)
			{
				var begin = new WorkDoneProgressBegin();
				begin.setTitle("Natural Language Server initializing");
				begin.setMessage("");
				begin.setPercentage(0);
				client.notifyProgress(new ProgressParams(params.getWorkDoneToken(), Either.forLeft(begin)));
			}
			else
			{
				progressMonitor.progress("Natural Language Server initializing", 0);
			}

			var startTime = System.currentTimeMillis();
			progressMonitor.progress("Begin Indexing", 10);
			languageService.indexProject(Paths.get(URI.create(params.getRootUri())), progressMonitor);
			workspaceService.setLanguageService(languageService);
			documentService.setLanguageService(languageService);
			var endTime = System.currentTimeMillis();

			if (params.getWorkDoneToken() != null)
			{
				var end = new WorkDoneProgressEnd();
				end.setMessage("Initialization done");
				client.notifyProgress(new ProgressParams(params.getWorkDoneToken(), Either.forLeft(end)));
			}
			else
			{
				progressMonitor.progress("Initialization done in %dms".formatted(endTime - startTime), 100);
			}

			BackgroundTasks.initialize(client);
			var lspName = App.class.getPackage().getImplementationTitle();
			var lspVersion = App.class.getPackage().getImplementationVersion();
			var initEnd = System.currentTimeMillis();
			log.info("Initialization done. Took %dms".formatted(initEnd - initStart));
			return new InitializeResult(capabilities, new ServerInfo(lspName != null ? lspName : "natls", lspVersion != null ? lspVersion : "dev"));
		});
	}

	@Override
	public void initialized(InitializedParams params)
	{
		log.info("initialized() called");
		if (NaturalLanguageService.getConfig().getInitialization().isAsync())
		{
			client.showMessage(ClientMessage.info("Natural project is initializing"));
			var fileReferences = languageService.parseFileReferencesAsync();
			var dataAreas = languageService.preparseDataAreasAsync();
			CompletableFuture.allOf(fileReferences, dataAreas)
				.whenComplete((v, error) ->
				{
					if (error == null)
					{
						client.showMessage(ClientMessage.info("Natural project initialization done"));
						client.refreshCodeLenses();
					}
					else
					{
						client.showMessage(ClientMessage.error("Natural project initialization failed"));
					}
					languageService.setInitialized();
				});
		}
		log.info("initialized() returned");
	}

	@Override
	public CompletableFuture<Object> shutdown()
	{
		return CompletableFuture.completedFuture(new Object());
	}

	@Override
	public void exit()
	{
		log.info("Exit signal received");
		System.exit(0);
	}

	public NaturalLanguageService getLanguageService()
	{
		return languageService;
	}

	@Override
	public TextDocumentService getTextDocumentService()
	{
		return documentService;
	}

	@Override
	public WorkspaceService getWorkspaceService()
	{
		return workspaceService;
	}

	@Override
	public void setTrace(SetTraceParams params)
	{
		var level = params.getValue().equalsIgnoreCase("verbose")
			? Level.FINE
			: Level.INFO;

		log.info(() -> "Setting LogLevel to %s".formatted(level));
		var rootLogger = Logger.getLogger("");
		rootLogger.setLevel(level);
		for (var handler : rootLogger.getHandlers())
		{
			handler.setLevel(level);
		}
	}

	@Override
	public void connect(LanguageClient client)
	{
		this.client = client;
		languageService.connect(client);
	}

	@JsonRequest
	@SuppressWarnings("unused") // used as endpoint
	public CompletableFuture<Void> parseProject(Object params)
	{
		if (languageService.isInitialized())
		{
			return ProgressTasks.startNewVoid("Parsing Natural Project", client, languageService::parseAll);
		}

		return CompletableFuture.completedFuture(null);
	}

	@JsonRequest
	@SuppressWarnings("unused") // used as endpoint
	public CompletableFuture<Void> reparseReferences(Object params)
	{
		if (languageService.isInitialized())
		{
			languageService.parseFileReferencesAsync();
		}

		return wrapSafe(() -> CompletableFuture.completedFuture(null));
	}

	@JsonRequest
	@SuppressWarnings("unused")
	public CompletableFuture<ReferableFileExistsResponse> referableFileExists(ReferableFileExistsParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() ->
		{
			var matchingFiles = languageService.findReferableName(params.getLibrary(), params.getReferableName());
			return new ReferableFileExistsResponse(matchingFiles != null && !matchingFiles.isEmpty());
		})
		);
	}

	@JsonRequest
	@SuppressWarnings("unused")
	public CompletableFuture<CalledModulesResponse> calledModules(CalledModulesParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.getCalledModules(params.getIdentifier())));
	}

	@JsonRequest
	@SuppressWarnings("unused")
	public CompletableFuture<InputStructureResponse> inputStructure(InputStructureParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.getInputStructure(params)));
	}

	@JsonRequest
	@SuppressWarnings("unused")
	public CompletableFuture<FindConstantsResponse> findConstants(FindConstantsParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.findConstants(params)));
	}

	@Override
	public NotebookDocumentService getNotebookDocumentService()
	{
		return null;
	}
}
