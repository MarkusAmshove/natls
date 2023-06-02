package org.amshove.natls.languageserver;

import com.google.common.io.Files;
import org.amshove.natls.DiagnosticTool;
import org.amshove.natls.catalog.CatalogResult;
import org.amshove.natls.natunit.NatUnitResultParser;
import org.amshove.natls.project.LanguageServerFile;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NaturalWorkspaceService implements WorkspaceService
{
	private NaturalLanguageService languageService;
	private ConcurrentHashMap<String, LanguageServerFile> filesWithCatError = new ConcurrentHashMap<>();

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params)
	{

	}

	@Override
	public void didRenameFiles(RenameFilesParams params)
	{
		WorkspaceService.super.didRenameFiles(params);
		// TODO: Do it here
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
		// TODO: Handle delete of a module
		// TODO: Handle new module
		for (var change : params.getChanges())
		{
			if (!change.getUri().endsWith(".xml") && !change.getUri().endsWith(".XML") && !change.getUri().endsWith("stow.log"))
			{
				continue;
			}

			if (change.getUri().endsWith("stow.log"))
			{
				parseCatalogResult(change);
				continue;
			}

			if (change.getUri().contains("merged/"))
			{
				// HTML Test report
				continue;
			}

			var testFile = LspUtil.uriToPath(change.getUri());
			var libraryAndTestname = testFile.getFileName().toString();
			var library = libraryAndTestname.substring(0, libraryAndTestname.lastIndexOf('-'));
			var testcase = libraryAndTestname.substring(libraryAndTestname.lastIndexOf('-') + 1).split("\\.")[0];
			var naturalFile = languageService.findNaturalFile(library, testcase);
			if (naturalFile == null)
			{
				continue;
			}

			naturalFile.clearDiagnosticsByTool(DiagnosticTool.NATUNIT);

			if (change.getType() == FileChangeType.Deleted)
			{
				languageService.publishDiagnostics(naturalFile);
			}
			else
			{
				var result = new NatUnitResultParser().parse(testFile);

				for (var testResult : result.getTestResults())
				{
					if (testResult.hasFailed())
					{
						try
						{
							var message = testResult.message();
							var lineNumberStartIndex = message.indexOf('(') + 1;
							var lineNumberEndIndex = message.indexOf(')');

							var line = Integer.parseInt(message.substring(lineNumberStartIndex, lineNumberEndIndex));
							line += 3; // Renumbering, but line is zero based

							var actualFailureMessage = message.substring(message.indexOf(':') + 1).trim();

							var theAssertionLine = Files.readLines(naturalFile.getPath().toFile(), Charset.defaultCharset()).get(line);
							var startIndex = theAssertionLine.length() - theAssertionLine.trim().length();

							naturalFile.addDiagnostic(
								DiagnosticTool.NATUNIT, new Diagnostic(
									new Range(new Position(line, startIndex), new Position(line, theAssertionLine.length())),
									"Assertion Failure: " + actualFailureMessage,
									DiagnosticSeverity.Error,
									DiagnosticTool.NATUNIT.getId()
								)
							);
						}
						catch (Exception e)
						{}
					}
				}

				languageService.publishDiagnostics(naturalFile);
			}
		}
	}

	private void parseCatalogResult(FileEvent change)
	{
		for (Map.Entry<String, LanguageServerFile> fileWithCatError : filesWithCatError.entrySet())
		{
			fileWithCatError.getValue().clearDiagnosticsByTool(DiagnosticTool.CATALOG);
		}

		filesWithCatError.clear();

		if (change.getType() == FileChangeType.Deleted)
		{
			return;
		}

		var path = LspUtil.uriToPath(change.getUri());
		try
		{
			var lines = Files.readLines(path.toFile(), Charset.defaultCharset());
			for (var line : lines)
			{
				if (!line.startsWith("CATALOG ERROR:"))
				{
					continue;
				}

				var split = Arrays.stream(line.split(" ")).toList();
				var libraryIndex = split.indexOf("Library:") + 1;
				var objectIndex = split.indexOf("Object:") + 1;
				var lineIndex = split.indexOf("Row:") + 1;
				var columnIndex = split.indexOf("Col:") + 1;
				var messageIndex = split.indexOf("Text:") + 1;
				var message = String.join(" ", split.subList(messageIndex, split.size()));

				var catalogResult = new CatalogResult(split.get(libraryIndex), split.get(objectIndex), Integer.parseInt(split.get(lineIndex)), Integer.parseInt(split.get(columnIndex)), message);
				var file = languageService.findNaturalFile(catalogResult.library(), catalogResult.module());
				if (file == null)
				{
					continue;
				}

				filesWithCatError.put(file.getUri(), file);
				file.addDiagnostic(
					DiagnosticTool.CATALOG, new Diagnostic(
						LspUtil.toSingleRange(catalogResult.line() + 3, 0),
						catalogResult.text(),
						DiagnosticSeverity.Error,
						DiagnosticTool.CATALOG.getId(),
						catalogResult.text().split(" ")[0]
					)
				);
				languageService.publishDiagnostics(file);
				languageService.invalidateStowCache(file);
			}
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
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
