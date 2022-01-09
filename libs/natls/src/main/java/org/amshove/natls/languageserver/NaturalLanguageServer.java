package org.amshove.natls.languageserver;

import org.amshove.natls.progress.ClientProgressType;
import org.amshove.natls.progress.MessageProgressMonitor;
import org.amshove.natls.progress.ProgressTasks;
import org.amshove.natls.progress.WorkDoneProgressMonitor;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.*;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NaturalLanguageServer implements LanguageServer, LanguageClientAware
{
	private final NaturalWorkspaceService workspaceService = new NaturalWorkspaceService();
	private final NaturalDocumentService documentService = new NaturalDocumentService();
	private final NaturalLanguageService languageService = new NaturalLanguageService();
	private LanguageClient client;

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params)
	{
		return CompletableFuture.supplyAsync(() -> {
			var capabilities = new ServerCapabilities();

			capabilities.setWorkspaceSymbolProvider(true);
			capabilities.setDocumentSymbolProvider(true);
			capabilities.setHoverProvider(true);
			capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
			capabilities.setDefinitionProvider(true);
			capabilities.setReferencesProvider(true);
			capabilities.setCompletionProvider(new CompletionOptions(false, List.of(".")));
			capabilities.setCodeLensProvider(new CodeLensOptions(true));
			capabilities.setSignatureHelpProvider(new SignatureHelpOptions()); // Maybe < for Functions?

			var progressMonitor = params.getWorkDoneToken() != null
				? new WorkDoneProgressMonitor(params.getWorkDoneToken().getLeft(), client)
				: new MessageProgressMonitor(client);

			if (client != null)
			{
				var watchFileMethod = "workspace/didChangeWatchedFiles";
				var natunitWatcher = new FileSystemWatcher("build/test-results/**/*.xml");
				var sourceWatcher = new FileSystemWatcher("Natural-Libraries/**/*.*");
				var watchChangesRegistrationOption = new DidChangeWatchedFilesRegistrationOptions(List.of(natunitWatcher, sourceWatcher));
				client.registerCapability(new RegistrationParams(List.of(new Registration(UUID.randomUUID().toString(), watchFileMethod, watchChangesRegistrationOption))));
			}

			if(params.getWorkDoneToken() != null)
			{
				ProgressTasks.setClientProgressType(ClientProgressType.WORK_DONE); // move somewhere else?
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
			progressMonitor.progress("Begin indexing", 5);
			languageService.indexProject(Paths.get(URI.create(params.getRootUri())), progressMonitor);
			workspaceService.setLanguageService(languageService);
			documentService.setLanguageService(languageService);
			var endTime = System.currentTimeMillis();

			if(params.getWorkDoneToken() != null)
			{
				var end = new WorkDoneProgressEnd();
				end.setMessage("Initialization done");
				client.notifyProgress(new ProgressParams(params.getWorkDoneToken(), Either.forLeft(end)));
			}
			else
			{
				progressMonitor.progress("Initialization done in %dms".formatted(endTime - startTime), 100);
			}
			return new InitializeResult(capabilities);
		});
	}

	@Override
	public CompletableFuture<Object> shutdown()
	{
		return CompletableFuture.completedFuture(new Object());
	}

	@Override
	public void exit()
	{

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
	public void connect(LanguageClient client)
	{
		this.client = client;
		languageService.connect(client);
	}

	@JsonRequest
	public CompletableFuture<Void> parseProject(Object params)
	{
		if (languageService.isInitialized())
		{
			return ProgressTasks.startNew("Parsing Natural Project", client, languageService::parseAll);
		}

		return CompletableFuture.completedFuture(null);
	}

	@JsonRequest
	public CompletableFuture<Void> reparseReferences(Object params)
	{
		if (languageService.isInitialized())
		{
			return languageService.parseFileReferences();
		}

		return CompletableFuture.completedFuture(null);
	}
}
