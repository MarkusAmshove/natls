package org.amshove.natls.hover;

import org.amshove.natls.markupcontent.IMarkupContentBuilder;
import org.amshove.natls.markupcontent.MarkupContentBuilderFactory;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;
import org.amshove.natparse.natural.builtin.SystemFunctionDefinition;
import org.amshove.natparse.natural.builtin.SystemVariableDefinition;
import org.eclipse.lsp4j.Hover;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

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

		var documentation = extractModuleDocumentation(module);
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

		var comment = getLineComment(variable.position().line(), variable.position().filePath());
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

	private String getLineComment(int line, Path filePath)
	{
		return getLineComment(line, project.findFile(filePath));
	}

	private @Nonnull String getLineComment(int line, LanguageServerFile file)
	{
		file.module(); // make sure we get comments
		return file.comments().stream()
			.filter(t -> t.line() == line)
			.map(SyntaxToken::source)
			.findFirst()
			.orElse("");
	}

	private TokenList lexPath(Path path)
	{
		try
		{
			return new Lexer().lex(Files.readString(path), path);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private String extractModuleDocumentation(INaturalModule module)
	{
		var tokens = lexPath(module.file().getPath());
		return extractDocumentation(tokens.comments(), tokens.subrange(0, 0).first().line());
	}

	private String extractDocumentation(ReadOnlyList<SyntaxToken> comments, int firstLineOfCode)
	{
		if(comments.isEmpty())
		{
			return null;
		}

		return comments.stream()
			.takeWhile(t -> t.line() < firstLineOfCode)
			.map(SyntaxToken::source)
			.filter(l -> !l.startsWith("* >") && !l.startsWith("* <") && !l.startsWith("* :"))
			.filter(l -> !l.trim().endsWith("*"))
			.collect(Collectors.joining(System.lineSeparator()));
	}

	private void addModuleParameter(IMarkupContentBuilder contentBuilder, INaturalModule module, HoverContext context)
	{
		if(!(module instanceof IHasDefineData hasDefineData) || hasDefineData.defineData() == null)
		{
			return;
		}

		contentBuilder.appendSection("Parameter", nested -> {
			var parameterBlock = new StringBuilder();
			var definedParameterUsings = hasDefineData.defineData().parameterUsings().stream()
				.map(using -> "PARAMETER USING %s %s".formatted(using.target().source(), getLineComment(using.position().line(), context.file())))
				.collect(Collectors.joining(System.lineSeparator()));

			var definedParameters = hasDefineData.defineData().variables().stream()
				.filter(v -> v.scope() == VariableScope.PARAMETER)
				.filter(v -> v.position().filePath().equals(module.file().getPath())) // get rid of parameters that are not directly declared in the module
				.map(v -> {
					var declaration = formatVariableDeclaration(v);
					return "%s%s".formatted(
						declaration.declaration,
						!declaration.comment.isEmpty()
						? " %s".formatted(declaration.comment)
						: "");
				})
				.collect(Collectors.joining(System.lineSeparator()));

			parameterBlock.append(definedParameterUsings);
			parameterBlock.append(System.lineSeparator());
			parameterBlock.append(definedParameters);

			nested.appendCode(parameterBlock.toString().stripIndent().trim());
		});
	}

	private record VariableDeclarationFormat(String declaration, String comment)
	{}
}
