package org.amshove.natls.documentsymbol;

import java.util.List;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.IVariableNode;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;

public class SymbolInformationProvider
{
	public List<SymbolInformation> provideSymbols(INaturalModule module)
	{
		var referencableNodes = module.referencableNodes();

		return referencableNodes
			.stream()
			.map(n -> new SymbolInformation(
				n.declaration().symbolName(),
				n instanceof IVariableNode ? SymbolKind.Variable : SymbolKind.Method,
				LspUtil.toLocation(n),
				n.position().fileNameWithoutExtension()
			))
			.toList();
	}
}
