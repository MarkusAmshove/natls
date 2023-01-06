package org.amshove.natls.snippets;

import org.amshove.natls.project.LanguageServerFile;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;
import java.util.Objects;

public class L4nSnippetProvider implements ISnippetProvider
{

	private static final String LOG_CODE = """
L4N-LOGLEVEL := C-LOGLEVEL-%s
COMPRESS ${1:'text'} INTO L4N-LOGTEXT
INCLUDE L4NLOGIT${0}
		""";

	private static final List<NaturalSnippet> LOG_SNIPPETS = List.of(
		createSnippet("enter-leave", "ENTER-LEAVE"),
		createSnippet("debug", "DEBUG"),
		createSnippet("info", "INFO"),
		createSnippet("warning", "WARNING"),
		createSnippet("error", "ERROR"),
		createSnippet("fatal", "FATAL")
	);

	private static NaturalSnippet createSnippet(String label, String logLevel)
	{
		return new NaturalSnippet("log%s".formatted(label))
			.insertsText(LOG_CODE.formatted(logLevel))
			.needsLocalUsing("L4NPARAM")
			.needsLocalUsing("L4NCONST")
			.applicableWhen(f -> f.getType().canHaveBody());
	}

	@Override
	public List<CompletionItem> provideSnippets(LanguageServerFile file)
	{
		return LOG_SNIPPETS.stream().map(s -> s.createCompletion(file)).filter(Objects::nonNull).toList();
	}
}
