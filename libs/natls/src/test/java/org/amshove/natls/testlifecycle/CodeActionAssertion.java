package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.CodeAction;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public record CodeActionAssertion(CodeAction action)
{
	public CodeActionAssertion hasTitle(String title)
	{
		assertThat(action.getTitle())
			.as("Title is different than expected")
			.isEqualTo(title);

		return this;
	}

	public CodeActionAssertion deletesLine(int line)
	{
		WorkspaceEditAssertion.assertThatEdit(action.getEdit()).deletesLine(line);

		return this;
	}

	public CodeActionAssertion fixes(String diagnosticId)
	{
		assertThat(action.getDiagnostics())
			.as("Expected the code action to fix Diagnostic " + diagnosticId)
			.anyMatch(d -> d.getCode().getLeft().equals(diagnosticId));

		return this;
	}

	public CodeActionAssertion insertsText(int line, int column, String newText)
	{
		WorkspaceEditAssertion.assertThatEdit(action.getEdit()).insertsText(line, column, newText);

		return this;
	}
}
