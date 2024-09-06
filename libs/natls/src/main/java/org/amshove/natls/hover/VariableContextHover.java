package org.amshove.natls.hover;

import org.amshove.natls.markupcontent.IMarkupContentBuilder;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.IUsingNode;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.IViewNode;

class VariableContextHover
{
	private final IVariableNode variable;
	private INaturalModule declaringModule;
	private IUsingNode usingNode;
	private String usingComment;

	private VariableContextHover(IVariableNode variable)
	{
		this.variable = variable;
	}

	public void addVariableContext(IMarkupContentBuilder builder)
	{
		var variableContext = new StringBuilder();

		appendUsingComment(variableContext);
		var parents = variable.getVariableParentsAscending();
		for (var parent : parents)
		{
			appendVariableComment(parent, variableContext, true);
		}

		appendVariableComment(variable, variableContext, !variableContext.isEmpty());

		if (!variableContext.isEmpty())
		{
			builder.appendSection("context", nestedBuilder -> nestedBuilder.appendCode(variableContext.toString()));
		}
	}

	private void appendUsingComment(StringBuilder chain)
	{
		if (usingNode == null)
		{
			return;
		}

		var usingSource = "%s USING %s".formatted(usingNode.scope().toSyntaxKind().name(), usingNode.target().symbolName());
		appendComment("%s %s".formatted(usingSource, usingComment), chain);
	}

	private void appendVariableComment(IVariableNode variable, StringBuilder chain, boolean alwaysInclude)
	{
		var source = "%d %s".formatted(variable.level(), variable.name());
		var comment = declaringModule.extractLineComment(variable.position().line());

		if (variable.isArray())
		{
			var formattedDimensions = new StringBuilder();
			for (var dimension : variable.dimensions())
			{
				if (dimension.parent() == variable)
				{
					if (!formattedDimensions.isEmpty())
					{
						formattedDimensions.append(",");
					}
					formattedDimensions.append(dimension.displayFormat());
				}
			}
			if (!formattedDimensions.isEmpty())
			{
				source += " (%s)".formatted(formattedDimensions);
			}
		}

		if (variable instanceof IViewNode view)
		{
			source += " VIEW OF %s".formatted(view.ddmNameToken().symbolName());
		}

		if (alwaysInclude || !comment.isEmpty())
		{
			appendComment("%s %s".formatted(source, comment), chain);
		}
	}

	private void appendComment(String comment, StringBuilder chain)
	{
		if (comment != null && !comment.isEmpty())
		{
			if (!chain.isEmpty())
			{
				chain.append(System.lineSeparator());
			}
			chain.append(comment);
		}
	}

	static VariableContextHover create(HoverContext hoverContext, IVariableNode variable)
	{
		var variableContext = new VariableContextHover(variable);
		if (variable.position().filePath().equals(hoverContext.file().getPath()))
		{
			variableContext.declaringModule = hoverContext.file().module();
		}
		else
		{
			var using = findUsingImportingVariable(variable, ((IHasDefineData) hoverContext.file().module()));
			if (using != null)
			{
				variableContext.usingNode = using;
				variableContext.usingComment = hoverContext.file().module().extractLineComment(using.position().line());
				variableContext.declaringModule = hoverContext.file().findNaturalModule(using.target().symbolName());
			}
		}

		return variableContext;
	}

	private static IUsingNode findUsingImportingVariable(IVariableNode variable, IHasDefineData module)
	{
		for (var using : module.defineData().usings())
		{
			if (using.defineData() != null && using.defineData().findVariable(variable.qualifiedName()) != null)
			{
				return using;
			}
		}

		return null;
	}
}
