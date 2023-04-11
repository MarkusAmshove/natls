package org.amshove.natparse.parsing;

public enum ParserError
{
	INTERNAL("NPP000"),
	NO_DEFINE_DATA_FOUND("NPP001"),
	MISSING_END_DEFINE("NPP002"),
	UNEXPECTED_TOKEN("NPP003"),
	INVALID_DATA_TYPE_FOR_DYNAMIC_LENGTH("NPP004"),
	VARIABLE_LENGTH_MISSING("NPP005"),
	INITIAL_VALUE_TYPE_MISMATCH("NPP006"),
	EMPTY_INITIAL_VALUE("NPP007"),
	DYNAMIC_AND_FIXED_LENGTH("NPP008"),
	INVALID_ARRAY_BOUND("NPP009"),
	INCOMPLETE_ARRAY_DEFINITION("NPP010"),
	INDEPENDENT_VARIABLES_NAMING("NPP011"),
	INDEPENDENT_CANNOT_BE_GROUP("NPP012"),
	GROUP_CANNOT_BE_EMPTY("NPP013"),
	NO_TARGET_VARIABLE_FOR_REDEFINE_FOUND("NPP014"),
	REDEFINE_LENGTH_EXCEEDS_TARGET_LENGTH("NPP015"),
	UNRESOLVED_REFERENCE("NPP016"),
	ARRAY_DIMENSION_MUST_BE_CONST_OR_INIT("NPP017"),
	BY_VALUE_NOT_ALLOWED_IN_SCOPE("NPP018"),
	OPTIONAL_NOT_ALLOWED_IN_SCOPE("NPP019"),
	TRAILING_TOKEN("NPP020"),
	FILLER_MISSING_X("NPP021"),
	REDEFINE_TARGET_CANT_BE_X_ARRAY("NPP022"),
	REDEFINE_TARGET_CANT_BE_DYNAMIC("NPP023"),
	REDEFINE_TARGET_CANT_CONTAIN_DYNAMIC("NPP024"),
	INVALID_LENGTH_FOR_DATA_TYPE("NPP025"),
	UNRESOLVED_IMPORT("NPP026"),
	DUPLICATED_SYMBOL("NPP027"),
	DUPLICATED_IMPORT("NPP028"),
	AMBIGUOUS_VARIABLE_REFERENCE("NPP029"),
	UNCLOSED_STATEMENT("NPP030"),
	INVALID_PRINTER_OUTPUT_FORMAT("NPP031"),
	INVALID_LENGTH_FOR_LITERAL("NPP032"),
	EXTENDED_RELATIONAL_EXPRESSION_NEEDS_EQUAL("NPP033"),
	INVALID_MASK_OR_SCAN_COMPARISON_OPERATOR("NPP034"),
	INVALID_OPERAND("NPP035"),
	COMPRESS_HAS_LEAVING_NO_AND_DELIMITERS("NPP036"),
	TYPE_MISMATCH("NPP037"),
	INVALID_LITERAL_VALUE("NPP038"),
	REFERENCE_NOT_MUTABLE("NPP039"),
	UNSUPPORTED_PROGRAMMING_MODE("NPP040");

	private final String id;

	ParserError(String id)
	{
		this.id = id;
	}

	public String id()
	{
		return id;
	}

	/**
	 * Returns whether the given id belongs to a ParserError that indicates that a symbol or module could not be
	 * resolved.
	 */
	public static boolean isUnresolvedError(String id)
	{
		return id.equals(ParserError.UNRESOLVED_IMPORT.id) || id.equals(ParserError.UNRESOLVED_REFERENCE.id);
	}
}
