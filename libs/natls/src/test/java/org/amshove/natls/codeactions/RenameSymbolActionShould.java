package org.amshove.natls.codeactions;

import org.amshove.natls.testlifecycle.*;
import org.eclipse.lsp4j.RenameParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RenameSymbolActionShould extends LanguageServerTest
{
	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext testContext)
	{
		RenameSymbolActionShould.testContext = testContext;
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Test
	void renameALocalVariableThroughItsDeclaration()
	{
		var source = SourceWithCursor.fromSourceWithCursor("""
			DEFINE DATA LOCAL
			1 #MY${}$VAR (N2)
			END-DEFINE
			WRITE #MYVAR
			END
			""");

		var file = createOrSaveFile("LIBONE", "RENAM.NSN", source);
		var edit = testContext.languageService().rename(new RenameParams(file, source.toSinglePosition(), "#NEWVAR"));

		WorkspaceEditAssertion.assertThatEdit(edit)
			.changesText(1, "1 #MYVAR (N2)", "1 #NEWVAR (N2)")
			.changesText(3, "WRITE #MYVAR", "WRITE #NEWVAR");
	}

	@Test
	void renameALocalVariableThroughItsUsage()
	{
		var source = SourceWithCursor.fromSourceWithCursor("""
			DEFINE DATA LOCAL
			1 #MYVAR (N2)
			END-DEFINE
			WRITE #MYV${}$AR
			END
			""");

		var file = createOrSaveFile("LIBONE", "RENAM.NSN", source);
		var edit = testContext.languageService().rename(new RenameParams(file, source.toSinglePosition(), "#NEWVAR"));

		WorkspaceEditAssertion.assertThatEdit(edit)
			.changesText(1, "1 #MYVAR (N2)", "1 #NEWVAR (N2)")
			.changesText(3, "WRITE #MYVAR", "WRITE #NEWVAR");
	}
}
