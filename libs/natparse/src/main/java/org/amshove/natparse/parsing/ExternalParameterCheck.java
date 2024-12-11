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
					naturalModule.addDiagnostic(
						ParserErrors.trailingParameter(
							passedParameters.get(i).usagePosition(),
							passedParameter.usagePosition(), i + 1, expectedParameters.size()
						)
					);
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

				if (passedParameter instanceof ProvidedOperand(var operand))
				{
					if (operand instanceof ISkipOperandNode skipOperand)
					{
						naturalModule.addDiagnostic(ParserErrors.cantSkipParameter(skipOperand, expectedParameter));
						return;
					}

					if (operand instanceof ILiteralNode literal)
					{
						typeCheckParameter(
							naturalModule, passedParameter,
							literal.reInferType(expectedParameter.type()), expectedParameter
						);
						continue;
					}

					var passedType = TypeInference.inferType(operand);
					if (passedType.isEmpty())
					{
						continue;
					}

					typeCheckParameter(naturalModule, passedParameter, passedType.get(), expectedParameter);
				}
				else
				{
					typeCheckParameter(
						naturalModule, passedParameter,
						((ProvidedVariable) passedParameter).variable().type(), expectedParameter
					);
				}
			}
		});
	}

	private static void typeCheckParameter(
		NaturalModule module, ProvidedParameter providedParameter,
		IDataType passedType, ITypedVariableNode receiver
	)
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

		checkArrayDimensions(module, providedParameter, receiver);
	}

	private static void checkArrayDimensions(
		NaturalModule module,
		ProvidedParameter providedParameter,
		ITypedVariableNode receiver
	)
	{
		if (providedParameter.isPassedAsGroupMember())
		{
			// When not this variable itself is passed as parameter, but the group that contains it
			// then we don't have to check for the access to the array (e.g. #ARR(*)) but the declaration on both sides
			// in the DEFINE DATA.
			checkArrayDeclarationDimensions(module, providedParameter, receiver);
			return;
		}

		var expectedDimensions = receiver.dimensions();
		var numberOfExpectedDimensions = expectedDimensions.size();

		var passedDimensions = providedParameter.passedDimensions();
		var numberOfPassedDimensions = passedDimensions.size();

		// Fewer dimensions passed than expected
		if (numberOfPassedDimensions == 0 && numberOfExpectedDimensions > 0)
		{
			module.addDiagnostic(
				ParserErrors.passedParameterNotArray(
					providedParameter.usagePosition(), numberOfExpectedDimensions, numberOfPassedDimensions, receiver,
					providedParameter.declarationPosition()
				)
			);
		}

		for (int i = 0; i < passedDimensions.size(); i++)
		{
			var passedDimension = passedDimensions.get(i);
			if (!(passedDimension instanceof IRangedArrayAccessNode rangedArrayAccessNode))
			{
				continue;
			}

			// More dimensions passed than expected
			if (i > numberOfExpectedDimensions - 1)
			{
				module.addDiagnostic(
					ParserErrors.passedParameterNotArray(
						providedParameter.usagePosition(), numberOfExpectedDimensions, numberOfPassedDimensions,
						receiver,
						providedParameter.declarationPosition()
					)
				);
				return;
			}

			var declaredVariable = ((ProvidedVariable) providedParameter).variable;
			if (declaredVariable.dimensions().size() < i - 1)
			{
				// When we try to pass more dimension accesses than the variable
				// is declared with, the error gets caught while parsing.
				continue;
			}

			var declaredDimension = declaredVariable.dimensions().get(i);
			var expectedDimension = expectedDimensions.get(i);
			// If the receiving parameter is an XArray, we can't do a static check
			if (expectedDimension.isLowerUnbound() || expectedDimension.isUpperUnbound())
			{
				continue;
			}

			// We're passing part of the whole dimension (e.g. * or 1:*), so we can check
			if (rangedArrayAccessNode.isAnyUnbound())
			{
				if (declaredDimension.isLowerUnbound() || declaredDimension.isUpperUnbound() || expectedDimension.isLowerUnbound() || expectedDimension.isUpperUnbound() || expectedDimension.isUpperVariable())
				{
					continue;
				}

				if (declaredDimension.lowerBound() != expectedDimension.lowerBound() || declaredDimension.upperBound() != expectedDimension.upperBound())
				{
					module.addDiagnostic(
						ParserErrors.parameterDimensionLengthMismatch(
							providedParameter.usagePosition(),
							i + 1,
							expectedDimension,
							declaredDimension,
							receiver,
							providedParameter.declarationPosition()
						)
					);
				}
			}
		}
	}

	private static void checkArrayDeclarationDimensions(
		NaturalModule module, ProvidedParameter providedParameter,
		ITypedVariableNode receiver
	)
	{
		if (!(providedParameter instanceof ProvidedVariable providedVariable))
		{
			return;
		}

		var expectedDimensions = receiver.dimensions();
		var providedDeclaredDimensions = providedVariable.variable().dimensions();

		var numberOfExpectedDimensions = expectedDimensions.size();
		var numberOfPassedDimensions = providedDeclaredDimensions.size();

		// Fewer dimensions passed than expected
		if (numberOfPassedDimensions != numberOfExpectedDimensions)
		{
			module.addDiagnostic(
				ParserErrors.passedParameterNotArray(
					providedParameter.usagePosition(), numberOfExpectedDimensions, numberOfPassedDimensions, receiver,
					providedParameter.declarationPosition()
				)
			);
			return;
		}

		for (int i = 0; i < numberOfExpectedDimensions; i++)
		{
			var expectedDimension = expectedDimensions.get(i);
			var providedDimension = providedDeclaredDimensions.get(i);

			var expectedIsXArray = expectedDimension.isLowerUnbound() || expectedDimension.isUpperUnbound() || expectedDimension.isUpperVariable();
			var providedIsXArray = providedDimension.isLowerUnbound() || providedDimension.isUpperUnbound() || providedDimension.isUpperVariable();

			if (expectedIsXArray || providedIsXArray)
			{
				continue;
			}

			if (providedDimension.lowerBound() != expectedDimension.lowerBound() || providedDimension.upperBound() != expectedDimension.upperBound())
			{
				module.addDiagnostic(
					ParserErrors.parameterDimensionLengthMismatch(
						providedParameter.usagePosition(),
						i + 1,
						expectedDimension,
						providedDimension,
						receiver,
						providedParameter.declarationPosition()
					)
				);
			}
		}
	}

	private static List<ProvidedParameter> flattenProvidedParameter(ReadOnlyList<IOperandNode> providedParameter)
	{
		var flattenedParameter = new ArrayList<ProvidedParameter>(providedParameter.size());
		for (var parameter : providedParameter)
		{
			if (parameter instanceof IVariableReferenceNode refNode)
			{
				if (refNode.reference()instanceof IGroupNode group && !(group instanceof IRedefinitionNode))
				{
					addAllGroupMemberAsParameter(group, refNode, flattenedParameter);
				}
				else
				{
					flattenedParameter.add(createPlainVariable(refNode));
				}
			}
			else
			{
				flattenedParameter.add(new ProvidedOperand(parameter));
			}
		}
		return flattenedParameter;
	}

	private static ProvidedVariable createPlainVariable(IVariableReferenceNode variableReference)
	{
		return new ProvidedVariable(
			(ITypedVariableNode) variableReference.reference(), variableReference.reference(),
			variableReference,
			false
		);
	}

	private static void addAllGroupMemberAsParameter(
		IGroupNode group, IVariableReferenceNode variableReference,
		List<ProvidedParameter> gatheredParameter
	)
	{
		for (var variable : group.flattenVariables())
		{
			// REDEFINEs and their member are not parameter themselves
			if (variable instanceof IRedefinitionNode || NodeUtil.findFirstParentOfType(
				variable,
				IRedefinitionNode.class
			) != null)
			{
				continue;
			}

			gatheredParameter.add(new ProvidedVariable((ITypedVariableNode) variable, variable, variableReference, true));
		}
	}

	private sealed interface ProvidedParameter permits ProvidedVariable,ProvidedOperand
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

		boolean isPassedAsGroupMember();

		ReadOnlyList<IOperandNode> passedDimensions();
	}

	private record ProvidedVariable(
		ITypedVariableNode variable,
		ISyntaxNode declarationPosition,
		IVariableReferenceNode usagePosition,
		boolean isPassedAsGroupMember
	) implements ProvidedParameter
	{
		@Override
		public ReadOnlyList<IOperandNode> passedDimensions()
		{
			return usagePosition.dimensions();
		}
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

		@Override
		public boolean isPassedAsGroupMember()
		{
			return false;
		}

		@Override
		public ReadOnlyList<IOperandNode> passedDimensions()
		{
			return ReadOnlyList.empty();
		}
	}
}
