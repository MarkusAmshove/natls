package org.amshove.natls.snippets;

import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.eclipse.lsp4j.InsertTextFormat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@LspTest
public class L4nSnippetProviderShould
{
	@ParameterizedTest
	@ValueSource(strings =
	{
		"ENTER-LEAVE", "DEBUG", "INFO", "WARNING", "ERROR", "FATAL"
	})
	void addSnippetsForEveryLogLevel(String level, @LspProjectName("modrefparser") LspTestContext testContext)
	{
		var file = testContext.project().findFileByReferableName("SUB");
		var sut = new L4nSnippetProvider();

		var maybeSnippet = sut.provideSnippets(file).stream().filter(ci -> ci.getLabel().equals("log" + level.toLowerCase())).findFirst();
		assertThat(maybeSnippet).isPresent();
		var snippet = maybeSnippet.get();
		assertAll(
			() -> assertThat(snippet.getLabel()).isEqualTo("log" + level.toLowerCase()),
			() -> assertThat(snippet.getDocumentation()).isNotNull(),
			() -> assertThat(snippet.getAdditionalTextEdits()).anyMatch(te -> te.getNewText().equals("LOCAL USING L4NPARAM%n".formatted())),
			() -> assertThat(snippet.getAdditionalTextEdits()).anyMatch(te -> te.getNewText().equals("LOCAL USING L4NCONST%n".formatted())),
			() -> assertThat(snippet.getInsertText()).isEqualTo(
				"""
					L4N-LOGLEVEL := C-LOGLEVEL-%s
					COMPRESS ${1:'text'} INTO L4N-LOGTEXT
					INCLUDE L4NLOGIT${0}
					""".formatted(level)
			),
			() -> assertThat(snippet.getInsertTextFormat()).isEqualTo(InsertTextFormat.Snippet)
		);
	}
}
