package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;

public class ValueTruncationAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription VALUE_TRUNCATED = DiagnosticDescription.create(
		"NL021",
		"Value is truncated from %s to %s at runtime. Extend the target variable or remove the truncated parts from this literal.",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(VALUE_TRUNCATED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IAssignmentStatementNode.class, this::analyzeAssign);
		context.registerNodeAnalyzer(ITypedVariableNode.class, this::analyzeInitialValue);
	}

	private void analyzeInitialValue(ISyntaxNode typedVariable, IAnalyzeContext context)
	{
		var typedVar = (ITypedVariableNode) typedVariable;
		var initialNode = typedVar.type().initialValue();
		if (initialNode == null || typedVar.type().hasDynamicLength() || !(initialNode instanceof ILiteralNode || initialNode instanceof IStringConcatOperandNode))
		{
			return;
		}

		var inferredInitialType = initialNode instanceof ILiteralNode literal
			? literal.reInferType(typedVar.type())
			: ((IStringConcatOperandNode) initialNode).inferType();

		checkTruncation(inferredInitialType, typedVar.type(), initialNode, context);
	}

	private void analyzeAssign(ISyntaxNode assignNode, IAnalyzeContext context)
	{
		var assignment = (IAssignmentStatementNode) assignNode;

		if (!(assignment.target()instanceof IVariableReferenceNode targetRef
			&& targetRef.reference()instanceof ITypedVariableNode typedTarget
			&& typedTarget.type() != null))
		{
			return;
		}

		if (assignment.operand()instanceof ILiteralNode literal)
		{
			checkTruncation(literal.reInferType(typedTarget.type()), typedTarget.type(), assignment.operand(), context);
		}
	}

	private void checkTruncation(IDataType operandType, IDataType targetType, ISyntaxNode location, IAnalyzeContext context)
	{
		if (!operandType.hasCompatibleFormat(targetType))
		{
			return;
		}

		if (!operandType.fitsInto(targetType))
		{
			context.report(
				VALUE_TRUNCATED.createFormattedDiagnostic(
					location.diagnosticPosition(),
					operandType.toShortString(),
					targetType.toShortString()
				)
			);
		}
	}
}
