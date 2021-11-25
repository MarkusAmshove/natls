package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;

import java.util.Arrays;
import java.util.stream.Collectors;

class ParserErrors
{
	public static ParserDiagnostic dataTypeNeedsLength(VariableNode variableNode)
	{
		return ParserDiagnostic.create(
			"Data type <%s> needs to have a length".formatted(variableNode.type().format()),
			variableNode,
			ParserError.VARIABLE_LENGTH_MISSING
		);
	}

	public static ParserDiagnostic dynamicVariableLengthNotAllowed(VariableNode variableNode)
	{
		return ParserDiagnostic.create(
			"Dynamic length not allowed for data type <%s>".formatted(variableNode.type().format()),
			variableNode,
			ParserError.INVALID_DATA_TYPE_FOR_DYNAMIC_LENGTH
		);
	}

	public static ParserDiagnostic initValueMismatch(VariableNode variable, SyntaxKind expectedKind)
	{
		return ParserDiagnostic.create(
			"Type mismatch on initial value. Got <%s> but expected <%s>".formatted(variable.type().initialValue().kind(), expectedKind),
			variable,
			ParserError.INITIAL_VALUE_TYPE_MISMATCH
		);
	}

	public static ParserDiagnostic initValueMismatch(VariableNode variable, SyntaxKind... expectedKinds)
	{
		return ParserDiagnostic.create(
			"Type mismatch on initial value. Got <%s> but expected one of <%s>".formatted(variable.type().initialValue().kind(), Arrays.stream(expectedKinds).map(k -> k.toString()).collect(Collectors.joining(","))),
			variable,
			ParserError.INITIAL_VALUE_TYPE_MISMATCH
		);
	}

	public static ParserDiagnostic emptyInitValue(VariableNode variable)
	{
		return ParserDiagnostic.create(
			"Initial value is empty",
			variable,
			ParserError.EMPTY_INITIAL_VALUE
		);
	}
}
