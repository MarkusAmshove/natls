package org.amshove.natls.testlifecycle;

import org.amshove.natls.codeactions.CodeActionRegistry;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@LspTest
public abstract class CodeActionTest extends LanguageServerTest
{
	protected abstract ICodeActionProvider getCodeActionUnderTest();

	protected CodeActionResult receiveCodeActions(String library, String name, String sourceWithCursor)
	{
		CodeActionRegistry.INSTANCE.unregisterAll();
		CodeActionRegistry.INSTANCE.register(getCodeActionUnderTest());
		var sourceAndCursor = extractSourceAndCursor(sourceWithCursor);
		var file = createOrSaveFile(library, name, sourceAndCursor);
		var codeActions = getContext().languageService().codeAction(new CodeActionParams(file, sourceAndCursor.cursorPosition(), new CodeActionContext()));
		return new CodeActionResult(sourceAndCursor.source(), codeActions);
	}

	protected CodeActionAssertion assertSingleCodeAction(List<CodeAction> codeActions)
	{
		assertThat(codeActions)
			.as("Expected only a single code action")
			.hasSize(1);
		return new CodeActionAssertion(codeActions.get(0));
	}

	protected ApplicableCodeActionAssertion assertCodeAction(CodeActionResult result, int codeActionIndex)
	{
		return new ApplicableCodeActionAssertion(result.savedSource(), result.codeActions().get(codeActionIndex));
	}

	protected void assertNoCodeAction(String library, String module, String source)
	{
		assertThat(receiveCodeActions(library, module, source).codeActions()).isEmpty();
	}

	protected void assertNoCodeAction(String library, String module, String source, String title)
	{
		assertThat(receiveCodeActions(library, module, source).codeActions()).noneMatch(ca -> ca.getTitle().equals(title));
	}

	protected ApplicableCodeActionAssertion assertCodeActionWithTitle(String actionTitle, String library, String moduleName, String code)
	{
		var result = receiveCodeActions(library, moduleName, code);
		var codeActions = result.codeActions();
		assertContainsCodeAction(actionTitle, codeActions);
		//noinspection OptionalGetWithoutIsPresent
		return new ApplicableCodeActionAssertion(result.savedSource(), codeActions.stream().filter(ca -> ca.getTitle().equals(actionTitle)).findAny().get());
	}

	protected void assertNoCodeActionWithTitle(String actionTitle, String library, String moduleName, String code)
	{
		var codeActions = receiveCodeActions(library, moduleName, code).codeActions();
		assertThat(codeActions).noneMatch(ca -> ca.getTitle().equals(actionTitle));
	}

	protected void assertContainsCodeAction(String title, List<CodeAction> codeActions)
	{
		assertThat(codeActions)
			.as("Could not find code action with title '%s'".formatted(title))
			.anyMatch(c -> c.getTitle().equals(title));
	}

	private SourceWithCursor extractSourceAndCursor(String source)
	{
		return SourceWithCursor.fromSourceWithCursor(source);
	}
}
