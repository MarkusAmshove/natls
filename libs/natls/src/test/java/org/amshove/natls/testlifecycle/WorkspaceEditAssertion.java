package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.Collection;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public record WorkspaceEditAssertion(WorkspaceEdit edit)
{
	public static WorkspaceEditAssertion assertThatEdit(WorkspaceEdit edit)
	{
		return new WorkspaceEditAssertion(edit);
	}

	public WorkspaceEditAssertion deletesLine(int line)
	{
		assertThat(edit.getChanges().values().stream().flatMap(Collection::stream))
			.as("Expected at least one TextEdit to delete line %d\nThis can be done with a Range at character 0 at the line it should delete and character 0 in the next line".formatted(line))
			.anyMatch(e -> e.getRange().getStart().getLine() == line && e.getRange().getStart().getCharacter() == 0 && e.getRange().getEnd().getLine() == line + 1 && e.getRange().getEnd().getCharacter() == 0);

		return this;
	}

	public WorkspaceEditAssertion insertsText(int line, int column, String newText)
	{
		assertThat(edit.getChanges().values().stream().flatMap(Collection::stream))
			.as("Expected to find a code action inserting %s at line %d and column %d".formatted(newText, line, column))
			.anyMatch(e -> e.getNewText().equals(newText) && e.getRange().getStart().getLine() == line && e.getRange().getStart().getCharacter() == column);

		return this;
	}

	public WorkspaceEditAssertion changesText(int line, String oldLine, String newLine)
	{
		var maybeEdit = edit.getChanges()
			.values()
			.stream()
			.flatMap(Collection::stream)
			.filter(e -> e.getRange().getStart().getLine() == line).findFirst();

		assertThat(maybeEdit)
			.as("Expected to find a TextEdit in line " + line)
			.isPresent();

		var edit = maybeEdit.get();

		var inFront = oldLine.substring(0, edit.getRange().getStart().getCharacter());
		var after = oldLine.substring(edit.getRange().getEnd().getCharacter());

		assertThat(inFront + edit.getNewText() + after)
			.as("Expected the edit to result in " + newLine)
			.isEqualTo(newLine);

		return this;
	}
}
