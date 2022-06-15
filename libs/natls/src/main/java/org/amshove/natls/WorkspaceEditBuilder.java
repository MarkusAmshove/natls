package org.amshove.natls;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.natural.ISyntaxNode;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceEditBuilder
{
	private Map<String, List<TextEdit>> textEdits = new HashMap<>();

	public WorkspaceEditBuilder()
	{

	}

	public WorkspaceEditBuilder removesNode(ISyntaxNode node)
	{
		return removesLine(LspUtil.pathToUri(node.position().filePath()), LspUtil.toRange(node));
	}

	public WorkspaceEditBuilder removesLine(String fileUri, Range range)
	{
		return changesText(fileUri, range, "");
	}

	public WorkspaceEditBuilder changesNode(ISyntaxNode node, String newText)
	{
		return changesText(LspUtil.pathToUri(node.position().filePath()), LspUtil.toRange(node), newText);
	}

	public WorkspaceEditBuilder changesText(String fileUri, Range range, String newText)
	{
		var edits = textEdits.computeIfAbsent(fileUri, u -> new ArrayList<>());

		var edit = new TextEdit();
		edit.setRange(range);
		edit.setNewText(newText);

		edits.add(edit);
		return this;
	}

	public WorkspaceEdit build()
	{
		var edit = new WorkspaceEdit();
		edit.setChanges(textEdits);
		return edit;
	}
}