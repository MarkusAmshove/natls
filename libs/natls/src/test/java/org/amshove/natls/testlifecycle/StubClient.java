package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class StubClient implements LanguageClient
{
	private final List<ShownMessage> shownMessages = new ArrayList<>();
	private final Map<String, List<Diagnostic>> publishedDiagnosticsPerUri = new HashMap<>();
	private int refreshCodeLensesCalls = 0;

	@Override
	public void telemetryEvent(Object object)
	{

	}

	@Override
	public void publishDiagnostics(PublishDiagnosticsParams diagnostics)
	{
		publishedDiagnosticsPerUri.put(diagnostics.getUri(), diagnostics.getDiagnostics());
	}

	@Override
	public void showMessage(MessageParams messageParams)
	{
		shownMessages.add(new ShownMessage(messageParams.getType(), messageParams.getMessage()));
	}

	@Override
	public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams)
	{
		return null;
	}

	@Override
	public void logMessage(MessageParams message)
	{

	}

	@Override
	public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params)
	{
		return LanguageClient.super.applyEdit(params);
	}

	@Override
	public CompletableFuture<Void> registerCapability(RegistrationParams params)
	{
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> unregisterCapability(UnregistrationParams params)
	{
		return LanguageClient.super.unregisterCapability(params);
	}

	@Override
	public CompletableFuture<ShowDocumentResult> showDocument(ShowDocumentParams params)
	{
		return LanguageClient.super.showDocument(params);
	}

	@Override
	public CompletableFuture<List<WorkspaceFolder>> workspaceFolders()
	{
		return LanguageClient.super.workspaceFolders();
	}

	@Override
	public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams)
	{
		return LanguageClient.super.configuration(configurationParams);
	}

	@Override
	public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params)
	{
		return LanguageClient.super.createProgress(params);
	}

	@Override
	public void notifyProgress(ProgressParams params)
	{
		LanguageClient.super.notifyProgress(params);
	}

	@Override
	public void logTrace(LogTraceParams params)
	{
		LanguageClient.super.logTrace(params);
	}

	@Override
	public CompletableFuture<Void> refreshSemanticTokens()
	{
		return LanguageClient.super.refreshSemanticTokens();
	}

	@Override
	public CompletableFuture<Void> refreshCodeLenses()
	{
		refreshCodeLensesCalls++;
		return CompletableFuture.completedFuture(null);
	}

	public List<String> getShownMessages()
	{
		return shownMessages
			.stream()
			.map(ShownMessage::message)
			.toList();
	}

	public int getRefreshCodeLensesCalls()
	{
		return refreshCodeLensesCalls;
	}

	public List<Diagnostic> getPublishedDiagnostics(TextDocumentIdentifier document)
	{
		return publishedDiagnosticsPerUri.get(document.getUri());
	}

	record ShownMessage(MessageType type, String message)
	{}
}
