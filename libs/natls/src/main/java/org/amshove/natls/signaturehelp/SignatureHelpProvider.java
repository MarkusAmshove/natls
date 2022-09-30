package org.amshove.natls.signaturehelp;

import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SignatureHelpProvider
{
	public SignatureHelp provideSignatureHelp(INaturalModule module, Position position)
	{

		if(!(module instanceof IModuleWithBody hasBody))
		{
			return null;
		}

		return NodeUtil.findStatementInLine(position.getLine(), hasBody.body())
			.or(() -> {
					var node = NodeUtil.findNodeAtPosition(position.getLine(), position.getCharacter(), hasBody.body());
					if(node != null && node.parent() instanceof IStatementNode statementNode)
					{
						return Optional.of(statementNode);
					}
					return Optional.empty();
				}
			)
			.map(statement -> provideSignatureForStatement(statement, position))
			.orElse(null);
	}

	private SignatureHelp provideSignatureForStatement(IStatementNode statement, Position position)
	{
		if(!(statement instanceof IModuleReferencingNode moduleReference))
		{
			return null;
		}

		var calledModule = moduleReference.reference();
		if(!(calledModule instanceof IHasDefineData hasDefineData) || hasDefineData.defineData() == null)
		{
			return null;
		}

		var signature = new SignatureHelp();

		var signatureInformation = new SignatureInformation();
		signature.setSignatures(List.of(signatureInformation));

		var parameter = new ArrayList<ParameterInformation>();
		signatureInformation.setParameters(parameter);

		var parameterIndex = 0;
		var parameterFound = false;
		for (var providedParameter : moduleReference.providedParameter())
		{
			if(providedParameter.enclosesPosition(position.getLine(), position.getCharacter()))
			{
				parameterFound = true;
				signatureInformation.setActiveParameter(parameterIndex);
				break;
			}

			parameterIndex++;
		}

		if(!parameterFound)
		{
			signatureInformation.setActiveParameter(moduleReference.providedParameter().size()); // size = next index that isn't provided yet
		}

		hasDefineData.defineData().parameterInOrder().stream()
			.map(this::mapToParameterInformation)
			.forEach(parameter::add);

		var parameterSignature = parameter.stream().map(pi -> pi.getLabel().getLeft()).collect(Collectors.joining(", "));
		signatureInformation.setLabel(
			"%s (%s)".formatted(
				calledModule.name(),
				parameterSignature
			)
		);

		return signature;
	}

	private ParameterInformation mapToParameterInformation(IParameterDefinitionNode parameter)
	{
		var information = new ParameterInformation();
		if(parameter instanceof IVariableNode variableNode)
		{
			information.setLabel(variableNode.name());
			if(variableNode instanceof ITypedVariableNode typedVariableNode)
			{
				information.setLabel("%s :%s%s".formatted(
						information.getLabel().getLeft(),
						typedVariableNode.type().toShortString(),
						typedVariableNode.findDescendantToken(SyntaxKind.OPTIONAL) != null ? " OPTIONAL" : ""
					)
				);
			}
		}

		if(parameter instanceof IUsingNode using)
		{
			information.setLabel("USING " + using.target().symbolName());
		}

		return information;
	}
}
