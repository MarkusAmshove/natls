package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IArrayDimension;
import org.amshove.natparse.natural.ITokenNode;
import org.amshove.natparse.natural.VariableScope;

import java.util.Arrays;
import java.util.stream.Collectors;

class ParserErrors
{
	public static ParserDiagnostic dataTypeNeedsLength(TypedVariableNode variableNode)
	{
		return ParserDiagnostic.create(
			"Data type <%s> needs to have a length".formatted(variableNode.type().format()),
			variableNode,
			ParserError.VARIABLE_LENGTH_MISSING
		);
	}

	public static ParserDiagnostic dynamicVariableLengthNotAllowed(TypedVariableNode variableNode)
	{
		return ParserDiagnostic.create(
			"Dynamic length not allowed for data type <%s>".formatted(variableNode.type().format()),
			variableNode,
			ParserError.INVALID_DATA_TYPE_FOR_DYNAMIC_LENGTH
		);
	}

	public static ParserDiagnostic initValueMismatch(TypedVariableNode variable, SyntaxKind expectedKind)
	{
		return ParserDiagnostic.create(
			"Type mismatch on initial value. Got <%s> but expected <%s>".formatted(variable.type().initialValue().kind(), expectedKind),
			variable,
			ParserError.INITIAL_VALUE_TYPE_MISMATCH
		);
	}

	public static ParserDiagnostic initValueMismatch(TypedVariableNode variable, SyntaxKind... expectedKinds)
	{
		return ParserDiagnostic.create(
			"Type mismatch on initial value. Got <%s> but expected one of <%s>".formatted(variable.type().initialValue().kind(), Arrays.stream(expectedKinds).map(Enum::toString).collect(Collectors.joining(","))),
			variable,
			ParserError.INITIAL_VALUE_TYPE_MISMATCH
		);
	}

	public static ParserDiagnostic emptyInitValue(TypedVariableNode variable)
	{
		return ParserDiagnostic.create(
			"Initial value is empty",
			variable,
			ParserError.EMPTY_INITIAL_VALUE
		);
	}

	public static ParserDiagnostic dynamicAndFixedLength(TypedVariableNode variable)
	{
		var dynamicToken = variable.findDescendantToken(SyntaxKind.DYNAMIC);
		if(dynamicToken != null)
		{
			return ParserDiagnostic.create("A variable with a fixed length can't also have dynamic length", dynamicToken, ParserError.DYNAMIC_AND_FIXED_LENGTH);
		}
		return null;
	}

    public static ParserDiagnostic invalidArrayBound(IArrayDimension dimension, int bound)
	{
		return ParserDiagnostic.create(
			"<%d> is not a valid array bound. Try a number >= 0 or *".formatted(bound),
			dimension,
			ParserError.INVALID_ARRAY_BOUND
		);
    }

	public static ParserDiagnostic incompleteArrayDefinition(BaseSyntaxNode node)
	{
		return ParserDiagnostic.create(
			"Incomplete array definition",
			node,
			ParserError.INCOMPLETE_ARRAY_DEFINITION
		);
	}

	public static ParserDiagnostic invalidAivNaming(VariableNode node)
	{
		return ParserDiagnostic.create(
			"Independent variable name must start with a +",
			node,
			ParserError.INDEPENDENT_VARIABLES_NAMING
		);
	}

	public static ParserDiagnostic independentCantBeGroup(VariableNode variable)
	{
		return ParserDiagnostic.create(
			"Independent variables can't be groups",
			variable,
			ParserError.INDEPENDENT_CANNOT_BE_GROUP
		);
	}

	public static ParserDiagnostic emptyGroupVariable(GroupNode groupNode)
	{
		return ParserDiagnostic.create(
			"Group can not be empty",
			groupNode,
			ParserError.GROUP_CANNOT_BE_EMPTY
		);
	}

	public static ParserDiagnostic noTargetForRedefineFound(RedefinitionNode redefinitionNode)
	{
		return ParserDiagnostic.create(
			"No target for REDEFINE found. The redefined variable must be declared beforehand",
			redefinitionNode,
			ParserError.NO_TARGET_VARIABLE_FOR_REDEFINE_FOUND
		);
	}

	public static ParserDiagnostic redefinitionLengthIsTooLong(RedefinitionNode node, double redefinitionLength, double maxLength)
	{
		return ParserDiagnostic.create(
			"Length of redefinition (%s bytes) exceeds target length (%s bytes)".formatted(DataFormat.formatLength(redefinitionLength), DataFormat.formatLength(maxLength)),
			node,
			ParserError.REDEFINE_LENGTH_EXCEEDS_TARGET_LENGTH
		);
	}

	public static ParserDiagnostic unresolvedReference(ITokenNode node)
	{
		return ParserDiagnostic.create(
			"Unresolved reference: %s".formatted(node.token().source()),
			node,
			ParserError.UNRESOLVED_REFERENCE
		);
	}

	public static ParserDiagnostic arrayDimensionMustBeConstOrInitialized(ITokenNode token)
	{
		return ParserDiagnostic.create(
			"If the array bound is a reference, the referenced variable must either be CONST or INIT",
			token,
			ParserError.ARRAY_DIMENSION_MUST_BE_CONST_OR_INIT
		);
	}

	public static ParserDiagnostic byValueNotAllowedInCurrentScope(TokenNode errorToken, VariableScope currentScope)
	{
		return ParserDiagnostic.create(
			"%s is not allowed in scope %s".formatted(errorToken.token().source(), currentScope),
			errorToken,
			ParserError.BY_VALUE_NOT_ALLOWED_IN_SCOPE
		);
	}

	public static IDiagnostic optionalNotAllowedInCurrentScope(TokenNode errorToken, VariableScope currentScope)
	{
		return ParserDiagnostic.create(
			"OPTIONAL is not allowed in scope %s".formatted(currentScope),
			errorToken,
			ParserError.OPTIONAL_NOT_ALLOWED_IN_SCOPE
		);
	}

	public static IDiagnostic trailingToken(SyntaxToken token)
	{
		return ParserDiagnostic.create(
			"Trailing token <%s> not allowed here".formatted(token.kind()),
			new TokenNode(token),
			ParserError.TRAILING_TOKEN
		);
	}

    public static IDiagnostic fillerMustHaveXKeyword(SyntaxToken token) {
		return ParserDiagnostic.create(
			"FILLER is missing X after value. (e.g. FILLER 10X)",
			new TokenNode(token),
			ParserError.FILLER_MISSING_X
		);
    }
}
