package org.amshove.natls.languageserver;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class NaturalDocumentService implements TextDocumentService
{
	private NaturalLanguageService languageService;

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams completionParams)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> Either.forLeft(languageService.complete(completionParams))));
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.resolveComplete(unresolved)));
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
		DefinitionParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> Either.forLeft(languageService.gotoDefinition(params))));
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.findReferences(params)));
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params)
	{
		wrapSafe(() -> languageService.fileOpened(LspUtil.uriToPath(params.getTextDocument().getUri())));
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params)
	{
		if (params.getContentChanges().size() > 1)
		{
			throw new RuntimeException("We currently support only full file sync");
		}

		var change = params.getContentChanges().get(0);
		wrapSafe(() -> languageService.fileChanged(LspUtil.uriToPath(params.getTextDocument().getUri()), change.getText()));
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params)
	{
		// TODO: Do we want to clear all diagnostics?
		//		at least clear the parse tree
		wrapSafe(() -> languageService.fileClosed(LspUtil.uriToPath(params.getTextDocument().getUri())));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params)
	{
		wrapSafe(() -> languageService.fileSaved(LspUtil.uriToPath(params.getTextDocument().getUri())));
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.codeLens(params)));
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved)
	{
		return wrapSafe(() -> CompletableFuture.completedFuture(unresolved));
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.findSymbolsInFile(params.getTextDocument()).stream().map(Either::<SymbolInformation, DocumentSymbol>forRight).toList()));
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.hoverSymbol(params.getTextDocument(), params.getPosition())));
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.signatureHelp(params.getTextDocument(), params.getPosition())));
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.codeAction(params).stream().map(Either::<Command, CodeAction>forRight).toList()));
	}

	@Override
	public CompletableFuture<List<CallHierarchyIncomingCall>> callHierarchyIncomingCalls(CallHierarchyIncomingCallsParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.createCallHierarchyIncomingCalls(params.getItem())));
	}

	@Override
	public CompletableFuture<List<CallHierarchyOutgoingCall>> callHierarchyOutgoingCalls(CallHierarchyOutgoingCallsParams params)
	{
		// TODO: Might contain work done token
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.createCallHierarchyOutgoingCalls(params.getItem())));
	}

	@Override
	public CompletableFuture<List<CallHierarchyItem>> prepareCallHierarchy(CallHierarchyPrepareParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(()->languageService.createCallHierarchyItems(params)));
	}

	@Override
	public CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>> prepareRename(PrepareRenameParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> Either3.forSecond(languageService.prepareRename(params))));
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.rename(params)));
	}

	@Override
	public CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params)
	{
		return wrapSafe(() -> CompletableFuture.supplyAsync(() -> languageService.inlayHints(params)));
	}

	@Override
	public CompletableFuture<InlayHint> resolveInlayHint(InlayHint unresolved)
	{
		return CompletableFuture.completedFuture(unresolved);
	}

	public void setLanguageService(NaturalLanguageService languageService)
	{
		this.languageService = languageService;
	}

	private <R> CompletableFuture<R> wrapSafe(Supplier<CompletableFuture<R>> function)
	{
		return function.get().handle((r, e) -> {
			if(e != null)
			{
				// TODO: log exception
				return null;
			}

			return r;
		});
	}

	private void wrapSafe(Runnable runnable)
	{
		try
		{
			runnable.run();
		}
		catch (Exception e)
		{
			// TODO: log exception
		}
	}
}
