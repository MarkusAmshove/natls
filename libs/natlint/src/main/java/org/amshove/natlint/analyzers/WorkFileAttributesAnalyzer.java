package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDefineWorkFileNode;
import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.ISyntaxNode;

import java.util.Arrays;
import java.util.List;

public class WorkFileAttributesAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription MULTIPLE_ATTRIBUTES_OF_SAME_TYPE = DiagnosticDescription.create(
		"NL014",
		"Multiple attributes of the same type are specified: %s and %s. Only the last one specified takes effect. Remove one to prevent confusion.",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(MULTIPLE_ATTRIBUTES_OF_SAME_TYPE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IDefineWorkFileNode.class, this::analyzeWork);
	}

	private static final List<AttributeTypePair> PAIRS = List.of(
		new AttributeTypePair("NOAPPEND", "APPEND"),
		new AttributeTypePair("DELETE", "KEEP"),
		new AttributeTypePair("BOM", "NOBOM"),
		new AttributeTypePair("KEEPCR", "REMOVECR")
	);

	private void analyzeWork(ISyntaxNode node, IAnalyzeContext context)
	{
		var work = (IDefineWorkFileNode) node;

		if (!(work.attributes()instanceof ILiteralNode literal))
		{
			return;
		}

		var attributeLiteral = literal.token().stringValue();
		var separator = attributeLiteral.contains(",")
			? ","
			: " ";

		var specifiedAttributes = Arrays.stream(attributeLiteral.split(separator)).map(String::trim).toList();

		for (var typePair : PAIRS)
		{
			if (specifiedAttributes.contains(typePair.firstValue) && specifiedAttributes.contains(typePair.secondValue))
			{
				context.report(
					MULTIPLE_ATTRIBUTES_OF_SAME_TYPE.createFormattedDiagnostic(
						work.attributes().position(),
						typePair.firstValue,
						typePair.secondValue
					)
				);
			}
		}
	}

	private record AttributeTypePair(String firstValue, String secondValue)
	{}
}
