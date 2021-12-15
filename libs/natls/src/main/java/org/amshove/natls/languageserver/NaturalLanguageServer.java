package org.amshove.natls.languageserver;

import org.eclipse.lsp4j.*;
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

			if (client != null)
			{
				var watchFileMethod = "workspace/didChangeWatchedFiles";
				var natunitWatcher = new FileSystemWatcher("build/test-results/**/*.xml");
				var sourceWatcher = new FileSystemWatcher("Natural-Libraries/**/*.*");
				var watchChangesRegistrationOption = new DidChangeWatchedFilesRegistrationOptions(List.of(natunitWatcher, sourceWatcher));
				client.registerCapability(new RegistrationParams(List.of(new Registration(UUID.randomUUID().toString(), watchFileMethod, watchChangesRegistrationOption))));
			}

			if (client != null)
			{
				client.showMessage(ClientMessage.info("Natural Language Server initializing..."));
			}

			var start = System.currentTimeMillis();
			languageService.indexProject(Paths.get(URI.create(params.getRootUri())));
			workspaceService.setLanguageService(languageService);
			documentService.setLanguageService(languageService);
			var done = System.currentTimeMillis();

			if (client != null)
			{
				client.showMessage(ClientMessage.info("Natural Language Server initialized after " + (done - start) + "ms"));
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
}
