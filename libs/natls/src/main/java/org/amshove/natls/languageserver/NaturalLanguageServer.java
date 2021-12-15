package org.amshove.natls.languageserver;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NaturalLanguageServer implements LanguageServer, LanguageClientAware
{
	private final NaturalWorkspaceService workspaceService = new NaturalWorkspaceService();
	private final NaturalDocumentService documentService = new NaturalDocumentService();

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
			System.err.println("Starte");

			var languageService = NaturalLanguageService.createService(Paths.get(URI.create(params.getRootUri())));
			workspaceService.setLanguageService(languageService);
			documentService.setLanguageService(languageService);

			System.err.println("Bereit");
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
		documentService.connect(client);
		workspaceService.connect(client);
	}
}
