package org.amshove.natls.signaturehelp;

import org.amshove.natls.markupcontent.MarkupContentBuilderFactory;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SignatureHelpProvider
{
	public SignatureHelp provideSignatureHelp(INaturalModule module, Position position)
	{

		if (!(module instanceof IModuleWithBody hasBody))
		{
			return null;
		}

		// If we find a module referencing statement, that's the most likely match
		var maybeStatement = NodeUtil.findStatementInLine(position.getLine(), hasBody.body());
		if (maybeStatement.isPresent() && maybeStatement.get() instanceof IModuleReferencingNode moduleReferencingNode)
		{
			return provideSignatureForStatement(moduleReferencingNode, position);
		}

		// Look deeper to find the exact Node the cursor on
		var node = NodeUtil.findNodeAtPosition(position.getLine(), position.getCharacter(), hasBody.body());
		if (node instanceof IModuleReferencingNode moduleReferencingNode)
		{
			return provideSignatureForStatement(moduleReferencingNode, position);
		}

		// We most likely landed on a Token, which could be a parameter
		if (node != null && node.parent() instanceof IModuleReferencingNode moduleReferencingNode)
		{
			return provideSignatureForStatement(moduleReferencingNode, position);
		}

		return null;
	}

	private SignatureHelp provideSignatureForStatement(IModuleReferencingNode moduleReference, Position position)
	{
		var calledModule = moduleReference.reference();
		if (!(calledModule instanceof IHasDefineData hasDefineData) || hasDefineData.defineData() == null)
		{
			if (calledModule.file().getFiletype() == NaturalFileType.COPYCODE && moduleReference instanceof IIncludeNode includeNode)
			{
				return provideSignatureForCopyCode(includeNode, position);
			}

			var signatureHelp = createDefaultSignatureHelp();
			signatureHelp.getSignatures().get(0).setLabel("%s ()".formatted(moduleReference.reference().name()));
			return signatureHelp;
		}

		var signatureHelp = createDefaultSignatureHelp();
		setActiveParameter(moduleReference, position, signatureHelp);
		var signatureInformation = signatureHelp.getSignatures().get(0);
		var moduleDocumentation = calledModule.moduleDocumentation();
		if(!moduleDocumentation.isEmpty())
		{
			signatureInformation.setDocumentation(MarkupContentBuilderFactory.newBuilder().appendCode(moduleDocumentation).build());
		}

		hasDefineData.defineData().parameterInOrder().stream()
			.map(pi -> mapToParameterInformation(calledModule, pi))
			.forEach(p -> signatureInformation.getParameters().add(p));

		var parameterSignature = signatureInformation.getParameters().stream().map(pi -> pi.getLabel().getLeft()).collect(Collectors.joining(", "));
		signatureInformation.setLabel(
			"%s (%s)".formatted(
				calledModule.name(),
				parameterSignature
			)
		);

		return signatureHelp;
	}

	private SignatureHelp provideSignatureForCopyCode(IIncludeNode include, Position position)
	{
		var help = createDefaultSignatureHelp();
		var signature = help.getSignatures().get(0);
		setActiveParameter(include, position, help);

		var copyCodeParameter = new ArrayList<String>();
		include.body().accept(n -> {
			if (n instanceof ITokenNode tokenNode && tokenNode.token().kind() == SyntaxKind.IDENTIFIER && tokenNode.token().source().matches(".*?&\\d+&"))
			{
				copyCodeParameter.add(tokenNode.token().source());
			}
		});

		copyCodeParameter
			.stream()
			.distinct()
			.map(p -> {
				var information = new ParameterInformation();
				information.setLabel(p);
				return information;
			})
			.forEach(i -> signature.getParameters().add(i));

		signature.setLabel("%s (%s)".formatted(
			include.referencingToken().symbolName(),
			signature.getParameters().stream().map(i -> i.getLabel().getLeft()).collect(Collectors.joining(", "))
		));

		return help;
	}

	private static SignatureHelp createDefaultSignatureHelp()
	{
		var signature = new SignatureHelp();

		var signatureInformation = new SignatureInformation();
		signature.setSignatures(List.of(signatureInformation));

		var parameter = new ArrayList<ParameterInformation>();
		signatureInformation.setParameters(parameter);

		return signature;
	}

	private static void setActiveParameter(IModuleReferencingNode moduleReference, Position position, SignatureHelp signatureHelp)
	{
		var signatureInformation = signatureHelp.getSignatures().get(0);
		var parameterIndex = 0;
		var parameterFound = false;
		for (var providedParameter : moduleReference.providedParameter())
		{
			if (providedParameter.enclosesPosition(position.getLine(), position.getCharacter()))
			{
				parameterFound = true;
				signatureInformation.setActiveParameter(parameterIndex);
				break;
			}

			parameterIndex++;
		}

		if (!parameterFound)
		{
			signatureInformation.setActiveParameter(moduleReference.providedParameter().size()); // size = next index that isn't provided yet
		}
	}

	private ParameterInformation mapToParameterInformation(INaturalModule module, IParameterDefinitionNode parameter)
	{
		var information = new ParameterInformation();
		if (parameter instanceof IVariableNode variableNode)
		{
			information.setLabel(variableNode.name());
			if (variableNode instanceof ITypedVariableNode typedVariableNode)
			{
				information.setLabel("%s :%s%s".formatted(
						information.getLabel().getLeft(),
						typedVariableNode.type().toShortString(),
						typedVariableNode.findDescendantToken(SyntaxKind.OPTIONAL) != null ? " OPTIONAL" : ""
					)
				);
			}
		}

		if (parameter instanceof IUsingNode using)
		{
			information.setLabel("USING " + using.target().symbolName());
		}

		var parameterDocumentation = module.extractLineComment(parameter.position().line());
		if(!parameterDocumentation.isEmpty())
		{
			information.setDocumentation(MarkupContentBuilderFactory.newBuilder().appendCode(parameterDocumentation).build());
		}

		return information;
	}
}
