package org.amshove.natparse.parsing;

class ParserErrors
{
	public static ParserDiagnostic dataTypeNeedsLength(VariableNode variableNode)
	{
		return ParserDiagnostic.create(
			"Data type <%s> needs to have a length".formatted(variableNode.dataFormat()),
			variableNode,
			ParserError.VARIABLE_LENGTH_MISSING
		);
	}

	public static ParserDiagnostic dynamicVariableLengthNotAllowed(VariableNode variableNode)
	{
		return ParserDiagnostic.create(
			"Dynamic length not allowed for data type <%s>".formatted(variableNode.dataFormat()),
			variableNode,
			ParserError.INVALID_DATA_TYPE_FOR_DYNAMIC_LENGTH
		);
	}
}
