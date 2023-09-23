package org.amshove.natls.lsp;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.testlifecycle.*;
import org.eclipse.lsp4j.FileRename;
import org.eclipse.lsp4j.RenameFilesParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FileRenameShould extends LanguageServerTest
{

	@Test
	void renameASubprogramOnTheCallSite()
	{
		whenRenamingFile("CALLEDN", "CHANGEN");

		itShouldChange(
			expectedRename("CALLER", 8, "CALLNAT 'CHANGEN' #PARM"),
			expectedRename("CALLER", 10, "    CALLNAT 'CHANGEN' #PARM"),
			expectedRename("CALLER2", 7, "CALLNAT 'CHANGEN' #TEST")
		);
	}

	@Test
	void renameALdaOnTheCallSite()
	{
		whenRenamingFile("SOMELDA", "MYLDA");

		itShouldChange(
			expectedRename("CALLER", 3, "LOCAL USING MYLDA"),
			expectedRename("CALLER2", 4, "LOCAL USING MYLDA")
		);
	}

	@Test
	void renameAPdaOnTheCallSite()
	{
		whenRenamingFile("SOMEPDA", "MYPDA");

		itShouldChange(
			expectedRename("CALLER", 1, "PARAMETER USING MYPDA"),
			expectedRename("CALLER2", 2, "PARAMETER USING MYPDA")
		);
	}

	@Test
	void renameAGdaOnTheCallSite()
	{
		whenRenamingFile("SOMEGDA", "MYGDA");

		itShouldChange(
			expectedRename("CALLER", 2, "GLOBAL USING MYGDA"),
			expectedRename("CALLER2", 3, "GLOBAL USING MYGDA")
		);
	}

	@Test
	void renameAProgramOnTheCallSite()
	{
		whenRenamingFile("SOMEPROG", "MYPROG");

		itShouldChange(
			expectedRename("CALLER", 13, "FETCH 'MYPROG' 50"),
			expectedRename("CALLER", 15, "    FETCH RETURN 'MYPROG' 22"),
			expectedRename("CALLER2", 11, "        FETCH REPEAT 'MYPROG' 100")
		);
	}

	@Test
	void renameCallSitesInCopycodes()
	{
		whenRenamingFile("PROGCC", "CCPROG");

		itShouldChange(
			expectedRename("INCL", 0, "FETCH 'CCPROG' 200")
		);

		whenRenamingFile("SUBCC", "CCSUB");

		itShouldChange(
			expectedRename("INCL", 2, "CALLNAT 'CCSUB' #VAR")
		);
	}

	@Test
	void renameCallSitesOfFunctions()
	{
		whenRenamingFile("FUNC", "MYFUNC");

		itShouldChange(
			expectedRename("CALLER", 18, "IF MYFUNC(<>)"),
			expectedRename("CALLER2", 12, "    WHEN MYFUNC(<>)"),
			expectedRename("INCL", 4, "#BOOL := MYFUNC(<>)")
		);
	}

	@Test
	void onlyRenameReferencesInReferencedLibraries()
	{
		whenRenamingFile("NOREF", "CALLEDN", "NEWSUBN");

		itShouldChange(
			expectedRename("NOREF", "CALLER", 2, "CALLNAT 'NEWSUBN'")
		);
	}

	@Test
	void notRenameReferencesInUnrelatedLibraries()
	{
		whenRenamingFile("CALLED", "CALLEDN", "CHANGEN");

		expectNoChangesInUnrelatedLibrary();
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
		var document = textDocumentIdentifier(library, module);
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
			textDocumentIdentifier(library, module),
			line,
			expectedLine
		);
	}

	record ExpectedRename(TextDocumentIdentifier document, int line, String expectedLineContent)
	{}
}
