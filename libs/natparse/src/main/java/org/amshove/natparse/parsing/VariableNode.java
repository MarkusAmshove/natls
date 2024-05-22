package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

class VariableNode extends BaseSyntaxNode implements IVariableNode
{
	private int level;
	private String name;
	private SyntaxToken declaration;
	private VariableScope scope;
	private ITokenNode identifierNode;
	private final List<ISymbolReferenceNode> references = new ArrayList<>();

	protected final List<IArrayDimension> dimensions = new ArrayList<>();

	private String qualifiedName; // Gets computed on first demand

	@Override
	@Nonnull
	public ReadOnlyList<ISymbolReferenceNode> references()
	{
		return ReadOnlyList.from(references); // TODO: Perf
	}

	@Override
	public void removeReference(ISymbolReferenceNode node)
	{
		references.remove(node);
	}

	@Override
	public void addReference(ISymbolReferenceNode node)
	{
		references.add(node);
		if (node instanceof SymbolReferenceNode symbolRef)
		{
			// REDEFINE does set it itself
			symbolRef.setReference(this);
		}
	}

	@Override
	public SyntaxToken declaration()
	{
		return declaration;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public String qualifiedName()
	{
		if (qualifiedName != null)
		{
			return qualifiedName;
		}

		if (level == 1)
		{
			qualifiedName = name;
			return name;
		}

		var parent = parent();
		while (parent != null)
		{
			if (parent instanceof IVariableNode && ((IVariableNode) parent).level() == 1)
			{
				qualifiedName = "%s.%s".formatted(((IVariableNode) parent).name(), name());
				return qualifiedName;
			}

			parent = parent.parent();
		}

		throw new NaturalParseException("Could not determine qualified name");
	}

	@Override
	public ITokenNode identifierNode()
	{
		return identifierNode;
	}

	@Override
	public ReadOnlyList<IArrayDimension> dimensions()
	{
		return ReadOnlyList.from(dimensions); // TODO: perf
	}

	@Override
	public int level()
	{
		return level;
	}

	@Override
	public VariableScope scope()
	{
		return scope;
	}

	@Override
	public IPosition position()
	{
		return declaration;
	}

	@Override
	public boolean isInView()
	{
		return NodeUtil.findFirstParentOfType(this, IViewNode.class) != null;
	}

	void setLevel(int level)
	{
		this.level = level;
	}

	void setDeclaration(ITokenNode identifierNode)
	{
		var token = identifierNode.token();
		name = token.symbolName();
		declaration = token;
		this.identifierNode = identifierNode;
	}

	void setScope(VariableScope scope)
	{
		this.scope = scope;
	}

	void addDimension(ArrayDimension dimension)
	{
		dimensions.add(dimension);

		// If we get a new dimension that is defined for a parent variable, we don't take its ownership
		if (dimension.parent() == null)
		{
			addNode(dimension);
		}
	}

	/**
	 * Inherits all the given dimensions if they're not specified for this variable yet.
	 */
	void inheritDimensions(ReadOnlyList<IArrayDimension> dimensions)
	{
		for (var dimension : dimensions)
		{
			if (!this.dimensions.contains(dimension))
			{
				this.dimensions.addFirst(dimension); // add inhereted dimensions first, as they're defined first
			}
		}
	}
}
