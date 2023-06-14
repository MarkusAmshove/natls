package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITypedVariableNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

public class QualifiedVariableAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription VARIABLE_SHOULD_BE_QUALIFIED = DiagnosticDescription.create(
		"NL018",
		"Variable %s should be qualified, change it to %s",
		DiagnosticSeverity.INFO
	);

	public static final DiagnosticDescription LEVEL_1_TYPED_VARIABLES_IS_DISCOURAGED = DiagnosticDescription.create(
		"NL019",
		"The typed variable %s should not be defined at level 1, add a group for typed variables",
		DiagnosticSeverity.INFO
	);

	private boolean isQualifiedVarsOff;
	private boolean isLevel1VarsOff;

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(VARIABLE_SHOULD_BE_QUALIFIED, LEVEL_1_TYPED_VARIABLES_IS_DISCOURAGED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IVariableReferenceNode.class, this::analyzeQualifiedVariable);
		context.registerNodeAnalyzer(ITypedVariableNode.class, this::analyzeLevel1Variable);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		isQualifiedVarsOff = !context.getConfiguration(context.getModule().file(), "natls.style.qualifyvars", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
		isLevel1VarsOff = !context.getConfiguration(context.getModule().file(), "natls.style.disallowtoplevelvars", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
	}

	private void analyzeQualifiedVariable(ISyntaxNode node, IAnalyzeContext context)
	{
		if (isQualifiedVarsOff)
		{
			return;
		}

		if (UNWANTED_FILETYPES.contains(context.getModule().file().getFiletype()))
		{
			return;
		}

		if (!NodeUtil.moduleContainsNode(context.getModule(), node))
		{
			return;
		}

		var variable = (IVariableReferenceNode) node;
		if (variable.reference()instanceof ITypedVariableNode typedVariable && !typedVariable.qualifiedName().equals(variable.referencingToken().symbolName()))
		{
			context.report(VARIABLE_SHOULD_BE_QUALIFIED.createFormattedDiagnostic(variable.referencingToken(), variable.referencingToken().source(), typedVariable.qualifiedName()));
		}
	}

	private void analyzeLevel1Variable(ISyntaxNode node, IAnalyzeContext context)
	{
		if (isLevel1VarsOff)
		{
			return;
		}

		if (UNWANTED_FILETYPES.contains(context.getModule().file().getFiletype()))
		{
			return;
		}

		if (!NodeUtil.moduleContainsNode(context.getModule(), node))
		{
			return;
		}

		var variable = (ITypedVariableNode) node;
		if (variable.level() == 1)
		{
			context.report(LEVEL_1_TYPED_VARIABLES_IS_DISCOURAGED.createFormattedDiagnostic(variable.declaration(), variable.name()));
		}
	}
}
