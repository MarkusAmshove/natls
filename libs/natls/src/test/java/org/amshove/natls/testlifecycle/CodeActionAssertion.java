package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.CodeAction;

import java.util.Collection;

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
		assertThat(action.getEdit().getChanges().values().stream().flatMap(Collection::stream))
			.as("Expected at least one TextEdit to delete line %d\nThis can be done with a Range at character 0 at the line it should delete and character 0 in the next line".formatted(line))
			.anyMatch(e -> e.getRange().getStart().getLine() == line && e.getRange().getStart().getCharacter() == 0 && e.getRange().getEnd().getLine() == line + 1 && e.getRange().getEnd().getCharacter() == 0);

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
		assertThat(action.getEdit().getChanges().values().stream().flatMap(Collection::stream))
			.as("Expected to find a code action inserting %s at line %d and column %d".formatted(newText, line, column))
			.anyMatch(e -> e.getNewText().equals(newText) && e.getRange().getStart().getLine() == line && e.getRange().getStart().getCharacter() == column);
		return this;
	}
}
