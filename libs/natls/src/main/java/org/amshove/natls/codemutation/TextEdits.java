package org.amshove.natls.codemutation;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.TextEdit;

import java.util.stream.Collectors;

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

		var insertion = rangeFinder.findInsertionPositionForStatementAtEnd(file);
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

	public static TextEdit addPrototype(LanguageServerFile inFile, IFunction calledFunction)
	{
		var insertion = rangeFinder.findInsertionPositionForStatementAtStart(inFile);

		var defineDataBlock = "";
		var parameter = calledFunction.defineData().parameterInOrder();
		if (!parameter.isEmpty())
		{
			defineDataBlock = """
				%n  DEFINE DATA
				%s
				  END-DEFINE""".formatted(
				calledFunction.defineData().parameterInOrder().stream().map(p ->
				{
					if (p instanceof IUsingNode using)
					{
						return "PARAMETER USING %s".formatted(using.target().symbolName());
					}
					var parameterVariable = (IVariableNode) p;
					if (parameterVariable instanceof ITypedVariableNode typedParameter)
					{
						return "PARAMETER %d %s %s".formatted(typedParameter.level(), typedParameter.name(), typedParameter.formatTypeForDisplay());
					}

					return "PARAMETER %d %s".formatted(parameterVariable.level(), ((IVariableNode) p).name());
				})
					.map(p -> "    " + p)
					.collect(Collectors.joining(System.lineSeparator()))
			);
		}

		return insertion.toTextEdit(
			"""
			DEFINE PROTOTYPE %s RETURNS %s
			%s
			END-PROTOTYPE
			""".formatted(
				calledFunction.name(),
				calledFunction.returnType().toShortString(),
				defineDataBlock
			)
		);
	}
}
