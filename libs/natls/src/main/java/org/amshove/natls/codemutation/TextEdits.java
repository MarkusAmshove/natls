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
		var variableInsert = rangeFinder.findRangeToInsertVariable(file, scope);
		var edit = new TextEdit();
		edit.setRange(variableInsert.range());
		edit.setNewText("%s%d %s %s%n".formatted(variableInsert.insertionPrefix(), 1, variableName, variableType));
		return edit;
	}

	public static TextEdit addUsing(LanguageServerFile file, UsingToAdd neededUsing)
	{
		if (alreadyHasUsing(neededUsing.name(), file))
		{
			return null;
		}

		return createUsingInsert(neededUsing, file);
	}

	private static TextEdit createUsingInsert(UsingToAdd using, LanguageServerFile file)
	{
		var edit = new TextEdit();
		var insertion = rangeFinder.findRangeToInsertUsing(file, using.scope());

		edit.setRange(insertion.range());
		edit.setNewText(insertion.insertionText("%s USING %s".formatted(using.scope(), using.name())));
		return edit;
	}

	private static boolean alreadyHasUsing(String using, LanguageServerFile file)
	{
		return ((IHasDefineData) file.module()).defineData().usings().stream().anyMatch(u -> u.target().symbolName().equals(using));
	}

}
