package org.amshove.natls.testlifecycle;

import org.junit.jupiter.api.BeforeEach;

public class EmptyProjectTest extends LanguageServerTest
{
	private LspTestContext context;

	@BeforeEach
	void beforeEach(@LspProjectName("emptyproject") LspTestContext context)
	{
		this.context = context;
	}

	@Override
	protected LspTestContext getContext()
	{
		return context;
	}
}
