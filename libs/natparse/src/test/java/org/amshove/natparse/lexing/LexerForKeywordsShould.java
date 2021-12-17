package org.amshove.natparse.lexing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class LexerForKeywordsShould extends AbstractLexerTest
{

	@TestFactory
	Iterable<DynamicTest> lexKCheckReservedKeywords()
	{
		return Arrays.asList(
			keywordTest("ABS", SyntaxKind.ABS),
			keywordTest("ACCEPT", SyntaxKind.ACCEPT),
			keywordTest("ADD", SyntaxKind.ADD),
			keywordTest("ALL", SyntaxKind.ALL),
			keywordTest("ANY", SyntaxKind.ANY),
			keywordTest("ASSIGN", SyntaxKind.ASSIGN),
			keywordTest("AT", SyntaxKind.AT),
			keywordTest("ATN", SyntaxKind.ATN),
			keywordTest("AVER", SyntaxKind.AVER),
			keywordTest("BACKOUT", SyntaxKind.BACKOUT),
			keywordTest("BEFORE", SyntaxKind.BEFORE),
			keywordTest("BREAK", SyntaxKind.BREAK),
			keywordTest("BROWSE", SyntaxKind.BROWSE),
			keywordTest("BY", SyntaxKind.BY),
			keywordTest("CALL", SyntaxKind.CALL),
			keywordTest("CALLDBPROC", SyntaxKind.CALLDBPROC),
			keywordTest("CALLNAT", SyntaxKind.CALLNAT),
			keywordTest("CLOSE", SyntaxKind.CLOSE),
			keywordTest("COMMIT", SyntaxKind.COMMIT),
			keywordTest("COMPOSE", SyntaxKind.COMPOSE),
			keywordTest("COMPRESS", SyntaxKind.COMPRESS),
			keywordTest("COMPUTE", SyntaxKind.COMPUTE),
			keywordTest("CONST", SyntaxKind.CONST),
			keywordTest("CONSTANT", SyntaxKind.CONSTANT),
			keywordTest("COPY", SyntaxKind.COPY),
			keywordTest("COS", SyntaxKind.COS),
			keywordTest("COUNT", SyntaxKind.COUNT),
			keywordTest("CREATE", SyntaxKind.CREATE),
			keywordTest("DATA", SyntaxKind.DATA),
			keywordTest("DECIDE", SyntaxKind.DECIDE),
			keywordTest("DEFINE", SyntaxKind.DEFINE),
			keywordTest("DELETE", SyntaxKind.DELETE),
			keywordTest("DISPLAY", SyntaxKind.DISPLAY),
			keywordTest("DIVIDE", SyntaxKind.DIVIDE),
			keywordTest("DLOGOFF", SyntaxKind.DLOGOFF),
			keywordTest("DLOGON", SyntaxKind.DLOGON),
			keywordTest("DNATIVE", SyntaxKind.DNATIVE),
			keywordTest("DO", SyntaxKind.DO),
			keywordTest("DOEND", SyntaxKind.DOEND),
			keywordTest("DOWNLOAD", SyntaxKind.DOWNLOAD),
			keywordTest("DYNAMIC", SyntaxKind.DYNAMIC),
			keywordTest("EJECT", SyntaxKind.EJECT),
			keywordTest("ELSE", SyntaxKind.ELSE),
			keywordTest("END", SyntaxKind.END),
			keywordTest("END-ALL", SyntaxKind.END_ALL),
			keywordTest("END-BEFORE", SyntaxKind.END_BEFORE),
			keywordTest("END-BREAK", SyntaxKind.END_BREAK),
			keywordTest("END-BROWSE", SyntaxKind.END_BROWSE),
			keywordTest("END-DATA", SyntaxKind.END_DATA),
			keywordTest("END-DECIDE", SyntaxKind.END_DECIDE),
			keywordTest("END-ENDDATA", SyntaxKind.END_ENDDATA),
			keywordTest("END-ENDFILE", SyntaxKind.END_ENDFILE),
			keywordTest("END-ENDPAGE", SyntaxKind.END_ENDPAGE),
			keywordTest("END-ERROR", SyntaxKind.END_ERROR),
			keywordTest("END-FILE", SyntaxKind.END_FILE),
			keywordTest("END-FIND", SyntaxKind.END_FIND),
			keywordTest("END-FOR", SyntaxKind.END_FOR),
			keywordTest("END-HISTOGRAM", SyntaxKind.END_HISTOGRAM),
			keywordTest("ENDHOC", SyntaxKind.ENDHOC),
			keywordTest("END-IF", SyntaxKind.END_IF),
			keywordTest("END-LOOP", SyntaxKind.END_LOOP),
			keywordTest("END-NOREC", SyntaxKind.END_NOREC),
			keywordTest("END-PARSE", SyntaxKind.END_PARSE),
			keywordTest("END-PROCESS", SyntaxKind.END_PROCESS),
			keywordTest("END-READ", SyntaxKind.END_READ),
			keywordTest("END-REPEAT", SyntaxKind.END_REPEAT),
			keywordTest("END-RESULT", SyntaxKind.END_RESULT),
			keywordTest("END-SELECT", SyntaxKind.END_SELECT),
			keywordTest("END-SORT", SyntaxKind.END_SORT),
			keywordTest("END-START", SyntaxKind.END_START),
			keywordTest("END-SUBROUTINE", SyntaxKind.END_SUBROUTINE),
			keywordTest("END-TOPPAGE", SyntaxKind.END_TOPPAGE),
			keywordTest("END-WORK", SyntaxKind.END_WORK),
			keywordTest("ENTIRE", SyntaxKind.ENTIRE),
			keywordTest("ESCAPE", SyntaxKind.ESCAPE),
			keywordTest("EXAMINE", SyntaxKind.EXAMINE),
			keywordTest("EXP", SyntaxKind.EXP),
			keywordTest("EXPAND", SyntaxKind.EXPAND),
			keywordTest("EXPORT", SyntaxKind.EXPORT),
			keywordTest("FALSE", SyntaxKind.FALSE),
			keywordTest("FETCH", SyntaxKind.FETCH),
			keywordTest("FILLER", SyntaxKind.FILLER),
			keywordTest("FIND", SyntaxKind.FIND),
			keywordTest("FOR", SyntaxKind.FOR),
			keywordTest("FORMAT", SyntaxKind.FORMAT),
			keywordTest("FRAC", SyntaxKind.FRAC),
			keywordTest("FULL", SyntaxKind.FULL),
			keywordTest("GET", SyntaxKind.GET),
			keywordTest("GLOBAL", SyntaxKind.GLOBAL),
			keywordTest("HISTOGRAM", SyntaxKind.HISTOGRAM),
			keywordTest("IF", SyntaxKind.IF),
			keywordTest("IGNORE", SyntaxKind.IGNORE),
			keywordTest("IMPORT", SyntaxKind.IMPORT),
			keywordTest("INCCONT", SyntaxKind.INCCONT),
			keywordTest("INCDIC", SyntaxKind.INCDIC),
			keywordTest("INCDIR", SyntaxKind.INCDIR),
			keywordTest("INCLUDE", SyntaxKind.INCLUDE),
			keywordTest("INCMAC", SyntaxKind.INCMAC),
			keywordTest("INDEPENDENT", SyntaxKind.INDEPENDENT),
			keywordTest("INIT", SyntaxKind.INIT),
			keywordTest("INPUT", SyntaxKind.INPUT),
			keywordTest("INSERT", SyntaxKind.INSERT),
			keywordTest("INT", SyntaxKind.INT),
			keywordTest("INVESTIGATE", SyntaxKind.INVESTIGATE),
			keywordTest("LIMIT", SyntaxKind.LIMIT),
			keywordTest("LENGTH", SyntaxKind.LENGTH),
			keywordTest("LOCAL", SyntaxKind.LOCAL),
			keywordTest("LOG", SyntaxKind.LOG),
			keywordTest("LOOP", SyntaxKind.LOOP),
			keywordTest("MAP", SyntaxKind.MAP),
			keywordTest("MAX", SyntaxKind.MAX),
			keywordTest("MIN", SyntaxKind.MIN),
			keywordTest("MOVE", SyntaxKind.MOVE),
			keywordTest("MULTIPLY", SyntaxKind.MULTIPLY),
			keywordTest("NAVER", SyntaxKind.NAVER),
			keywordTest("NCOUNT", SyntaxKind.NCOUNT),
			keywordTest("NEWPAGE", SyntaxKind.NEWPAGE),
			keywordTest("NMIN", SyntaxKind.NMIN),
			keywordTest("NONE", SyntaxKind.NONE),
			keywordTest("NULL-HANDLE", SyntaxKind.NULL_HANDLE),
			keywordTest("OBTAIN", SyntaxKind.OBTAIN),
			keywordTest("OF", SyntaxKind.OF),
			keywordTest("OLD", SyntaxKind.OLD),
			keywordTest("ON", SyntaxKind.ON),
			keywordTest("OPEN", SyntaxKind.OPEN),
			keywordTest("OPTIONAL", SyntaxKind.OPTIONAL),
			keywordTest("OPTIONS", SyntaxKind.OPTIONS),
			keywordTest("PARAMETER", SyntaxKind.PARAMETER),
			keywordTest("PARSE", SyntaxKind.PARSE),
			keywordTest("PASSW", SyntaxKind.PASSW),
			keywordTest("PERFORM", SyntaxKind.PERFORM),
			keywordTest("POS", SyntaxKind.POS),
			keywordTest("PRINT", SyntaxKind.PRINT),
			keywordTest("PROCESS", SyntaxKind.PROCESS),
			keywordTest("READ", SyntaxKind.READ),
			keywordTest("REDEFINE", SyntaxKind.REDEFINE),
			keywordTest("REDUCE", SyntaxKind.REDUCE),
			keywordTest("REINPUT", SyntaxKind.REINPUT),
			keywordTest("REJECT", SyntaxKind.REJECT),
			keywordTest("RELEASE", SyntaxKind.RELEASE),
			keywordTest("REPEAT", SyntaxKind.REPEAT),
			keywordTest("REQUEST", SyntaxKind.REQUEST),
			keywordTest("RESET", SyntaxKind.RESET),
			keywordTest("RESIZE", SyntaxKind.RESIZE),
			keywordTest("RESTORE", SyntaxKind.RESTORE),
			keywordTest("RESULT", SyntaxKind.RESULT),
			keywordTest("RET", SyntaxKind.RET),
			keywordTest("RETRY", SyntaxKind.RETRY),
			keywordTest("RETURN", SyntaxKind.RETURN),
			keywordTest("ROLLBACK", SyntaxKind.ROLLBACK),
			keywordTest("RULEVAR", SyntaxKind.RULEVAR),
			keywordTest("RUN", SyntaxKind.RUN),
			keywordTest("SELECT", SyntaxKind.SELECT),
			keywordTest("SEND", SyntaxKind.SEND),
			keywordTest("SEPARATE", SyntaxKind.SEPARATE),
			keywordTest("SET", SyntaxKind.SET),
			keywordTest("SETTIME", SyntaxKind.SETTIME),
			keywordTest("SGN", SyntaxKind.SGN),
			keywordTest("SHOW", SyntaxKind.SHOW),
			keywordTest("SIN", SyntaxKind.SIN),
			keywordTest("SKIP", SyntaxKind.SKIP),
			keywordTest("SORT", SyntaxKind.SORT),
			keywordTest("SORTKEY", SyntaxKind.SORTKEY),
			keywordTest("SQRT", SyntaxKind.SQRT),
			keywordTest("STACK", SyntaxKind.STACK),
			keywordTest("START", SyntaxKind.START),
			keywordTest("STOP", SyntaxKind.STOP),
			keywordTest("STORE", SyntaxKind.STORE),
			keywordTest("SUBSTR", SyntaxKind.SUBSTR),
			keywordTest("SUBSTRING", SyntaxKind.SUBSTRING),
			keywordTest("SUBTRACT", SyntaxKind.SUBTRACT),
			keywordTest("SUM", SyntaxKind.SUM),
			keywordTest("SUSPEND", SyntaxKind.SUSPEND),
			keywordTest("TAN", SyntaxKind.TAN),
			keywordTest("TERMINATE", SyntaxKind.TERMINATE),
			keywordTest("TOP", SyntaxKind.TOP),
			keywordTest("TOTAL", SyntaxKind.TOTAL),
			keywordTest("TRANSFER", SyntaxKind.TRANSFER),
			keywordTest("TRUE", SyntaxKind.TRUE),
			keywordTest("UNTIL", SyntaxKind.UNTIL),
			keywordTest("UPDATE", SyntaxKind.UPDATE),
			keywordTest("USING", SyntaxKind.USING),
			keywordTest("UPLOAD", SyntaxKind.UPLOAD),
			keywordTest("VAL", SyntaxKind.VAL),
			keywordTest("VALUE", SyntaxKind.VALUE),
			keywordTest("VALUES", SyntaxKind.VALUES),
			keywordTest("VIEW", SyntaxKind.VIEW),
			keywordTest("WASTE", SyntaxKind.WASTE),
			keywordTest("WHEN", SyntaxKind.WHEN),
			keywordTest("WHILE", SyntaxKind.WHILE),
			keywordTest("WITH_CTE", SyntaxKind.WITH_CTE),
			keywordTest("WRITE", SyntaxKind.WRITE));
	}

	@TestFactory
	Iterable<DynamicTest> lexAllLightKeywordsAsIdentifiersOrKeyword()
	{
		return Arrays.asList(
			keywordTest("ABSOLUTE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ACTION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ACTIVATION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("AD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("AFTER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("AL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ALARM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ALPHA", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ALPHABETICALLY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("AND", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("APPL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("APPLICATION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ARRAY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("AS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ASC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ASCENDING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ASSIGNING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ASYNC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ATT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ATTRIBUTES", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("AUTH", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("AUTHORIZATION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("AUTO", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("AVG", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("BACKWARD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("BASE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("BETWEEN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("BLOCK", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("BOT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("BOTTOM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("BUT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("BX", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CABINET", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CALLING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CAP", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CAPTIONED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CASE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CDID", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CF", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CHAR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CHARLENGTH", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CHARPOSITION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CHILD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CIPH", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CIPHER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CLASS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CLR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("COALESCE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CODEPAGE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("COMMAND", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CONCAT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CONDITION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CONTEXT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CONTROL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CONVERSATION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("COPIES", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("COUPLED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CURRENT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CURSOR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("CV", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DATAAREA", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DATE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DAY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DAYS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DECIMAL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DEFINITION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DELIMITED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DELIMITER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DELIMITERS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DESC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DESCENDING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DIALOG", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DIALOG-ID", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DIGITS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DIRECTION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DISABLED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DISP", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DISTINCT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DNRET", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DOCUMENT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DU", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("DY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EDITED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EJ", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ENCODED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("END-CLASS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("END-FUNCTION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("END-INTERFACE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("END-METHOD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("END-PARAMETERS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("END-PROPERTY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("END-PROTOTYPE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ENDING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ENTER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ENTR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EQUAL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ERASE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ERROR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ERRORS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ES", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EVEN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EVENT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EVERY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EXCEPT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EXISTS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EXIT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EXTERNAL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("EXTRACTING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FIELD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FIELDS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FILE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FILL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FINAL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FIRST", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FLOAT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FORM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FORMATTED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FORMATTING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FORMS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FORWARD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FOUND", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FRAMED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FROM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FUNCTION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("FUNCTIONS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("GC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("GEN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("GENERATED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("GFID", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("GIVE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("GIVING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("GLOBALS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("GREATER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("GUI", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HANDLE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HAVING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HEADER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HEX", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HOLD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HORIZ", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HORIZONTALLY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HOUR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HOURS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("HW", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("IA", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("IC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ID", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("IDENTICAL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("IM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("IMMEDIATE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("IN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INCLUDED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INCLUDING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INDEX", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INDEXED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INDICATOR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INITIAL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INNER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INSENSITIVE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INTEGER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INTERCEPTED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INTERFACE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INTERFACE4", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INTERMEDIATE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INTERSECT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INTO", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("INVERTED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("IP", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("IS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ISN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("JOIN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("JUST", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("JUSTIFIED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("KD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("KEEP", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("KEY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("KEYS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LANGUAGE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LAST", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LEAVE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LEAVING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LEFT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LESS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LEVEL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LIB", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LIBPW", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LIBRARY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LIBRARY-PASSWORD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LIKE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LINDICATOR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LINES", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LISTED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LOCKS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LOG-LS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LOG-PS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LOGICAL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LOWER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("LS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MACROAREA", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MARK", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MASK", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MCG", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MESSAGES", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("METHOD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MGID", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MICROSECOND", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MINUTE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MODAL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MODIFIED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MODULE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MONTH", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MORE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MOVING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MP", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("MULTI-FETCH", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NAME", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NAMED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NAMESPACE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NATIVE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NO", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NODE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NOHDR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NORMALIZE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NORMALIZED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NOT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NOTIT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NOTITLE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NULL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NUMBER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("NUMERIC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("OBJECT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("OCCURRENCES", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("OFF", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("OFFSET", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ONCE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ONLY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("OPTIMIZE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("OR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ORDER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("OUTER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("OUTPUT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PACKAGESET", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PAGE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PARAMETERS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PARENT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PASS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PASSWORD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PATH", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PATTERN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PA1", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PA2", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PA3", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PEN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF1", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF2", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF3", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF4", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF5", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF6", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF7", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF8", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF9", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF10", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF11", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF12", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF13", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF14", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF15", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF16", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF17", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF18", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF19", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF20", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF21", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF22", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF23", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF24", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF25", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF26", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF27", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF28", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF29", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF30", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF31", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF32", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF33", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF34", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF35", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF36", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF37", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF38", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF39", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF40", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF41", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF42", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF43", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF44", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF45", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF46", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF47", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF48", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF49", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF50", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF51", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF52", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF53", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF54", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF55", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF56", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF57", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF58", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF59", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF60", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF61", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF62", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF63", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF64", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF65", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF66", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF67", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF68", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF69", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF70", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF71", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF72", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF73", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF74", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF75", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF76", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF77", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF78", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF79", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF80", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF81", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF82", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF83", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF84", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF85", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF86", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF87", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF88", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF89", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF90", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF91", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF92", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF93", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF94", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF95", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF96", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF97", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF98", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PF99", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PGDN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PGUP", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PGM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PHYSICAL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("POLICY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("POSITION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PREFIX", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PRINTER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PROCESSING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PROFILE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PROGRAM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PROPERTY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PROTOTYPE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PRTY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("PW", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("QUARTER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("QUERYNO", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("READONLY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RECORD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RECORDS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RECURSIVELY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REFERENCED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REFERENCING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RELATION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RELATIONSHIP", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REMAINDER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REPLACE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REPORT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REPORTER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REPOSITION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REQUIRED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RESETTING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RESPONSE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RETAIN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RETAINED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RETURNS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("REVERSED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RG", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RIGHT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ROUNDED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ROUTINE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ROW", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ROWS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("RS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SA", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SAME", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SCAN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SCREEN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SCROLL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SECOND", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SELECTION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SENSITIVE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SEQUENCE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SERVER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SETS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SF", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SG", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SHORT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SINGLE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SIZE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SOME", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SORTED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SOUND", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SPACE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SPECIFIED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SQL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SQLID", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("STARTING", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("STATEMENT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("STATIC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("STATUS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("STEP", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SUBPROGRAM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SUBPROGRAMS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SUBROUTINE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SUPPRESS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SUPPRESSED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SYMBOL", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SYNC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("SYSTEM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TEXT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TEXTAREA", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TEXTVARIABLE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("THAN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("THEM", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("THEN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("THRU", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TIME", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TIMESTAMP", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TIMEZONE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TITLE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TO", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TP", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TRAILER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TRANSACTION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TRANSLATE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TREQ", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TS", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TYPE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("TYPES", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("UC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("UNDERLINED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("UNION", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("UNIQUE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("UNKNOWN", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("UPPER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("UR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("USED", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("USER", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("VARGRAPHIC", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("VARIABLE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("VARIABLES", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("VERT", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("VERTICALLY", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("VIA", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("WH", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("WHERE", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("WINDOW", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("WITH", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("WORK", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("XML", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("YEAR", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ZD", SyntaxKind.IDENTIFIER_OR_KEYWORD),
			keywordTest("ZP", SyntaxKind.IDENTIFIER_OR_KEYWORD));
	}

	private DynamicTest keywordTest(String keyword, SyntaxKind expectedKind)
	{
		return dynamicTest(keyword, () -> assertTokens(keyword, token(expectedKind, keyword)));
	}
}
