package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDataType;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

class OperandChecker
{
	private final List<IDiagnostic> diagnostics = new ArrayList<>();

	public ReadOnlyList<IDiagnostic> checkOperands(Map<IOperandNode, EnumSet<OperandDefinition>> operandChecks)
	{
		for (var queuedCheck : operandChecks.entrySet())
		{
			// TODO: Type inference is also done in the TypeChecker, so at least twice now
			//       maybe the typechecker isn't needed anymore if all statements use the
			//       operator check queue
			var inferredType = TypeInference.inferType(queuedCheck.getKey());
			inferredType
				.ifPresent(type -> check(queuedCheck.getKey(), type, queuedCheck.getValue()));
		}
		return ReadOnlyList.from(diagnostics);
	}

	private void check(IOperandNode operand, IDataType type, EnumSet<OperandDefinition> definitionTable)
	{
		if (OperandDefinition.forDataFormat(type.format())instanceof OperandDefinition definition)
		{
			checkFormatDefinition(operand, definition, definitionTable);
		}
	}

	private void checkFormatDefinition(
		IOperandNode operand, OperandDefinition definition,
		EnumSet<OperandDefinition> definitionTable
	)
	{
		if (!definitionTable.contains(definition))
		{
			diagnostics.add(
				ParserErrors.typeMismatch(
					"Operand can't be of format %s. Allowed formats: %s".formatted(definition.shortform(), formatAllowedDataFormats(definitionTable)),
					operand
				)
			);
		}
	}

	private static String formatAllowedDataFormats(EnumSet<OperandDefinition> definitionTable)
	{
		var builder = new StringBuilder();
		var separator = "";

		for (var definition : OperandDefinition.FORMAT_DEFINITIONS)
		{
			if (definitionTable.contains(definition))
			{
				builder.append(separator);
				builder.append(definition.shortform());
				separator = ", ";
			}
		}

		return builder.toString();
	}
}
