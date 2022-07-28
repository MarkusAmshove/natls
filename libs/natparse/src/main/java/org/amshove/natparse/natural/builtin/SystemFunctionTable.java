package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.DataType;

import java.util.Map;

import static org.amshove.natparse.natural.DataFormat.*;

public class SystemFunctionTable
{
	private static final Map<SyntaxKind, IBuiltinFunctionDefinition> TABLE;

	public static IBuiltinFunctionDefinition getDefinition(SyntaxKind kind)
	{
		return TABLE.get(kind);
	}

	static
	{
		TABLE = Map.ofEntries(
			unmodifiableVariable(SyntaxKind.APPLIC_ID, "Returns the ID of the current library", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.INIT_ID, "Returns the ID of the device that Natural invoked", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.TIMX, "Returns the current time of the day", TIME, 0.0),
			unmodifiableVariable(SyntaxKind.TIMN, "Returns the current time of the day", NUMERIC, 7.0),
			unmodifiableVariable(SyntaxKind.DATD, "Returns the current date in the format `DD.MM.YY`", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.DATX, "Returns the current date as internal date format", DATE, 0.0),
			unmodifiableVariable(SyntaxKind.DATN, "Returns the current date in the format `YYYYMMDD`", ALPHANUMERIC, 10.0),
			modifiableVariable(SyntaxKind.LANGUAGE, "Returns the language code, e.g. 1 for english, 2 for german etc.", INTEGER, 1.0),
			modifiableVariable(SyntaxKind.STARTUP, "Get or set the name of the program which will be executed when Natural would show the command prompt", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.STEPLIB, "Returns the name of the current steplib", ALPHANUMERIC, 8.0),
			modifiableVariable(SyntaxKind.PAGE_NUMBER, "Get or set the current page number of an report", PACKED, 5.0),
			unmodifiableVariable(SyntaxKind.WINDOW_PS, "Returns the page size of the logical window (without the frame)", NUMERIC, 3.0),
			unmodifiableVariable(SyntaxKind.LIBRARY_ID, "Returns the ID the the current library. This returns the same as *APPLIC-ID", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.LINEX, """
				Returns the line number of the invocation of this variable.
				When this variable is used within copycodes, it contains the line numbers of all includes leading to this variable.
				
				If this variable is not used within a copy code, it returns the same value as `*LINE`.
				
				Example:
					
				```
				0100 INCLUDE FIRSTCC
				  0200 INCLUDE SCNDCC
				    0300 PRINT *LINEX
				```
				
				In this case the variable returns `0100/0200/0300`.
				""".stripIndent(), ALPHANUMERIC, 100),
			unmodifiableVariable(SyntaxKind.CURRENT_UNIT, "Returns the name of the current executing unit.", ALPHANUMERIC, 32.0),
			modifiableVariable(SyntaxKind.ERROR_NR, """
				Get or set the current error number.
				
				This contains the number of the ERROR that triggered the `ON ERROR` block.
				
				If this is manually set, it terminates execution to the next `ON ERROR` block within the stack.
				
				**Not** modifiable within an `ON ERROR` block.
				
				Value can only range from 0 to 9999.
				""", NUMERIC, 7.0),
			modifiableVariable(SyntaxKind.ERROR_TA, "Get or set the name of the error transaction program which receives control if an error occurs", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.LINE, """
				Returns the number of the line where this variable is used.
				
				If this is inside a copycode, it will return the number within the copycode, not the `INCLUDE`.
				If you want to get all line numbers, including the `INCLUDE`s, use `*LINEX` instead.
				""", INTEGER, 4.0)
		);
	}

	private static Map.Entry<SyntaxKind, SystemVariableDefinition> unmodifiableVariable(SyntaxKind kind, String documentation, DataFormat format, double length)
	{
		return variable(kind, documentation, format, length, false);
	}

	private static Map.Entry<SyntaxKind, SystemVariableDefinition> modifiableVariable(SyntaxKind kind, String documentation, DataFormat format, double length)
	{
		return variable(kind, documentation, format, length, true);
	}

	private static Map.Entry<SyntaxKind, SystemVariableDefinition> variable(SyntaxKind kind, String documentation, DataFormat format, double length, boolean modifiable)
	{
		var name = kind.toString().replace("_", "-");
		return Map.entry(kind, new SystemVariableDefinition("*%s".formatted(name), documentation, new DataType(format, length), modifiable));
	}
}
