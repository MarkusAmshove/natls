package org.amshove.natls.codeactions;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.natural.IReferencableNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SymbolRenameComputer
{
	public WorkspaceEdit rename(ISymbolReferenceNode referenceNode, String newName)
	{
		return rename(referenceNode.reference(), newName);
	}

	public WorkspaceEdit rename(IReferencableNode referencableNode, String newName)
	{
		var workspaceEdit = new WorkspaceEdit();
		var changesPerFile = new HashMap<String, List<TextEdit>>();

		referencableNode.references().stream().forEach(ref -> {
			var changes = changesPerFile.computeIfAbsent(LspUtil.pathToUri(ref.position().filePath()), k -> new ArrayList<>());
			var edit = new TextEdit();
			edit.setNewText(newName);
			edit.setRange(LspUtil.toRange(ref.position()));
			changes.add(edit);
		});

		var declarationEdit = new TextEdit();
		declarationEdit.setRange(LspUtil.toRange(referencableNode.position()));
		declarationEdit.setNewText(newName);
		changesPerFile.computeIfAbsent(LspUtil.pathToUri(referencableNode.position().filePath()), k -> new ArrayList<>()).add(declarationEdit);

		workspaceEdit.setChanges(changesPerFile);
		return workspaceEdit;
	}
}
