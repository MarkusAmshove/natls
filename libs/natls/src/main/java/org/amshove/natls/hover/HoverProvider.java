package org.amshove.natls.hover;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.amshove.natls.markupcontent.IMarkupContentBuilder;
import org.amshove.natls.markupcontent.MarkupContentBuilderFactory;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.IModuleReferencingNode;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISymbolReferenceNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITypedVariableNode;
import org.amshove.natparse.natural.IUsingNode;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;
import org.amshove.natparse.natural.builtin.SystemFunctionDefinition;
import org.amshove.natparse.natural.builtin.SystemVariableDefinition;
import org.eclipse.lsp4j.Hover;

public class HoverProvider
{
	private static final Hover EMPTY_HOVER = null; // This should be null according to the LSP spec
	private final LanguageServerProject project;

	public HoverProvider(LanguageServerProject project)
	{
		this.project = project;
	}

	public Hover createHover(HoverContext context)
	{
		if(context.nodeToHover() == null)
		{
			return EMPTY_HOVER;
		}

		// TODO: This should use nodes instead of tokens, but does not work currently in every case, as they're only created when parsing operands
		//		and some node types that use operands aren't implemented yet.
		//		The `tokenToHover` can then be removed from the context.
		if(context.tokenToHover().kind().isSystemVariable() || context.tokenToHover().kind().isSystemFunction())
		{
			return hoverBuiltinFunction(context.tokenToHover().kind());
		}

		if(context.nodeToHover() instanceof IModuleReferencingNode moduleReferencingNode)
		{
			return hoverExternalModule(moduleReferencingNode, context);
		}

		if(context.nodeToHover() instanceof IVariableNode variableNode)
		{
			return hoverVariable(variableNode, context);
		}

		if(context.nodeToHover() instanceof ISymbolReferenceNode symbolReferenceNode)
		{
			if(symbolReferenceNode.reference() instanceof IVariableNode variableNode)
			{
				return hoverVariable(variableNode, context);
			}
		}


		return EMPTY_HOVER;
	}

	private Hover hoverExternalModule(IModuleReferencingNode moduleReferencingNode, HoverContext context)
	{
		var module = moduleReferencingNode.reference();
		if(module == null)
		{
			return EMPTY_HOVER;
		}

		var contentBuilder = MarkupContentBuilderFactory.newBuilder();
		contentBuilder.appendStrong("%s.%s".formatted(module.file().getLibrary().getName(), module.file().getReferableName())).appendNewline();
		/*
		if(module instanceof IFunction function)
		{
			TODO: Add return type
		}
		 */

		if(!module.file().getFilenameWithoutExtension().equals(module.file().getReferableName()))
		{
			contentBuilder.appendItalic("File: %s".formatted(module.file().getFilenameWithoutExtension())).appendNewline();
		}

		var calledFile = project.findFile(module.file());
		var documentation = calledFile.extractModuleDocumentation();
		if(documentation != null && !documentation.trim().isEmpty())
		{
			contentBuilder.appendCode(documentation);
		}

		addModuleParameter(contentBuilder, module, context);

		return new Hover(contentBuilder.build());
	}


	private Hover hoverBuiltinFunction(SyntaxKind kind)
	{
		var builtinFunction = BuiltInFunctionTable.getDefinition(kind);
		var contentBuilder = MarkupContentBuilderFactory.newBuilder();

		var signature = builtinFunction.name();
		if(builtinFunction instanceof SystemFunctionDefinition function)
		{
			signature += "(";
			signature += function.parameter().stream()
				.map(p -> {
					var parameter = p.name();
					if(p.type().format() != DataFormat.NONE)
					{
						parameter += p.type().toShortString();
					}
					return p.mandatory() ? parameter : "[%s]".formatted(parameter);
				})
				.collect(Collectors.joining(", "));
			signature += ")";
		}

		signature += " : %s".formatted(builtinFunction.type().toShortString());
		contentBuilder.appendCode(signature);
		contentBuilder.appendParagraph("---");

		if(builtinFunction instanceof SystemVariableDefinition variableDefinition)
		{
			contentBuilder.appendStrong(variableDefinition.isModifiable() ? "modifiable" : "unmodifiable").appendNewline();
		}

		contentBuilder.appendParagraph(builtinFunction.documentation());
		return new Hover(contentBuilder.build());
	}

