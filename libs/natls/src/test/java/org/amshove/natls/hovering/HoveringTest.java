package org.amshove.natls.hovering;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.SourceWithCursor;
import org.eclipse.lsp4j.HoverParams;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class HoveringTest extends LanguageServerTest
{
	protected void assertHover(String sourceWithCursor, String expectedHoverText)
	{
		var sourceAndCursor = SourceWithCursor.fromSourceWithCursor(sourceWithCursor);
		var file = createOrSaveFile("LIBONE", "SUB.NSN", sourceAndCursor);

		var params = new HoverParams();
		params.setPosition(sourceAndCursor.toSinglePosition());
		params.setTextDocument(file);

		try
		{
			var hover = getContext().server().getTextDocumentService().hover(params).get();
			assertThat(hover.getContents().getRight().getValue()).isEqualTo(expectedHoverText);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
