package org.amshove.natparse.parsing;

public enum ParserError
{
	// TODO: Remove name again
	NO_DEFINE_DATA_FOUND("NPP001", "No DEFINE DATA has been found"),
	MISSING_END_DEFINE("NPP002", "Missing END-DEFINE"),
	UNEXPECTED_TOKEN("NPP003", "Unexpected token"),
	INVALID_DATA_TYPE_FOR_DYNAMIC_LENGTH("NPP004", "Invalid data type for dynamic length"),
	VARIABLE_LENGTH_MISSING("NPP005", "Length of data type is missing"),
	INITIAL_VALUE_TYPE_MISMATCH("NPP006", "Type mismatch on initial value"),
	EMPTY_INITIAL_VALUE("NPP007", "Initial value can't be empty"),
	DYNAMIC_AND_FIXED_LENGTH("NPP008", "Variable with fixed data type length can't also be dynamic"),
	INVALID_ARRAY_BOUND("NPP009", "Invalid array bound"),
	INCOMPLETE_ARRAY_DEFINITION("NPP010", "Incomplete array definition"),
	INDEPENDENT_VARIABLES_NAMING("NPP011", "Invalid naming for independent variable"),
	INDEPENDENT_CANNOT_BE_GROUP("NPP012", "Independent variable can't be a group"),
	GROUP_CANNOT_BE_EMPTY("NPP013", "Groups can't be empty"),
	NO_TARGET_VARIABLE_FOR_REDEFINE_FOUND("NPP014", "Variable to REDEFINE not found"),
	REDEFINE_LENGTH_EXCEEDS_TARGET_LENGTH("NPP015", "Redefined length exceeds target type length"),
	UNRESOLVED_REFERENCE("NPP016", "Unresolved reference"),
	ARRAY_DIMENSION_MUST_BE_CONST_OR_INIT("NPP017", "Array dimension must be CONST or INITialized"),
	BY_VALUE_NOT_ALLOWED_IN_SCOPE("NPP018", "BY VALUE not allowed for this scope"),
	OPTIONAL_NOT_ALLOWED_IN_SCOPE("NPP019", "OPTIONAL not allowed for this scope"),
	TRAILING_TOKEN("NPP020", "Trailing token"),
	FILLER_MISSING_X("NPP021", "Filler missing the 'X'"),
	REDEFINE_TARGET_CANT_BE_X_ARRAY("NPP022", "Target of REDEFINE can't be an X-Array"),
	REDEFINE_TARGET_CANT_BE_DYNAMIC("NPP023", "Target of REDEFINE can't be DYNAMIC"),
	REDEFINE_TARGET_CANT_CONTAIN_DYNAMIC("NPP024", "Target of REDEFINE can't contain DYNAMIC"),
	INVALID_LENGTH_FOR_DATA_TYPE("NPP025", "Invalid length for data type"),
	UNRESOLVED_IMPORT("NPP026", "Unresolved import"),
	DUPLICATED_SYMBOL("NPP027", "Duplicated symbol"),
	DUPLICATED_IMPORT("NPP028", "Duplicated import"),
	KEYWORD_USED_AS_IDENTIFIER("NPP029", "Keyword is used as identifier"),
	AMBIGUOUS_VARIABLE_REFERENCE("NPP030", "Ambiguous variable reference"),
	UNCLOSED_STATEMENT("NPP031", "Unclosed statement"),
	INVALID_PRINTER_OUTPUT_FORMAT("NPP032", ""),
	INVALID_LENGTH_FOR_LITERAL("NPP033", "");

	private final String id;
	private final String name;

	ParserError(String id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public String id()
	{
		return id;
	}

	public String errorName()
	{
		return name;
	}

	/**
	 * Returns whether the given id belongs to a ParserError that indicates that a symbol
	 * or module could not be resolved.
	 */
	public static boolean isUnresolvedError(String id)
	{
		return id.equals(ParserError.UNRESOLVED_IMPORT.id) || id.equals(ParserError.UNRESOLVED_REFERENCE.id);
	}
}
