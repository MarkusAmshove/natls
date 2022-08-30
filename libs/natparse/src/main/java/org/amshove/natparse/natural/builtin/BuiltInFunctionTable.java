package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.DataType;

import java.util.Arrays;
import java.util.Map;

import static org.amshove.natparse.natural.DataFormat.*;

public class BuiltInFunctionTable
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
			unmodifiableVariable(SyntaxKind.TIMX, "Returns the current time of the day as builtin time format", TIME, 0.0),
			unmodifiableVariable(SyntaxKind.TIMN, "Returns the current time of the day as numeric format", NUMERIC, 7.0),
			unmodifiableVariable(SyntaxKind.DATD, "Returns the current date in the format `DD.MM.YY`", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.DAT4D, "Returns the current date in the format `DD.MM.YYYY`", ALPHANUMERIC, 10.0),
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
				""", INTEGER, 4.0),
			unmodifiableVariable(SyntaxKind.ERROR_LINE, "Returns the line of the statement that raised an error", NUMERIC, 4),
			modifiableVariable(SyntaxKind.CURS_LINE, """
				Get or set the number of the line where the cursor is positioned.
				To get the cursor column, use `*CURS-COL`.

				When setting the value, it has to be > 0.

				`*CURS_LINE` may return the following special values:

				 - 0: On the top **or** bottom line of a window
				- -1: On the message line
				- -2: On the info/statistics line
				- -3: On the upper function-key line
				- -4: On the lower function-key line

				""", PACKED, 3),
			unmodifiableVariable(SyntaxKind.DEVICE, """
				Returns the type or mode of the device from which Natural was started.

				It can contain one of the following values:

				`BATCH`: Natural was started with `BATCH` parameter
				`VIDEO`: With a screen (PC screen, VT, X-Term, ...)
				`TTY`: With a teletype or other start/stop device
				`PC`: Natural connection with profile parameter `PC=ON` or terminal command `%+`
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.TPSYS, """
				Returns the Natural name of the TP monitor or environment.

				Will return `NONE` on Windows, UNIX and OpenVMS platforms.

				Can contain one of the following values:

				```
				NONE
				AIM/DC
				CICS
				COMPLETE
				IMS/DC
				OS/400
				SERVSTUB (Natural Development Server)
				TIAM
				TSO
				TSS
				UTM
				```
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.OPSYS, """
				Returns the Natural name of the operating system.

				More in depth information can be retrieved with a combination of `MACHINE-CLASS`, `*HARDWARE` and `*OS`.
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.PROGRAM, "Returns the name of the current Natural object", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.INIT_USER, """
				Returns the value of the profile parameter `USER`.

				If the profile parameter is not specified it will return the UNIX uid of the current user.
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.PF_KEY, """
				Returns the name of the sensitive key that was pressed last.

				If no sensitive key was pressed, will return `ENTR`.

				Possible return values:

				- `PA1 to PA3`: Program attention keys 1 to 3
				- `PF1 to PF48`: Program function keys 1 to 48
				- `ENTR`: The `ENTER` or `RETURN` key.
				- `CLR`: The `CLEAR` key
				- `PEN`: Light pen
				- `PGDN`: `PAGE DOWN` key
				- `PGUP`: `PAGE UP` key

				Notes:

				- If a page break occurs, the value changes to `ENTR`.
				""", ALPHANUMERIC, 4),
			function(SyntaxKind.COUNTER, """
				Returns the number of times a processing loop initiated by `FIND`, `READ`, `HISTOGRAM` or `PARSE` has been entered.

				If a record is rejected through a `WHERE`-clause, `*COUNTER` is not incremented.
				If a record is rejected through `ACCEPT` or `REJECT`, `*COUNTER` is incremented.


				Usage:
				```natural
				#I := *COUNTER
				#I := *COUNTER(RD.)
				```
				""", PACKED, 10, new BuiltInFunctionParameter("label", new DataType(DataFormat.NONE, 1), false)),
			function(SyntaxKind.OCCURRENCE, "See `*OCC`", INTEGER, 4,
					new BuiltInFunctionParameter("array", new DataType(DataFormat.NONE, 1), true),
					new BuiltInFunctionParameter("dimension", new DataType(DataFormat.NONE, 1), false)
			),
			function(SyntaxKind.OCC, """
				Returns the current length of an array.

				The optional `dimension` parameter handles for which dimension the length is returned. Defaults to 1 if not specified.

				Possible value of `dimension`:

				- `1`: One-dimensional array (**default**)
				- `2`: Two-dimensional array
				- `3`: Three-dimensional array
				- `*`: All dimensions defined for the corresponding array apply

				Example:

				```natural
				DEFINE DATA LOCAL
				1 #LENGTH (I4)
				1 #ARRAY (A10/1:*,1:*)
				1 #DIMENSIONS (I4/1:3)
				END-DEFINE

				EXPAND ARRAY #ARRAY TO (1:10,1:20)
				#LENGTH := *OCC(#ARRAY) /* #LENGTH = 10, first dimension
				#LENGTH := *OCC(#ARRAY, 1) /* #LENGTH = 10, first dimension
				#LENGTH := *OCC(#ARRAY, 2) /* #LENGTH = 20, second dimension
				#DIMENSIONS(1:2) := *OCC(#ARRAY, *) /* #DIMENSIONS(1) = 10; #DIMENSIONS(2) = 20
				```
				""", INTEGER, 4,
					new BuiltInFunctionParameter("array", new DataType(DataFormat.NONE, 1), true),
					new BuiltInFunctionParameter("dimension", new DataType(DataFormat.NONE, 1), false)
			),
			function(SyntaxKind.MINVAL, """
				Returns the minimal value of all given operand values.
				
				The result type can be optionally specified with `(IR=`, e.g. `(IR=F8)`. Otherwise the biggest data type of the operands is chosen.
				
				If an array is passed, this function returns the minimum value of all arrays values.
				
				If a binary or alphanumeric value is passed, this function returns the minimum length of the operands.
				""", FLOAT, 8,
				new BuiltInFunctionParameter("operand1", new DataType(NONE, 1), true),
				new BuiltInFunctionParameter("operand2", new DataType(NONE, 1), false),
				new BuiltInFunctionParameter("operand3", new DataType(NONE, 1), false)
			),
			function(SyntaxKind.MAXVAL, """
				Returns the maximum value of all given operand values.
				
				The result type can be optionally specified with `(IR=`, e.g. `(IR=F8)`. Otherwise the biggest data type of the operands is chosen.
				
				If an array is passed, this function returns the maximum value of all arrays values.
				
				If a binary or alphanumeric value is passed, this function returns the maximum length of the operands.
				""", FLOAT, 8,
				new BuiltInFunctionParameter("operand1", new DataType(NONE, 1), true),
				new BuiltInFunctionParameter("operand2", new DataType(NONE, 1), false),
				new BuiltInFunctionParameter("operand3", new DataType(NONE, 1), false)
			),
			function(SyntaxKind.TRIM, """
				Remove all leading and trailing whitespace from an alphanumeric or binary string.

				The content of the passed variable is not modified.
				
				`LEADING` or `TRIALING` can be specified if only one of them should be trimmed.
				
				Example:
				
				```natural
				#NO-LEADING-TRAILING := *TRIM(#ALPHA)
				#NO-LEADING := *TRIM(#ALPHA, LEADING)
				#NO-TRAILING := *TRIM(#ALPHA, TRAILING)
				""", ALPHANUMERIC, DataType.DYNAMIC_LENGTH,
				new BuiltInFunctionParameter("operand", new DataType(ALPHANUMERIC, DataType.DYNAMIC_LENGTH), true)
			),
			modifiableVariable(SyntaxKind.COM, """
				Get or set the value of the communication area which can be used to process data from outside a screen window.

				When a window is active, no data can be entered outside the window.
				If a map contains *COM as modifiable field, it will be available for the user to enter data even though a window is currently active on the screen.
				""", ALPHANUMERIC, 128)
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

	private static Map.Entry<SyntaxKind, SystemFunctionDefinition> function(SyntaxKind kind, String documentation, DataFormat format, double length, BuiltInFunctionParameter... parameter)
	{
		var name = kind.toString().replace("_", "-");
		return Map.entry(kind, new SystemFunctionDefinition("*%s".formatted(name), documentation, new DataType(format, length), Arrays.asList(parameter)));
	}
}
