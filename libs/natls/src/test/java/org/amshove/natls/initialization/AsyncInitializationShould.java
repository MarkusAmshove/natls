package org.amshove.natls.initialization;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

class AsyncInitializationShould extends LanguageServerTest
{
	private LspTestContext context;

	@BeforeEach
	void setUp(@LspProjectName(value = "modrefparser", config = "{\"initialization\": { \"async\": true}}") LspTestContext context)
	{
		this.context = context;
	}

	@Test
	void initializeWithBackgroundTasks()
	{
		waitForInitialization();
		var messages = context.getClient().getShownMessages();
		assertThat(messages)
			.containsSubsequence(
				"Natural project is initializing",
				"Natural project initialization done"
			);
	}

	@Test
	void callRefreshCodeLensesAfterInitialization()
	{
		waitForInitialization();
		assertThat(getContext().getClient().getRefreshCodeLensesCalls())
			.isPositive();
	}

	private void waitForInitialization()
	{
		await().atMost(3, TimeUnit.SECONDS).until(() -> getContext().languageService().isInitialized());
	}

	@Override
	protected LspTestContext getContext()
	{
		return context;
	}
}
