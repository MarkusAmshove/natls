package org.amshove.natls.languageserver;

import org.amshove.natls.DiagnosticTool;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NaturalDocumentService implements TextDocumentService, LanguageClientAware
{
	private NaturalLanguageService languageService;
	private LanguageClient client;

	@Override
	public void didOpen(DidOpenTextDocumentParams params)
	{
		checkFile(params.getTextDocument().getUri());
	}

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
	public void didChange(DidChangeTextDocumentParams params)
	{
		if (params.getContentChanges().size() > 1)
		{
			throw new RuntimeException("We currently support only full file sync");
		}
		var change = params.getContentChanges().get(0);
		var fileUri = params.getTextDocument().getUri();
		try
		{
			var diagnostics = languageService.parseSource(change.getText());
			publishDiagnostics(fileUri, diagnostics);
		}
		catch (Exception e)
		{
			publishUnhandledException(fileUri, e);
		}
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params)
	{
		// TODO: Do we want to clear all diagnostics?
		//		at least clear the parse tree
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params)
	{
		checkFile(params.getTextDocument().getUri());
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

	@Override
	public void connect(LanguageClient client)
	{
		this.client = client;
	}

	private void checkFile(String textDocumentIdentifier)
	{
		try
		{
			System.err.println("checkFile start");
			var filepath = LspUtil.uriToPath(textDocumentIdentifier);
			var diagnostics = languageService.parseFile(filepath);
			publishDiagnostics(textDocumentIdentifier, diagnostics);
			System.err.println("checkFile end");
		}
		catch (Exception e)
		{
			clearDiagnostics(textDocumentIdentifier);
			publishUnhandledException(textDocumentIdentifier, e);
		}
	}

	private void publishUnhandledException(String textDocumentIdentifier, Exception e)
	{
		client.publishDiagnostics(
			new PublishDiagnosticsParams(
				textDocumentIdentifier,
				List.of(
					new Diagnostic(
						new Range(
							new Position(0, 0),
							new Position(0, 0)
						),
						"Unhandled exception: %s".formatted(e.getMessage())
					)
				)
			)
		);

		throw new RuntimeException(e);
	}

	private void publishDiagnostics(String fileUri, ReadOnlyList<IDiagnostic> diagnostics)
	{
		var naturalFile = languageService.findNaturalFile(LspUtil.uriToPath(fileUri));
		naturalFile.clearDiagnosticsByTool(DiagnosticTool.NATPARSE);

		for (var diagnostic : diagnostics)
		{
			naturalFile.addDiagnostic(DiagnosticTool.NATPARSE, diagnostic);
		}

		languageService.publishDiagnostics(naturalFile);
	}

	private void clearDiagnostics(String fileUri)
	{
		client.publishDiagnostics(new PublishDiagnosticsParams(fileUri, List.of()));
	}

}
