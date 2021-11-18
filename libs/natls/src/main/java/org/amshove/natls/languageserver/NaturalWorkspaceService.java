package org.amshove.natls.languageserver;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NaturalWorkspaceService implements WorkspaceService
{
	private NaturalLanguageService languageService;

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params)
	{

	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params)
	{

	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params)
	{
		return CompletableFutures.computeAsync(cancelChecker -> {
			return languageService.findWorkspaceSymbols(params.getQuery(), cancelChecker);
		});
	}

	public void setLanguageService(NaturalLanguageService languageService)
	{
		this.languageService = languageService;
	}
}
