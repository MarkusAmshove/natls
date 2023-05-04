package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.CodeAction;

import java.util.Objects;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CodeActionAssertion
{
	protected final CodeAction action;

	public CodeActionAssertion(CodeAction action)
	{
		this.action = action;
	}

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

	public CodeActionAssertion deletesLines(int startLine, int endLine)
	{
		WorkspaceEditAssertion.assertThatEdit(action.getEdit()).deletesLines(startLine, endLine);

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

	public CodeAction action()
	{
		return action;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (CodeActionAssertion) obj;
		return Objects.equals(this.action, that.action);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(action);
	}

	@Override
	public String toString()
	{
		return "CodeActionAssertion[action=" + action + ']';
	}

}
