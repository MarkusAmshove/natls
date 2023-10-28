package org.amshove.natls.initialization;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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

	@Override
	protected LspTestContext getContext()
	{
		return context;
	}
}
