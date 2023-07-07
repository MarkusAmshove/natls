package org.amshove.natls.codemutation;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.Optional;

public class CodeInsertionPlacer
{
	public CodeInsertion findRangeToInsertUsing(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		ReadOnlyList<IUsingNode> usings = scope == VariableScope.PARAMETER
			? defineData.parameterUsings()
			: defineData.localUsings();

		if (usings.hasItems())
		{
			var firstUsing = usings.first();
			var range = LspUtil.toSingleRange(firstUsing.position().line(), firstUsing.position().offsetInLine());
			return new CodeInsertion(range, System.lineSeparator());
		}

		var emptyScopeToken = defineData.findFirstScopeNode(scope);
		if (emptyScopeToken != null)
		{
			/*
			If there is DEFINE DATA LOCAL this will add the using before the empty LOCAL
			and add a line break after DATA and before LOCAL
			 */
			var scopeTokenRange = LspUtil.toSingleRange(emptyScopeToken.position().line(), emptyScopeToken.position().offsetInLine());
			return new CodeInsertion(
				scopeTokenRange.getStart().getCharacter() == 0 ? "" : System.lineSeparator(), // the scope token is not the only thing in the line
				scopeTokenRange,
				System.lineSeparator()
			);
		}

		var range = findRangeOfFirstScope(file, scope)
			.orElse(LspUtil.toSingleRange(defineData.descendants().get(0).position().line() + 1, 0));
		return new CodeInsertion(range, System.lineSeparator());
	}

	public CodeInsertion findRangeToInsertVariable(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		return findRangeOfFirstVariableWithScope(file, scope)
			.map(r -> new CodeInsertion("", r, System.lineSeparator()))
			.or(() -> findRangeOfFirstScope(file, scope).map(r -> new CodeInsertion("", moveOneDown(r), System.lineSeparator())))
			.orElse(new CodeInsertion("%s%n".formatted(scope.toString()), LspUtil.toSingleRange(defineData.descendants().get(0).position().line() + 1, 0), System.lineSeparator()));
	}

	private static Optional<Range> findRangeOfFirstVariableWithScope(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		if (defineData.variables().hasItems())
		{
			return defineData.variables().stream().filter(v -> v.scope() == scope)
				.filter(v -> v.position().filePath().equals(file.getPath()))
				.findFirst()
				.map(v -> (ISyntaxNode) v.parent()) // Scope node
				.map(v -> LspUtil.toSingleRange(v.position().line() + 1, 0));
		}

		return Optional.empty();
	}

	private static Range moveOneDown(Range range)
	{
		return new Range(
			new Position(range.getStart().getLine() + 1, range.getStart().getCharacter()),
			new Position(range.getEnd().getLine() + 1, range.getEnd().getCharacter())
		);
	}

	private static Optional<Range> findRangeOfFirstScope(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		return defineData.directDescendantsOfType(IScopeNode.class)
			.filter(n -> n.scope() == scope && n.position().filePath().equals(file.getPath()))
			.map(n -> LspUtil.toSingleRange(n.position().line(), 0))
			.findFirst();
	}
}