	private Hover hoverVariable(IVariableNode variable, HoverContext context)
	{
		var contentBuilder = MarkupContentBuilderFactory.newBuilder();
		var declaration = formatVariableDeclaration(variable);
		contentBuilder.appendCode(declaration.declaration);
		if(!declaration.comment.isEmpty())
		{
			contentBuilder.appendSection("comment", nestedBuilder -> nestedBuilder.appendCode(declaration.comment));
		}

		if(variable.isArray())
		{
			contentBuilder.appendSection("dimensions", nested -> {
				for (var dimension : variable.dimensions())
				{
					nested.appendBullet(dimension.displayFormat());
				}
			});
		}

		if(variable.level() > 1)
		{
			var owner = NodeUtil.findLevelOneParentOf(variable);
			contentBuilder.appendItalic("member of:");
			contentBuilder.appendNewline();
			contentBuilder.appendCode("%s %d %s".formatted(owner.scope().toString(), owner.level(), owner.name()));
		}

		addSourceFileIfNeeded(contentBuilder, variable.declaration(), context);
		return new Hover(contentBuilder.build());
	}

	private VariableDeclarationFormat formatVariableDeclaration(IVariableNode variable)
	{
		var declaration = "%s %d %s".formatted(variable.scope().toString(), variable.level(), variable.name());
		if(variable instanceof ITypedVariableNode typedVariableNode)
		{
			declaration += " %s".formatted(typedVariableNode.type().toShortString());
		}

		if(variable.findDescendantToken(SyntaxKind.OPTIONAL) != null)
		{
			declaration += " OPTIONAL";
		}

		var comment = getLineComment(variable);
		return new VariableDeclarationFormat(declaration, comment);
	}

	private static void addSourceFileIfNeeded(IMarkupContentBuilder contentBuilder, IPosition hoveredPosition, HoverContext context)
	{
		if(!hoveredPosition.filePath().equals(context.file().getPath()))
		{
			contentBuilder.appendItalic("source:");
			contentBuilder.appendNewline();
			contentBuilder.append("- %s.%s", context.file().getLibrary().name(), hoveredPosition.fileNameWithoutExtension());
		}
	}

	private void addModuleParameter(IMarkupContentBuilder contentBuilder, INaturalModule module, HoverContext context)
	{
		if(!(module instanceof IHasDefineData hasDefineData) || hasDefineData.defineData() == null)
		{
			return;
		}

		Function<IUsingNode, String> usingFormatter = (using) -> "PARAMETER USING %s %s".formatted(using.target().source(), getLineComment(using));
		Function<IVariableNode, String> variableFormatter = (variable) -> {
			var declaration = formatVariableDeclaration(variable);
			return "%s%s".formatted(
				declaration.declaration,
				!declaration.comment.isEmpty()
					? " %s".formatted(declaration.comment)
					: "");
		};

		contentBuilder.appendSection("Parameter", nested -> {
			var parameterBlock = new StringBuilder();
			for (var parameterDefinition : hasDefineData.defineData().parameterInOrder())
			{
				if(parameterDefinition instanceof IUsingNode using)
				{
					parameterBlock.append(usingFormatter.apply(using));
				}
				else if (parameterDefinition instanceof IVariableNode variable)
				{
					parameterBlock.append(variableFormatter.apply(variable));
				}

				parameterBlock.append(System.lineSeparator());
			}

			nested.appendCode(parameterBlock.toString().stripIndent().trim());
		});
	}

	private String getLineComment(ISyntaxNode node)
	{
		var file = project.findFile(node.position().filePath());
		return file.extractLineComment(node.position().line());
	}

	private record VariableDeclarationFormat(String declaration, String comment)
	{}
}
