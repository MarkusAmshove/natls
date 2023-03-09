package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;

public class CompressAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription COMPRESS_SHOULD_HAVE_NUMERIC = DiagnosticDescription.create(
		"NL012",
		"This COMPRESS statement uses floating point numbers. Did you forget to add NUMERIC?",
		DiagnosticSeverity.INFO
	);

	public static final DiagnosticDescription COMPRESS_SHOULD_HAVE_ALL_DELIMITERS = DiagnosticDescription.create(
		"NL013",
		"This COMPRESS looks like it's building CSV. You might want to add ALL to delimiters to include empty fields",
		DiagnosticSeverity.INFO
	);

	public static final DiagnosticDescription COMPRESS_SHOULD_HAVE_LEAVING_NO = DiagnosticDescription.create(
		"NL015",
		"%s is used as a path for a work file and this COMPRESS leaves space between the operands. Did you mean to add LEAVING NO SPACE?",
		DiagnosticSeverity.INFO
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(COMPRESS_SHOULD_HAVE_NUMERIC, COMPRESS_SHOULD_HAVE_ALL_DELIMITERS, COMPRESS_SHOULD_HAVE_LEAVING_NO);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(ICompressStatementNode.class, this::analyzeCompress);
	}

	private void analyzeCompress(ISyntaxNode node, IAnalyzeContext context)
	{
		var compress = (ICompressStatementNode) node;

		if (!compress.isNumeric())
		{
			checkIfNumericShouldBeApplied(context, compress);
		}

		if (compress.isWithDelimiters() && !compress.isWithAllDelimiters())
		{
			checkIfCompressShouldHaveAllDelimiters(context, compress);
		}

		if (compress.isLeavingSpace()
			&& compress.operands().size() > 1
			&& compress.intoTarget() instanceof IVariableReferenceNode variableReference)
		{
			checkIfIntoTargetIsUsedAsWorkfilePath(context, variableReference);
		}
	}

	private void checkIfIntoTargetIsUsedAsWorkfilePath(IAnalyzeContext context, IVariableReferenceNode variableReference)
	{
		if (variableReference.reference() == null)
		{
			return;
		}

		for (var referenceToVariable : variableReference.reference().references())
		{
			if (referenceToVariable.parent() instanceof IDefineWorkFileNode workfileNode
				&& workfileNode.path() instanceof IVariableReferenceNode pathReference
				&& pathReference.reference() == variableReference.reference())
			{
				context.report(COMPRESS_SHOULD_HAVE_LEAVING_NO.createFormattedDiagnostic(variableReference.referencingToken(), variableReference.referencingToken().source()));
			}
		}
	}

	private void checkIfCompressShouldHaveAllDelimiters(IAnalyzeContext context, ICompressStatementNode compress)
	{
		if (compress.delimiter()instanceof ILiteralNode literalNode && literalNode.token().kind() == SyntaxKind.STRING_LITERAL && literalNode.token().stringValue().equals(";"))
		{
			context.report(COMPRESS_SHOULD_HAVE_ALL_DELIMITERS.createDiagnostic(compress));
		}
	}

	private static void checkIfNumericShouldBeApplied(IAnalyzeContext context, ICompressStatementNode compress)
	{
		for (var operand : compress.operands())
		{
			if (operand instanceof IVariableReferenceNode reference)
			{
				if (reference.reference()instanceof ITypedVariableNode typedVariable
					&& typedVariable.type() != null) // TODO: this is only null when the variable is from a DDM. This should not happen
				{
					if (typedVariable.type().length() % 1 != 0 || typedVariable.type().format() == DataFormat.FLOAT)
					{
						context.report(COMPRESS_SHOULD_HAVE_NUMERIC.createDiagnostic(compress));
						return;
					}
				}
			}
		}
	}
}
