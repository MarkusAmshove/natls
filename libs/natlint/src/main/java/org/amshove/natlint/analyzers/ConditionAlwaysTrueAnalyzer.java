package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.amshove.natparse.natural.conditionals.ISpecifiedCriteriaNode;

public class ConditionAlwaysTrueAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription CONDITION_ALWAYS_TRUE = DiagnosticDescription.create(
		"NL023",
		"Condition is always true: %s",
		DiagnosticSeverity.INFO
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(CONDITION_ALWAYS_TRUE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(ISpecifiedCriteriaNode.class, this::analyzeIfSpecified);
	}

	private void analyzeIfSpecified(ISyntaxNode node, IAnalyzeContext context)
	{
		var specified = (ISpecifiedCriteriaNode) node;

		if (!(specified.operand()instanceof IVariableReferenceNode variableRef) || variableRef.reference() == null)
		{
			// not resolved
			return;
		}

		if (variableRef.reference().findDescendantToken(SyntaxKind.OPTIONAL) == null)
		{
			context.report(
				CONDITION_ALWAYS_TRUE.createFormattedDiagnostic(
					specified.operand().diagnosticPosition(),
					"Variable %s is not declared OPTIONAL".formatted(variableRef.referencingToken().symbolName())
				)
			);
		}
	}
}
