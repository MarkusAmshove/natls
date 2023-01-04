package org.amshove.natls.quickfixes;

import org.amshove.natls.WorkspaceEditBuilder;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.ArrayList;
import java.util.List;

public class CodeActionBuilder
{
	private final String title;
	private final String codeActionKind;
	private final List<Diagnostic> diagnostics = new ArrayList<>();
	private WorkspaceEdit workspaceEdit;

	public CodeActionBuilder(String title, String codeActionKind)
	{
		this.title = title;
		this.codeActionKind = codeActionKind;
	}

	public CodeActionBuilder fixesDiagnostic(Diagnostic diagnostic)
	{
		diagnostics.add(diagnostic);
		return this;
	}

	public CodeActionBuilder appliesWorkspaceEdit(WorkspaceEdit edit)
	{
		workspaceEdit = edit;
		return this;
	}

	public CodeActionBuilder appliesWorkspaceEdit(WorkspaceEditBuilder builder)
	{
		workspaceEdit = builder.build();
		return this;
	}

	public CodeAction build()
	{
		var action = new CodeAction(title);
		action.setEdit(workspaceEdit);
		action.setKind(codeActionKind);
		if (!diagnostics.isEmpty())
		{
			action.setDiagnostics(diagnostics);
		}
		return action;
	}
}
