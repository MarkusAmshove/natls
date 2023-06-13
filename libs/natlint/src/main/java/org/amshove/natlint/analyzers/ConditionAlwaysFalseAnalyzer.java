package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;

public class ConditionAlwaysFalseAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription CONDITION_ALWAYS_FALSE = DiagnosticDescription.create(
		"NL017",
		"Condition is always false: %s",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(CONDITION_ALWAYS_FALSE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IDecideOnBranchNode.class, this::analyzeDecideBranch);
	}

	private void analyzeDecideBranch(ISyntaxNode node, IAnalyzeContext context)
	{
		var branch = (IDecideOnBranchNode) node;
		var decide = ((IDecideOnNode) branch.parent());

		if (!(decide.operand()instanceof IVariableReferenceNode varRef)
			|| !(varRef.reference()instanceof ITypedVariableNode typedTarget)
			|| typedTarget.type() == null)
		{
			return;
		}

		for (var value : branch.values())
		{
			if (!(value instanceof ILiteralNode literal))
			{
				continue;
			}

			checkLiteralType(context, typedTarget, literal);
		}
	}

	private static void checkLiteralType(IAnalyzeContext context, ITypedVariableNode typedTarget, ILiteralNode literal)
	{
		var inferredType = literal.inferType(typedTarget.type().format());
		if (!inferredType.fitsInto(typedTarget.type()))
		{
			context.report(
				CONDITION_ALWAYS_FALSE.createFormattedDiagnostic(
					literal.position(), "Inferred type %s does not fit into %s %s"
						.formatted(
							inferredType.toShortString(),
							typedTarget.declaration().symbolName(),
							typedTarget.type().toShortString()
						)
				)
			);
		}
	}
}
