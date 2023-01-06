package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.ForLoopAnalyzer;
import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.natural.IForLoopNode;
import org.amshove.natparse.natural.ISystemFunctionNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.amshove.natparse.natural.VariableScope;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

public class ForLoopOccQuickFix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(ForLoopAnalyzer.UPPER_BOUND_OCC, this::fixUpperBoundOcc);
	}

	private CodeAction fixUpperBoundOcc(QuickFixContext quickFixContext)
	{
		var systemFunctionNode = (ISystemFunctionNode) quickFixContext.nodeAtPosition();
		var forLoop = (IForLoopNode) systemFunctionNode.parent();
		var operand = systemFunctionNode.parameter().first();
		if (!(operand instanceof IVariableReferenceNode variableReferenceNode))
		{
			return null;
		}

		var targetVariableName = variableReferenceNode.reference().declaration().symbolName();
		var sizeVariableName = "#S-%s".formatted(targetVariableName);
		return new CodeActionBuilder("Use a variable for the upper bound", CodeActionKind.QuickFix)
			.fixesDiagnostic(quickFixContext.diagnostic())
			.appliesWorkspaceEdit(
				new WorkspaceEditBuilder()
					.addsVariable(quickFixContext.file(), sizeVariableName, "(I4)", VariableScope.LOCAL)
					.changesNode(systemFunctionNode, sizeVariableName)
					.changesText(
						quickFixContext.fileUri(),
						LspUtil.toSingleRange(forLoop.position().line(), 0),
						"%s := *OCC(%s)%n".formatted(sizeVariableName, targetVariableName)
					)
			)
			.build();
	}
}
