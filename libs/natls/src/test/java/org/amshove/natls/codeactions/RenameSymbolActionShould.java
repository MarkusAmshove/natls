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
			.changesText(1, "1 #MYVAR (N2)", "1 #NEWVAR (N2)", file)
			.changesText(3, "WRITE #MYVAR", "WRITE #NEWVAR", file);
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
			.changesText(1, "1 #MYVAR (N2)", "1 #NEWVAR (N2)", file)
			.changesText(3, "WRITE #MYVAR", "WRITE #NEWVAR", file);
	}

	@Test
	void renameAVariableFromADataArea()
	{
		var dataArea = """
			DEFINE DATA
			LOCAL
			1 #INLDA (A2)
			END-DEFINE
			""";

		var module = SourceWithCursor.fromSourceWithCursor("""
			DEFINE DATA
			LOCAL USING MYLDA
			END-DEFINE
			WRITE #I${}$NLDA
			END
			""");

		var ldaFile = createOrSaveFile("LIBONE", "MYLDA.NSL", dataArea);
		var moduleFile = createOrSaveFile("LIBONE", "LDATEST.NSN", module);

		var edit = testContext.languageService().rename(new RenameParams(moduleFile, module.toSinglePosition(), "#STILLLDA"));

		WorkspaceEditAssertion.assertThatEdit(edit)
			.changesText(2, "1 #INLDA (A2)", "1 #STILLLDA (A2)", ldaFile)
			.changesText(3, "WRITE #INLDA", "WRITE #STILLLDA", moduleFile);
	}

	@Test
	void renameAnInternalSubroutineWhenCursorIsOnPerform()
	{
		var source = SourceWithCursor.fromSourceWithCursor("""
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE SUBROUTINE MY-ROUTINE
			IGNORE
			END-SUBROUTINE
			PERFORM MY-ROU${}$TINE
			END
			""");

		var file = createOrSaveFile("LIBONE", "RENAMSUB.NSN", source);
		var edit = testContext.languageService().rename(new RenameParams(file, source.toSinglePosition(), "NEW-SUBROUTINE"));

		WorkspaceEditAssertion.assertThatEdit(edit)
			.changesText(2, "DEFINE SUBROUTINE MY-ROUTINE", "DEFINE SUBROUTINE NEW-SUBROUTINE", file)
			.changesText(5, "PERFORM MY-ROUTINE", "PERFORM NEW-SUBROUTINE", file);
	}

	@Test
	void renameAnInternalSubroutineWhenCursorIsOnSubroutineDeclaration()
	{
		var source = SourceWithCursor.fromSourceWithCursor("""
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE SUBROUTINE MY-${}$ROUTINE
			IGNORE
			END-SUBROUTINE
			PERFORM MY-ROUTINE
			END
			""");

		var file = createOrSaveFile("LIBONE", "RENAMSUB.NSN", source);
		var edit = testContext.languageService().rename(new RenameParams(file, source.toSinglePosition(), "NEW-SUBROUTINE"));

		WorkspaceEditAssertion.assertThatEdit(edit)
			.changesText(2, "DEFINE SUBROUTINE MY-ROUTINE", "DEFINE SUBROUTINE NEW-SUBROUTINE", file)
			.changesText(5, "PERFORM MY-ROUTINE", "PERFORM NEW-SUBROUTINE", file);
	}

	@Test
	void renameAVariableFromItsUsageWithinAStatementBody()
	{
		var source = SourceWithCursor.fromSourceWithCursor("""
			DEFINE DATA LOCAL
			1 #MYVAR (A5)
			END-DEFINE
			DEFINE SUBROUTINE MY-ROUTINE
			    WRITE #MY${}$VAR
			END-SUBROUTINE
			PERFORM MY-ROUTINE
			END
			""");

		var file = createOrSaveFile("LIBONE", "RENAMEINBODY.NSN", source);
		var edit = testContext.languageService().rename(new RenameParams(file, source.toSinglePosition(), "#NEW-VAR"));

		WorkspaceEditAssertion.assertThatEdit(edit)
			.changesText(1, "1 #MYVAR (A5)", "1 #NEW-VAR (A5)", file)
			.changesText(4, "    WRITE #MYVAR", "    WRITE #NEW-VAR", file);
	}
}
