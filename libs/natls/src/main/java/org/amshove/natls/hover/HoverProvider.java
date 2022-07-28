package org.amshove.natls.hover;

import org.amshove.natls.markupcontent.IMarkupContentBuilder;
import org.amshove.natls.markupcontent.MarkupContentBuilderFactory;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;
import org.amshove.natparse.natural.builtin.SystemFunctionDefinition;
import org.eclipse.lsp4j.Hover;

import java.nio.file.Path;

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

	private Hover hoverBuiltinFunction(SyntaxKind kind)
	{
		var builtinFunction = BuiltInFunctionTable.getDefinition(kind);
		var contentBuilder = MarkupContentBuilderFactory.newBuilder();
		contentBuilder.appendCode("%s : %s".formatted(builtinFunction.name(), builtinFunction.type().toShortString()));
		contentBuilder.appendParagraph("---");

		if(builtinFunction instanceof SystemFunctionDefinition)
		{
			contentBuilder.appendSection("Parameter", nested -> {}); // TODO: Format parameter
		}

		contentBuilder.appendParagraph(builtinFunction.documentation());
		return new Hover(contentBuilder.build());
	}

	private Hover hoverVariable(IVariableNode variable, HoverContext context)
	{
		var contentBuilder = MarkupContentBuilderFactory.newBuilder();
		var declaration = "%s %d %s".formatted(variable.scope().toString(), variable.level(), variable.name());
		if(variable instanceof ITypedVariableNode typedVariableNode)
		{
			declaration += " %s".formatted(typedVariableNode.type().toShortString());
		}

		contentBuilder.appendCode(declaration);

		var comment = getLineComment(variable.position().line(), variable.position().filePath());
		if(comment != null)
		{
			contentBuilder.appendSection("comment", nestedBuilder -> {
				nestedBuilder.appendCode(comment);
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

	private String getLineComment(int line, LanguageServerFile file)
	{
		return file.comments().stream()
			.filter(t -> t.line() == line)
			.map(SyntaxToken::source)
			.findFirst()
			.orElse(null);
	}
}
