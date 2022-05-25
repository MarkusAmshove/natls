package org.amshove.natls.hovering;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natls.testlifecycle.SourceWithCursor;
import org.eclipse.lsp4j.Position;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class HoveringTest extends LanguageServerTest
{
	protected void assertHover(String sourceWithCursor, String expectedHoverText)
	{
		var sourceAndCursor = SourceWithCursor.fromSourceWithCursor(sourceWithCursor);
		var file = createOrSaveFile("LIBONE", "SUB.NSN", sourceAndCursor);
		var hover = getContext().languageService().hoverSymbol(file, sourceAndCursor.toSinglePosition());

		assertThat(hover.getContents().getRight().getValue()).isEqualTo(expectedHoverText);
	}
}
