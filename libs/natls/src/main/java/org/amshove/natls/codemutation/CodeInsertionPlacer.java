package org.amshove.natls.codemutation;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.List;
import java.util.Optional;

public class CodeInsertionPlacer
{
	// reversed order of scope priority. last element in this list
	// should be first in define data
	private static final List<VariableScope> SCOPE_ORDERS = List.of(
		VariableScope.INDEPENDENT,
		VariableScope.LOCAL,
		VariableScope.PARAMETER,
		VariableScope.GLOBAL
	);

	public CodeInsertion findInsertionPositionToInsertUsing(LanguageServerFile file, VariableScope scope)
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
			.orElse(findRangeForNewScope(file, scope));
		return new CodeInsertion(range, System.lineSeparator());
	}

	public CodeInsertion findInsertionPositionToInsertVariable(LanguageServerFile file, VariableScope scope)
	{
		var bestGuess = scope == VariableScope.PARAMETER
			? findLastInsertionPositionForParameter(file)
			: findFirstInsertionPositionForVariable(file, scope);
		return bestGuess
			.map(r -> new CodeInsertion("", r, System.lineSeparator()))
			.or(() -> findRangeOfFirstScope(file, scope).map(r -> new CodeInsertion("", moveOneDown(r), System.lineSeparator())))
			.orElse(new CodeInsertion("%s%n".formatted(scope.toString()), findRangeForNewScope(file, scope), System.lineSeparator()));
	}

	public CodeInsertion findInsertionPositionForStatement(LanguageServerFile file)
	{
		var withBody = (IModuleWithBody) file.module();

		if (file.getType() == NaturalFileType.FUNCTION)
		{

		}

		if (withBody.body().statements().isEmpty())
		{
			var lastNodeThatIsNotBodyNode = file.module().syntaxTree().descendants().stream()
				.filter(n -> !(n instanceof IStatementListNode))
				.reduce((p, n) -> n)
				.orElseThrow();
			return new CodeInsertion(System.lineSeparator(), LspUtil.toRangeAfter(NodeUtil.deepFindLeaf(lastNodeThatIsNotBodyNode).position()));
		}

		var lastNodePosition = findLastNodeInFileThatCantHaveStatementsAfter(file.getType(), withBody);

		return new CodeInsertion(LspUtil.toRangeBefore(lastNodePosition), System.lineSeparator());
	}

	private static IPosition findLastNodeInFileThatCantHaveStatementsAfter(NaturalFileType type, IModuleWithBody withBody)
	{
		if (type == NaturalFileType.FUNCTION)
		{
			var lastNode = withBody.body().statements().last();
			var index = withBody.body().statements().indexOf(lastNode);
			return withBody.body().statements().get(index - 1).position();
		}

		return type == NaturalFileType.SUBROUTINE
			? withBody.body().statements().first().descendants().last().position()
			: withBody.body().statements().last().position();
	}

	private static Optional<Range> findLastInsertionPositionForParameter(LanguageServerFile file)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		if (defineData.variables().hasItems())
		{
			return defineData.variables().stream().filter(v -> v.scope() == VariableScope.PARAMETER)
				.filter(v -> v.position().filePath().equals(file.getPath()))
				.reduce((p, n) -> n)
				.map(v -> LspUtil.toSingleRange(v.position().line() + 1, 0));
		}

		return Optional.empty();
	}

	private static Optional<Range> findFirstInsertionPositionForVariable(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		if (defineData.variables().hasItems())
		{
			return defineData.variables().stream().filter(v -> v.scope() == scope)
				.filter(v -> v.position().filePath().equals(file.getPath()))
				.findFirst()
				.map(ISyntaxNode::parent) // Scope node
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

	private static Range findRangeForNewScope(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		var ownScopeOrderFound = false;
		for (var scopeOrder : SCOPE_ORDERS)
		{
			if (!ownScopeOrderFound && scopeOrder == scope)
			{
				ownScopeOrderFound = true;
				continue;
			}

			if (ownScopeOrderFound)
			{
				var lastNodeWithScope = defineData.findLastScopeNode(scopeOrder);
				if (lastNodeWithScope != null)
				{
					// position at the start of the latest node of this scope
					var latestLeaf = NodeUtil.deepFindLeaf(lastNodeWithScope);
					return LspUtil.toSingleRange(
						latestLeaf.position().line() + 1,
						0
					);
				}
			}
		}

		return LspUtil.toSingleRange(defineData.descendants().get(0).position().line() + 1, 0); // This should place it into the next line after DEFINE DATA
	}
}
