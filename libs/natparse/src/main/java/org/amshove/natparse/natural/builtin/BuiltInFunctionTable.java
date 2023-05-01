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

	static
	{
		TABLE = Map.ofEntries(
			unmodifiableVariable(SyntaxKind.APPLIC_ID, "Returns the ID of the current library", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.INIT_ID, "Returns the ID of the device that Natural invoked", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_TIME, "Returns the current time of the day as A10 in format HH:II:SS.T", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.TIMX, "Returns the current time of the day as builtin time format", TIME, 0.0),
			unmodifiableVariable(SyntaxKind.TIMESTMP, "Returns the machine-internal clock value", BINARY, 8.0),
			unmodifiableVariable(SyntaxKind.TIME_OUT, "Contains the number of seconds remaining before the current transaction will be timed out (Natural Security only).", NUMERIC, 5.0),
			unmodifiableVariable(SyntaxKind.TIMN, "Returns the current time of the day as numeric format", NUMERIC, 7.0),
			function(
				SyntaxKind.TIMD, """
					Returns the time passed since the `SET TIME` statement which is referred to by the first parameter.

					The format returned is: `HHISST` (hour hour minute second second tenth-second).

					Example:

					```
					T1. SET TIME
					PERFORM EXPENSIVE-COMPUTATION
					WRITE 'Computation took' *TIMD(T1.)
					```
					""",
				NUMERIC,
				7.0,
				labelParameter(true)
			),
			unmodifiableVariable(SyntaxKind.DATD, "Returns the current date in the format `DD.MM.YY`", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_DATE, "Returns the current date in the format `DD/MM/YY`", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.DAT4E, "Returns the current date in the format `DD/MM/YYYY`", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.DATG, "Returns the current date in gregorian format `DDmonthnameYYYY`", ALPHANUMERIC, 15.0),
			unmodifiableVariable(SyntaxKind.DAT4D, "Returns the current date in the format `DD.MM.YYYY`", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.DATI, "Returns the current date in the format `YY-MM-DD`", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.DAT4I, "Returns the current date in the format `YYYY-MM-DD`", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.DATJ, "Returns the current date in the format `YYJJJ` (Julian date)", ALPHANUMERIC, 5.0),
			unmodifiableVariable(SyntaxKind.DAT4J, "Returns the current date in the format `YYYYJJJ` (Julian date)", ALPHANUMERIC, 7.0),
			unmodifiableVariable(SyntaxKind.DATX, "Returns the current date as internal date for mat", DATE, 0.0),
			unmodifiableVariable(SyntaxKind.DATN, "Returns the current date in the format `YYYYMMDD`", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.DATU, "Returns the current date in the format `MM/DD/YY`", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.DAT4U, "Returns the current date in the format `MM/DD/YYYY`", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.DATV, "Returns the current date in the format `DD-MON-YY`, where MON is the name of the month", ALPHANUMERIC, 11.0),
			unmodifiableVariable(SyntaxKind.DATVS, "Returns the current date in the format `DDMONYYYY`, where MON is the name of the month", ALPHANUMERIC, 9.0),
			unmodifiableVariable(SyntaxKind.LINESIZE, "Returns the physical line size of the I/O device Natural was started with. For vertical look at `*PAGESIZE`", NUMERIC, 7.0),
			unmodifiableVariable(SyntaxKind.PAGESIZE, "Returns the physical page size of the I/O device Natural was started with. For horizontal look at `*LINESIZE`", NUMERIC, 7.0),
			unmodifiableVariable(SyntaxKind.MACHINE_CLASS, """
				Returns the name of the machine class Natural was started on

				Possible return values:

				- `MAINFRAME`
				- `PC`
				- `UNIX`
				- `VMS`
				""", ALPHANUMERIC, 16.0),
			modifiableVariable(SyntaxKind.LANGUAGE, "Returns the language code, e.g. 1 for english, 2 for german etc.", INTEGER, 1.0),
			modifiableVariable(SyntaxKind.STARTUP, "Get or set the name of the program which will be executed when Natural would show the command prompt", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.STEPLIB, "Returns the name of the current steplib", ALPHANUMERIC, 8.0),
			modifiableVariable(SyntaxKind.PAGE_NUMBER, "Get or set the current page number of an report", PACKED, 5.0),
			unmodifiableVariable(SyntaxKind.LINE_COUNT, "Returns the line number of the current pages's line.", PACKED, 5.0),
			unmodifiableVariable(SyntaxKind.WINDOW_LS, "Returns the line size of the logical window (without the frame)", NUMERIC, 3.0),
			unmodifiableVariable(SyntaxKind.WINDOW_PS, "Returns the page size of the logical window (without the frame)", NUMERIC, 3.0),
			unmodifiableVariable(SyntaxKind.LIBRARY_ID, "Returns the ID the the current library. This returns the same as *APPLIC-ID", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.TRANSLATE, """
				Converts the characters passed as first argument into either `LOWER` or `UPPER` case.

				Accepts an operand of type `A`, `B` or `U`.

				Usage:

				```
				#UPPER := *TRANSLATE(#VAR2, UPPER)
				#LOWER := *TRANSLATE(#VAR2, LOWER)
				""", ALPHANUMERIC, 0),
			modifiableVariable(SyntaxKind.SV_NUMBER, "Get or set the number of record a FIND or HISTOGRAM statement. Uses the innermost statement if no label identifier is passed.", PACKED, 10),
			modifiableVariable(SyntaxKind.SV_LENGTH, "This system variable returns the currently used length of a field defined as dynamic variable in terms of code units; for A and B format the size of one code unit is 1 byte and for U format the size of one code unit is 2 bytes (UTF-16). *LENGTH(field) applies to dynamic variables only.", INTEGER, 4),
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
			modifiableVariable(SyntaxKind.SV_ERROR, "Short form of *ERROR-NR (discouraged)", NUMERIC, 7.0),
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
			unmodifiableVariable(SyntaxKind.CURS_FIELD, """
				Returns the identification of the field in which the cursor is positioned" +
				Can only be used together withe the `POS` function.
				""", INTEGER, 4),
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
			modifiableVariable(SyntaxKind.CURS_COL, """
				Get or set the number of the column where the current cursor is located
				""", PACKED, 3),
			unmodifiableVariable(SyntaxKind.DEVICE, """
				Returns the type or mode of the device from which Natural was started.

				It can contain one of the following values:

				`BATCH`: Natural was started with `BATCH` parameter
				`VIDEO`: With a screen (PC screen, VT, X-Term, ...)
				`TTY`: With a teletype or other start/stop device
				`PC`: Natural connection with profile parameter `PC=ON` or terminal command `%+`
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.CPU_TIME, """
				Returns the CPU time currently used by the Natural process in units of 10 ms.
				""", INTEGER, 4),
			unmodifiableVariable(SyntaxKind.ETID, """
				Returns the current identifier of transaction data for Adabas.

				The default value is one of the following:

				- the value of the Natural profile parameter ETID,
				- the value from the security profile of the currently active user (applies only under Natural Security).
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.INIT_PROGRAM, """
				Return the name of program (transaction) currently executing as Natural.
				""", ALPHANUMERIC, 8),
			function(SyntaxKind.LBOUND, """
				Returns the current lower boundary (index value) of an array for the specified dimension(s) (1, 2 or 3) or for all dimensions (asterisk (*) notation).
				""", INTEGER, 4),
			function(SyntaxKind.UBOUND, """
				Returns the current upper boundary (index value) of an array for the specified dimension(s) (1, 2 or 3) or for all dimensions (asterisk (*) notation).
				""", INTEGER, 4),
			unmodifiableVariable(SyntaxKind.SERVER_TYPE, """
				This system variable indicates the server type Natural has been started as. It can contain one of the following values:

				- DB2-SP	Natural DB2 Stored Procedures server
				- DEVELOP	Natural development server
				- RPC		Natural RPC server
				- WEBIO		Natural Web I/O Interface server

				If Natural is not started as a server, *SERVER-TYPE is set to blanks.
				""", ALPHANUMERIC, 32),
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
			unmodifiableVariable(SyntaxKind.SV_USER, "Returns the user id of the current user, as taken from Natural Security", ALPHANUMERIC, 8),
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
			function(SyntaxKind.SV_ISN, """
				Gets or sets the internal sequence number of the current Adabas record initiated by `FIND` or `READ`.

				Usage:
				```natural
				#ISN := *ISN
				#ISN := *ISN(R1.)
				```
				""", PACKED, 10, labelParameter(false)),
			function(SyntaxKind.COUNTER, """
				Returns the number of times a processing loop initiated by `FIND`, `READ`, `HISTOGRAM` or `PARSE` has been entered.

				If a record is rejected through a `WHERE`-clause, `*COUNTER` is not incremented.
				If a record is rejected through `ACCEPT` or `REJECT`, `*COUNTER` is incremented.


				Usage:
				```natural
				#I := *COUNTER
				#I := *COUNTER(RD.)
				```
				""", PACKED, 10, labelParameter(false)),
			function(
				SyntaxKind.OCCURRENCE, "See `*OCC`", INTEGER, 4,
				new BuiltInFunctionParameter("array", new DataType(DataFormat.NONE, 1), true),
				new BuiltInFunctionParameter("dimension", new DataType(DataFormat.NONE, 1), false)
			),
			function(
				SyntaxKind.OCC, """
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
			function(
				SyntaxKind.MINVAL, """
					Returns the minimal value of all given operand values.

					The result type can be optionally specified with `(IR=`, e.g. `(IR=F8)`. Otherwise the biggest data type of the operands is chosen.

					If an array is passed, this function returns the minimum value of all arrays values.

					If a binary or alphanumeric value is passed, this function returns the minimum length of the operands.
					""", FLOAT, 8,
				new BuiltInFunctionParameter("operand1", new DataType(NONE, 1), true),
				new BuiltInFunctionParameter("operand2", new DataType(NONE, 1), false),
				new BuiltInFunctionParameter("operand3", new DataType(NONE, 1), false)
			),
			function(
				SyntaxKind.MAXVAL, """
					Returns the maximum value of all given operand values.

					The result type can be optionally specified with `(IR=`, e.g. `(IR=F8)`. Otherwise the biggest data type of the operands is chosen.

					If an array is passed, this function returns the maximum value of all arrays values.

					If a binary or alphanumeric value is passed, this function returns the maximum length of the operands.
					""", FLOAT, 8,
				new BuiltInFunctionParameter("operand1", new DataType(NONE, 1), true),
				new BuiltInFunctionParameter("operand2", new DataType(NONE, 1), false),
				new BuiltInFunctionParameter("operand3", new DataType(NONE, 1), false)
			),
			function(
				SyntaxKind.TRIM, """
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
				""", ALPHANUMERIC, 128),
			unmodifiableVariable(SyntaxKind.SV_DATA, """
				Returns the number of elements in the Natural stack available for next `INPUT`.

				`0` is returned if the stack is empty.
				`-1` is returned if the next value in the stack is a command or name of a transaction
				""", NUMERIC, 3),
			unmodifiableVariable(SyntaxKind.SV_LEVEL, """
				Returns the level number of the current program, dialog, ... which is currently active.

				Level 1 is the main program.
				""", NUMERIC, 2)
		);
	}

	public static IBuiltinFunctionDefinition getDefinition(SyntaxKind kind)
	{
		return TABLE.get(kind);
	}

	private static BuiltInFunctionParameter labelParameter(boolean mandatory)
	{
		return new BuiltInFunctionParameter("label", new DataType(DataFormat.NONE, 1), mandatory);
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

	private BuiltInFunctionTable()
	{}
}
