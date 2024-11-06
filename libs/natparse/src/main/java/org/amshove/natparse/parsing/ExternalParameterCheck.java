package org.amshove.natparse.parsing;

import org.amshove.natparse.NodeUtil;
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

			if (node instanceof IIncludeNode || node instanceof IUsingNode)
			{
				// USINGs have no parameter and INCLUDE validation is done in StatementListParser
				return;
			}

			if (moduleRef.reference() == null)
			{
				// Unresolved external module
				return;
			}

			var theirDefineData = moduleRef.reference() instanceof IHasDefineData hasDD ? hasDD.defineData() : null;

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
			var passedParameters = flattenProvidedParameter(moduleRef.providedParameter());
			var upperbound = Math.max(expectedParameters.size(), passedParameters.size());

			for (var i = 0; i < upperbound; i++)
			{
				var passedParameter = i < passedParameters.size() ? passedParameters.get(i) : null;
				var expectedParameter = i < expectedParameters.size() ? expectedParameters.get(i) : null;

				// Make flow analysis happy
				if (passedParameter == null && expectedParameter == null)
				{
					continue;
				}

				if (passedParameter != null && expectedParameter == null)
				{
					naturalModule.addDiagnostic(ParserErrors.trailingParameter(passedParameters.get(i).usagePosition(),
						passedParameter.usagePosition(), i + 1, expectedParameters.size()));
					return;
				}

				var expectedParameterIsOptional = expectedParameter.findDescendantToken(SyntaxKind.OPTIONAL) != null;
				if (passedParameter == null && !expectedParameterIsOptional)
				{
					naturalModule.addDiagnostic(ParserErrors.missingParameter(moduleRef, expectedParameter));
					return;
				}

				if (passedParameter == null || expectedParameterIsOptional)
				{
					continue;
				}

				if (passedParameter instanceof ProvidedVariable providedVar && (providedVar.variable() == null || providedVar.variable()
					.type() == null))
				{
					// Passed parameter is not resolvable. This is already handled by a different diagnostic.
					continue;
				}

				if (passedParameter instanceof ProvidedOperand providedOperand)
				{
					if (providedOperand.operand instanceof ISkipOperandNode skipOperand && !expectedParameterIsOptional)
					{
						naturalModule.addDiagnostic(ParserErrors.cantSkipParameter(skipOperand, expectedParameter));
						return;
					}

					if (providedOperand.operand() instanceof ILiteralNode literal)
					{
						typeCheckParameter(naturalModule, passedParameter,
							literal.reInferType(expectedParameter.type()), expectedParameter);
						continue;
					}

					var passedType = TypeInference.inferType(providedOperand.operand());
					if (passedType.isEmpty())
					{
						continue;
					}

					typeCheckParameter(naturalModule, passedParameter, passedType.get(), expectedParameter);
				}
				else
				{
					typeCheckParameter(naturalModule, passedParameter,
						((ProvidedVariable) passedParameter).variable().type(), expectedParameter);
				}
			}
		});
	}

	private static void typeCheckParameter(NaturalModule module, ProvidedParameter providedParameter,
		IDataType passedType, ITypedVariableNode receiver)
	{
		var receiverType = receiver.type();
		var expectedParameterIsByValue = receiver.findDescendantToken(SyntaxKind.VALUE) != null;
		var expectedParameterIsByReference = !expectedParameterIsByValue;

		if (!receiverType.fitsInto(passedType) && expectedParameterIsByReference)
		{
			module.addDiagnostic(
				ParserErrors.parameterTypeMismatch(
					providedParameter.usagePosition(),
					providedParameter.declarationPosition(), passedType, receiver
				)
			);
			return;
		}

		if (!passedType.fitsInto(receiverType) && expectedParameterIsByReference)
		{
			module.addDiagnostic(
				ParserErrors.parameterTypeMismatch(
					providedParameter.usagePosition(),
					providedParameter.declarationPosition(), passedType, receiver
				)
			);
		}
	}

	private static List<ProvidedParameter> flattenProvidedParameter(ReadOnlyList<IOperandNode> providedParameter)
	{
		var flattenedParameter = new ArrayList<ProvidedParameter>(providedParameter.size());
		for (var parameter : providedParameter)
		{
			if (parameter instanceof IVariableReferenceNode refNode)
			{
				if (refNode.reference() instanceof IGroupNode group && !(group instanceof IRedefinitionNode))
				{
					for (var variable : group.flattenVariables())
					{
						// REDEFINEs and their member are not parameter themselves
						if (variable instanceof IRedefinitionNode || NodeUtil.findFirstParentOfType(variable,
							IRedefinitionNode.class) != null)
						{
							continue;
						}

						flattenedParameter.add(new ProvidedVariable((ITypedVariableNode) variable, variable, refNode));
					}
				}
				else
				{
					flattenedParameter.add(
						new ProvidedVariable((ITypedVariableNode) refNode.reference(), refNode.reference(), refNode));
				}
			}
			else
			{
				flattenedParameter.add(new ProvidedOperand(parameter));
			}
		}
		return flattenedParameter;
	}

	private sealed interface ProvidedParameter permits ProvidedVariable, ProvidedOperand
	{
		/**
		 * Where the passed parameter is used on the calling side (the parameter list to the module)
		 */
		ISyntaxNode usagePosition();

		/**
		 * Where the parameter is declared precisely (e.g. differs from usagePosition() when usagePosition() points to a
		 * passed group)
		 */
		ISyntaxNode declarationPosition();
	}

	private record ProvidedVariable(ITypedVariableNode variable, ISyntaxNode declarationPosition,
									IVariableReferenceNode usagePosition) implements ProvidedParameter
	{
	}

	private record ProvidedOperand(IOperandNode operand) implements ProvidedParameter
	{
		@Override
		public ISyntaxNode usagePosition()
		{
			return operand;
		}

		@Override
		public ISyntaxNode declarationPosition()
		{
			return operand;
		}
	}
}
