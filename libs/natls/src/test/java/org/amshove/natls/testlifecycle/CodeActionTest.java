package org.amshove.natls.testlifecycle;

import org.amshove.natls.codeactions.CodeActionRegistry;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class CodeActionTest extends LanguageServerTest
{
	protected abstract ICodeActionProvider getCodeActionUnderTest();

	protected List<CodeAction> receiveCodeActions(String library, String name, String sourceWithCursor)
	{
		CodeActionRegistry.INSTANCE.unregisterAll();
		CodeActionRegistry.INSTANCE.register(getCodeActionUnderTest());
		var sourceAndCursor = extractSourceAndCursor(sourceWithCursor);
		var file = createOrSaveFile(library, name, sourceAndCursor);
		return getContext().languageService().codeAction(new CodeActionParams(file, sourceAndCursor.cursorPosition(), new CodeActionContext()));
	}

	protected CodeActionAssertion assertSingleCodeAction(List<CodeAction> codeActions)
	{
		assertThat(codeActions)
			.as("Expected only a single code action")
			.hasSize(1);
		return new CodeActionAssertion(codeActions.get(0));
	}

	protected void assertNoCodeAction(String library, String module, String source)
	{
		assertThat(receiveCodeActions(library, module, source)).isEmpty();
	}

	protected CodeActionAssertion assertSingleCodeAction(String actionTitle, String library, String moduleName, String code)
	{
		return assertSingleCodeAction(receiveCodeActions(library, moduleName, code))
			.hasTitle(actionTitle);
	}

	protected void assertContainsCodeAction(String title, List<CodeAction> codeActions)
	{
		assertThat(codeActions)
			.anyMatch(c -> c.getTitle().equals(title));
	}

	private SourceWithCursor extractSourceAndCursor(String source)
	{
		return SourceWithCursor.fromSourceWithCursor(source);
	}
}
