package org.amshove.natls.codemutation;

import org.amshove.natls.project.LanguageServerFile;
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
		var variableInsert = rangeFinder.findInsertionPositionToInsertVariable(file, scope);
		return variableInsert.toTextEdit("%d %s %s".formatted(1, variableName, variableType));
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
