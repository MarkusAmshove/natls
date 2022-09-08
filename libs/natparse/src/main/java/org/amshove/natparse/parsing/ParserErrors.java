package org.amshove.natparse.parsing;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ParserErrors
{
	private static String formatTokenKind(SyntaxToken token)
	{
		if (token == null || token.kind() == null)
		{
			return SyntaxKind.NONE.toString();
		}

		return token.kind().toString();
	}

	public static ParserDiagnostic missingClosingToken(SyntaxKind expectedClosingToken, SyntaxToken openingToken)
	{
		return ParserDiagnostic.create(
			"Missing closing %s for %s".formatted(expectedClosingToken, formatTokenKind(openingToken)),
			openingToken,
			ParserError.UNCLOSED_STATEMENT
		);
	}

	public static ParserDiagnostic unexpectedToken(SyntaxKind expectedToken, TokenList tokens)
	{
		var currentToken = tokens.peek();
		var invalidToken = currentToken != null ? currentToken : tokens.peek(-1);
		var message = currentToken != null ? "Unexpected token <%s>, expected <%s>".formatted(formatTokenKind(invalidToken), expectedToken) : "Unexpected token after this, expected <%s>".formatted(expectedToken);
		return ParserDiagnostic.create(
			message,
			invalidToken,
			ParserError.UNEXPECTED_TOKEN
		);
	}

	public static ParserDiagnostic unexpectedToken(SyntaxKind expectedKind, SyntaxToken invalidToken)
	{
		var message = "Unexpected token <%s>, expected <%s>".formatted(formatTokenKind(invalidToken), expectedKind);
		return ParserDiagnostic.create(
			message,
			invalidToken,
			ParserError.UNEXPECTED_TOKEN
		);
	}

	public static ParserDiagnostic unexpectedToken(List<SyntaxKind> expectedTokenKinds, SyntaxToken invalidToken)
	{
		return ParserDiagnostic.create(
			"Unexpected token <%s>, expected one of <%s>".formatted(formatTokenKind(invalidToken), expectedTokenKinds.stream().map(Enum::toString).collect(Collectors.joining(", "))),
			invalidToken,
			ParserError.UNEXPECTED_TOKEN
		);
	}

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
		if (expectedKinds.length == 1)
		{
			return initValueMismatch(variable, expectedKinds[0]);
		}

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
		return variable.findDescendantToken(SyntaxKind.DYNAMIC)
			.map(dynamicToken -> ParserDiagnostic.create("A variable with a fixed length can't also have dynamic length", dynamicToken, ParserError.DYNAMIC_AND_FIXED_LENGTH))
			.orElse(null);
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
			node.token(),
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

	public static IDiagnostic fillerMustHaveXKeyword(SyntaxToken token)
	{
		return ParserDiagnostic.create(
			"FILLER is missing X after value. (e.g. FILLER 10X)",
			new TokenNode(token),
			ParserError.FILLER_MISSING_X
		);
	}

	public static IDiagnostic redefineTargetCantBeXArray(IArrayDimension dimension)
	{
		return ParserDiagnostic.create(
			"Can not redefine X-Arrays",
			dimension,
			ParserError.REDEFINE_TARGET_CANT_BE_X_ARRAY
		);
	}

	public static IDiagnostic redefineCantTargetDynamic(RedefinitionNode redefinitionNode)
	{
		return ParserDiagnostic.create(
			"REDEFINE can not target variable with dynamic length",
			redefinitionNode,
			ParserError.REDEFINE_TARGET_CANT_BE_DYNAMIC
		);
	}

	public static IDiagnostic redefineCantContainVariableWithDynamicLength(ITypedVariableNode variable)
	{
		return ParserDiagnostic.create(
			"REDEFINE can not contain a variable with dynamic length",
			variable,
			ParserError.REDEFINE_TARGET_CANT_CONTAIN_DYNAMIC
		);
	}

	public static IDiagnostic invalidLengthForDataTypeRange(ITypedVariableNode typeNode, int lowestValue, int highestValue)
	{
		return ParserDiagnostic.create(
			"Invalid length: %s. Has to be in range of %d to %d".formatted(
				DataFormat.formatLength(typeNode.type().length()),
				lowestValue,
				highestValue),
			typeNode,
			ParserError.INVALID_LENGTH_FOR_DATA_TYPE
		);
	}

	public static IDiagnostic invalidLengthForDataType(ITypedVariableNode typeNode, int... possibleValues)
	{
		return ParserDiagnostic.create(
			"Invalid length: %s. Has to be one of %s".formatted(
				DataFormat.formatLength(typeNode.type().length()),
				Arrays.stream(possibleValues).mapToObj(Integer::toString).collect(Collectors.joining(","))),
			typeNode,
			ParserError.INVALID_LENGTH_FOR_DATA_TYPE
		);
	}

	public static IDiagnostic typeCantHaveLength(ITypedVariableNode typeNode, int... possibleValues)
	{
		return ParserDiagnostic.create(
			"Invalid length: Length for %s can not be specified".formatted(DataFormat.formatLength(typeNode.type().length())),
			typeNode,
			ParserError.INVALID_LENGTH_FOR_DATA_TYPE
		);
	}

	public static IDiagnostic unresolvedImport(ITokenNode importNode)
	{
		return ParserDiagnostic.create(
			"Could not resolve external module %s".formatted(importNode.token().symbolName()),
			importNode.token(),
			ParserError.UNRESOLVED_IMPORT
		);
	}

	public static IDiagnostic duplicatedSymbols(ISymbolNode duplicatedSymbol, ISymbolNode firstDeclaration)
	{
		return ParserDiagnostic.create(
			"Symbol with name %s already declared in %s".formatted(
				duplicatedSymbol.declaration().symbolName(),
				firstDeclaration.position().fileNameWithoutExtension()
			),
			duplicatedSymbol,
			ParserError.DUPLICATED_SYMBOL
		);
	}

	public static IDiagnostic duplicatedImport(SyntaxToken identifier)
	{
		return ParserDiagnostic.create(
			"Import with name %s is already defined".formatted(identifier.symbolName()),
			identifier,
			ParserError.DUPLICATED_IMPORT
		);
	}

	public static IDiagnostic keywordUsedAsIdentifier(SyntaxToken currentToken)
	{
		return ParserDiagnostic.create(
			"Keywords used as identifier are discouraged. Consider prefixing it with a #: %s".formatted(currentToken.kind()),
			currentToken,
			ParserError.KEYWORD_USED_AS_IDENTIFIER,
			DiagnosticSeverity.WARNING
		);
	}

	public static IDiagnostic ambiguousSymbolReference(ISymbolReferenceNode symbolReferenceNode, String possibleQualifications)
	{
		return ParserDiagnostic.create(
			"Reference %s is ambiguous and needs to be qualified. Ambiguous with: %s".formatted(symbolReferenceNode.referencingToken().symbolName(), possibleQualifications.trim()),
			symbolReferenceNode,
			ParserError.AMBIGUOUS_VARIABLE_REFERENCE
		);
	}

	public static IDiagnostic invalidPrinterOutputFormat(SyntaxToken token)
	{
		return ParserDiagnostic.create(
			"Printer output '%s' is invalid. It has to be in the form of 'LTPn' ('LTP1' - 'LTP31')",
			token,
			ParserError.INVALID_PRINTER_OUTPUT_FORMAT
		);
	}

	public static IDiagnostic invalidLengthForLiteral(SyntaxToken token, int maxLength)
	{
		return ParserDiagnostic.create(
			"The maximum literal length at this position is restricted to %d".formatted(maxLength),
			token,
			ParserError.INVALID_LENGTH_FOR_LITERAL
		);
	}

	public static IDiagnostic extendedRelationalExpressionCanOnlyBeUsedWithEquals(SyntaxToken token)
	{
		return ParserDiagnostic.create(
			"Extended relational expression can only be compared with =, EQ, EQUAL or EQUAL TO",
			token,
			ParserError.EXTENDED_RELATIONAL_EXPRESSION_NEEDS_EQUAL
		);
	}

	public static IDiagnostic invalidMaskOrScanComparisonOperator(SyntaxToken maskToken)
	{
		return ParserDiagnostic.create(
			"MASK and SCAN can only be compared for direct equality (=, <>, EQ, NE, ...)",
			maskToken,
			ParserError.INVALID_MASK_OR_SCAN_COMPARISON_OPERATOR
		);
	}

	public static IDiagnostic unexpectedToken(SyntaxToken wrongToken, String message)
	{
		return ParserDiagnostic.create(
			message,
			wrongToken,
			ParserError.UNEXPECTED_TOKEN
		);
	}
}
