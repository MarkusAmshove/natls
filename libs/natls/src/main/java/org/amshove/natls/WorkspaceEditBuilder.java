package org.amshove.natls;

import org.amshove.natls.codemutation.FileEdits;
import org.amshove.natls.codemutation.UsingToAdd;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.*;

public class WorkspaceEditBuilder
{
	private final Map<String, List<TextEdit>> textEdits = new HashMap<>();

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

	public WorkspaceEditBuilder changesText(IPosition position, String newText)
	{
		var edits = textEdits.computeIfAbsent(LspUtil.pathToUri(position.filePath()), u -> new ArrayList<>());

		var edit = new TextEdit();
		edit.setRange(LspUtil.toRange(position));
		edit.setNewText(newText);

		edits.add(edit);
		return this;
	}

	public WorkspaceEditBuilder appendsToNode(ISyntaxNode node, String text)
	{
		var lastNode = node.descendants().last();
		var lastNodeSource = lastNode instanceof ITokenNode tokenNode
			? tokenNode.token().source()
			: Objects.requireNonNull(lastNode.findDescendantOfType(ITokenNode.class)).token().source();
		return changesText(
			lastNode.position(),
			lastNodeSource + text
		);
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

	public WorkspaceEditBuilder addsUsing(LanguageServerFile file, String using, VariableScope scope)
	{
		var fileEdit = FileEdits.addUsing(file, new UsingToAdd(using, scope));
		if (fileEdit != null)
		{
			var edits = textEdits.computeIfAbsent(fileEdit.fileUri(), u -> new ArrayList<>());
			edits.add(fileEdit.textEdit());
		}

		return this;
	}

	public WorkspaceEditBuilder addsVariable(LanguageServerFile file, String name, String type, VariableScope scope)
	{
		if (file.module()instanceof IHasDefineData hasDefineData && hasDefineData.defineData() != null && hasDefineData.defineData().findVariable(name) != null)
		{
			return this;
		}

		var fileEdit = FileEdits.addVariable(file, name, type, scope);
		var edits = textEdits.computeIfAbsent(fileEdit.fileUri(), u -> new ArrayList<>());
		edits.add(fileEdit.textEdit());
		return this;
	}

	public WorkspaceEdit build()
	{
		var edit = new WorkspaceEdit();
		edit.setChanges(textEdits);
		return edit;
	}

	public WorkspaceEditBuilder addsSubroutine(LanguageServerFile file, String name, String source)
	{
		if (!file.getType().canHaveBody())
		{
			throw new IllegalStateException("Module of type %s can not have subroutines".formatted(file.getType()));
		}

		var fileEdit = FileEdits.addSubroutine(file, name, source);
		var edits = textEdits.computeIfAbsent(fileEdit.fileUri(), u -> new ArrayList<>());
		edits.add(fileEdit.textEdit());

		return this;
	}

	public WorkspaceEditBuilder changesRange(LanguageServerFile file, Range range, String source)
	{
		var edits = textEdits.computeIfAbsent(file.getUri(), u -> new ArrayList<>());
		edits.add(new TextEdit(range, source));
		return this;
	}

	public WorkspaceEditBuilder addPrototype(LanguageServerFile file, IFunction calledFunction)
	{
		var fileEdit = FileEdits.addPrototype(file, calledFunction);
		var edits = textEdits.computeIfAbsent(fileEdit.fileUri(), u -> new ArrayList<>());
		edits.add(fileEdit.textEdit());
		return this;
	}
}
