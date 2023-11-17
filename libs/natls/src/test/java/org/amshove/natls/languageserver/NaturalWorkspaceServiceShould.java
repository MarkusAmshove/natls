package org.amshove.natls.languageserver;

import org.amshove.natls.testlifecycle.EmptyProjectTest;
import org.eclipse.lsp4j.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NaturalWorkspaceServiceShould extends EmptyProjectTest
{
	@Test
	void addAFileToTheProjectIfItIsExternallyAdded()
	{
		assertThat(getContext().languageService().findNaturalFile("LIBONE", "EXTADD"))
			.as("Module shouldn't exist before adding it")
			.isNull();

		var identifier = createOrSaveFileExternally("LIBONE", "EXTADD.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			END
			""");

		assertThat(getContext().languageService().findNaturalFile(identifier))
			.isNotNull();
		assertThat(getContext().languageService().findNaturalFile("LIBONE", "EXTADD"))
			.isNotNull();
		assertThat(getContext().languageService().findNaturalFile(LspUtil.uriToPath(identifier.getUri())))
			.isNotNull();
	}

	@Test
	void removeAFileFromAProjectIfItIsDeletedExternally()
	{
		var identifier = createOrSaveFile("LIBONE", "EXTADD.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			END
			""");

		assertThat(getContext().languageService().findNaturalFile(identifier))
			.as("Module should exist now")
			.isNotNull();
		getContext().workspaceService().didChangeWatchedFiles(new DidChangeWatchedFilesParams(List.of(new FileEvent(identifier.getUri(), FileChangeType.Deleted))));
		assertThat(getContext().languageService().findNaturalFile(identifier))
			.as("Module should have been deleted")
			.isNull();
	}

	@Test
	void reparseExternallyChangedModulesIfTheyAreNotOpenedAndParseTheirCallersIfTheyAreOpen()
	{
		createOrSaveFile("LIBONE", "MYLDA.NSL", """
			DEFINE DATA LOCAL
			* >Natural Source Header 000000
			* :Mode S
			* :CP
			* <Natural Source Header
			END-DEFINE
			""");

		var subprogram = createOrSaveFile("LIBONE", "EXTADD.NSN", """
			* >Natural Source Header 000000
			* :Mode S
			* :CP
			* <Natural Source Header
			DEFINE DATA
			LOCAL USING MYLDA
			END-DEFINE
			WRITE #VAR /* Does not exist yet
			END
			""");

		getContext().documentService().didOpen(
			new DidOpenTextDocumentParams(
				new TextDocumentItem(
					subprogram.getUri(),
					"natural",
					1,
					""
				)
			)
		);

		// The subprogram is open and should have a diagnostic indicating that #VAR is not resolved
		assertThat(getContext().client().getPublishedDiagnostics(subprogram))
			.as("Unresolved Reference diagnostic should be present, because the variable is not declared")
			.anyMatch(d -> d.getMessage().equals("Unresolved reference: #VAR"));

		createOrSaveFileExternally("LIBONE", "MYLDA.NSL", """
			DEFINE DATA LOCAL
			* >Natural Source Header 000000
			* :Mode S
			* :CP
			* <Natural Source Header
			1 #VAR (A10)
			END-DEFINE
			""");

		getContext().waitForRunningTasksToFinish();
		assertThat(getContext().client().getPublishedDiagnostics(subprogram))
			.as("Diagnostic should have gone away, because it was externally added to the LDA")
			.isEmpty();
	}
}
