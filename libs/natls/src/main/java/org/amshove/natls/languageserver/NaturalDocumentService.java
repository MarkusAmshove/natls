package org.amshove.natls.languageserver;

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
			var diagnostics = languageService.parseFile(LspUtil.uriToPath(textDocumentIdentifier));
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
		clearDiagnostics(fileUri);
		if (diagnostics.size() == 0)
		{
			return;
		}

		var lspDiagnostics = diagnostics.stream().map(d -> new Diagnostic(
			new Range(new Position(d.line(), d.offsetInLine()), new Position(d.line(), d.offsetInLine() + d.length())),
			d.message(),
			DiagnosticSeverity.Error,
			"natls",
			d.id()
		)).toList();

		client.publishDiagnostics(new PublishDiagnosticsParams(fileUri, lspDiagnostics));
	}

	private void clearDiagnostics(String fileUri)
	{
		client.publishDiagnostics(new PublishDiagnosticsParams(fileUri, List.of()));
	}

}
