package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IDataType;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

class OperandChecker
{
	private static final Map<DataFormat, OperandDefinition> FORMAT_MAPPING_TABLE = Map.of(
		DataFormat.NUMERIC, OperandDefinition.FORMAT_NUMERIC_UNPACKED,
		DataFormat.PACKED, OperandDefinition.FORMAT_NUMERIC_PACKED,
		DataFormat.ALPHANUMERIC, OperandDefinition.FORMAT_ALPHANUMERIC_ASCII,
		DataFormat.UNICODE, OperandDefinition.FORMAT_ALPHANUMERIC_UNICODE
	);

	private final List<IDiagnostic> diagnostics = new ArrayList<>();

	public ReadOnlyList<IDiagnostic> checkOperands(Map<IOperandNode, EnumSet<OperandDefinition>> operandChecks)
	{
		for (var queuedCheck : operandChecks.entrySet())
		{
			// TODO: Type inference is also done in the TypeChecker, so at least twice now
			var inferredType = TypeInference.inferType(queuedCheck.getKey());
			inferredType
				.ifPresent(type -> check(queuedCheck.getKey(), type, queuedCheck.getValue()));
		}
		return ReadOnlyList.from(diagnostics);
	}

	private void check(IOperandNode operand, IDataType type, EnumSet<OperandDefinition> definitionTable)
	{
		if (FORMAT_MAPPING_TABLE.get(type.format())instanceof OperandDefinition definition)
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
