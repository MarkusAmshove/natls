package org.amshove.natparse.lexing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class LexerForKeywordsShould extends AbstractLexerTest
{

	@Test
	void recognizeKeywordsWhenACommentDirectlyFollows()
	{
		assertTokens("END-SUBROUTINE/* Comment", token(SyntaxKind.END_SUBROUTINE));
	}

	@TestFactory
	Iterable<DynamicTest> lexKCheckReservedKeywords()
	{
		return Arrays.asList(
			keywordTest("ABS", SyntaxKind.ABS),
			keywordTest("ABSOLUTE", SyntaxKind.ABSOLUTE),
			keywordTest("ACCEPT", SyntaxKind.ACCEPT),
			keywordTest("ACTION", SyntaxKind.ACTION),
			keywordTest("ACTIVATION", SyntaxKind.ACTIVATION),
			keywordTest("AD", SyntaxKind.AD),
			keywordTest("CD", SyntaxKind.CD),
			keywordTest("ADD", SyntaxKind.ADD),
			keywordTest("AFTER", SyntaxKind.AFTER),
			keywordTest("AL", SyntaxKind.AL),
			keywordTest("ALARM", SyntaxKind.ALARM),
			keywordTest("ALL", SyntaxKind.ALL),
			keywordTest("ALPHA", SyntaxKind.ALPHA),
			keywordTest("ALPHABETICALLY", SyntaxKind.ALPHABETICALLY),
			keywordTest("AND", SyntaxKind.AND),
			keywordTest("ANY", SyntaxKind.ANY),
			keywordTest("APPL", SyntaxKind.APPL),
			keywordTest("APPLICATION", SyntaxKind.APPLICATION),
			keywordTest("ARRAY", SyntaxKind.ARRAY),
			keywordTest("AS", SyntaxKind.AS),
			keywordTest("ASC", SyntaxKind.ASC),
			keywordTest("ASCENDING", SyntaxKind.ASCENDING),
			keywordTest("ASSIGN", SyntaxKind.ASSIGN),
			keywordTest("ASSIGNING", SyntaxKind.ASSIGNING),
			keywordTest("ASYNC", SyntaxKind.ASYNC),
			keywordTest("AT", SyntaxKind.AT),
			keywordTest("ATN", SyntaxKind.ATN),
			keywordTest("ATT", SyntaxKind.ATT),
			keywordTest("ATTRIBUTES", SyntaxKind.ATTRIBUTES),
			keywordTest("AUTH", SyntaxKind.AUTH),
			keywordTest("AUTHORIZATION", SyntaxKind.AUTHORIZATION),
			keywordTest("AUTO", SyntaxKind.AUTO),
			keywordTest("AVER", SyntaxKind.AVER),
			keywordTest("AVG", SyntaxKind.AVG),
			keywordTest("BACKOUT", SyntaxKind.BACKOUT),
			keywordTest("BACKWARD", SyntaxKind.BACKWARD),
			keywordTest("BASE", SyntaxKind.BASE),
			keywordTest("BEFORE", SyntaxKind.BEFORE),
			keywordTest("BETWEEN", SyntaxKind.BETWEEN),
			keywordTest("BLOCK", SyntaxKind.BLOCK),
			keywordTest("BOT", SyntaxKind.BOT),
			keywordTest("BOTTOM", SyntaxKind.BOTTOM),
			keywordTest("BREAK", SyntaxKind.BREAK),
			keywordTest("BROWSE", SyntaxKind.BROWSE),
			keywordTest("BUT", SyntaxKind.BUT),
			keywordTest("BX", SyntaxKind.BX),
			keywordTest("BY", SyntaxKind.BY),
			keywordTest("CABINET", SyntaxKind.CABINET),
			keywordTest("CALL", SyntaxKind.CALL),
			keywordTest("CALLDBPROC", SyntaxKind.CALLDBPROC),
			keywordTest("CALLING", SyntaxKind.CALLING),
			keywordTest("CALLNAT", SyntaxKind.CALLNAT),
			keywordTest("CAP", SyntaxKind.CAP),
			keywordTest("CAPTIONED", SyntaxKind.CAPTIONED),
			keywordTest("CASE", SyntaxKind.CASE),
			keywordTest("CC", SyntaxKind.CC),
			keywordTest("CD", SyntaxKind.CD),
			keywordTest("DF", SyntaxKind.DF),
			keywordTest("CDID", SyntaxKind.CDID),
			keywordTest("CF", SyntaxKind.CF),
			keywordTest("CHAR", SyntaxKind.CHAR),
			keywordTest("CHARLENGTH", SyntaxKind.CHARLENGTH),
			keywordTest("CHARPOSITION", SyntaxKind.CHARPOSITION),
			keywordTest("CHILD", SyntaxKind.CHILD),
			keywordTest("CIPH", SyntaxKind.CIPH),
			keywordTest("CIPHER", SyntaxKind.CIPHER),
			keywordTest("CLASS", SyntaxKind.CLASS),
			keywordTest("CLOSE", SyntaxKind.CLOSE),
			keywordTest("COALESCE", SyntaxKind.COALESCE),
			keywordTest("CODEPAGE", SyntaxKind.CODEPAGE),
			keywordTest("COMMAND", SyntaxKind.COMMAND),
			keywordTest("COMMIT", SyntaxKind.COMMIT),
			keywordTest("COMPOSE", SyntaxKind.COMPOSE),
			keywordTest("COMPRESS", SyntaxKind.COMPRESS),
			keywordTest("COMPUTE", SyntaxKind.COMPUTE),
			keywordTest("CONCAT", SyntaxKind.CONCAT),
			keywordTest("CONDITION", SyntaxKind.CONDITION),
			keywordTest("CONST", SyntaxKind.CONST),
			keywordTest("CONSTANT", SyntaxKind.CONSTANT),
			keywordTest("CONTEXT", SyntaxKind.CONTEXT),
			keywordTest("CONTROL", SyntaxKind.CONTROL),
			keywordTest("CONVERSATION", SyntaxKind.CONVERSATION),
			keywordTest("COPIES", SyntaxKind.COPIES),
			keywordTest("COPY", SyntaxKind.COPY),
			keywordTest("COS", SyntaxKind.COS),
			keywordTest("COUNT", SyntaxKind.COUNT),
			keywordTest("COUPLED", SyntaxKind.COUPLED),
			keywordTest("CS", SyntaxKind.CS),
			keywordTest("CURRENT", SyntaxKind.CURRENT),
			keywordTest("CURSOR", SyntaxKind.CURSOR),
			keywordTest("DATA", SyntaxKind.DATA),
			keywordTest("DATAAREA", SyntaxKind.DATAAREA),
			keywordTest("DATE", SyntaxKind.DATE),
			keywordTest("DAY", SyntaxKind.DAY),
			keywordTest("DAYS", SyntaxKind.DAYS),
			keywordTest("DC", SyntaxKind.DC),
			keywordTest("DECIDE", SyntaxKind.DECIDE),
			keywordTest("DECIMAL", SyntaxKind.DECIMAL),
			keywordTest("DEFINE", SyntaxKind.DEFINE),
			keywordTest("DEFINITION", SyntaxKind.DEFINITION),
			keywordTest("DELETE", SyntaxKind.DELETE),
			keywordTest("DELIMITED", SyntaxKind.DELIMITED),
			keywordTest("DELIMITER", SyntaxKind.DELIMITER),
			keywordTest("DELIMITERS", SyntaxKind.DELIMITERS),
			keywordTest("DESC", SyntaxKind.DESC),
			keywordTest("DESCENDING", SyntaxKind.DESCENDING),
			keywordTest("DIALOG", SyntaxKind.DIALOG),
			keywordTest("DIALOG-ID", SyntaxKind.DIALOG_ID),
			keywordTest("DIGITS", SyntaxKind.DIGITS),
			keywordTest("DIRECTION", SyntaxKind.DIRECTION),
			keywordTest("DISABLED", SyntaxKind.DISABLED),
			keywordTest("DISP", SyntaxKind.DISP),
			keywordTest("DISPLAY", SyntaxKind.DISPLAY),
			keywordTest("DISTINCT", SyntaxKind.DISTINCT),
			keywordTest("DIVIDE", SyntaxKind.DIVIDE),
			keywordTest("DL", SyntaxKind.DL),
			keywordTest("DLOGOFF", SyntaxKind.DLOGOFF),
			keywordTest("DLOGON", SyntaxKind.DLOGON),
			keywordTest("DNATIVE", SyntaxKind.DNATIVE),
			keywordTest("DNRET", SyntaxKind.DNRET),
			keywordTest("DO", SyntaxKind.DO),
			keywordTest("DOCUMENT", SyntaxKind.DOCUMENT),
			keywordTest("DOEND", SyntaxKind.DOEND),
			keywordTest("DOWNLOAD", SyntaxKind.DOWNLOAD),
			keywordTest("DU", SyntaxKind.DU),
			keywordTest("DY", SyntaxKind.DY),
			keywordTest("DYNAMIC", SyntaxKind.DYNAMIC),
			keywordTest("EDITED", SyntaxKind.EDITED),
			keywordTest("EJ", SyntaxKind.EJ),
			keywordTest("EJECT", SyntaxKind.EJECT),
			keywordTest("ELSE", SyntaxKind.ELSE),
			keywordTest("EM", SyntaxKind.EM),
			keywordTest("ENCODED", SyntaxKind.ENCODED),
			keywordTest("END", SyntaxKind.END),
			keywordTest("END-ALL", SyntaxKind.END_ALL),
			keywordTest("END-BEFORE", SyntaxKind.END_BEFORE),
			keywordTest("END-BREAK", SyntaxKind.END_BREAK),
			keywordTest("END-BROWSE", SyntaxKind.END_BROWSE),
			keywordTest("END-CLASS", SyntaxKind.END_CLASS),
			keywordTest("END-DECIDE", SyntaxKind.END_DECIDE),
			keywordTest("END-DEFINE", SyntaxKind.END_DEFINE),
			keywordTest("END-ENDDATA", SyntaxKind.END_ENDDATA),
			keywordTest("END-ENDFILE", SyntaxKind.END_ENDFILE),
			keywordTest("END-ENDPAGE", SyntaxKind.END_ENDPAGE),
			keywordTest("END-ERROR", SyntaxKind.END_ERROR),
			keywordTest("END-FILE", SyntaxKind.END_FILE),
			keywordTest("END-FIND", SyntaxKind.END_FIND),
			keywordTest("END-FOR", SyntaxKind.END_FOR),
			keywordTest("END-FUNCTION", SyntaxKind.END_FUNCTION),
			keywordTest("END-HISTOGRAM", SyntaxKind.END_HISTOGRAM),
			keywordTest("ENDHOC", SyntaxKind.ENDHOC),
			keywordTest("END-IF", SyntaxKind.END_IF),
			keywordTest("END-INTERFACE", SyntaxKind.END_INTERFACE),
			keywordTest("END-LOOP", SyntaxKind.END_LOOP),
			keywordTest("END-METHOD", SyntaxKind.END_METHOD),
			keywordTest("END-NOREC", SyntaxKind.END_NOREC),
			keywordTest("END-PARAMETERS", SyntaxKind.END_PARAMETERS),
			keywordTest("END-PARSE", SyntaxKind.END_PARSE),
			keywordTest("END-PROCESS", SyntaxKind.END_PROCESS),
			keywordTest("END-PROPERTY", SyntaxKind.END_PROPERTY),
			keywordTest("END-PROTOTYPE", SyntaxKind.END_PROTOTYPE),
			keywordTest("END-READ", SyntaxKind.END_READ),
			keywordTest("END-REPEAT", SyntaxKind.END_REPEAT),
			keywordTest("END-RESULT", SyntaxKind.END_RESULT),
			keywordTest("END-SELECT", SyntaxKind.END_SELECT),
			keywordTest("END-SORT", SyntaxKind.END_SORT),
			keywordTest("END-START", SyntaxKind.END_START),
			keywordTest("END-SUBROUTINE", SyntaxKind.END_SUBROUTINE),
			keywordTest("END-TOPPAGE", SyntaxKind.END_TOPPAGE),
			keywordTest("END-WORK", SyntaxKind.END_WORK),
			keywordTest("ENDING", SyntaxKind.ENDING),
			keywordTest("ENTER", SyntaxKind.ENTER),
			keywordTest("ENTIRE", SyntaxKind.ENTIRE),
			keywordTest("EQ", SyntaxKind.EQ),
			keywordTest("EQUAL", SyntaxKind.EQUAL),
			keywordTest("ERASE", SyntaxKind.ERASE),
			keywordTest("ERROR", SyntaxKind.ERROR),
			keywordTest("ERRORS", SyntaxKind.ERRORS),
			keywordTest("ES", SyntaxKind.ES),
			keywordTest("ESCAPE", SyntaxKind.ESCAPE),
			keywordTest("EVEN", SyntaxKind.EVEN),
			keywordTest("EVENT", SyntaxKind.EVENT),
			keywordTest("EVERY", SyntaxKind.EVERY),
			keywordTest("EXAMINE", SyntaxKind.EXAMINE),
			keywordTest("EXCEPT", SyntaxKind.EXCEPT),
			keywordTest("EXISTS", SyntaxKind.EXISTS),
			keywordTest("EXIT", SyntaxKind.EXIT),
			keywordTest("EXP", SyntaxKind.EXP),
			keywordTest("EXPAND", SyntaxKind.EXPAND),
			keywordTest("EXPORT", SyntaxKind.EXPORT),
			keywordTest("EXTERNAL", SyntaxKind.EXTERNAL),
			keywordTest("EXTRACTING", SyntaxKind.EXTRACTING),
			keywordTest("FALSE", SyntaxKind.FALSE),
			keywordTest("FC", SyntaxKind.FC),
			keywordTest("FETCH", SyntaxKind.FETCH),
			keywordTest("FIELD", SyntaxKind.FIELD),
			keywordTest("FIELDS", SyntaxKind.FIELDS),
			keywordTest("FILE", SyntaxKind.FILE),
			keywordTest("FILL", SyntaxKind.FILL),
			keywordTest("FILLER", SyntaxKind.FILLER),
			keywordTest("FINAL", SyntaxKind.FINAL),
			keywordTest("FIND", SyntaxKind.FIND),
			keywordTest("FIRST", SyntaxKind.FIRST),
			keywordTest("FL", SyntaxKind.FL),
			keywordTest("FLOAT", SyntaxKind.FLOAT),
			keywordTest("FOR", SyntaxKind.FOR),
			keywordTest("FORM", SyntaxKind.FORM),
			keywordTest("FORMAT", SyntaxKind.FORMAT),
			keywordTest("FORMATTED", SyntaxKind.FORMATTED),
			keywordTest("FORMATTING", SyntaxKind.FORMATTING),
			keywordTest("FORMS", SyntaxKind.FORMS),
			keywordTest("FORWARD", SyntaxKind.FORWARD),
			keywordTest("FOUND", SyntaxKind.FOUND),
			keywordTest("FRAC", SyntaxKind.FRAC),
			keywordTest("FRAMED", SyntaxKind.FRAMED),
			keywordTest("FROM", SyntaxKind.FROM),
			keywordTest("FS", SyntaxKind.FS),
			keywordTest("FULL", SyntaxKind.FULL),
			keywordTest("FUNCTION", SyntaxKind.FUNCTION),
			keywordTest("FUNCTIONS", SyntaxKind.FUNCTIONS),
			keywordTest("GC", SyntaxKind.GC),
			keywordTest("GE", SyntaxKind.GE),
			keywordTest("GEN", SyntaxKind.GEN),
			keywordTest("GENERATED", SyntaxKind.GENERATED),
			keywordTest("GET", SyntaxKind.GET),
			keywordTest("GFID", SyntaxKind.GFID),
			keywordTest("GIVE", SyntaxKind.GIVE),
			keywordTest("GIVING", SyntaxKind.GIVING),
			keywordTest("GLOBAL", SyntaxKind.GLOBAL),
			keywordTest("GLOBALS", SyntaxKind.GLOBALS),
			keywordTest("GREATER", SyntaxKind.GREATER),
			keywordTest("GT", SyntaxKind.GT),
			keywordTest("GUI", SyntaxKind.GUI),
			keywordTest("HANDLE", SyntaxKind.HANDLE),
			keywordTest("HAVING", SyntaxKind.HAVING),
			keywordTest("HC", SyntaxKind.HC),
			keywordTest("HD", SyntaxKind.HD),
			keywordTest("HE", SyntaxKind.HE),
			keywordTest("HEADER", SyntaxKind.HEADER),
			keywordTest("HEX", SyntaxKind.HEX),
			keywordTest("HISTOGRAM", SyntaxKind.HISTOGRAM),
			keywordTest("HELP", SyntaxKind.HELP),
			keywordTest("HOLD", SyntaxKind.HOLD),
			keywordTest("HORIZ", SyntaxKind.HORIZ),
			keywordTest("HORIZONTALLY", SyntaxKind.HORIZONTALLY),
			keywordTest("HOUR", SyntaxKind.HOUR),
			keywordTest("HOURS", SyntaxKind.HOURS),
			keywordTest("HW", SyntaxKind.HW),
			keywordTest("IA", SyntaxKind.IA),
			keywordTest("IC", SyntaxKind.IC),
			keywordTest("ID", SyntaxKind.ID),
			keywordTest("IDENTICAL", SyntaxKind.IDENTICAL),
			keywordTest("IF", SyntaxKind.IF),
			keywordTest("IGNORE", SyntaxKind.IGNORE),
			keywordTest("IM", SyntaxKind.IM),
			keywordTest("IMMEDIATE", SyntaxKind.IMMEDIATE),
			keywordTest("IMPORT", SyntaxKind.IMPORT),
			keywordTest("IN", SyntaxKind.IN),
			keywordTest("INC", SyntaxKind.INC),
			keywordTest("INCCONT", SyntaxKind.INCCONT),
			keywordTest("INCDIC", SyntaxKind.INCDIC),
			keywordTest("INCDIR", SyntaxKind.INCDIR),
			keywordTest("INCLUDE", SyntaxKind.INCLUDE),
			keywordTest("INCLUDED", SyntaxKind.INCLUDED),
			keywordTest("INCLUDING", SyntaxKind.INCLUDING),
			keywordTest("INCMAC", SyntaxKind.INCMAC),
			keywordTest("INDEPENDENT", SyntaxKind.INDEPENDENT),
			keywordTest("INDEX", SyntaxKind.INDEX),
			keywordTest("INDEXED", SyntaxKind.INDEXED),
			keywordTest("INDICATOR", SyntaxKind.INDICATOR),
			keywordTest("INIT", SyntaxKind.INIT),
			keywordTest("INITIAL", SyntaxKind.INITIAL),
			keywordTest("INNER", SyntaxKind.INNER),
			keywordTest("INPUT", SyntaxKind.INPUT),
			keywordTest("INSENSITIVE", SyntaxKind.INSENSITIVE),
			keywordTest("INSERT", SyntaxKind.INSERT),
			keywordTest("INT", SyntaxKind.INT),
			keywordTest("INTEGER", SyntaxKind.INTEGER),
			keywordTest("INTERCEPTED", SyntaxKind.INTERCEPTED),
			keywordTest("INTERFACE", SyntaxKind.INTERFACE),
			keywordTest("INTERFACE4", SyntaxKind.INTERFACE4),
			keywordTest("INTERMEDIATE", SyntaxKind.INTERMEDIATE),
			keywordTest("INTERSECT", SyntaxKind.INTERSECT),
			keywordTest("INTO", SyntaxKind.INTO),
			keywordTest("INVERTED", SyntaxKind.INVERTED),
			keywordTest("INVESTIGATE", SyntaxKind.INVESTIGATE),
			keywordTest("IP", SyntaxKind.IP),
			keywordTest("IS", SyntaxKind.IS),
			keywordTest("ISN", SyntaxKind.KW_ISN),
			keywordTest("JOIN", SyntaxKind.JOIN),
			keywordTest("JUST", SyntaxKind.JUST),
			keywordTest("JUSTIFIED", SyntaxKind.JUSTIFIED),
			keywordTest("KD", SyntaxKind.KD),
			keywordTest("DEL", SyntaxKind.DEL),
			keywordTest("KEEP", SyntaxKind.KEEP),
			keywordTest("KEY", SyntaxKind.KEY),
			keywordTest("KEYS", SyntaxKind.KEYS),
			keywordTest("LANGUAGE", SyntaxKind.LANGUAGE),
			keywordTest("LAST", SyntaxKind.LAST),
			keywordTest("LC", SyntaxKind.LC),
			keywordTest("LE", SyntaxKind.LE),
			keywordTest("LEAVE", SyntaxKind.LEAVE),
			keywordTest("LEAVING", SyntaxKind.LEAVING),
			keywordTest("LEFT", SyntaxKind.LEFT),
			keywordTest("LENGTH", SyntaxKind.LENGTH),
			keywordTest("LESS", SyntaxKind.LESS),
			keywordTest("LEVEL", SyntaxKind.LEVEL),
			keywordTest("LIB", SyntaxKind.LIB),
			keywordTest("LIBPW", SyntaxKind.LIBPW),
			keywordTest("LIBRARY", SyntaxKind.LIBRARY),
			keywordTest("LIBRARY-PASSWORD", SyntaxKind.LIBRARY_PASSWORD),
			keywordTest("LIKE", SyntaxKind.LIKE),
			keywordTest("LIMIT", SyntaxKind.LIMIT),
			keywordTest("LINDICATOR", SyntaxKind.LINDICATOR),
			keywordTest("LINES", SyntaxKind.LINES),
			keywordTest("LISTED", SyntaxKind.LISTED),
			keywordTest("LOCAL", SyntaxKind.LOCAL),
			keywordTest("LOCKS", SyntaxKind.LOCKS),
			keywordTest("LOG", SyntaxKind.LOG),
			keywordTest("LOG-LS", SyntaxKind.LOG_LS),
			keywordTest("LOG-PS", SyntaxKind.LOG_PS),
			keywordTest("LOGICAL", SyntaxKind.LOGICAL),
			keywordTest("LOOP", SyntaxKind.LOOP),
			keywordTest("LOWER", SyntaxKind.LOWER),
			keywordTest("LS", SyntaxKind.LS),
			keywordTest("LT", SyntaxKind.LT),
			keywordTest("MACROAREA", SyntaxKind.MACROAREA),
			keywordTest("MAP", SyntaxKind.MAP),
			keywordTest("MARK", SyntaxKind.MARK),
			keywordTest("MASK", SyntaxKind.MASK),
			keywordTest("MAX", SyntaxKind.MAX),
			keywordTest("MC", SyntaxKind.MC),
			keywordTest("MCG", SyntaxKind.MCG),
			keywordTest("MESSAGES", SyntaxKind.MESSAGES),
			keywordTest("METHOD", SyntaxKind.METHOD),
			keywordTest("MGID", SyntaxKind.MGID),
			keywordTest("MICROSECOND", SyntaxKind.MICROSECOND),
			keywordTest("MIN", SyntaxKind.MIN),
			keywordTest("MINUTE", SyntaxKind.MINUTE),
			keywordTest("MODE", SyntaxKind.MODE),
			keywordTest("MODAL", SyntaxKind.MODAL),
			keywordTest("MODIFIED", SyntaxKind.MODIFIED),
			keywordTest("MODULE", SyntaxKind.MODULE),
			keywordTest("MONTH", SyntaxKind.MONTH),
			keywordTest("MORE", SyntaxKind.MORE),
			keywordTest("MOVE", SyntaxKind.MOVE),
			keywordTest("MOVING", SyntaxKind.MOVING),
			keywordTest("MP", SyntaxKind.MP),
			keywordTest("MS", SyntaxKind.MS),
			keywordTest("MT", SyntaxKind.MT),
			keywordTest("MULTI-FETCH", SyntaxKind.MULTI_FETCH),
			keywordTest("MULTIPLY", SyntaxKind.MULTIPLY),
			keywordTest("NAME", SyntaxKind.NAME),
			keywordTest("NAMED", SyntaxKind.NAMED),
			keywordTest("NAMESPACE", SyntaxKind.NAMESPACE),
			keywordTest("NATIVE", SyntaxKind.NATIVE),
			keywordTest("NAVER", SyntaxKind.NAVER),
			keywordTest("NC", SyntaxKind.NC),
			keywordTest("NCOUNT", SyntaxKind.NCOUNT),
			keywordTest("NE", SyntaxKind.NE),
			keywordTest("NEWPAGE", SyntaxKind.NEWPAGE),
			keywordTest("NL", SyntaxKind.NL),
			keywordTest("NMIN", SyntaxKind.NMIN),
			keywordTest("NO", SyntaxKind.NO),
			keywordTest("NODE", SyntaxKind.NODE),
			keywordTest("NOHDR", SyntaxKind.NOHDR),
			keywordTest("NONE", SyntaxKind.NONE),
			keywordTest("NORMALIZE", SyntaxKind.NORMALIZE),
			keywordTest("NORMALIZED", SyntaxKind.NORMALIZED),
			keywordTest("NOT", SyntaxKind.NOT),
			keywordTest("NOTIT", SyntaxKind.NOTIT),
			keywordTest("NOTITLE", SyntaxKind.NOTITLE),
			keywordTest("NULL", SyntaxKind.NULL),
			keywordTest("NULL-HANDLE", SyntaxKind.NULL_HANDLE),
			keywordTest("NUMBER", SyntaxKind.KW_NUMBER),
			keywordTest("NUMERIC", SyntaxKind.NUMERIC),
			keywordTest("OBJECT", SyntaxKind.OBJECT),
			keywordTest("OBTAIN", SyntaxKind.OBTAIN),
			keywordTest("OCCURRENCES", SyntaxKind.OCCURRENCES),
			keywordTest("OF", SyntaxKind.OF),
			keywordTest("OFF", SyntaxKind.OFF),
			keywordTest("OFFSET", SyntaxKind.OFFSET),
			keywordTest("OLD", SyntaxKind.OLD),
			keywordTest("ON", SyntaxKind.ON),
			keywordTest("ONCE", SyntaxKind.ONCE),
			keywordTest("ONLY", SyntaxKind.ONLY),
			keywordTest("OPEN", SyntaxKind.OPEN),
			keywordTest("OPTIMIZE", SyntaxKind.OPTIMIZE),
			keywordTest("OPTIONAL", SyntaxKind.OPTIONAL),
			keywordTest("OPTIONS", SyntaxKind.OPTIONS),
			keywordTest("OR", SyntaxKind.OR),
			keywordTest("ORDER", SyntaxKind.ORDER),
			keywordTest("OUTER", SyntaxKind.OUTER),
			keywordTest("OUTPUT", SyntaxKind.OUTPUT),
			keywordTest("PACKAGESET", SyntaxKind.PACKAGESET),
			keywordTest("PAGE", SyntaxKind.PAGE),
			keywordTest("PAGES", SyntaxKind.PAGES),
			keywordTest("PARAMETER", SyntaxKind.PARAMETER),
			keywordTest("PARAMETERS", SyntaxKind.PARAMETERS),
			keywordTest("PARENT", SyntaxKind.PARENT),
			keywordTest("PARSE", SyntaxKind.PARSE),
			keywordTest("PASS", SyntaxKind.PASS),
			keywordTest("PASSW", SyntaxKind.PASSW),
			keywordTest("PASSWORD", SyntaxKind.PASSWORD),
			keywordTest("PATH", SyntaxKind.PATH),
			keywordTest("PATTERN", SyntaxKind.PATTERN),
			keywordTest("PC", SyntaxKind.PC),
			keywordTest("PD", SyntaxKind.PD),
			keywordTest("PEN", SyntaxKind.PEN),
			keywordTest("PERFORM", SyntaxKind.PERFORM),
			keywordTest("PGDN", SyntaxKind.PGDN),
			keywordTest("PGUP", SyntaxKind.PGUP),
			keywordTest("PGM", SyntaxKind.PGM),
			keywordTest("PHYSICAL", SyntaxKind.PHYSICAL),
			keywordTest("PM", SyntaxKind.PM),
			keywordTest("POLICY", SyntaxKind.POLICY),
			keywordTest("POS", SyntaxKind.POS),
			keywordTest("POSITION", SyntaxKind.POSITION),
			keywordTest("PREFIX", SyntaxKind.PREFIX),
			keywordTest("PRINT", SyntaxKind.PRINT),
			keywordTest("PRINTER", SyntaxKind.PRINTER),
			keywordTest("PROCESS", SyntaxKind.PROCESS),
			keywordTest("PROCESSING", SyntaxKind.PROCESSING),
			keywordTest("PROFILE", SyntaxKind.PROFILE),
			keywordTest("PROGRAM", SyntaxKind.PROGRAM),
			keywordTest("PROPERTY", SyntaxKind.PROPERTY),
			keywordTest("PROTOTYPE", SyntaxKind.PROTOTYPE),
			keywordTest("PRTY", SyntaxKind.PRTY),
			keywordTest("PS", SyntaxKind.PS),
			keywordTest("PT", SyntaxKind.PT),
			keywordTest("PW", SyntaxKind.PW),
			keywordTest("QUARTER", SyntaxKind.QUARTER),
			keywordTest("QUERYNO", SyntaxKind.QUERYNO),
			keywordTest("RD", SyntaxKind.RD),
			keywordTest("READ", SyntaxKind.READ),
			keywordTest("READONLY", SyntaxKind.READONLY),
			keywordTest("REC", SyntaxKind.REC),
			keywordTest("RECORD", SyntaxKind.RECORD),
			keywordTest("RECORDS", SyntaxKind.RECORDS),
			keywordTest("RECURSIVELY", SyntaxKind.RECURSIVELY),
			keywordTest("REDEFINE", SyntaxKind.REDEFINE),
			keywordTest("REDUCE", SyntaxKind.REDUCE),
			keywordTest("REFERENCED", SyntaxKind.REFERENCED),
			keywordTest("REFERENCING", SyntaxKind.REFERENCING),
			keywordTest("REINPUT", SyntaxKind.REINPUT),
			keywordTest("REJECT", SyntaxKind.REJECT),
			keywordTest("REL", SyntaxKind.REL),
			keywordTest("RELATION", SyntaxKind.RELATION),
			keywordTest("RELATIONSHIP", SyntaxKind.RELATIONSHIP),
			keywordTest("RELEASE", SyntaxKind.RELEASE),
			keywordTest("REMAINDER", SyntaxKind.REMAINDER),
			keywordTest("REPEAT", SyntaxKind.REPEAT),
			keywordTest("REPLACE", SyntaxKind.REPLACE),
			keywordTest("REPORT", SyntaxKind.REPORT),
			keywordTest("REPORTER", SyntaxKind.REPORTER),
			keywordTest("REPOSITION", SyntaxKind.REPOSITION),
			keywordTest("REQUEST", SyntaxKind.REQUEST),
			keywordTest("REQUIRED", SyntaxKind.REQUIRED),
			keywordTest("RESET", SyntaxKind.RESET),
			keywordTest("RESETTING", SyntaxKind.RESETTING),
			keywordTest("RESIZE", SyntaxKind.RESIZE),
			keywordTest("RESPONSE", SyntaxKind.RESPONSE),
			keywordTest("RESTORE", SyntaxKind.RESTORE),
			keywordTest("RESULT", SyntaxKind.RESULT),
			keywordTest("RET", SyntaxKind.RET),
			keywordTest("RETAIN", SyntaxKind.RETAIN),
			keywordTest("RETAINED", SyntaxKind.RETAINED),
			keywordTest("RETRY", SyntaxKind.RETRY),
			keywordTest("RETURN", SyntaxKind.RETURN),
			keywordTest("RETURNS", SyntaxKind.RETURNS),
			keywordTest("REVERSED", SyntaxKind.REVERSED),
			keywordTest("RG", SyntaxKind.RG),
			keywordTest("RIGHT", SyntaxKind.RIGHT),
			keywordTest("ROLLBACK", SyntaxKind.ROLLBACK),
			keywordTest("ROUNDED", SyntaxKind.ROUNDED),
			keywordTest("ROUTINE", SyntaxKind.ROUTINE),
			keywordTest("ROW", SyntaxKind.ROW),
			keywordTest("ROWS", SyntaxKind.ROWS),
			keywordTest("RR", SyntaxKind.RR),
			keywordTest("RS", SyntaxKind.RS),
			keywordTest("RULEVAR", SyntaxKind.RULEVAR),
			keywordTest("RUN", SyntaxKind.RUN),
			keywordTest("SA", SyntaxKind.SA),
			keywordTest("SAME", SyntaxKind.SAME),
			keywordTest("SCAN", SyntaxKind.SCAN),
			keywordTest("SCREEN", SyntaxKind.SCREEN),
			keywordTest("SCROLL", SyntaxKind.SCROLL),
			keywordTest("SECOND", SyntaxKind.SECOND),
			keywordTest("SELECT", SyntaxKind.SELECT),
			keywordTest("SELECTION", SyntaxKind.SELECTION),
			keywordTest("SEND", SyntaxKind.SEND),
			keywordTest("SENSITIVE", SyntaxKind.SENSITIVE),
			keywordTest("SEPARATE", SyntaxKind.SEPARATE),
			keywordTest("SEQUENCE", SyntaxKind.SEQUENCE),
			keywordTest("SERVER", SyntaxKind.SERVER),
			keywordTest("SET", SyntaxKind.SET),
			keywordTest("SETS", SyntaxKind.SETS),
			keywordTest("SETTIME", SyntaxKind.SETTIME),
			keywordTest("SF", SyntaxKind.SF),
			keywordTest("SG", SyntaxKind.SG),
			keywordTest("SGN", SyntaxKind.SGN),
			keywordTest("SHARED", SyntaxKind.SHARED),
			keywordTest("SHORT", SyntaxKind.SHORT),
			keywordTest("SHOW", SyntaxKind.SHOW),
			keywordTest("SIN", SyntaxKind.SIN),
			keywordTest("SINGLE", SyntaxKind.SINGLE),
			keywordTest("SIZE", SyntaxKind.SIZE),
			keywordTest("SKIP", SyntaxKind.SKIP),
			keywordTest("SL", SyntaxKind.SL),
			keywordTest("SM", SyntaxKind.SM),
			keywordTest("SOME", SyntaxKind.SOME),
			keywordTest("SORT", SyntaxKind.SORT),
			keywordTest("SORTED", SyntaxKind.SORTED),
			keywordTest("SORTKEY", SyntaxKind.SORTKEY),
			keywordTest("SOUND", SyntaxKind.SOUND),
			keywordTest("SPACE", SyntaxKind.SPACE),
			keywordTest("SPECIFIED", SyntaxKind.SPECIFIED),
			keywordTest("SQL", SyntaxKind.SQL),
			keywordTest("SQLID", SyntaxKind.SQLID),
			keywordTest("SQRT", SyntaxKind.SQRT),
			keywordTest("STACK", SyntaxKind.STACK),
			keywordTest("START", SyntaxKind.START),
			keywordTest("STARTING", SyntaxKind.STARTING),
			keywordTest("STATEMENT", SyntaxKind.STATEMENT),
			keywordTest("STATIC", SyntaxKind.STATIC),
			keywordTest("STATUS", SyntaxKind.STATUS),
			keywordTest("STEP", SyntaxKind.STEP),
			keywordTest("STOP", SyntaxKind.STOP),
			keywordTest("STORE", SyntaxKind.STORE),
			keywordTest("SUBPROGRAM", SyntaxKind.SUBPROGRAM),
			keywordTest("SUBPROGRAMS", SyntaxKind.SUBPROGRAMS),
			keywordTest("SUBROUTINE", SyntaxKind.SUBROUTINE),
			keywordTest("SUBSTR", SyntaxKind.SUBSTR),
			keywordTest("SUBSTRING", SyntaxKind.SUBSTRING),
			keywordTest("SUBTRACT", SyntaxKind.SUBTRACT),
			keywordTest("SUM", SyntaxKind.SUM),
			keywordTest("SUPPRESS", SyntaxKind.SUPPRESS),
			keywordTest("SUPPRESSED", SyntaxKind.SUPPRESSED),
			keywordTest("SUSPEND", SyntaxKind.SUSPEND),
			keywordTest("SYMBOL", SyntaxKind.SYMBOL),
			keywordTest("SYNC", SyntaxKind.SYNC),
			keywordTest("SYSTEM", SyntaxKind.SYSTEM),
			keywordTest("TAN", SyntaxKind.TAN),
			keywordTest("TC", SyntaxKind.TC),
			keywordTest("TERMINATE", SyntaxKind.TERMINATE),
			keywordTest("TEXT", SyntaxKind.TEXT),
			keywordTest("TEXTAREA", SyntaxKind.TEXTAREA),
			keywordTest("TEXTVARIABLE", SyntaxKind.TEXTVARIABLE),
			keywordTest("THAN", SyntaxKind.THAN),
			keywordTest("THEM", SyntaxKind.THEM),
			keywordTest("THEN", SyntaxKind.THEN),
			keywordTest("THRU", SyntaxKind.THRU),
			keywordTest("TIME", SyntaxKind.TIME),
			keywordTest("TIMESTAMP", SyntaxKind.TIMESTAMP),
			keywordTest("TIMEZONE", SyntaxKind.TIMEZONE),
			keywordTest("TITLE", SyntaxKind.TITLE),
			keywordTest("TO", SyntaxKind.TO),
			keywordTest("TOP", SyntaxKind.TOP),
			keywordTest("TOTAL", SyntaxKind.TOTAL),
			keywordTest("TP", SyntaxKind.TP),
			keywordTest("TR", SyntaxKind.TR),
			keywordTest("TRAILER", SyntaxKind.TRAILER),
			keywordTest("TRANSACTION", SyntaxKind.TRANSACTION),
			keywordTest("TRANSFER", SyntaxKind.TRANSFER),
			keywordTest("TRANSLATE", SyntaxKind.TRANSLATE),
			keywordTest("TREQ", SyntaxKind.TREQ),
			keywordTest("TRUE", SyntaxKind.TRUE),
			keywordTest("TS", SyntaxKind.TS),
			keywordTest("TYPE", SyntaxKind.TYPE),
			keywordTest("TYPES", SyntaxKind.TYPES),
			keywordTest("UC", SyntaxKind.UC),
			keywordTest("UNDERLINED", SyntaxKind.UNDERLINED),
			keywordTest("UNION", SyntaxKind.UNION),
			keywordTest("UNIQUE", SyntaxKind.UNIQUE),
			keywordTest("UNKNOWN", SyntaxKind.UNKNOWN),
			keywordTest("UNTIL", SyntaxKind.UNTIL),
			keywordTest("UPDATE", SyntaxKind.UPDATE),
			keywordTest("UPLOAD", SyntaxKind.UPLOAD),
			keywordTest("UPPER", SyntaxKind.UPPER),
			keywordTest("UR", SyntaxKind.UR),
			keywordTest("USED", SyntaxKind.USED),
			keywordTest("USER", SyntaxKind.USER),
			keywordTest("USING", SyntaxKind.USING),
			keywordTest("VAL", SyntaxKind.VAL),
			keywordTest("VALUE", SyntaxKind.VALUE),
			keywordTest("VALUES", SyntaxKind.VALUES),
			keywordTest("VARGRAPHIC", SyntaxKind.VARGRAPHIC),
			keywordTest("VARIABLE", SyntaxKind.VARIABLE),
			keywordTest("VARIABLES", SyntaxKind.VARIABLES),
			keywordTest("VERT", SyntaxKind.VERT),
			keywordTest("VERTICALLY", SyntaxKind.VERTICALLY),
			keywordTest("VIA", SyntaxKind.VIA),
			keywordTest("VIEW", SyntaxKind.VIEW),
			keywordTest("WH", SyntaxKind.WH),
			keywordTest("WHEN", SyntaxKind.WHEN),
			keywordTest("WHERE", SyntaxKind.WHERE),
			keywordTest("WHILE", SyntaxKind.WHILE),
			keywordTest("WINDOW", SyntaxKind.WINDOW),
			keywordTest("WITH", SyntaxKind.WITH),
			keywordTest("WORK", SyntaxKind.WORK),
			keywordTest("WRITE", SyntaxKind.WRITE),
			keywordTest("WITH_CTE", SyntaxKind.WITH_CTE),
			keywordTest("XML", SyntaxKind.XML),
			keywordTest("YEAR", SyntaxKind.YEAR),
			keywordTest("ZD", SyntaxKind.ZD),
			keywordTest("ZP", SyntaxKind.ZP)
		);
	}

	private DynamicTest keywordTest(String keyword, SyntaxKind expectedKind)
	{
		return dynamicTest(keyword, () -> assertTokens(keyword, token(expectedKind, keyword)));
	}
}
