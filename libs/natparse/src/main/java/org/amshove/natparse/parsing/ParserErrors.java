package org.amshove.natparse.parsing;

import org.amshove.natparse.AdditionalDiagnosticInfo;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalProgrammingMode;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class ParserErrors
{

	private static final String PASSED_VARIABLE_DECLARED_HERE = "Passed variable is declared here";
	private static final String RECEIVED_PARAMETER_DECLARED_HERE = "Received parameter is declared here";

	private static SyntaxKind getKindFromInitialValue(IOperandNode node)
	{
		if (node instanceof IStringConcatOperandNode)
		{
			return SyntaxKind.STRING_LITERAL;
		}
		return ((ITokenNode) node).token().kind();
	}

	private static String formatTokenKind(SyntaxToken token)
	{
		if (token == null || token.kind() == null)
		{
			return SyntaxKind.NONE.toString();
		}

		return token.kind().toString();
	}

	public static ParserDiagnostic missingNoneBranch(ISyntaxNode decideNode)
	{
		return ParserDiagnostic.create(
			"DECIDE misses NONE branch",
			decideNode,
			ParserError.DECIDE_MISSES_NONE_BRANCH
		);
	}

	public static ParserDiagnostic missingClosingToken(SyntaxKind expectedClosingToken, SyntaxToken openingToken)
	{
		return ParserDiagnostic.create(
			"Missing closing %s for %s".formatted(expectedClosingToken, formatTokenKind(openingToken)),
			openingToken,
			ParserError.UNCLOSED_STATEMENT
		);
	}

	public static ParserDiagnostic unexpectedTokenUnsafe(SyntaxKind expectedTokenKind, SyntaxToken currentToken)
	{
		return ParserDiagnostic.create(
			"Unexpected currentToken <%s>, expected <%s>".formatted(currentToken.kind(), expectedTokenKind),
			currentToken,
			ParserError.UNEXPECTED_TOKEN
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

	public static ParserDiagnostic unexpectedToken(Collection<SyntaxKind> expectedTokenKinds, TokenList tokens)
	{
		var currentToken = tokens.peek();
		var invalidToken = currentToken != null ? currentToken : tokens.peek(-1);
		var expectedTokens = expectedTokenKinds.stream().map(Enum::toString).collect(Collectors.joining(", "));
		var message = currentToken != null ? "Unexpected token <%s>, expected one of <%s>".formatted(formatTokenKind(invalidToken), expectedTokens) : "Unexpected token after this, expected one of <%s>".formatted(expectedTokens);
		return ParserDiagnostic.create(
			message,
			invalidToken,
			ParserError.UNEXPECTED_TOKEN
		);
	}

	public static ParserDiagnostic dataTypeNeedsLength(TypedVariableNode variableNode)
	{
		return ParserDiagnostic.create(
			"Data type <%s> needs to have a length".formatted(variableNode.type().format()),
			variableNode.identifierNode(),
			ParserError.VARIABLE_LENGTH_MISSING
		);
	}

	public static ParserDiagnostic dynamicVariableLengthNotAllowed(TypedVariableNode variableNode)
	{
		return ParserDiagnostic.create(
			"Dynamic length not allowed for data type <%s>".formatted(variableNode.type().format()),
			variableNode.identifierNode(),
			ParserError.INVALID_DATA_TYPE_FOR_DYNAMIC_LENGTH
		);
	}

	public static ParserDiagnostic initValueMismatch(TypedVariableNode variable, SyntaxKind expectedKind)
	{
		return ParserDiagnostic.create(
			"Type mismatch on initial value. Got <%s> but expected <%s>".formatted(
				getKindFromInitialValue(variable.type().initialValue()), expectedKind
			),
			variable.identifierNode(),
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
			"Type mismatch on initial value. Got <%s> but expected one of <%s>".formatted(
				getKindFromInitialValue(variable.type().initialValue()),
				Arrays.stream(expectedKinds).map(Enum::toString).collect(Collectors.joining(","))
			),
			variable.identifierNode(),
			ParserError.INITIAL_VALUE_TYPE_MISMATCH
		);
	}

	public static ParserDiagnostic emptyInitValue(TypedVariableNode variable)
	{
		return ParserDiagnostic.create(
			"Initial value is empty",
			variable.identifierNode(),
			ParserError.EMPTY_INITIAL_VALUE
		);
	}

	public static ParserDiagnostic dynamicAndFixedLength(TypedVariableNode variable)
	{
		var dynamicToken = variable.findDescendantToken(SyntaxKind.DYNAMIC);
		if (dynamicToken != null)
		{
			return ParserDiagnostic.create(
				"A variable with a fixed length can't also have dynamic length",
				dynamicToken, ParserError.DYNAMIC_AND_FIXED_LENGTH
			);
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

	public static ParserDiagnostic incompleteArrayDefinition(VariableNode node)
	{
		return incompleteArrayDefinition((BaseSyntaxNode) node.identifierNode());
	}

	public static ParserDiagnostic invalidAivNaming(VariableNode node)
	{
		return ParserDiagnostic.create(
			"Independent variable name must start with a +",
			node.identifierNode(),
			ParserError.INDEPENDENT_VARIABLES_NAMING
		);
	}

	public static ParserDiagnostic independentCantBeGroup(VariableNode variable)
	{
		return ParserDiagnostic.create(
			"Independent variables can't be groups",
			variable.identifierNode(),
			ParserError.INDEPENDENT_CANNOT_BE_GROUP
		);
	}

	public static ParserDiagnostic emptyGroupVariable(GroupNode groupNode)
	{
		return ParserDiagnostic.create(
			"Group can not be empty",
			groupNode.identifierNode(),
			ParserError.GROUP_CANNOT_BE_EMPTY
		);
	}

	public static ParserDiagnostic noTargetForRedefineFound(RedefinitionNode redefinitionNode)
	{
		return ParserDiagnostic.create(
			"No target for REDEFINE found. The redefined variable must be declared beforehand",
			redefinitionNode.identifierNode() != null ? redefinitionNode.identifierNode() : redefinitionNode,
			ParserError.NO_TARGET_VARIABLE_FOR_REDEFINE_FOUND
		);
	}

	public static ParserDiagnostic redefinitionLengthIsTooLong(
		IRedefinitionNode node, double redefinitionLength,
		double maxLength
	)
	{
		return ParserDiagnostic.create(
			"Length of redefinition (%s bytes) exceeds target length (%s bytes) of %s".formatted(
				DataFormat.formatLength(redefinitionLength), DataFormat.formatLength(maxLength),
				node.declaration().source()
			),
			node.identifierNode(),
			ParserError.REDEFINE_LENGTH_EXCEEDS_TARGET_LENGTH
		);
	}

	public static ParserDiagnostic unresolvedReference(ITokenNode node)
	{
		var diagnostic = ParserDiagnostic.create(
			"Unresolved reference: %s".formatted(node.token().source()),
			node.token(),
			ParserError.UNRESOLVED_REFERENCE
		);

		if (!node.diagnosticPosition().isSamePositionAs(node.position()))
		{
			diagnostic.addAdditionalInfo(new AdditionalDiagnosticInfo("Used here", node.position()));
		}

		return diagnostic;
	}

	public static ParserDiagnostic unresolvedDdmField(ITokenNode node)
	{
		return unresolvedDdmField(node, node.token().symbolName());
	}

	public static ParserDiagnostic unresolvedDdmField(ITokenNode node, String fieldName)
	{
		return ParserDiagnostic.create(
			"Unresolved DDM field: %s".formatted(fieldName),
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

	public static ParserDiagnostic emhdpmNotAllowedInCurrentScope(TokenNode errorToken, VariableScope currentScope)
	{
		return ParserDiagnostic.create(
			"%s is not allowed in scope %s".formatted(errorToken.token().source(), currentScope),
			errorToken,
			ParserError.EMHDPM_NOT_ALLOWED_IN_SCOPE
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
			redefinitionNode.identifierNode(),
			ParserError.REDEFINE_TARGET_CANT_BE_DYNAMIC
		);
	}

	public static IDiagnostic redefineCantContainVariableWithDynamicLength(ITypedVariableNode variable)
	{
		return ParserDiagnostic.create(
			"REDEFINE can not contain a variable with dynamic length",
			variable.identifierNode(),
			ParserError.REDEFINE_TARGET_CANT_CONTAIN_DYNAMIC
		);
	}

	public static IDiagnostic invalidLengthForDataTypeRange(
		ITypedVariableNode typeNode, int lowestValue,
		int highestValue
	)
	{
		return ParserDiagnostic.create(
			"Invalid length: %s. Has to be in range of %d to %d".formatted(
				DataFormat.formatLength(typeNode.type().length()),
				lowestValue,
				highestValue
			),
			typeNode.identifierNode(),
			ParserError.INVALID_LENGTH_FOR_DATA_TYPE
		);
	}

	public static IDiagnostic invalidLengthForDataType(ITypedVariableNode typeNode, int... possibleValues)
	{
		return ParserDiagnostic.create(
			"Invalid length: %s. Has to be one of %s".formatted(
				DataFormat.formatLength(typeNode.type().length()),
				Arrays.stream(possibleValues).mapToObj(Integer::toString).collect(Collectors.joining(","))
			),
			typeNode.identifierNode(),
			ParserError.INVALID_LENGTH_FOR_DATA_TYPE
		);
	}

	public static IDiagnostic typeCantHaveLength(ITypedVariableNode typeNode, int... possibleValues)
	{
		return ParserDiagnostic.create(
			"Invalid length: Length for %s can not be specified".formatted(
				DataFormat.formatLength(typeNode.type().length())
			),
			typeNode.identifierNode(),
			ParserError.INVALID_LENGTH_FOR_DATA_TYPE
		);
	}

	public static IDiagnostic unresolvedDdm(SyntaxToken token)
	{
		return ParserDiagnostic.create(
			"Could not resolve DDM %s".formatted(token.symbolName()),
			token,
			ParserError.UNRESOLVED_MODULE
		);
	}

	public static ParserDiagnostic unresolvedExternalModule(SyntaxToken token)
	{
		return ParserDiagnostic.create(
			"Could not resolve external module %s".formatted(token.symbolName()),
			token,
			ParserError.UNRESOLVED_MODULE
		);
	}

	public static IDiagnostic duplicatedSymbols(
		ISymbolNode duplicatedSymbol, ISymbolNode firstDeclaration,
		ISyntaxNode diagnosticPosition
	)
	{
		return ParserDiagnostic.create(
			"Symbol with name %s already declared in %s".formatted(
				duplicatedSymbol.declaration().symbolName(),
				firstDeclaration.position().fileNameWithoutExtension()
			),
			diagnosticPosition,
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

	public static IDiagnostic ambiguousSymbolReference(
		ISymbolReferenceNode symbolReferenceNode,
		String possibleQualifications
	)
	{
		return ParserDiagnostic.create(
			"Reference %s is ambiguous and needs to be qualified. Ambiguous with: %s".formatted(
				symbolReferenceNode.referencingToken().symbolName(), possibleQualifications.trim()
			),
			symbolReferenceNode,
			ParserError.AMBIGUOUS_VARIABLE_REFERENCE
		);
	}

	public static IDiagnostic invalidPrinterOutputFormat(SyntaxToken token)
	{
		return ParserDiagnostic.create(
			"Printer output '%s' is invalid. It has to be in the form of 'LTPn' ('LTP1' - 'LTP31'), DUMMY, INFOLINE, SOURCE or NOM".formatted(
				token.kind()
			),
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

	public static IDiagnostic invalidOperand(
		IOperandNode operand, String message,
		StatementListParser.AllowedOperand... allowedOperands
	)
	{
		return ParserDiagnostic.create(
			"Invalid operand: %s (Allowed operands: %s)".formatted(
				message,
				Arrays.stream(allowedOperands).map(Enum::name).collect(Collectors.joining(", "))
			),
			operand,
			ParserError.INVALID_OPERAND
		);
	}

	public static IDiagnostic compressCantHaveLeavingNoAndWithDelimiters(SyntaxToken previousToken)
	{
		return ParserDiagnostic.create(
			"COMPRESS can't have both LEAVING NO and WITH DELIMITERS. LEAVING NO is already implied when specyfing delimiters",
			previousToken,
			ParserError.COMPRESS_HAS_LEAVING_NO_AND_DELIMITERS
		);
	}

	public static IDiagnostic invalidLiteralType(ILiteralNode literal, SyntaxKind... allowedKinds)
	{
		var format = allowedKinds.length == 1
			? allowedKinds[0].name()
			: "one of (" + Arrays.stream(allowedKinds).map(SyntaxKind::name).collect(Collectors.joining(", ")) + ")";
		return ParserDiagnostic.create(
			"Invalid type for literal. Expected %s but got %s".formatted(format, literal.token().kind()),
			literal,
			ParserError.TYPE_MISMATCH
		);
	}

	public static IDiagnostic invalidNumericValue(ILiteralNode node, int actualValue, int allowedValue)
	{
		return ParserDiagnostic.create(
			"Value %d is not allowed here, only %d can be used".formatted(actualValue, allowedValue),
			node,
			ParserError.INVALID_LITERAL_VALUE
		);
	}

	public static IDiagnostic invalidNumericRange(ILiteralNode node, int actualValue, int lowestValue, int highestValue)
	{
		return ParserDiagnostic.create(
			"Constant %d is not within the allowed range of %d to %d (both inclusive)".formatted(
				actualValue,
				lowestValue, highestValue
			),
			node,
			ParserError.INVALID_LITERAL_VALUE
		);
	}

	public static IDiagnostic invalidStringLiteral(IOperandNode node, String actual, List<String> allowed)
	{
		return ParserDiagnostic.create(
			"Value %s is not allowed. Allowed values: %s".formatted(actual, String.join(", ", allowed)),
			node,
			ParserError.INVALID_LITERAL_VALUE
		);
	}

	public static IDiagnostic internalError(String message, ISyntaxNode node)
	{
		return ParserDiagnostic.create(
			"%s. Please raise an issue.".formatted(message),
			node,
			ParserError.INTERNAL
		);
	}

	public static IDiagnostic internal(String message, SyntaxToken token)
	{
		return ParserDiagnostic.create(
			"%s. If you see this, please raise an issue.".formatted(message),
			token,
			ParserError.INTERNAL
		);
	}

	public static IDiagnostic invalidLiteralType(SyntaxToken literalToken, SyntaxKind allowedKind)
	{
		return ParserDiagnostic.create(
			"Invalid literal type. Only %s is allowed".formatted(allowedKind),
			literalToken,
			ParserError.INVALID_LITERAL_VALUE
		);
	}

	public static IDiagnostic referenceNotMutable(String message, SyntaxToken token)
	{
		return ParserDiagnostic.create(
			message,
			token,
			ParserError.REFERENCE_NOT_MUTABLE
		);
	}

	public static IDiagnostic referenceNotMutable(String message, ISyntaxNode node)
	{
		return ParserDiagnostic.create(
			message,
			node,
			ParserError.REFERENCE_NOT_MUTABLE
		);
	}

	public static IDiagnostic typeMismatch(String message, ISyntaxNode node)
	{
		return ParserDiagnostic.create(
			message,
			node,
			ParserError.TYPE_MISMATCH
		);
	}

	public static IDiagnostic unsupportedProgrammingMode(NaturalProgrammingMode mode, Path filePath)
	{
		return ParserDiagnostic.create(
			"Unsupported programming mode: %s. This file will not be parsed or analyzed.".formatted(mode),
			0, 0, 0, 0, filePath, ParserError.UNSUPPORTED_PROGRAMMING_MODE
		);
	}

	public static IDiagnostic invalidModuleType(String message, SyntaxToken errorToken)
	{
		return ParserDiagnostic.create(
			"Invalid module type: %s".formatted(message),
			errorToken,
			ParserError.INVALID_MODULE_TYPE
		);
	}

	public static IDiagnostic invalidArrayAccess(SyntaxToken token, String message)
	{
		return ParserDiagnostic.create(
			message,
			token,
			ParserError.INVALID_ARRAY_ACCESS
		);
	}

	public static IDiagnostic emptyBodyDisallowed(IStatementWithBodyNode statement)
	{
		return ParserDiagnostic.create(
			"Statement must have a body. Add IGNORE if body should be empty.",
			statement,
			ParserError.STATEMENT_HAS_EMPTY_BODY
		);
	}

	public static IDiagnostic emptyBodyDisallowed(SyntaxToken errorToken)
	{
		return ParserDiagnostic.create(
			"Statement must have a body. Add IGNORE if body should be empty.",
			errorToken,
			ParserError.STATEMENT_HAS_EMPTY_BODY
		);
	}

	public static IDiagnostic groupHasMixedConstVariables(ISyntaxNode variable)
	{
		return ParserDiagnostic.create(
			"A group can not have a mix of CONST and non-CONST variables. Either make all CONST or none.",
			variable,
			ParserError.GROUP_HAS_MIXED_CONST
		);
	}

	public static IDiagnostic cyclomaticInclude(SyntaxToken referencingToken)
	{
		return ParserDiagnostic.create(
			"Cyclomatic INCLUDE found. %s is recursively included multiple times.".formatted(
				referencingToken.symbolName()
			),
			referencingToken,
			ParserError.CYCLOMATIC_INCLUDE
		);
	}

	public static IDiagnostic unexpectedTokenWhenIdentifierWasExpected(SyntaxToken token)
	{
		return ParserDiagnostic.create(
			"Identifier expected, but got %s".formatted(token.kind()),
			token,
			ParserError.UNEXPECTED_TOKEN_EXPECTED_IDENTIFIER
		);
	}

	public static IDiagnostic operandExpected(SyntaxToken token)
	{
		return ParserDiagnostic.create(
			"Expected operand, but got %s".formatted(token.kind()),
			token,
			ParserError.UNEXPECTED_TOKEN_EXPECTED_OPERAND
		);
	}

	public static ParserDiagnostic invalidScopeForFileType(
		SyntaxKind expectedScope, SyntaxKind actualScope,
		SyntaxToken position
	)
	{
		return ParserDiagnostic.create(
			"Invalid scope for file type. Expected: %s but got %s".formatted(expectedScope, actualScope),
			position,
			ParserError.INVALID_SCOPE_FOR_FILE_TYPE
		);
	}

	public static ParserDiagnostic variableQualificationNotAllowedHere(String message, IPosition position)
	{
		return ParserDiagnostic.create(
			message,
			position,
			ParserError.VARIABLE_QUALIFICATION_NOT_ALLOWED
		);
	}

	public static IDiagnostic invalidInputStatementAttribute(IAttributeNode statementAttribute)
	{
		return ParserDiagnostic.create(
			"%s is not a valid INPUT attribute at the statement level".formatted(statementAttribute.kind()),
			statementAttribute,
			ParserError.INVALID_INPUT_STATEMENT_ATTRIBUTE
		);
	}

	public static IDiagnostic invalidInputElementAttribute(IAttributeNode attribute)
	{
		return ParserDiagnostic.create(
			"%s is not a valid attribute for an INPUT operand".formatted(attribute.kind()),
			attribute,
			ParserError.INVALID_INPUT_ELEMENT_ATTRIBUTE
		);
	}

	public static IDiagnostic noSourceCodeAllowedAfterEnd(IStatementNode statement)
	{
		return ParserDiagnostic.create(
			"No source code allowed after the END statement",
			statement,
			ParserError.NO_SOURCE_ALLOWED_AFTER_END_STATEMENT
		);
	}

	public static IDiagnostic endStatementMissing(IStatementNode statement)
	{
		return ParserDiagnostic.create(
			"END statement missing after",
			statement,
			ParserError.END_STATEMENT_MISSING
		);
	}

	public static IDiagnostic parameterCountMismatch(ISyntaxNode node, int providedParameter, int expectedParameter)
	{
		return ParserDiagnostic.create(
			"Parameter count mismatch. Expected %d parameter but got %d".formatted(
				expectedParameter,
				providedParameter
			),
			node.diagnosticPosition(),
			ParserError.PARAMETER_COUNT_MISMATCH
		);
	}

	public static IDiagnostic trailingParameter(
		ISyntaxNode node, ISyntaxNode passedParameter, int parameterIndex,
		int expectedParameterCount
	)
	{
		var diagnostic = ParserDiagnostic.create(
			"Trailing parameter number %d. Module only expects %d parameter".formatted(
				parameterIndex,
				expectedParameterCount
			),
			node.diagnosticPosition(),
			ParserError.PARAMETER_COUNT_MISMATCH
		);
		diagnostic.addAdditionalInfo(
			new AdditionalDiagnosticInfo("This parameter is trailing", passedParameter.position())
		);
		return diagnostic;
	}

	public static IDiagnostic missingParameter(ISyntaxNode node, ITypedVariableNode expectedParameter)
	{
		var diagnostic = ParserDiagnostic.create(
			"Expected parameter %s %s not provided".formatted(
				expectedParameter.qualifiedName(),
				expectedParameter.formatTypeForDisplay()
			),
			node.diagnosticPosition(),
			ParserError.PARAMETER_COUNT_MISMATCH
		);
		diagnostic.addAdditionalInfo(
			new AdditionalDiagnosticInfo("This parameter is missing", expectedParameter.position())
		);
		return diagnostic;
	}

	public static IDiagnostic cantSkipParameter(ISkipOperandNode node, ITypedVariableNode expectedParameter)
	{
		return ParserDiagnostic.create(
			"Parameter %s %s can not be skipped".formatted(
				expectedParameter.qualifiedName(),
				expectedParameter.formatTypeForDisplay()
			),
			node.diagnosticPosition(),
			ParserError.PARAMETER_NOT_OPTIONAL
		);
	}

	public static IDiagnostic parameterTypeMismatch(
		ISyntaxNode usagePosition, ISyntaxNode declarationPosition,
		IDataType passedType, ITypedVariableNode receiver
	)
	{
		var receiverType = receiver.type();
		var diagnostic = ParserDiagnostic.create(
			"Parameter type mismatch. Expected %s by reference but got %s".formatted(
				receiverType.toShortString(),
				passedType.toShortString()
			),
			usagePosition.diagnosticPosition(),
			ParserError.PARAMETER_TYPE_MISMATCH_BY_REFERENCE
		);
		if (usagePosition != declarationPosition)
		{
			diagnostic.addAdditionalInfo(
				new AdditionalDiagnosticInfo(
					PASSED_VARIABLE_DECLARED_HERE,
					declarationPosition.diagnosticPosition()
				)
			);
		}
		diagnostic.addAdditionalInfo(
			new AdditionalDiagnosticInfo(RECEIVED_PARAMETER_DECLARED_HERE, receiver.position())
		);
		return diagnostic;
	}

	public static IDiagnostic passedParameterNotArray(
		ISyntaxNode node, int expectedDimensions,
		int passedDimensions, ITypedVariableNode receiver, ISyntaxNode declarationPosition
	)
	{
		var diagnostic = ParserDiagnostic.create(
			"Parameter dimension mismatch. Expected an array with %d dimensions but got %d instead".formatted(
				expectedDimensions, passedDimensions
			),
			node.diagnosticPosition(),
			ParserError.PARAMETER_TYPE_MISMATCH_BY_REFERENCE
		);
		diagnostic.addAdditionalInfo(
			new AdditionalDiagnosticInfo(PASSED_VARIABLE_DECLARED_HERE, declarationPosition.diagnosticPosition())
		);
		diagnostic.addAdditionalInfo(
			new AdditionalDiagnosticInfo(RECEIVED_PARAMETER_DECLARED_HERE, receiver.position())
		);
		return diagnostic;
	}

	public static IDiagnostic parameterDimensionLengthMismatch(
		ISyntaxNode node,
		int dimensionNumber,
		IArrayDimension expectedDimension, IArrayDimension passedDimension,
		ITypedVariableNode receiver, ISyntaxNode declarationPosition
	)
	{
		var diagnostic = ParserDiagnostic.create(
			"Parameter array length mismatch. Expected (%s) but got (%s) in dimension %d".formatted(
				expectedDimension.displayFormat(), passedDimension.displayFormat(), dimensionNumber
			),
			node.diagnosticPosition(),
			ParserError.PARAMETER_TYPE_MISMATCH_BY_REFERENCE
		);
		diagnostic.addAdditionalInfo(
			new AdditionalDiagnosticInfo(PASSED_VARIABLE_DECLARED_HERE, declarationPosition.diagnosticPosition())
		);
		diagnostic.addAdditionalInfo(
			new AdditionalDiagnosticInfo(RECEIVED_PARAMETER_DECLARED_HERE, receiver.position())
		);
		return diagnostic;
	}
}
