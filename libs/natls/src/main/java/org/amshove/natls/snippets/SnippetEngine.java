package org.amshove.natls.snippets;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SnippetEngine
{
	// TODO: Reflection
	private static final List<ISnippetProvider> snippetProvider = new ArrayList<>();

	public SnippetEngine(LanguageServerProject project)
	{
		snippetProvider.add(new L4nSnippetProvider());
		snippetProvider.add(new NatUnitSnippetProvider(project));
	}

	public List<CompletionItem> provideSnippets(LanguageServerFile file)
	{
		return snippetProvider.stream()
			.flatMap(sp -> sp.provideSnippets(file).stream())
			.filter(Objects::nonNull)
			.toList();
	}
}
