package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;

import java.util.ArrayList;
import java.util.List;

public class ExternalParameterCheck
{
	public static void performParameterCheck(NaturalModule naturalModule)
	{
		naturalModule.syntaxTree().acceptNodeVisitor((node) ->
		{
			if (!(node instanceof IModuleReferencingNode moduleRef))
			{
				return;
			}

			if (node instanceof IIncludeNode)
			{
				// TODO: Includes only take string literals, this can be handled in
				// StatementListParser
				return;
			}

			if (node instanceof IUsingNode)
			{
				return;
			}

			if (moduleRef.reference() == null)
			{
				// Unresolved external module
				return;
			}

			var theirDefineData = moduleRef.reference()instanceof IHasDefineData hasDD ? hasDD.defineData() : null;

			var numberOfPassedParameter = moduleRef.providedParameter().size();
			if (theirDefineData == null)
			{
				if (numberOfPassedParameter > 0)
				{
					naturalModule.addDiagnostic(ParserErrors.parameterCountMismatch(node, numberOfPassedParameter, 0));
				}
				return;
			}

			var expectedParameters = theirDefineData.effectiveParameterInOrder();
			var passedParameters = flatternProvidedParameter(moduleRef.providedParameter());
			var upperbound = Math.max(expectedParameters.size(), passedParameters.size());

			for (var i = 0; i < upperbound; i++)
			{
				var passedParameter = i < passedParameters.size() ? passedParameters.get(i) : null;
				var expectedParameter = i < expectedParameters.size() ? expectedParameters.get(i) : null;

				if (passedParameter != null && expectedParameter == null)
				{
					naturalModule.addDiagnostic(ParserErrors.leftOverParameter(passedParameters.get(i).position(), expectedParameters.size()));
					continue;
				}

				if (passedParameter == null && expectedParameter != null && expectedParameter.findDescendantToken(SyntaxKind.OPTIONAL) == null)
				{
					naturalModule.addDiagnostic(ParserErrors.missingParameter(moduleRef, expectedParameter));
					continue;
				}
			}
		});
	}

	private static List<ProvidedParameter> flatternProvidedParameter(ReadOnlyList<IOperandNode> providedParameter)
	{
		var flattenedParameter = new ArrayList<ProvidedParameter>(providedParameter.size());
		for (var parameter : providedParameter)
		{
			if (parameter instanceof IVariableReferenceNode refNode)
			{
				if (refNode.reference()instanceof IGroupNode group)
				{
					for (var variable : group.flattenVariables())
					{
						flattenedParameter.add(new ProvidedVariable((ITypedVariableNode) variable, refNode)); // TODO: They should all be typed. Right?...
					}
				}
				else
				{
					flattenedParameter.add(new ProvidedVariable((ITypedVariableNode) refNode.reference(), refNode));
				}
			}
			else
			{
				flattenedParameter.add(new ProvidedOperand(parameter));
			}
		}
		return flattenedParameter;
	}

	private sealed interface ProvidedParameter permits ProvidedVariable,ProvidedOperand
	{
		ISyntaxNode position();
	}

	private record ProvidedVariable(ITypedVariableNode variable, IVariableReferenceNode position) implements ProvidedParameter
	{}

	private record ProvidedOperand(IOperandNode operand) implements ProvidedParameter
	{
		@Override
		public ISyntaxNode position()
		{
			return operand;
		}
	}
}
