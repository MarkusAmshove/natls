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
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams completionParams)
	{
		return CompletableFuture.supplyAsync(() -> Either.forLeft(languageService.complete(completionParams)));
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
		DefinitionParams params)
	{
		return CompletableFuture.supplyAsync(() -> Either.forLeft(languageService.gotoDefinition(params)));
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.findReferences(params));
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params)
	{
		languageService.fileOpened(LspUtil.uriToPath(params.getTextDocument().getUri()));
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params)
	{
		if (params.getContentChanges().size() > 1)
		{
			throw new RuntimeException("We currently support only full file sync");
		}

		var change = params.getContentChanges().get(0);
		languageService.fileChanged(LspUtil.uriToPath(params.getTextDocument().getUri()), change.getText());
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params)
	{
		// TODO: Do we want to clear all diagnostics?
		//		at least clear the parse tree
		languageService.fileClosed(LspUtil.uriToPath(params.getTextDocument().getUri()));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params)
	{
		languageService.fileSaved(LspUtil.uriToPath(params.getTextDocument().getUri()));
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.findSymbolsInFile(params.getTextDocument()));
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.hoverSymbol(params.getTextDocument(), params.getPosition()));
	}

	public void setLanguageService(NaturalLanguageService languageService)
	{
		this.languageService = languageService;
	}

}
