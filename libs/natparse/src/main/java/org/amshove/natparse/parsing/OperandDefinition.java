package org.amshove.natparse.parsing;

/// This enum corresponds to the "Operand Definition Table"
/// from the Natural documentation.
/// These are used for typechecking operands in statements.
public enum OperandDefinition
{
	STRUCTURE_CONSTANT('C'),
	STRUCTURE_SCALAR('S'),
	STRUCTURE_ARRAY('A'),
	STRUCTURE_GROUP('G'),
	STRUCTURE_SYSTEM_VARIABLE('N'),
	STRUCTURE_MODIFIABLE_SYSTEM_VARIABLE_ONLY('M'),
	STRUCTURE_ARITHMETIC_EXPRESSION('E'),

	FORMAT_ALPHANUMERIC_ASCII('A'),
	FORMAT_ALPHANUMERIC_UNICODE('U'),
	FORMAT_NUMERIC_UNPACKED('N'),
	FORMAT_NUMERIC_PACKED('P'),
	FORMAT_INTEGER('I'),
	FORMAT_FLOATING('F'),
	FORMAT_BINARY('B'),
	FORMAT_DATE('D'),
	FORMAT_TIME('T'),
	FORMAT_LOGICAL('L'),
	FORMAT_ATTRIBUTE_CONTROL('C'),
	FORMAT_HANDLE_OF_OBJECT('O'),

	REFERENCING_BY_LABEL_PERMITTED('y'),
	REFERENCING_BY_LABEL_NOT_PERMITTED('n'),

	DYNAMIC_DEFINITION_PERMITTED('y'),
	DYNAMIC_DEFINITION_NOT_PERMITTED('n');

	private final char shortform;

	OperandDefinition(char shortform)
	{
		this.shortform = shortform;
	}

	public char shortform()
	{
		return shortform;
	}

	static final OperandDefinition[] FORMAT_DEFINITIONS = new OperandDefinition[]
	{
		FORMAT_ALPHANUMERIC_ASCII,
		FORMAT_ALPHANUMERIC_UNICODE,
		FORMAT_NUMERIC_UNPACKED,
		FORMAT_NUMERIC_PACKED,
		FORMAT_INTEGER,
		FORMAT_FLOATING,
		FORMAT_BINARY,
		FORMAT_DATE,
		FORMAT_TIME,
		FORMAT_LOGICAL,
		FORMAT_ATTRIBUTE_CONTROL,
		FORMAT_HANDLE_OF_OBJECT
	};
}
