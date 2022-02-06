package org.amshove.natls.testlifecycle;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.amshove.testhelpers.NaturalProjectResourceResolver;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.WindowClientCapabilities;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LspProjectNameResolver implements ParameterResolver
{
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(LspProjectNameResolver.class);

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException
	{
		return parameterContext.getParameter().getType() == LspTestContext.class && parameterContext.findAnnotation(LspProjectName.class).isPresent();
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException
	{
		try
		{
			var projectName = parameterContext.getParameter().getAnnotation(LspProjectName.class).value();
			var tempDir = new NaturalProjectResourceResolver.AutoDeleteTempDirectory(projectName);
			extensionContext.getStore(NAMESPACE).put("tempdir", tempDir);
			var project = TestProjectLoader.loadProjectFromResources(tempDir.getPath(), projectName);
			var server = new NaturalLanguageServer();
			var params = new InitializeParams();
			params.setCapabilities(createCapabilities());
			params.setWorkspaceFolders(List.of(new WorkspaceFolder(tempDir.getPath().toUri().toString())));
			params.setRootUri(tempDir.getPath().toUri().toString());
			var client = new StubClient();
			server.connect(client);
			server.initialize(params).get(1, TimeUnit.MINUTES);
			return new LspTestContext(project, client, server, server.getLanguageService());
		}
		catch (InterruptedException | ExecutionException | TimeoutException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private ClientCapabilities createCapabilities()
	{
		var capabilities = new ClientCapabilities();
		var window = new WindowClientCapabilities();
		window.setWorkDoneProgress(false);
		capabilities.setWindow(window);
		return capabilities;
	}
}
