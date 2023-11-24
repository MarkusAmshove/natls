package org.amshove.natls.codemutation;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.natural.IGroupNode;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.VariableScope;
import org.eclipse.lsp4j.TextEdit;

public class TextEdits
{
	private static final CodeInsertionPlacer rangeFinder = new CodeInsertionPlacer();

	private TextEdits()
	{}

	public static TextEdit addVariable(LanguageServerFile file, String variableName, String variableType, VariableScope scope)
	{
		if (variableName.contains("."))
		{
			var split = variableName.split("\\.");
			var groupPart = split[0];
			var variablePart = split[1];
			return addVariableToGroup(file, groupPart, variablePart, variableType, scope);
		}
		var variableInsert = rangeFinder.findInsertionPositionToInsertVariable(file, scope);
		return variableInsert.toTextEdit("%d %s %s".formatted(1, variableName, variableType));
	}

	private static TextEdit addVariableToGroup(LanguageServerFile file, String groupPart, String variablePart, String variableType, VariableScope scope)
	{
		var group = ((IHasDefineData) file.module()).defineData().findVariable(groupPart);
		if (group instanceof IGroupNode groupNode)
		{
			var insertion = rangeFinder.insertInNextLineAfter(groupNode.variables().last());
			return insertion.toTextEdit("2 %s %s".formatted(variablePart, variableType));
		}

		var insertion = rangeFinder.findInsertionPositionToInsertVariable(file, scope);
		return insertion.toTextEdit("1 %s%n2 %s %s".formatted(groupPart, variablePart, variableType));
	}

	public static TextEdit addUsing(LanguageServerFile file, UsingToAdd neededUsing)
	{
		if (alreadyHasUsing(neededUsing.name(), file))
		{
			return null;
		}

		return createUsingInsert(neededUsing, file);
	}

	public static TextEdit addSubroutine(LanguageServerFile file, String name, String source)
	{

		var subroutine = """
			/***********************************************************************
			DEFINE SUBROUTINE %s
			/***********************************************************************

			%s

			END-SUBROUTINE
			""".formatted(name, source);

		var insertion = rangeFinder.findInsertionPositionForStatement(file);
		return insertion.toTextEdit(subroutine);
	}

	private static TextEdit createUsingInsert(UsingToAdd using, LanguageServerFile file)
	{
		var insertion = rangeFinder.findInsertionPositionToInsertUsing(file, using.scope());
		return insertion.toTextEdit("%s USING %s".formatted(using.scope(), using.name()));
	}

	private static boolean alreadyHasUsing(String using, LanguageServerFile file)
	{
		return ((IHasDefineData) file.module()).defineData().usings().stream().anyMatch(u -> u.target().symbolName().equals(using));
	}

}
