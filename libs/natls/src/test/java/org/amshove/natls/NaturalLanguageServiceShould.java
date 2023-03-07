package org.amshove.natls;

import org.amshove.natlint.linter.LinterContext;
import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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

	@BeforeAll
	static void disableStdErr()
	{
		stderr = System.err;
		System.setErr(new PrintStream(new ByteArrayOutputStream()));
	}

	private static PrintStream stderr;

	@AfterAll
	static void enableStdErr()
	{
		System.setErr(stderr);
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
		var stdErr = System.err;
		System.setErr(null);
		testContext = context;
		assertThat(LinterContext.INSTANCE.editorConfig()).isEmpty();
		System.setErr(stdErr);
	}
}
