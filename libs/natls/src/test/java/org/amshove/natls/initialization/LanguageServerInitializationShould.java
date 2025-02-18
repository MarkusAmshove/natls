package org.amshove.natls.initialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natls.testlifecycle.*;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("deprecation") // Deprecated LSP APIs
class LanguageServerInitializationShould extends LanguageServerTest
{
	private LspTestContext context;

	@BeforeEach
	void setUp(@LspProjectName(value = "modrefparser") LspTestContext context)
	{
		this.context = context;
	}

	@Test
	void fullyInitializeInInitializationRequest()
	{
		assertThat(getContext().languageService().isInitialized())
			.isTrue();
	}

	@Test
	void sendMessagesAboutInitialization()
	{
		assertThat(getContext().getClient().getShownMessages())
			.containsSubsequence(
				"10% Begin Indexing",
				"20% Reading project file"
				// ...
			);
	}

	@Test
	void parseFileReferences()
	{
		assertThat(getContext().getClient().getShownMessages())
			.anyMatch(m -> m.contains("Parsing references LIBONE."));
	}

	@Test
	void useDefaultLSConfigurationWhenEmptyInitOptionsArePassed()
		throws ExecutionException, InterruptedException, TimeoutException
	{
		var params = new InitializeParams();
		params.setCapabilities(LspProjectNameResolver.createCapabilities());
		var projectUri = context.project().rootPath().toUri().toString();
		params.setRootUri(projectUri);
		params.setWorkspaceFolders(
			List.of(new WorkspaceFolder(projectUri, "Natural"))
		);

		var emptyInitOptions = new JsonObject();
		params.setInitializationOptions(emptyInitOptions);

		var server = new NaturalLanguageServer();
		server.connect(new StubClient());
		server.initialize(params).get(1, TimeUnit.MINUTES);

		var effectiveConfig = NaturalLanguageService.getConfig();
		assertThat(effectiveConfig.getCompletion())
			.as("Initial configuration was not set correctly")
			.isNotNull();
	}

	@Test
	void useDefaultConfigurationValuesWhenOnlyPartialConfigurationIsSupplied()
		throws ExecutionException, InterruptedException, TimeoutException
	{
		var params = new InitializeParams();
		params.setCapabilities(LspProjectNameResolver.createCapabilities());
		var projectUri = context.project().rootPath().toUri().toString();
		params.setRootUri(projectUri);
		params.setWorkspaceFolders(
			List.of(new WorkspaceFolder(projectUri, "Natural"))
		);

		var jsonConfig = "{ \"completion\": { \"qualify\": true } }";
		params.setInitializationOptions(new Gson().fromJson(jsonConfig, JsonObject.class));

		var server = new NaturalLanguageServer();
		server.connect(new StubClient());
		server.initialize(params).get(1, TimeUnit.MINUTES);

		var effectiveConfig = NaturalLanguageService.getConfig();

		assertThat(effectiveConfig.getCompletion().isQualify())
			.as("User set configuration should be set")
			.isTrue();

		assertThat(effectiveConfig.getInitialization().isAsync())
			.as("Configuration not included in partial configuration should have default value")
			.isFalse();
	}

	@Override
	protected LspTestContext getContext()
	{
		return context;
	}
}
