package org.amshove.natls.quickfixes;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@LspTest
public class RemoveUnusedVariableQuickfixShould extends LanguageServerTest
{
	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("modrefparser")LspTestContext context)
	{
		testContext = context;
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Test
	void recognizeTheQuickfix()
	{
		var file = createOrSaveFile("LIBONE", "MEINS.NSN", """
               DEFINE DATA
               LOCAL
               01 #UNUSED (A10)
               END-DEFINE
               END
            """);
		var actions = testContext.languageService().codeAction(new CodeActionParams(file, singleCharacterPosition(2, 5), new CodeActionContext()));
		assertThat(actions).anyMatch(a -> a.getTitle().equals("Remove unused variable"));
	}
}
