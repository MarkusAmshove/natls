package org.amshove.natls.codelens;

import org.amshove.natls.CustomCommands;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.languageserver.inputstructure.InputStructureParams;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.natural.IInputStatementNode;
import org.amshove.natparse.natural.IModuleWithBody;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;

import java.util.ArrayList;
import java.util.List;

public class InputPreviewCodeLensProvider implements ICodeLensProvider
{
	@Override
	public List<CodeLens> provideCodeLens(LanguageServerFile file)
	{
		var module = file.module();
		if (!(module instanceof IModuleWithBody withBody))
		{
			return List.of();
		}

		var inputs = NodeUtil.findNodesOfType(withBody.body(), IInputStatementNode.class);
		var lenses = new ArrayList<CodeLens>();

		for (int i = 0; i < inputs.size(); i++)
		{
			var input = inputs.get(i);
			var lens = codeLensWithoutCommand("Open Preview", LspUtil.toRange(input));
			var params = new InputStructureParams();
			params.setUri(file.getUri());
			params.setInputIndex(i);
			lens.setCommand(new Command("$(open-preview) Open Preview", CustomCommands.CODELENS_PREVIEW_INPUT_STATEMENT, List.of(params)));
			lenses.add(lens);
		}

		return lenses;
	}
}
