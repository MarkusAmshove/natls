package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IArrayDimension;

import java.util.Arrays;
import java.util.stream.Collectors;

class ParserErrors
{
	public static ParserDiagnostic dataTypeNeedsLength(TypedNode variableNode)
	{
		return ParserDiagnostic.create(
			"Data type <%s> needs to have a length".formatted(variableNode.type().format()),
			variableNode,
			ParserError.VARIABLE_LENGTH_MISSING
		);
	}

	public static ParserDiagnostic dynamicVariableLengthNotAllowed(TypedNode variableNode)
	{
		return ParserDiagnostic.create(
			"Dynamic length not allowed for data type <%s>".formatted(variableNode.type().format()),
			variableNode,
			ParserError.INVALID_DATA_TYPE_FOR_DYNAMIC_LENGTH
		);
	}

	public static ParserDiagnostic initValueMismatch(TypedNode variable, SyntaxKind expectedKind)
	{
		return ParserDiagnostic.create(
			"Type mismatch on initial value. Got <%s> but expected <%s>".formatted(variable.type().initialValue().kind(), expectedKind),
			variable,
			ParserError.INITIAL_VALUE_TYPE_MISMATCH
		);
	}

	public static ParserDiagnostic initValueMismatch(TypedNode variable, SyntaxKind... expectedKinds)
	{
		return ParserDiagnostic.create(
			"Type mismatch on initial value. Got <%s> but expected one of <%s>".formatted(variable.type().initialValue().kind(), Arrays.stream(expectedKinds).map(k -> k.toString()).collect(Collectors.joining(","))),
			variable,
			ParserError.INITIAL_VALUE_TYPE_MISMATCH
		);
	}

	public static ParserDiagnostic emptyInitValue(TypedNode variable)
	{
		return ParserDiagnostic.create(
			"Initial value is empty",
			variable,
			ParserError.EMPTY_INITIAL_VALUE
		);
	}

	public static ParserDiagnostic dynamicAndFixedLength(TypedNode variable)
	{
		var dynamicToken = variable.findDirectChildSyntaxToken(SyntaxKind.DYNAMIC);
		if(dynamicToken != null)
		{
			return ParserDiagnostic.create("A variable with a fixed length can't also have dynamic length", dynamicToken, ParserError.DYNAMIC_AND_FIXED_LENGTH);
		}
		return null;
	}

    public static ParserDiagnostic invalidArrayBound(IArrayDimension dimension, int bound)
	{
		return ParserDiagnostic.create(
			"<%d> is not a valid array bound. Try a number > 0 or *".formatted(bound),
			dimension,
			ParserError.INVALID_ARRAY_BOUND
		);
    }
}
