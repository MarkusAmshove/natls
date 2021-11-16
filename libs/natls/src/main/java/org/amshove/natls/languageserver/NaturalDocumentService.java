package org.amshove.natls.languageserver;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NaturalDocumentService implements TextDocumentService
{
	private NaturalLanguageService languageService;

	@Override
	public void didOpen(DidOpenTextDocumentParams params)
	{

	}

	@Override
	public void didChange(DidChangeTextDocumentParams params)
	{

	}

	@Override
	public void didClose(DidCloseTextDocumentParams params)
	{

	}

	@Override
	public void didSave(DidSaveTextDocumentParams params)
	{

	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.findSymbolsInFile(params.getTextDocument()));
	}

	public void setLanguageService(NaturalLanguageService languageService)
	{
		this.languageService = languageService;
	}
}
