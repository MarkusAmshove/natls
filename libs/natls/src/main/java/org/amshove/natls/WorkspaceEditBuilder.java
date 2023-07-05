package org.amshove.natls;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.languageserver.TextEdits;
import org.amshove.natls.languageserver.UsingToAdd;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.*;

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

	public WorkspaceEditBuilder changesText(IPosition position, String newText)
	{
		var edits = textEdits.computeIfAbsent(LspUtil.pathToUri(position.filePath()), u -> new ArrayList<>());

		var edit = new TextEdit();
		edit.setRange(LspUtil.toRange(position));
		edit.setNewText(newText);

		edits.add(edit);
		return this;
	}

	public WorkspaceEditBuilder changesText(LanguageServerFile file, Range range, String newText)
	{
		var edits = textEdits.computeIfAbsent(LspUtil.pathToUri(file.getPath()), u -> new ArrayList<>());

		var edit = new TextEdit();
		edit.setRange(range);
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

	public WorkspaceEditBuilder addsUsing(LanguageServerFile file, String using)
	{
		var edits = textEdits.computeIfAbsent(file.getUri(), u -> new ArrayList<>());

		edits.add(TextEdits.addUsing(file, new UsingToAdd(using, VariableScope.LOCAL)));
		return this;
	}

	public WorkspaceEditBuilder addsVariable(LanguageServerFile file, String name, String type, VariableScope scope)
	{
		if (file.module()instanceof IHasDefineData hasDefineData && hasDefineData.defineData() != null && hasDefineData.defineData().findVariable(name) != null)
		{
			return this;
		}

		var edits = textEdits.computeIfAbsent(file.getUri(), u -> new ArrayList<>());

		edits.add(TextEdits.addVariable(file, name, type, scope));
		return this;
	}

	public WorkspaceEditBuilder addsSubroutine(LanguageServerFile file, String subroutineName, String subroutineSource)
	{
		if (!file.getType().canHaveBody())
		{
			throw new IllegalStateException("Module of type %s can not have subroutines".formatted(file.getType()));
		}

		var source = """
			%n/***********************************************************************
			DEFINE SUBROUTINE %s
			/***********************************************************************
			
			%s
			
			END-SUBROUTINE%n
			""".formatted(subroutineName, subroutineSource);

		return changesText(file, findInsertionPositionForStatement(file), source);
	}

	public WorkspaceEdit build()
	{
		var edit = new WorkspaceEdit();
		edit.setChanges(textEdits);
		return edit;
	}

	private Range findInsertionPositionForStatement(LanguageServerFile file)
	{
		var withBody = (IModuleWithBody) file.module();
		var lastNode = file.getType() == NaturalFileType.SUBROUTINE
			? withBody.body().statements().first().descendants().last().position()
			: withBody.body().statements().last().position();

		return LspUtil.toRangeBefore(lastNode);
	}
}
