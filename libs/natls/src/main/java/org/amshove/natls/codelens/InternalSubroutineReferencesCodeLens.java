package org.amshove.natls.codelens;

import org.amshove.natls.CustomCommands;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.natural.IModuleWithBody;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.ISubroutineNode;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class InternalSubroutineReferencesCodeLens implements ICodeLensProvider
{
	@Override
	public List<CodeLens> provideCodeLens(LanguageServerFile file)
	{
		var module = file.module();
		var codelens = new ArrayList<CodeLens>();

		if (module instanceof IModuleWithBody hasBody && hasBody.body() != null && hasBody.body().descendants().hasItems())
		{
			hasBody.body().statements().stream()
				.filter(ISubroutineNode.class::isInstance)
				.map(ISubroutineNode.class::cast)
				.map(s ->
				{
					if (file.getType() == NaturalFileType.SUBROUTINE && isTopLevelSubroutine(s))
					{
						return null;
					}

					var references = s.references().size();
					var declarationRange = LspUtil.toRange(s.declaration());

					if (references == 0)
					{
						return codeLensWithoutCommand("No references", declarationRange);
					}

					var uri = file.getUri();
					return new CodeLens(
						declarationRange,
						new Command(
							"%d references".formatted(references),
							CustomCommands.CODELENS_SHOW_REFERENCES,
							Arrays.asList(uri, declarationRange)
						),
						null
					);
				})
				.filter(Objects::nonNull)
				.forEach(codelens::add);
		}

		return codelens;
	}

	private boolean isTopLevelSubroutine(ISubroutineNode node)
	{
		/*
		Module
		StatementList
			Subroutine <- top level
		
		Module
		StatementList
			Subroutine
			StatementList
				Subroutine <- not top level
		 */
		return !(node.parent()instanceof IStatementListNode statementList && statementList.parent() instanceof ISubroutineNode);
	}
}
