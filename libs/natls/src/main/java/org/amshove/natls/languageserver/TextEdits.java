package org.amshove.natls.languageserver;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Optional;

public class TextEdits
{
	public static TextEdit addVariable(LanguageServerFile file, String variableName, String variableType, VariableScope scope)
	{
		var variableInsert = deduceVariableInsertPosition(file, scope);
		var edit = new TextEdit();
		edit.setRange(variableInsert.range);
		edit.setNewText("%s%d %s %s%n".formatted(variableInsert.insertPrefix(), 1, variableName, variableType));
		return edit;
	}

	public static TextEdit addUsing(LanguageServerFile file, UsingToAdd neededUsing)
	{
		if(alreadyHasUsing(neededUsing.name(), file))
		{
			return null;
		}

		return createUsingInsert(neededUsing, file);
	}

	private static TextEdit createUsingInsert(UsingToAdd using, LanguageServerFile file)
	{
		var edit = new TextEdit();
		var range = findRangeToInsertUsing(file, using.scope());

		edit.setRange(range);
		edit.setNewText("%s USING %s%n".formatted(using.scope(), using.name()));
		return edit;
	}

	private static Range findRangeToInsertUsing(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		ReadOnlyList<IUsingNode> usings = scope == VariableScope.PARAMETER
			? defineData.parameterUsings()
			: defineData.localUsings();

		if(usings.hasItems())
		{
			var firstUsing = usings.first();
			return LspUtil.toSingleRange(firstUsing.position().line(), 0);
		}

		return findRangeOfFirstScope(file, scope)
			.orElse(LspUtil.toSingleRange(defineData.descendants().get(0).position().line() + 1, 0));
	}

	private static Optional<Range> findRangeOfFirstScope(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		return defineData.directDescendantsOfType(IScopeNode.class)
			.filter(n -> n.scope() == scope && n.position().filePath().equals(file.getPath()))
			.map(n -> LspUtil.toSingleRange(n.position().line(), 0))
			.findFirst();
	}

	private static VariableInsert deduceVariableInsertPosition(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		return findRangeOfFirstVariableWithScope(file, scope)
			.map(r -> new VariableInsert("", r))
			.or(() -> findRangeOfFirstScope(file, scope).map(r -> new VariableInsert("", moveOneDown(r))))
			.orElse(new VariableInsert("%s%n".formatted(scope.toString()), LspUtil.toSingleRange(defineData.descendants().get(0).position().line() + 1, 0)));
	}

	private static Optional<Range> findRangeOfFirstVariableWithScope(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		if(defineData.variables().hasItems())
		{
			return defineData.variables().stream().filter(v -> v.scope() == scope)
				.filter(v -> v.position().filePath().equals(file.getPath()))
				.findFirst()
				.map(v -> (ISyntaxNode)v.parent()) // Scope node
				.map(v -> LspUtil.toSingleRange(v.position().line() + 1, 0));
		}

		return Optional.empty();
	}

	private static boolean alreadyHasUsing(String using, LanguageServerFile file)
	{
		return ((IHasDefineData) file.module()).defineData().usings().stream().anyMatch(u -> u.target().symbolName().equals(using));
	}

	private static Range moveOneDown(Range range)
	{
		return new Range(
			new Position(range.getStart().getLine() + 1, range.getStart().getCharacter()),
			new Position(range.getEnd().getLine() + 1, range.getEnd().getCharacter())
		);
	}

	private record VariableInsert(String insertPrefix, Range range) {}
}
