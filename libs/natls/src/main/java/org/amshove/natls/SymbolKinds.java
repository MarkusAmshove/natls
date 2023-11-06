package org.amshove.natls;

import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.SymbolKind;

public class SymbolKinds
{
	private SymbolKinds()
	{}

	public static SymbolKind forModuleFromNode(ISyntaxNode node)
	{
		var fileType = node.diagnosticPosition().fileType();
		return forFileType(node.diagnosticPosition().fileNameWithoutExtension(), fileType);
	}

	public static SymbolKind forUsing(IUsingNode using)
	{
		return using.isParameterUsing() ? SymbolKind.TypeParameter : SymbolKind.Field;
	}

	public static SymbolKind forReferencable(IReferencableNode referencable)
	{
		if (referencable instanceof IVariableNode variable)
		{
			return forVariable(variable);
		}

		if (referencable instanceof ISubroutineNode)
		{
			return SymbolKind.Method;
		}

		return null;
	}

	public static SymbolKind forVariable(IVariableNode variable)
	{
		if (variable.scope().isParameter())
		{
			return SymbolKind.TypeParameter;
		}

		if (variable.isArray())
		{
			return SymbolKind.Array;
		}

		return variable instanceof IGroupNode ? SymbolKind.Struct : SymbolKind.Variable;
	}

	public static SymbolKind forModule(INaturalModule module)
	{
		return forFileType(module.name(), module.file().getFiletype());
	}

	public static SymbolKind forFileType(String fileName, NaturalFileType fileType)
	{
		return switch (fileType)
		{
			case SUBPROGRAM -> fileName.startsWith("TC") ? SymbolKind.Interface : SymbolKind.Class;
			case PROGRAM -> SymbolKind.Class;
			case SUBROUTINE -> SymbolKind.Method;
			case HELPROUTINE -> SymbolKind.Key;
			case GDA, LDA, DDM -> SymbolKind.Struct;
			case PDA -> SymbolKind.TypeParameter;
			case MAP -> SymbolKind.Enum;
			case COPYCODE -> SymbolKind.Package;
			case FUNCTION -> SymbolKind.Event;
		};
	}
}
