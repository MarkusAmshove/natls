package org.amshove.natls.languageserver;

import com.google.common.io.Files;
import org.amshove.natls.natunit.NatUnitResultParser;
import org.amshove.natls.natunit.NatUnitTestResult;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NaturalWorkspaceService implements WorkspaceService, LanguageClientAware
{
	private NaturalLanguageService languageService;
	private LanguageClient client;

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params)
	{

	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params)
	{
		for (var change : params.getChanges())
		{
			if (!change.getUri().endsWith(".xml") && !change.getUri().endsWith(".XML"))
			{
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
			if(change.getType() == FileChangeType.Deleted)
			{
				// TODO(natunit): We have to send the normal parser/linter diagnostics, because this will clear them
				client.publishDiagnostics(new PublishDiagnosticsParams(naturalFile.getPath().toUri().toString(), List.of()));
			}
			else
			{
				var result = new NatUnitResultParser().parse(testFile);
				// TODO(natunit): We have to send the normal parser/linter diagnostics, because this will clear them
				var failureDiagnostics = new ArrayList<Diagnostic>();

				for (NatUnitTestResult testResult : result.getTestResults())
				{
					if(testResult.hasFailed())
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

							failureDiagnostics.add(new Diagnostic(
								new Range(new Position(line, startIndex), new Position(line, theAssertionLine.length())),
								"Assertion Failure: " + actualFailureMessage,
								DiagnosticSeverity.Error,
								"NatUnit"
							));
						}
						catch (Exception e) {}
					}
				}

				client.publishDiagnostics(new PublishDiagnosticsParams(naturalFile.getPath().toUri().toString(), failureDiagnostics));
			}
		}
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

	@Override
	public void connect(LanguageClient client)
	{
		this.client = client;
	}
}
