package org.amshove.natls.codeactions;

import org.amshove.natls.testlifecycle.*;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

class RenameSymbolActionShould extends LanguageServerTest
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
	void notBeAbleToRenameKeywords()
	{
		var source = SourceWithCursor.fromSourceWithCursor("""
			D${}$EFINE DATA LOCAL
			END-DEFINE
			END
			""");

		var file = createOrSaveFile("LIBONE", "RENAM.NSN", source);
		assertNoRename(new RenameParams(file, source.toSinglePosition(), ""));
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
		assertRename(new RenameParams(file, source.toSinglePosition(), "#NEWVAR"))
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
		assertRename(new RenameParams(file, source.toSinglePosition(), "#NEWVAR"))
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

		assertRename(new RenameParams(moduleFile, module.toSinglePosition(), "#STILLLDA"))
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
		assertRename(new RenameParams(file, source.toSinglePosition(), "NEW-SUBROUTINE"))
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
		assertRename(new RenameParams(file, source.toSinglePosition(), "NEW-SUBROUTINE"))
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
		assertRename(new RenameParams(file, source.toSinglePosition(), "#NEW-VAR"))
			.changesText(1, "1 #MYVAR (A5)", "1 #NEW-VAR (A5)", file)
			.changesText(4, "    WRITE #MYVAR", "    WRITE #NEW-VAR", file);
	}

	@Test
	void renameAVariableFromItsAssignmentTargetPosition()
	{
		var source = SourceWithCursor.fromSourceWithCursor("""
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE
			#V${}$AR := 'Hi'
			END
			""");

		var file = createOrSaveFile("LIBONE", "RENAMEAS.NSN", source);
		assertRename(new RenameParams(file, source.toSinglePosition(), "NEW-VARIABLE"))
			.changesText(1, "1 #VAR (A10)", "1 NEW-VARIABLE (A10)", file)
			.changesText(3, "#VAR := 'Hi'", "NEW-VARIABLE := 'Hi'", file);
	}

	@Test
	void renameAVariableFromItsAssignTargetPosition()
	{
		var source = SourceWithCursor.fromSourceWithCursor("""
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE
			ASSIGN #V${}$AR = 'Hi'
			END
			""");

		var file = createOrSaveFile("LIBONE", "RENAMEAS.NSN", source);
		assertRename(new RenameParams(file, source.toSinglePosition(), "NEW-VARIABLE"))
			.changesText(1, "1 #VAR (A10)", "1 NEW-VARIABLE (A10)", file)
			.changesText(3, "ASSIGN #VAR = 'Hi'", "ASSIGN NEW-VARIABLE = 'Hi'", file);
	}

	@Test
	void keepNamesQualifiedIfTheyHaveBeenQualifiedBefore()
	{
		var source = SourceWithCursor.fromSourceWithCursor("""
			DEFINE DATA LOCAL
			1 #GROUP
			2 #MY${}$VAR (N2)
			END-DEFINE
			WRITE #GROUP.#MYVAR
			END
			""");

		var file = createOrSaveFile("LIBONE", "RENAM.NSN", source);
		assertRename(new RenameParams(file, source.toSinglePosition(), "#NEWVAR"))
			.changesText(2, "2 #MYVAR (N2)", "2 #NEWVAR (N2)", file)
			.changesText(4, "WRITE #GROUP.#MYVAR", "WRITE #GROUP.#NEWVAR", file);
	}

	@Test
	void renameAVariableFromItsDecideOnBranch()
	{
		var source = SourceWithCursor.fromSourceWithCursor("""
			DEFINE DATA LOCAL
			1 #VAR1 (A10)
			1 #VAR2 (A10)
			END-DEFINE
			DECIDE ON FIRST VALUE OF #VAR1
			VALUE #VA${}$R2
			NONE IGNORE
			END-DECIDE
			END
			""");

		var file = createOrSaveFile("LIBONE", "RENAM.NSN", source);
		assertRename(new RenameParams(file, source.toSinglePosition(), "#NEWVAR"))
			.changesText(2, "1 #VAR2 (A10)", "1 #NEWVAR (A10)", file)
			.changesText(5, "VALUE #VAR2", "VALUE #NEWVAR", file);
	}

	private WorkspaceEditAssertion assertRename(RenameParams params)
	{
		var prepareParams = new PrepareRenameParams(params.getTextDocument(), params.getPosition());
		var prepareResponse = testContext.languageService().prepareRename(prepareParams);
		assertThat(prepareResponse).isNotNull(); // prepareRename throws if it can't rename, so this is just for sanity
		return WorkspaceEditAssertion.assertThatEdit(testContext.languageService().rename(params));
	}

	private void assertNoRename(RenameParams params)
	{
		assertThatThrownBy(() ->
		{
			var prepareParams = new PrepareRenameParams(params.getTextDocument(), params.getPosition());
			testContext.languageService().prepareRename(prepareParams);
		})
			.isInstanceOf(ResponseErrorException.class)
			.message().startsWith("Can't rename");
	}
}
