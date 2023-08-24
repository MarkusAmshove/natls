package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.CodeAction;

import java.util.Collection;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public final class ApplicableCodeActionAssertion extends CodeActionAssertion
{
	private final String previousSource;

	public ApplicableCodeActionAssertion(String previousSource, CodeAction action)
	{
		super(action);
		this.previousSource = previousSource;
	}

	public CodeActionAssertion resultsApplied(String expectedSource)
	{
		var applier = new TextEditApplier();
		var allEdits = action.getEdit().getChanges().values().stream().flatMap(Collection::stream).toList();

		var changedText = applier.applyAll(allEdits, previousSource);

		assertThat(changedText)
			.isEqualToNormalizingNewlines(expectedSource);

		return this;
	}

	@Override
	public ApplicableCodeActionAssertion hasTitle(String title)
	{
		super.hasTitle(title);
		return this;
	}

	@Override
	public ApplicableCodeActionAssertion deletesLine(int line)
	{
		super.deletesLine(line);
		return this;
	}

	@Override
	public ApplicableCodeActionAssertion deletesLines(int startLine, int endLine)
	{
		super.deletesLines(startLine, endLine);
		return this;
	}

	@Override
	public ApplicableCodeActionAssertion fixes(String diagnosticId)
	{
		super.fixes(diagnosticId);
		return this;
	}

	@Override
	public ApplicableCodeActionAssertion insertsText(int line, int column, String newText)
	{
		super.insertsText(line, column, newText);
		return this;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (ApplicableCodeActionAssertion) obj;
		return Objects.equals(this.previousSource, that.previousSource) &&
			Objects.equals(this.action, that.action);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(previousSource, action);
	}

	@Override
	public String toString()
	{
		return "ApplicableCodeActionAssertion[" +
			"previousSource=" + previousSource + ", " +
			"action=" + action + ']';
	}

}
