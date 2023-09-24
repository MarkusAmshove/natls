package org.amshove.natls.lsp;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natls.testlifecycle.TextEditApplier;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FileRenameShould extends LanguageServerTest
{

	@Test
	void renameASubprogramOnTheCallSite()
	{
		whenRenamingFile("CALLEDN.NSN", "CHANGEN");

		itShouldChange(
			expectedRename("CALLER.NSN", 8, "CALLNAT 'CHANGEN' #PARM"),
			expectedRename("CALLER.NSN", 10, "    CALLNAT 'CHANGEN' #PARM"),
			expectedRename("CALLER2.NSN", 7, "CALLNAT 'CHANGEN' #TEST")
		);
	}

	@Test
	void renameALdaOnTheCallSite()
	{
		whenRenamingFile("SOMELDA.NSL", "MYLDA");

		itShouldChange(
			expectedRename("CALLER.NSN", 3, "LOCAL USING MYLDA"),
			expectedRename("CALLER2.NSN", 4, "LOCAL USING MYLDA")
		);
	}

	@Test
	void renameAPdaOnTheCallSite()
	{
		whenRenamingFile("SOMEPDA.NSA", "MYPDA");

		itShouldChange(
			expectedRename("CALLER.NSN", 1, "PARAMETER USING MYPDA"),
			expectedRename("CALLER2.NSN", 2, "PARAMETER USING MYPDA")
		);
	}

	@Test
	void renameAGdaOnTheCallSite()
	{
		whenRenamingFile("SOMEGDA.NSG", "MYGDA");

		itShouldChange(
			expectedRename("CALLER.NSN", 2, "GLOBAL USING MYGDA"),
			expectedRename("CALLER2.NSN", 3, "GLOBAL USING MYGDA")
		);
	}

	@Test
	void renameAProgramOnTheCallSite()
	{
		whenRenamingFile("SOMEPROG.NSP", "MYPROG");

		itShouldChange(
			expectedRename("CALLER.NSN", 13, "FETCH 'MYPROG' 50"),
			expectedRename("CALLER.NSN", 15, "    FETCH RETURN 'MYPROG' 22"),
			expectedRename("CALLER2.NSN", 11, "        FETCH REPEAT 'MYPROG' 100")
		);
	}

	@Test
	void renameCallSitesInCopycodes()
	{
		whenRenamingFile("PROGCC.NSP", "CCPROG");

		itShouldChange(
			expectedRename("INCL.NSC", 0, "FETCH 'CCPROG' 200")
		);

		whenRenamingFile("SUBCC.NSN", "CCSUB");

		itShouldChange(
			expectedRename("INCL.NSC", 2, "CALLNAT 'CCSUB' #VAR")
		);
	}

	@Test
	void renameCallSitesOfFunctionsAndTheFunctionItself()
	{
		whenRenamingFile("FUNC.NS7", "MYFUNC");

		itShouldChange(
			expectedRename("CALLER.NSN", 18, "IF MYFUNC(<>)"),
			expectedRename("CALLER2.NSN", 12, "    WHEN MYFUNC(<>)"),
			expectedRename("INCL.NSC", 4, "#BOOL := MYFUNC(<>)"),
			expectedRename("CALLED", "FUNC.NS7", 0, "DEFINE FUNCTION MYFUNC"),
			expectedRename("CALLED", "FUNC.NS7", 4, "MYFUNC := FALSE")
		);
	}

	@Test
	void onlyRenameReferencesInReferencedLibraries()
	{
		whenRenamingFile("NOREF", "CALLEDN.NSN", "NEWSUBN");

		itShouldChange(
			expectedRename("NOREF", "CALLER.NSN", 2, "CALLNAT 'NEWSUBN'")
		);
	}

	@Test
	void notRenameReferencesInUnrelatedLibraries()
	{
		whenRenamingFile("CALLED", "CALLEDN.NSN", "CHANGEN");

		expectNoChangesInUnrelatedLibrary();
	}

	@Test
	void notChangeAnythingWhenRenamingTheFileOfAnExternalSubroutine()
	{
		whenRenamingFile("EXTSUB.NSS", "EXTSUB2");

		assertThat(renameEdit.getChanges())
			.as("No actions needed if just renaming the file of an external subroutine")
			.isEmpty();
	}

	@Test
	void renameAFunctionAndItsFileOnRenameTriggeredOnFunctionName()
	{
		var textDocument = textDocumentIdentifier("CALLED", "FUNC");
		var renameParams = new RenameParams(
			textDocument,
			new Position(4, 1),
			"FUNCY"
		);

		var changes = getContext().languageService().rename(renameParams);
		assertThat(changes.getChanges()).isEmpty();
		assertThat(changes.getDocumentChanges()).isNotEmpty();

		var docChanges = changes.getDocumentChanges();

		// The changes to change to the new function name should come first
		var expectedChanges = List.of(
			new ExpectedDocumentChange(uri("CALLEE", "CALLER.NSN"), List.of(new LineAndNewText(18, "FUNCY"))),
			new ExpectedDocumentChange(uri("CALLED", "FUNC.NS7"), List.of(new LineAndNewText(0, "FUNCY"), new LineAndNewText(4, "FUNCY"))),
			new ExpectedDocumentChange(uri("CALLEE", "INCL.NSC"), List.of(new LineAndNewText(4, "FUNCY"))),
			new ExpectedDocumentChange(uri("CALLEE", "CALLER2.NSN"), List.of(new LineAndNewText(12, "FUNCY")))
		);

		assertThat(docChanges)
			.as("Expected changes to have %d document changes and one file rename change (%d total)".formatted(expectedChanges.size(), expectedChanges.size() + 1))
			.hasSize(expectedChanges.size() + 1);

		var documentEditsOnly = docChanges.stream().filter(Either::isLeft).map(Either::getLeft).toList();
		for (var expectedChange : expectedChanges)
		{
			assertThat(documentEditsOnly).anySatisfy(edit ->
			{
				assertThat(edit.getTextDocument().getUri()).as("Expected an edit to this file").isEqualTo(expectedChange.uri);
				assertThat(edit.getEdits()).as("Expected this amount of file edits to file %s".formatted(expectedChange.uri)).hasSize(expectedChange.changes.size());

				for (var change : expectedChange.changes)
				{
					assertThat(edit.getEdits())
						.as("Expected changes to file %s to have an edit in line %d with new text %s".formatted(expectedChange.uri, change.line, change.newText))
						.anyMatch(te -> te.getRange().getStart().getLine() == change.line && te.getNewText().equals(change.newText));
				}
			});
		}

		// The last change to do is renaming the old file
		var lastChange = docChanges.get(4);
		assertThat(lastChange.isRight()).isTrue();
		assertThat(lastChange.getRight()).isInstanceOf(RenameFile.class);
		var renameFile = (RenameFile) lastChange.getRight();
		assertThat(renameFile.getOldUri()).isEqualTo(textDocument.getUri());
		var expectedNewUri = LspUtil.uriToPath(textDocument.getUri()).getParent().resolve("FUNCY.NS7").toUri().toString();
		assertThat(renameFile.getNewUri()).isEqualTo(expectedNewUri);
	}

	private LspTestContext theTestContext;

	@Override
	protected LspTestContext getContext()
	{
		return theTestContext;
	}

	@BeforeEach
	void beforeEach(@LspProjectName("filerename") LspTestContext testContext)
	{
		theTestContext = testContext;
	}

	private WorkspaceEdit renameEdit;
	private String renamedFileUri;

	private void whenRenamingFile(String module, String newName)
	{
		whenRenamingFile("CALLED", module, newName);
	}

	private void whenRenamingFile(String library, String module, String newName)
	{
		var document = new TextDocumentIdentifier(uri(library, module));
		var folderUri = document.getUri().substring(0, document.getUri().lastIndexOf('/') + 1);
		var extension = document.getUri().substring(document.getUri().lastIndexOf('.'));
		renamedFileUri = "%s%s%s".formatted(folderUri, newName, extension);

		var rename = new FileRename(document.getUri(), FileRenameShould.this.renamedFileUri);
		try
		{
			renameEdit = getContext().workspaceService().willRenameFiles(new RenameFilesParams(List.of(rename))).get(5, TimeUnit.HOURS);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Request did not finish withing timeout", e);
		}
	}

	private String uri(String library, String module)
	{
		return getContext().project().rootPath().resolve(Path.of("Natural-Libraries", library, module)).toUri().toString();
	}

	private void itShouldChange(ExpectedRename... expectedRenames)
	{
		try
		{
			var changedFilesTested = new HashSet<String>();
			for (var expectedRename : expectedRenames)
			{
				var applier = new TextEditApplier();

				var changedFileUri = expectedRename.document.getUri();
				var sourceBeforeRename = Files.readString(LspUtil.uriToPath(changedFileUri));

				assertThat(renameEdit.getChanges())
					.as("Expected at least one change in file %s but none found".formatted(changedFileUri))
					.containsKey(changedFileUri);

				var fileEdits = renameEdit.getChanges().get(changedFileUri);
				var sourceAfterRename = applier.applyAll(fileEdits, sourceBeforeRename);

				var sourceLines = sourceAfterRename.lines().toList();
				assertThat(sourceLines.get(expectedRename.line)).isEqualTo(expectedRename.expectedLineContent);

				changedFilesTested.add(changedFileUri);
			}

			for (var changedFile : changedFilesTested)
			{
				renameEdit.getChanges().remove(changedFile);
			}

			assertThat(renameEdit.getChanges())
				.as("There are left over changes that haven't been explicitly tested")
				.isEmpty();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private void expectNoChangesInUnrelatedLibrary()
	{
		assertThat(renameEdit.getChanges())
			.as("Expected no changes in unrelated library NOREF")
			.allSatisfy((uri, changes) -> assertThat(uri).doesNotContain("NOREF"));
	}

	private ExpectedRename expectedRename(String module, int line, String expectedLine)
	{
		return expectedRename("CALLEE", module, line, expectedLine);
	}

	private ExpectedRename expectedRename(String library, String module, int line, String expectedLine)
	{
		return new ExpectedRename(
			new TextDocumentIdentifier(getContext().project().rootPath().resolve(Path.of("Natural-Libraries", library, module)).toUri().toString()),
			line,
			expectedLine
		);
	}

	record ExpectedRename(TextDocumentIdentifier document, int line, String expectedLineContent)
	{}

	record ExpectedDocumentChange(String uri, List<LineAndNewText> changes)
	{}

	record LineAndNewText(int line, String newText)
	{}
}
