package org.amshove.natls.codelens;

import org.amshove.natls.CustomCommands;
import org.amshove.natls.project.LanguageServerFile;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import java.util.List;

public interface ICodeLensProvider
{
	List<CodeLens> provideCodeLens(LanguageServerFile file);

	default CodeLens codeLensWithoutCommand(String label, Range range)
	{
		var lens = new CodeLens();
		lens.setRange(range);
		lens.setCommand(new Command(label, CustomCommands.CODELENS_NON_INTERACTIVE));
		return lens;
	}
}
