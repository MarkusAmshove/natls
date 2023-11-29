package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDefinePrototypeNode;
import org.amshove.natparse.natural.ISyntaxNode;

public class DefinePrototypeAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription PROTOTYPE_DEFINED_MORE_THAN_ONCE = DiagnosticDescription.create(
		"NL022",
		"Prototype for function %s is defined more than once",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(PROTOTYPE_DEFINED_MORE_THAN_ONCE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IDefinePrototypeNode.class, this::analyzeDefinePrototype);
	}

	private void analyzeDefinePrototype(ISyntaxNode node, IAnalyzeContext context)
	{
		var prototype = ((IDefinePrototypeNode) node);
		var prototypesInModule = NodeUtil.findNodesOfType(context.getModule().syntaxTree(), IDefinePrototypeNode.class);

		for (var otherPrototype : prototypesInModule)
		{
			if (otherPrototype == prototype)
			{
				continue;
			}

			if (otherPrototype.nameToken().symbolName().equals(prototype.nameToken().symbolName()))
			{
				context.report(
					PROTOTYPE_DEFINED_MORE_THAN_ONCE.createFormattedDiagnostic(
						otherPrototype.nameToken(),
						otherPrototype.nameToken().symbolName()
					)
				);
			}
		}
	}
}
