package org.amshove.natls.codeactions;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.natural.IReferencableNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RenameSymbolAction
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
			if(ref.referencingToken().isQualified())
			{
				edit.setNewText(String.format("%s.%s",
					ref.referencingToken().symbolName().split("\\.")[0],
					newName
				));
			}
			else
			{
				edit.setNewText(newName);
			}
			edit.setRange(LspUtil.toRange(ref.referencingToken()));
			changes.add(edit);
		});

		var declarationEdit = new TextEdit();
		declarationEdit.setRange(LspUtil.toRange(referencableNode.declaration()));
		declarationEdit.setNewText(newName);
		changesPerFile.computeIfAbsent(LspUtil.pathToUri(referencableNode.position().filePath()), k -> new ArrayList<>()).add(declarationEdit);

		workspaceEdit.setChanges(changesPerFile);
		return workspaceEdit;
	}
}
