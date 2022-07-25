package org.amshove.natls.snippets;

import org.amshove.natls.project.LanguageServerFile;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

public interface ISnippetProvider
{
	List<CompletionItem> provideSnippets(LanguageServerFile file);
}
