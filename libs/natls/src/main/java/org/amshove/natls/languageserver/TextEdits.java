package org.amshove.natls.languageserver;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IUsingNode;
import org.amshove.natparse.natural.VariableScope;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

public class TextEdits
{

	public static TextEdit addUsing(LanguageServerFile file, UsingToAdd neededUsing)
	{
		if(alreadyHasUsing(neededUsing.name(), file))
		{
			return null;
		}

		return createUsingInsert(neededUsing, file);
	}

	private static TextEdit createUsingInsert(UsingToAdd using, LanguageServerFile file)
	{
		var edit = new TextEdit();
		var range = findRangeToInsertUsing(file, using.scope());

		edit.setRange(range);
		edit.setNewText("%s USING %s%n".formatted(using.scope(), using.name()));
		return edit;
	}

	private static Range findRangeToInsertUsing(LanguageServerFile file, VariableScope scope)
	{
		var defineData = ((IHasDefineData) file.module()).defineData();
		ReadOnlyList<IUsingNode> usings = scope == VariableScope.PARAMETER
			? defineData.parameterUsings()
			: defineData.localUsings();

		if(usings.hasItems())
		{
			var firstUsing = usings.first();
			return LspUtil.toSingleRange(firstUsing.position().line(), 0);
		}

		if(defineData.variables().hasItems() && defineData.variables().stream().anyMatch(v -> v.scope() == scope && v.position().filePath().equals(file.getPath())))
		{
			return defineData.variables().stream().filter(v -> v.scope() == scope)
				.findFirst()
				.map(v -> (ISyntaxNode)v.parent()) // Scope node
				.map(v -> LspUtil.toSingleRange(v.position().line(), 0))
				.orElseThrow(() -> new RuntimeException("Could not deduce position to insert using by looking for the first variable with scope %s".formatted(scope)));
		}

		return LspUtil.toSingleRange(defineData.descendants().get(0).position().line() + 1, 0);
	}

	private static boolean alreadyHasUsing(String using, LanguageServerFile file)
	{
		return ((IHasDefineData) file.module()).defineData().usings().stream().anyMatch(u -> u.target().symbolName().equals(using));
	}
}
