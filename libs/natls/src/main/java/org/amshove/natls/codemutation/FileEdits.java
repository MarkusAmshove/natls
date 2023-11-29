package org.amshove.natls.codemutation;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.natural.*;

import java.util.stream.Collectors;

public class FileEdits
{
	private static final CodeInsertionPlacer rangeFinder = new CodeInsertionPlacer();

	private FileEdits()
	{}

	public static FileEdit addVariable(LanguageServerFile file, String variableName, String variableType, VariableScope scope)
	{
		if (variableName.contains("."))
		{
			var split = variableName.split("\\.");
			var groupPart = split[0];
			var variablePart = split[1];
			return addVariableToGroup(file, groupPart, variablePart, variableType, scope);
		}
		var variableInsert = rangeFinder.findInsertionPositionToInsertVariable(file, scope);
		return variableInsert.toFileEdit("%d %s %s".formatted(1, variableName, variableType));
	}

	private static FileEdit addVariableToGroup(LanguageServerFile file, String groupPart, String variablePart, String variableType, VariableScope scope)
	{
		var group = ((IHasDefineData) file.module()).defineData().findVariable(groupPart);
		if (group instanceof IGroupNode groupNode)
		{
			var insertion = rangeFinder.insertInNextLineAfter(groupNode.variables().last());
			return insertion.toFileEdit("2 %s %s".formatted(variablePart, variableType));
		}

		var insertion = rangeFinder.findInsertionPositionToInsertVariable(file, scope);
		return insertion.toFileEdit("1 %s%n2 %s %s".formatted(groupPart, variablePart, variableType));
	}

	public static FileEdit addUsing(LanguageServerFile file, UsingToAdd neededUsing)
	{
		if (alreadyHasUsing(neededUsing.name(), file))
		{
			return null;
		}

		return createUsingInsert(neededUsing, file);
	}

	public static FileEdit addSubroutine(LanguageServerFile file, String name, String source)
	{

		var subroutine = """
			/***********************************************************************
			DEFINE SUBROUTINE %s
			/***********************************************************************

			%s

			END-SUBROUTINE
			""".formatted(name, source);

		var insertion = rangeFinder.findInsertionPositionForStatementAtEnd(file);
		return insertion.toFileEdit(subroutine);
	}

	public static FileEdit addPrototype(LanguageServerFile inFile, IFunction calledFunction)
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

		return insertion.toFileEdit(
			"""
			DEFINE PROTOTYPE %s RETURNS %s%s
			END-PROTOTYPE
			""".formatted(
				calledFunction.name(),
				calledFunction.returnType().toShortString(),
				defineDataBlock
			)
		);
	}

	private static FileEdit createUsingInsert(UsingToAdd using, LanguageServerFile file)
	{
		var insertion = rangeFinder.findInsertionPositionToInsertUsing(file, using.scope());
		return insertion.toFileEdit("%s USING %s".formatted(using.scope(), using.name()));
	}

	private static boolean alreadyHasUsing(String using, LanguageServerFile file)
	{
		return ((IHasDefineData) file.module()).defineData().usings().stream().anyMatch(u -> u.target().symbolName().equals(using));
	}

}
