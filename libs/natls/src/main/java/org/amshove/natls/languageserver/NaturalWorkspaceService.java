package org.amshove.natls.languageserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.amshove.natls.config.LSConfiguration;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NaturalWorkspaceService implements WorkspaceService
{
	private static final Logger log = Logger.getAnonymousLogger();
	private NaturalLanguageService languageService;

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params)
	{
		var settings = (JsonObject) params.getSettings();
		var jsonObject = settings.getAsJsonObject("natls");
		var configuration = new Gson().fromJson(jsonObject, LSConfiguration.class);
		NaturalLanguageService.setConfiguration(configuration);
	}

	@Override
	public CompletableFuture<WorkspaceEdit> willRenameFiles(RenameFilesParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.willRenameFiles(params.getFiles()));
	}

	@Override
	public void didCreateFiles(CreateFilesParams params)
	{
		for (var file : params.getFiles())
		{
			languageService.createdFile(file.getUri());
		}
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params)
	{
		log.fine("didChangeWatchedFiles start");
		var changedModules = 0;
		for (var change : params.getChanges())
		{
			try
			{
				var filepath = LspUtil.uriToPath(change.getUri());

				var isNaturalModule = NaturalFileType.isNaturalFile(filepath);
				if (isNaturalModule)
				{
					changedModules++;
					handleNaturalModuleChange(filepath, change);
				}
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "Error during changed watched file changed (%s), skipping file %s".formatted(change.getType(), change.getUri()), e);
			}
		}

		if (changedModules > 0)
		{
			languageService.reparseOpenFiles();
		}

		log.fine(() -> "didChangeWatchedFiles end");
	}

	private void handleNaturalModuleChange(Path filepath, FileEvent change)
	{
		log.fine(() -> "Handling watched natural module change: %s".formatted(filepath));
		switch (change.getType())
		{
			case Created ->
			{
				log.fine("Module is new, adding to project");
				languageService.createdFile(change.getUri());
			}
			case Changed ->
			{
				log.fine("Module is saved or externally changed, reparsing with callers");
				languageService.fileExternallyChanged(filepath);
			}
			case Deleted ->
			{
				log.fine("Module is deleted, removing");
				languageService.fileDeleted(filepath);
			}
		}
	}

	@Override
	public CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> symbol(WorkspaceSymbolParams params)
	{
		return CompletableFutures.computeAsync(cancelChecker -> Either.forLeft(languageService.findWorkspaceSymbols(params.getQuery(), cancelChecker)));
	}

	public void setLanguageService(NaturalLanguageService languageService)
	{
		this.languageService = languageService;
	}
}
