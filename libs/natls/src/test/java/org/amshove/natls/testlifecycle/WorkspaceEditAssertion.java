package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public record WorkspaceEditAssertion(WorkspaceEdit edit)
{
	public static WorkspaceEditAssertion assertThatEdit(WorkspaceEdit edit)
	{
		assertThat(edit)
			.as("No WorkspaceEdit was returned by server")
			.isNotNull();
		return new WorkspaceEditAssertion(edit);
	}

	public WorkspaceEditAssertion deletesLine(int line)
	{
		assertThat(edit.getChanges().values().stream().flatMap(Collection::stream))
			.as("Expected at least one TextEdit to delete line %d\nThis can be done with a Range at character 0 at the line it should delete and character 0 in the next line".formatted(line))
			.anyMatch(e -> e.getRange().getStart().getLine() == line && e.getRange().getStart().getCharacter() == 0 && e.getRange().getEnd().getLine() == line + 1 && e.getRange().getEnd().getCharacter() == 0);

		return this;
	}

	public WorkspaceEditAssertion deletesLines(int startLine, int endLine)
	{
		assertThat(edit.getChanges().values().stream().flatMap(Collection::stream))
			.as("Expected at least one TextEdit to delete lines %d to %d\nThis can be done with a Range at character 0 at the first line it should delete and the last character last line".formatted(startLine, endLine))
			.anyMatch(e -> e.getRange().getStart().getLine() == startLine && e.getRange().getStart().getCharacter() == 0 && e.getRange().getEnd().getLine() == endLine);

		return this;
	}

	public WorkspaceEditAssertion insertsText(int line, int column, String newText)
	{
		assertThat(edit.getChanges().values().stream().flatMap(Collection::stream))
			.as("Expected to find a code action inserting %s at line %d and column %d".formatted(newText, line, column))
			.anyMatch(e -> e.getNewText().equals(newText) && e.getRange().getStart().getLine() == line && e.getRange().getStart().getCharacter() == column);

		return this;
	}

	public WorkspaceEditAssertion changesText(int line, String oldLine, String newLine, TextDocumentIdentifier file)
	{
		assertThat(edit.getChanges()).as("The following file does not have edits: " + file.getUri()).containsKey(file.getUri());
		assertThat(edit.getChanges().get(file.getUri())).as("No edits returned for file: " + file.getUri()).isNotEmpty();

		var editsForFile = edit.getChanges().get(file.getUri());
		var maybeEdit = editsForFile
			.stream()
			.filter(e -> e.getRange().getStart().getLine() == line).findFirst();

		assertThat(maybeEdit)
			.as(
				"Expected to find a TextEdit in line " + line
					+ ". There were however edits (or none) in lines "
					+ editsForFile.stream()
						.map(e -> Integer.toString(e.getRange().getStart().getLine()))
						.collect(Collectors.joining(","))
			)
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
