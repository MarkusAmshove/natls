package org.amshove.natls.languageserver.constantfinding;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFileType;

import java.util.List;
import java.util.stream.Stream;

public class ConstantsFinder
{
	/**
	 * Finds constant variables from LDAs that are reachable by {@code startFile}
	 * 
	 * @param startFile the file that constants need to be reachable from
	 */
	public List<FoundConstant> findConstants(LanguageServerFile startFile)
	{

		return startFile.getLibrary().getModulesOfType(NaturalFileType.LDA, true)
			.stream()
			.flatMap(
				file -> extractConstants(file).map(
					tv -> new FoundConstant(tv.declaration().symbolName(), file.getReferableName(), extractValue(tv))
				)
			)
			.toList();
	}

	private String extractValue(ITypedVariableNode variable)
	{
		var valueNode = variable.type().initialValue();

		return switch (valueNode)
		{
			case ILiteralNode literal -> literal.token().source();
			case IStringConcatOperandNode concat -> "'" + concat.stringValue() + "'";
			case null, default -> "";
		};
	}

	private Stream<ITypedVariableNode> extractConstants(LanguageServerFile file)
	{
		if (file.getType() != NaturalFileType.LDA
			|| !(file.module()instanceof IHasDefineData hasDefineData)
			|| hasDefineData.defineData() == null)
		{
			return Stream.of();
		}

		return hasDefineData.defineData()
			.variables()
			.stream()
			.filter(ITypedVariableNode.class::isInstance)
			.map(v -> ((ITypedVariableNode) v))
			.filter(tv -> tv.type().isConstant());
	}
}
