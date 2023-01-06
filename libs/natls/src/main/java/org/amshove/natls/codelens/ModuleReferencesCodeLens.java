package org.amshove.natls.codelens;

import org.amshove.natls.CustomCommands;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.ModuleReferenceCache;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;

import java.util.Arrays;
import java.util.List;

public class ModuleReferencesCodeLens implements ICodeLensProvider
{
	@Override
	public List<CodeLens> provideCodeLens(LanguageServerFile file)
	{
		var references = file.module().callers().size() + ModuleReferenceCache.retrieveCachedPositions(file).size();

		var firstNodeRange = LspUtil.toRange(file.module().syntaxTree().descendants().first().position());
		if (references == 0)
		{
			return List.of(
				codeLensWithoutCommand("No references", firstNodeRange)
			);
		}

		return List.of(
			new CodeLens(
				firstNodeRange,
				new Command(
					"%d references".formatted(references),
					CustomCommands.CODELENS_SHOW_REFERENCES,
					Arrays.asList(file.getUri(), firstNodeRange)
				),
				null
			)
		);
	}
}
