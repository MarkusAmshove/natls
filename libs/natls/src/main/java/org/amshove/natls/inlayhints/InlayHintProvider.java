package org.amshove.natls.inlayhints;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.output.IOutputNewLineNode;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

public class InlayHintProvider
{
	public List<InlayHint> provideInlayHints(INaturalModule module, Range range)
	{
		var hints = new ArrayList<InlayHint>();

		module.syntaxTree().acceptNodeVisitor(n ->
		{
			if (!n.isInFile(module.file().getPath()))
			{
				return;
			}

			if (n.diagnosticPosition().line() < range.getStart().getLine() || n.diagnosticPosition()
				.line() > range.getEnd().getLine())
			{
				return;
			}

			if (NaturalLanguageService.getConfig().getInlayhints()
				.isShowAssignmentTargetType() && n instanceof IAssignmentStatementNode assignment && assignment.target()instanceof IVariableReferenceNode reference && reference.reference()instanceof ITypedVariableNode typedRef && typedRef.type() != null)
			{
				var hint = new InlayHint();
				hint.setPosition(LspUtil.toPositionAfter(reference.diagnosticPosition()));
				hint.setLabel(typedRef.formatTypeForDisplay());
				hint.setKind(InlayHintKind.Type);
				hint.setPaddingLeft(true);
				hints.add(hint);
				return;
			}

			if (n instanceof ISubroutineNode subroutineNode)
			{
				var endSubroutine = subroutineNode.findDescendantToken(SyntaxKind.END_SUBROUTINE);
				if (endSubroutine == null)
				{
					return;
				}

				var hint = new InlayHint();
				hint.setPosition(LspUtil.toPositionAfter(endSubroutine.diagnosticPosition()));
				hint.setLabel(subroutineNode.declaration().symbolName());
				hint.setKind(InlayHintKind.Type);
				hint.setPaddingLeft(true);
				hints.add(hint);
				return;
			}

			if (n instanceof IInputStatementNode input)
			{
				addInputLineHints(input, hints);
				return;
			}

			if (n instanceof IInternalPerformNode internalPerform && !internalPerform.reference()
				.isInFile(module.file().getPath()))
			{
				var hint = new InlayHint();
				hint.setPosition(LspUtil.toPositionAfter(internalPerform.position()));
				hint.setLabel(internalPerform.reference().position().fileNameWithoutExtension());
				hint.setKind(InlayHintKind.Type);
				hint.setPaddingLeft(true);
				hints.add(hint);
				return;
			}

			if (n instanceof IModuleReferencingNode moduleReferencingNode)
			{
				addInlayHintsToParameter(moduleReferencingNode, hints);
			}
		});

		return hints;
	}

	private void addInlayHintsToParameter(IModuleReferencingNode moduleReferencingNode, ArrayList<InlayHint> hints)
	{
		if (moduleReferencingNode.reference() == null || moduleReferencingNode.providedParameter().isEmpty())
		{
			return;
		}

		var theirDefineData = moduleReferencingNode.reference()instanceof IHasDefineData hasDD ? hasDD.defineData() : null;

		if (theirDefineData == null)
		{
			return;
		}

		var theirParameterInDeclarationOrder = theirDefineData.declaredParameterInOrder();
		if (theirParameterInDeclarationOrder.isEmpty())
		{
			return;
		}

		var parameterIndex = 0;
		for (var passedParameter : moduleReferencingNode.providedParameter())
		{
			if (passedParameter instanceof ISkipOperandNode skip)
			{
				var numberOfSkippedParameter = skip.skipAmount();

				for (var i = 0; i < numberOfSkippedParameter; i++)
				{
					var skippedParameter = theirParameterInDeclarationOrder.get(parameterIndex + i);
					var skippedParameterName = skippedParameter instanceof IVariableNode variable ? variable.name() : "Unknown";
					var hint = new InlayHint();
					hint.setPosition(LspUtil.toPositionAfter(passedParameter.position()));
					hint.setLabel(skippedParameterName);
					hint.setKind(InlayHintKind.Parameter);
					hint.setPaddingLeft(true);
					hints.add(hint);
				}

				parameterIndex += numberOfSkippedParameter;
				continue;
			}

			parameterIndex++;
		}
	}

	private void addInputLineHints(IInputStatementNode input, ArrayList<InlayHint> hints)
	{
		if (input.operands().isEmpty())
		{
			return;
		}
		// first hind needs special treatment before new lines are parsed as operands on input statements
		var firstLineFirstOperand = input.operands().first();
		var firstLineOperand = new InlayHint();
		firstLineOperand.setPosition(LspUtil.toPosition(firstLineFirstOperand.position()));
		firstLineOperand.setLabel("line %d".formatted(1));
		firstLineOperand.setPaddingRight(true);
		firstLineOperand.setKind(InlayHintKind.Type);
		hints.add(firstLineOperand);

		var lineNo = 1;
		var lastWasNewLine = false;
		for (var operand : input.operands())
		{
			var isNewLineNode = operand instanceof IOutputNewLineNode;

			if (lastWasNewLine)
			{
				lastWasNewLine = false;
				var hint = new InlayHint();
				hint.setPosition(LspUtil.toPosition(operand.position()));
				hint.setLabel("line %d".formatted(lineNo));
				if (isNewLineNode) // If we're in an empty new line, put the hint on the left side
				{
					hint.setPaddingLeft(true);
				}
				else
				{
					hint.setPaddingRight(true);
				}
				hints.add(hint);
				hint.setKind(InlayHintKind.Type);
			}

			if (operand instanceof IOutputNewLineNode)
			{
				lineNo++;
				lastWasNewLine = true;
			}
		}
	}
}
