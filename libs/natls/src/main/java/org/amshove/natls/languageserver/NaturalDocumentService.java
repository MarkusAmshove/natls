package org.amshove.natls.languageserver;

import org.amshove.natparse.natural.IHasDefineData;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.ArrayList;
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
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved)
	{
		return CompletableFuture.supplyAsync(() -> languageService.resolveComplete(unresolved));
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
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params)
	{
		return CompletableFuture.supplyAsync(() -> {
			var path = LspUtil.uriToPath(params.getTextDocument().getUri());
			var file = languageService.findNaturalFile(path);
			if (file == null)
			{
				return List.of();
			}

			var codelens = new ArrayList<CodeLens>();

			var moduleReferenceCodeLens = new CodeLens();
			moduleReferenceCodeLens.setCommand(
				new Command(
					file.getIncomingReferences().size() + " references",
					"")
			);

			var module = file.module();
			if (module instanceof IHasDefineData naturalModule && naturalModule.defineData() != null)
			{
				moduleReferenceCodeLens.setRange(LspUtil.toRange(
					naturalModule.defineData().position()
				));
				// TODO(code-lens): Add an actual command
				naturalModule
					.defineData()
					.variables()
					.stream()
					.filter(v -> v.references().size() > 0)
					.map(v -> new CodeLens(
						LspUtil.toRange(v.declaration()),
						new Command(v.references().size() + " references", ""),
						new Object()
					))
					.forEach(codelens::add);
			}

			if(!file.getIncomingReferences().isEmpty())
			{
				codelens.add(moduleReferenceCodeLens);
			}

			return codelens;
		});
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved)
	{
		return CompletableFuture.completedFuture(unresolved);
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.findSymbolsInFile(params.getTextDocument()).stream().map(Either::<SymbolInformation, DocumentSymbol>forLeft).toList());
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.hoverSymbol(params.getTextDocument(), params.getPosition()));
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.signatureHelp(params.getTextDocument(), params.getPosition()));
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.codeAction(params).stream().map(Either::<Command, CodeAction>forRight).toList());
	}

	@Override
	public CompletableFuture<List<CallHierarchyIncomingCall>> callHierarchyIncomingCalls(CallHierarchyIncomingCallsParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.createCallHierarchyIncomingCalls(params.getItem()));
	}

	@Override
	public CompletableFuture<List<CallHierarchyOutgoingCall>> callHierarchyOutgoingCalls(CallHierarchyOutgoingCallsParams params)
	{
		// TODO: Might contain work done token
		return CompletableFuture.supplyAsync(() -> languageService.createCallHierarchyOutgoingCalls(params.getItem()));
	}

	@Override
	public CompletableFuture<List<CallHierarchyItem>> prepareCallHierarchy(CallHierarchyPrepareParams params)
	{
		return CompletableFuture.supplyAsync(()->languageService.createCallHierarchyItems(params));
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.rename(params));
	}

	public void setLanguageService(NaturalLanguageService languageService)
	{
		this.languageService = languageService;
	}

}
