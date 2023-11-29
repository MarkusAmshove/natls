package org.amshove.natls.codelens;

import org.amshove.natls.CustomCommands;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.ModuleReferenceCache;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.natural.ISubroutineNode;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import java.util.Arrays;
import java.util.List;

public class ModuleReferencesCodeLens implements ICodeLensProvider
{
	@Override
	public List<CodeLens> provideCodeLens(LanguageServerFile file)
	{
		var references = file.module().callers().size() + ModuleReferenceCache.retrieveCachedPositions(file).size();

		var codeLensRange = findRangeForCodeLens(file);

		if (references == 0)
		{
			return List.of(
				codeLensWithoutCommand("No references", codeLensRange)
			);
		}

		return List.of(
			new CodeLens(
				codeLensRange,
				new Command(
					"%d references".formatted(references),
					CustomCommands.CODELENS_SHOW_REFERENCES,
					Arrays.asList(file.getUri(), codeLensRange)
				),
				null
			)
		);
	}

	private Range findRangeForCodeLens(LanguageServerFile file)
	{
		var firstRangeInFile = LspUtil.toRange(file.module().syntaxTree().descendants().first().position());

		if (file.getType() == NaturalFileType.SUBROUTINE)
		{
			var topLevelSubroutine = NodeUtil.findFirstStatementOfType(ISubroutineNode.class, file.module().syntaxTree());
			return topLevelSubroutine != null
				? LspUtil.toRange(topLevelSubroutine.position())
				: firstRangeInFile;
		}

		return firstRangeInFile;
	}

}
