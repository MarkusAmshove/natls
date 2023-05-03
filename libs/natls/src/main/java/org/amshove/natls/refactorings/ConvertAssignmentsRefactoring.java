package org.amshove.natls.refactorings;

import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.codeactions.RefactoringContext;
import org.amshove.natls.quickfixes.CodeActionBuilder;
import org.amshove.natparse.natural.IAssignStatementNode;
import org.amshove.natparse.natural.IComputeStatementNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

import java.util.List;

import static org.amshove.natls.SourceExtractor.extractSource;

public class ConvertAssignmentsRefactoring implements ICodeActionProvider
{
	@Override
	public boolean isApplicable(RefactoringContext context)
	{
		return (context.nodeAtPosition()instanceof IAssignStatementNode assign && !assign.isRounded())
			|| (context.nodeAtPosition()instanceof IComputeStatementNode compute && !compute.isRounded());
	}

	@Override
	public List<CodeAction> createCodeAction(RefactoringContext context)
	{
		var targetAndOperand = extractTargetAndOperand(context.nodeAtPosition());
		return List.of(
			new CodeActionBuilder("Convert to assignment", CodeActionKind.RefactorRewrite)
				.appliesWorkspaceEdit(
					new WorkspaceEditBuilder()
						.changesNode(
							context.nodeAtPosition(),
							"%s := %s".formatted(targetAndOperand.targetSource, targetAndOperand.operandSource)
						)
				)
				.build()
		);
	}

	private TargetAndOperand extractTargetAndOperand(ISyntaxNode node)
	{
		if (node instanceof IAssignStatementNode assign)
		{
			return new TargetAndOperand(extractSource(assign.target()), extractSource(assign.operand()));
		}

		if (node instanceof IComputeStatementNode compute)
		{
			return new TargetAndOperand(extractSource(compute.target()), extractSource(compute.operand()));
		}

		throw new IllegalStateException("unreachable");
	}

	private record TargetAndOperand(String targetSource, String operandSource)
	{}

}
