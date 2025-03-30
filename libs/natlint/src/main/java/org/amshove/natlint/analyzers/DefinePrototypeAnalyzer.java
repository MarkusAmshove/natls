package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.MultiValueMap;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDefinePrototypeNode;
import org.amshove.natparse.natural.IFunctionCallNode;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.project.NaturalFileType;

public class DefinePrototypeAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription PROTOTYPE_DEFINED_MORE_THAN_ONCE = DiagnosticDescription.create(
		"NL022",
		"Prototype for function %s is defined more than once",
		DiagnosticSeverity.WARNING
	);

	public static final DiagnosticDescription PROTOTYPE_UNDEFINED = DiagnosticDescription.create(
		"NL036",
		"Called function %s has no prototype definition. This may lead to compile errors",
		DiagnosticSeverity.INFO
	);

	public static final DiagnosticDescription PROTOTYPE_UNUSED = DiagnosticDescription.create(
		"NL037",
		"Prototype %s is defined but not used",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(PROTOTYPE_DEFINED_MORE_THAN_ONCE, PROTOTYPE_UNDEFINED, PROTOTYPE_UNUSED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IDefinePrototypeNode.class, this::analyzeDefinePrototype);
		context.registerModuleAnalyzer(this::analyzeFunction);
	}

	private void analyzeFunction(INaturalModule module, IAnalyzeContext context)
	{
		if (module.file().getFiletype() != NaturalFileType.FUNCTION)
		{
			return;
		}

		var definedPrototypes = new MultiValueMap<String, IDefinePrototypeNode>();
		var calledFunctions = new MultiValueMap<String, IFunctionCallNode>();
		module.syntaxTree().acceptNodeVisitor(node ->
		{
			if (node instanceof IDefinePrototypeNode definePrototype)
			{
				definedPrototypes.put(definePrototype.nameToken().symbolName(), definePrototype);
				return;
			}

			if (node instanceof IFunctionCallNode functionCall)
			{
				calledFunctions.put(functionCall.referencingToken().symbolName(), functionCall);
			}
		});

		for (var function : calledFunctions.keys())
		{
			if (!definedPrototypes.containsKey(function))
			{
				for (var functionCall : calledFunctions.get(function))
				{
					context.report(
						PROTOTYPE_UNDEFINED.createFormattedDiagnostic(
							functionCall.referencingToken().diagnosticPosition(),
							function
						)
					);
				}
			}
		}

		for (var prototype : definedPrototypes.keys())
		{
			if (!calledFunctions.containsKey(prototype))
			{
				for (var definedPrototype : definedPrototypes.get(prototype))
				{
					context.report(
						PROTOTYPE_UNUSED.createFormattedDiagnostic(
							definedPrototype.nameToken().diagnosticPosition(),
							prototype
						)
					);
				}
			}
		}
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
