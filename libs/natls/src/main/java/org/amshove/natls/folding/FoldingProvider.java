package org.amshove.natls.folding;

import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.FoldingRange;

import java.util.ArrayList;
import java.util.List;

public class FoldingProvider
{
	public List<FoldingRange> provideFolding(INaturalModule module)
	{
		if (module instanceof IModuleWithBody withBody && withBody.body() != null)
		{
			var visitor = new FoldingStatementVisitor();
			return visitor.provideFolding(withBody.body());
		}

		return List.of();
	}

	private static class FoldingStatementVisitor implements IStatementVisitor
	{
		private final List<FoldingRange> ranges = new ArrayList<>();

		List<FoldingRange> provideFolding(IStatementListNode body)
		{
			body.acceptStatementVisitor(this);
			return ranges;
		}

		@Override
		public void visit(IStatementNode statement)
		{
			if (statement instanceof ISubroutineNode subroutine)
			{
				ranges.add(defaultStartToLastNodeFolding(subroutine));
				return;
			}

			if (statement instanceof IDecideOnNode decideOn)
			{
				ranges.add(defaultStartToLastNodeFolding(decideOn));
				for (var branch : decideOn.branches())
				{
					ranges.add(defaultStartToLastNodeFolding(branch));
				}
				if (decideOn.allValues() != null)
				{
					ranges.add(defaultStartToLastNodeFolding(decideOn.allValues()));
				}
				if (decideOn.anyValue() != null)
				{
					ranges.add(defaultStartToLastNodeFolding(decideOn.anyValue()));
				}
				ranges.add(defaultStartToLastNodeFolding(decideOn.noneValue()));
				return;
			}

			if (statement instanceof IDecideForConditionNode decideFor)
			{
				ranges.add(defaultStartToLastNodeFolding(decideFor));
				for (var branch : decideFor.branches())
				{
					ranges.add(defaultStartToLastNodeFolding(branch));
				}
				decideFor.whenAll().ifPresent(wa -> ranges.add(defaultStartToLastNodeFolding(wa)));
				decideFor.whenAny().ifPresent(wa -> ranges.add(defaultStartToLastNodeFolding(wa)));
				ranges.add(defaultStartToLastNodeFolding(decideFor.whenNone()));
				return;
			}

			if (statement instanceof IIfStatementNode ifStatement)
			{
				ranges.add(defaultStartToLastNodeFolding(ifStatement));
				if (ifStatement.elseBranch() != null)
				{
					ranges.add(defaultStartToLastNodeFolding(ifStatement));
				}
				return;
			}

			if (statement instanceof IForLoopNode forLoop)
			{
				ranges.add(defaultStartToLastNodeFolding(forLoop));
				return;
			}

			if (statement instanceof IIncludeNode include && include.referencingToken().symbolName().equals("L4NLOGIT"))
			{
				var statements = ((IStatementListNode) statement.parent()).statements();
				var includeIndex = statements.indexOf(include);

				var start = include.position().line();
				var end = include.position().line();
				var range = new FoldingRange();
				if (includeIndex > 0 && statements.get(includeIndex - 1)instanceof ICompressStatementNode compress
					&& compress.intoTarget()instanceof IVariableReferenceNode targetVariable
					&& targetVariable.referencingToken().symbolName().equals("L4N-LOGTEXT"))
				{
					if (includeIndex > 1 && statements.get(includeIndex - 2)instanceof IAssignmentStatementNode assignment
						&& assignment.target()instanceof IVariableReferenceNode assignTarget
						&& assignTarget.referencingToken().symbolName().equals("L4N-LOGLEVEL"))
					{
						start = assignment.position().line();
						if (assignment.operand()instanceof IVariableReferenceNode logLevelVar)
						{
							var logLevelName = logLevelVar.referencingToken().symbolName();
							range.setCollapsedText("%s: %s".formatted(logLevelName.substring(logLevelName.lastIndexOf('-')), compress.operands()));
						}
					}
					else
					{
						start = compress.position().line();
					}
				}

				range.setStartLine(start);
				range.setEndLine(end);
				ranges.add(range);
			}
		}

		private FoldingRange defaultStartToLastNodeFolding(ISyntaxNode node)
		{
			return new FoldingRange(node.position().line(), node.descendants().last().position().line());
		}
	}
}
