package org.amshove.natls.hovering;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natls.testlifecycle.SourceWithCursor;
import org.eclipse.lsp4j.HoverParams;
import org.junit.jupiter.api.BeforeAll;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HoveringTest extends LanguageServerTest
{
	protected void assertHover(String sourceWithCursor, String expectedHoverText)
	{
		assertThat(getHover(sourceWithCursor)).isEqualToNormalizingNewlines(expectedHoverText);
	}

	protected void assertHoverContains(String sourceWithCursor, String expectedContain)
	{
		assertThat(getHover(sourceWithCursor)).contains(expectedContain);
	}

	private String getHover(String sourceWithCursor)
	{

		var sourceAndCursor = SourceWithCursor.fromSourceWithCursor(sourceWithCursor);
		var file = createOrSaveFile("LIBONE", "SUB.NSN", sourceAndCursor);

		var params = new HoverParams();
		params.setPosition(sourceAndCursor.toSinglePosition());
		params.setTextDocument(file);

		try
		{
			var hover = getContext().server().getTextDocumentService().hover(params).get();
			return hover.getContents().getRight().getValue();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}
}
