package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;

public class CompressNumericAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription COMPRESS_SHOULD_HAVE_NUMERIC = DiagnosticDescription.create(
		"NL012",
		"This COMPRESS statement uses floating point numbers. Did you forget to add NUMERIC?",
		DiagnosticSeverity.INFO
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(COMPRESS_SHOULD_HAVE_NUMERIC);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(ICompressStatementNode.class, this::analyzeCompress);
	}

	private void analyzeCompress(ISyntaxNode node, IAnalyzeContext context)
	{
		var compress = (ICompressStatementNode) node;

		if (compress.isNumeric())
		{
			return;
		}

		for (var operand : compress.operands())
		{
			if (operand instanceof IVariableReferenceNode reference)
			{
				if (reference.reference()instanceof ITypedVariableNode typedVariable)
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
