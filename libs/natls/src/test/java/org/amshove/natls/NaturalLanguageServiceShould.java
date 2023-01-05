package org.amshove.natls;

import org.amshove.natlint.linter.LinterContext;
import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class NaturalLanguageServiceShould extends LanguageServerTest
{
	private static LspTestContext testContext;

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@BeforeEach
	void before()
	{
		LinterContext.INSTANCE.updateEditorConfig(null);
	}

	@Test
	void loadTheEditorConfigFile(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
		assertThat(LinterContext.INSTANCE.editorConfig()).isPresent();
	}

	@Test
	void notLoadAnEditorConfigIfNoneIsPresent(@LspProjectName("modrefparser") LspTestContext context)
	{
		testContext = context;
		assertThat(LinterContext.INSTANCE.editorConfig()).isEmpty();
	}
}
