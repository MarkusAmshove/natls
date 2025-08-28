package org.amshove.natls.inlayhints;

import org.amshove.natls.config.IConfigChangedSubscriber;
import org.amshove.natls.config.LSConfiguration;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.output.IOutputNewLineNode;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

public class InlayHintProvider implements IConfigChangedSubscriber
{
	private LSConfiguration config;

	public InlayHintProvider(LSConfiguration config)
	{
		this.config = config;
	}

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

			if (n instanceof IAssignmentStatementNode assignment)
			{
				addTypeHintToAssignment(assignment, hints);
			}

			if (n instanceof ISubroutineNode subroutineNode)
			{
				addInlayHintToEndSubroutine(subroutineNode, hints);
			}

			if (n instanceof IInputStatementNode input)
			{
				addInputLineHints(input, hints);
			}

			if (n instanceof IInternalPerformNode internalPerform && !internalPerform.reference()
				.isInFile(module.file().getPath()))
			{
				addFileNameInlayHintToInternalPerform(internalPerform, hints);
			}

			if (n instanceof IModuleReferencingNode moduleReferencingNode && config.getInlayhints().isShowSkippedParameter())
			{
				addInlayHintsToParameter(moduleReferencingNode, hints);
			}

			if (n instanceof ILabelReferencable labelReferencable)
			{
				addInlayHintsToStatementLabel(labelReferencable, hints);
			}
		});

		return hints;
	}

	private static void addInlayHintsToStatementLabel(ILabelReferencable labelReferencable, ArrayList<InlayHint> hints)
	{
		var label = labelReferencable.labelIdentifier();
		if (label == null)
		{
			return;
		}

		if (labelReferencable instanceof IStatementWithBodyNode statement)
		{
			var endKeyword = statement.descendants().last();
			var hint = new InlayHint();
			hint.setPosition(LspUtil.toPositionAfter(endKeyword.position()));
			hint.setLabel(label.source());
			hint.setKind(InlayHintKind.Type);
			hint.setPaddingLeft(true);
			hints.add(hint);
		}
	}

	private static void addFileNameInlayHintToInternalPerform(IInternalPerformNode internalPerform, ArrayList<InlayHint> hints)
	{
		var hint = new InlayHint();
		hint.setPosition(LspUtil.toPositionAfter(internalPerform.position()));
		hint.setLabel(internalPerform.reference().position().fileNameWithoutExtension());
		hint.setKind(InlayHintKind.Type);
		hint.setPaddingLeft(true);
		hints.add(hint);
	}

	private void addInlayHintToEndSubroutine(ISubroutineNode subroutineNode, ArrayList<InlayHint> hints)
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
	}

	private void addTypeHintToAssignment(IAssignmentStatementNode assignment, ArrayList<InlayHint> hints)
	{
		if (!config.getInlayhints().isShowAssignmentTargetType())
		{
			return;
		}

		if (!(assignment.target()instanceof IVariableReferenceNode variableReference))
		{
			return;
		}

		if (!(variableReference.reference()instanceof ITypedVariableNode typedRef))
		{
			return;
		}

		if (typedRef.type() == null)
		{
			return;
		}

		var hint = new InlayHint();
		hint.setPosition(LspUtil.toPositionAfter(variableReference.diagnosticPosition()));
		hint.setLabel(typedRef.formatTypeForDisplay());
		hint.setKind(InlayHintKind.Type);
		hint.setPaddingLeft(true);
		hints.add(hint);
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
		// first hint needs special treatment before new lines are parsed as operands on input statements
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

	@Override
	public void configChanged(LSConfiguration newConfig)
	{
		config = newConfig;
	}
}
