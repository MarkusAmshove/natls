package org.amshove.natparse.lexing;

import java.util.Locale;

public class KeywordTable
{
	public static SyntaxKind getKeyword(String possibleKeyword)
	{
		var key = possibleKeyword.toLowerCase(Locale.ENGLISH);
		return switch (key)
		{
			case "abs" -> SyntaxKind.ABS;
			case "absolute" -> SyntaxKind.ABSOLUTE;
			case "accept" -> SyntaxKind.ACCEPT;
			case "action" -> SyntaxKind.ACTION;
			case "activation" -> SyntaxKind.ACTIVATION;
			case "ad" -> SyntaxKind.AD;
			case "add" -> SyntaxKind.ADD;
			case "adjust" -> SyntaxKind.ADJUST;
			case "after" -> SyntaxKind.AFTER;
			case "al" -> SyntaxKind.AL;
			case "alarm" -> SyntaxKind.ALARM;
			case "all" -> SyntaxKind.ALL;
			case "alpha" -> SyntaxKind.ALPHA;
			case "alphabetically" -> SyntaxKind.ALPHABETICALLY;
			case "and" -> SyntaxKind.AND;
			case "any" -> SyntaxKind.ANY;
			case "appl" -> SyntaxKind.APPL;
			case "application" -> SyntaxKind.APPLICATION;
			case "array" -> SyntaxKind.ARRAY;
			case "as" -> SyntaxKind.AS;
			case "asc" -> SyntaxKind.ASC;
			case "ascending" -> SyntaxKind.ASCENDING;
			case "assign" -> SyntaxKind.ASSIGN;
			case "assigning" -> SyntaxKind.ASSIGNING;
			case "async" -> SyntaxKind.ASYNC;
			case "at" -> SyntaxKind.AT;
			case "atn" -> SyntaxKind.ATN;
			case "att" -> SyntaxKind.ATT;
			case "attributes" -> SyntaxKind.ATTRIBUTES;
			case "auth" -> SyntaxKind.AUTH;
			case "authorization" -> SyntaxKind.AUTHORIZATION;
			case "auto" -> SyntaxKind.AUTO;
			case "aver" -> SyntaxKind.AVER;
			case "avg" -> SyntaxKind.AVG;
			case "backout" -> SyntaxKind.BACKOUT;
			case "backward" -> SyntaxKind.BACKWARD;
			case "base" -> SyntaxKind.BASE;
			case "before" -> SyntaxKind.BEFORE;
			case "between" -> SyntaxKind.BETWEEN;
			case "block" -> SyntaxKind.BLOCK;
			case "bot" -> SyntaxKind.BOT;
			case "bottom" -> SyntaxKind.BOTTOM;
			case "break" -> SyntaxKind.BREAK;
			case "browse" -> SyntaxKind.BROWSE;
			case "but" -> SyntaxKind.BUT;
			case "bx" -> SyntaxKind.BX;
			case "by" -> SyntaxKind.BY;
			case "cabinet" -> SyntaxKind.CABINET;
			case "call" -> SyntaxKind.CALL;
			case "calldbproc" -> SyntaxKind.CALLDBPROC;
			case "calling" -> SyntaxKind.CALLING;
			case "callnat" -> SyntaxKind.CALLNAT;
			case "cap" -> SyntaxKind.CAP;
			case "capt" -> SyntaxKind.CAPT;
			case "captioned" -> SyntaxKind.CAPTIONED;
			case "case" -> SyntaxKind.CASE;
			case "cc" -> SyntaxKind.CC;
			case "cd" -> SyntaxKind.CD;
			case "cdid" -> SyntaxKind.CDID;
			case "cf" -> SyntaxKind.CF;
			case "char" -> SyntaxKind.CHAR;
			case "charlength" -> SyntaxKind.CHARLENGTH;
			case "charposition" -> SyntaxKind.CHARPOSITION;
			case "child" -> SyntaxKind.CHILD;
			case "ciph" -> SyntaxKind.CIPH;
			case "cipher" -> SyntaxKind.CIPHER;
			case "class" -> SyntaxKind.CLASS;
			case "close" -> SyntaxKind.CLOSE;
			case "coalesce" -> SyntaxKind.COALESCE;
			case "codepage" -> SyntaxKind.CODEPAGE;
			case "command" -> SyntaxKind.COMMAND;
			case "commit" -> SyntaxKind.COMMIT;
			case "compose" -> SyntaxKind.COMPOSE;
			case "compress" -> SyntaxKind.COMPRESS;
			case "compute" -> SyntaxKind.COMPUTE;
			case "concat" -> SyntaxKind.CONCAT;
			case "condition" -> SyntaxKind.CONDITION;
			case "const" -> SyntaxKind.CONST;
			case "constant" -> SyntaxKind.CONSTANT;
			case "context" -> SyntaxKind.CONTEXT;
			case "control" -> SyntaxKind.CONTROL;
			case "conversation" -> SyntaxKind.CONVERSATION;
			case "copies" -> SyntaxKind.COPIES;
			case "copy" -> SyntaxKind.COPY;
			case "cos" -> SyntaxKind.COS;
			case "count" -> SyntaxKind.COUNT;
			case "coupled" -> SyntaxKind.COUPLED;
			case "cs" -> SyntaxKind.CS;
			case "current" -> SyntaxKind.CURRENT;
			case "cursor" -> SyntaxKind.CURSOR;
			case "data" -> SyntaxKind.DATA;
			case "dataarea" -> SyntaxKind.DATAAREA;
			case "date" -> SyntaxKind.DATE;
			case "day" -> SyntaxKind.DAY;
			case "days" -> SyntaxKind.DAYS;
			case "dc" -> SyntaxKind.DC;
			case "decide" -> SyntaxKind.DECIDE;
			case "decimal" -> SyntaxKind.DECIMAL;
			case "define" -> SyntaxKind.DEFINE;
			case "definition" -> SyntaxKind.DEFINITION;
			case "del" -> SyntaxKind.DEL;
			case "delete" -> SyntaxKind.DELETE;
			case "delimited" -> SyntaxKind.DELIMITED;
			case "delimiter" -> SyntaxKind.DELIMITER;
			case "delimiters" -> SyntaxKind.DELIMITERS;
			case "desc" -> SyntaxKind.DESC;
			case "descending" -> SyntaxKind.DESCENDING;
			case "df" -> SyntaxKind.DF;
			case "dialog" -> SyntaxKind.DIALOG;
			case "dialog-id" -> SyntaxKind.DIALOG_ID;
			case "digits" -> SyntaxKind.DIGITS;
			case "direction" -> SyntaxKind.DIRECTION;
			case "disabled" -> SyntaxKind.DISABLED;
			case "disp" -> SyntaxKind.DISP;
			case "display" -> SyntaxKind.DISPLAY;
			case "distinct" -> SyntaxKind.DISTINCT;
			case "divide" -> SyntaxKind.DIVIDE;
			case "dl" -> SyntaxKind.DL;
			case "dlogoff" -> SyntaxKind.DLOGOFF;
			case "dlogon" -> SyntaxKind.DLOGON;
			case "dnative" -> SyntaxKind.DNATIVE;
			case "dnret" -> SyntaxKind.DNRET;
			case "do" -> SyntaxKind.DO;
			case "document" -> SyntaxKind.DOCUMENT;
			case "doend" -> SyntaxKind.DOEND;
			case "download" -> SyntaxKind.DOWNLOAD;
			case "du" -> SyntaxKind.DU;
			case "dy" -> SyntaxKind.DY;
			case "dynamic" -> SyntaxKind.DYNAMIC;
			case "edited" -> SyntaxKind.EDITED;
			case "ej" -> SyntaxKind.EJ;
			case "eject" -> SyntaxKind.EJECT;
			case "else" -> SyntaxKind.ELSE;
			case "em" -> SyntaxKind.EM;
			case "emu" -> SyntaxKind.EMU;
			case "encoded" -> SyntaxKind.ENCODED;
			case "end" -> SyntaxKind.END;
			case "end-all" -> SyntaxKind.END_ALL;
			case "end-before" -> SyntaxKind.END_BEFORE;
			case "end-break" -> SyntaxKind.END_BREAK;
			case "end-browse" -> SyntaxKind.END_BROWSE;
			case "end-class" -> SyntaxKind.END_CLASS;
			case "end-decide" -> SyntaxKind.END_DECIDE;
			case "end-define" -> SyntaxKind.END_DEFINE;
			case "end-enddata" -> SyntaxKind.END_ENDDATA;
			case "end-endfile" -> SyntaxKind.END_ENDFILE;
			case "end-endpage" -> SyntaxKind.END_ENDPAGE;
			case "end-error" -> SyntaxKind.END_ERROR;
			case "end-file" -> SyntaxKind.END_FILE;
			case "end-find" -> SyntaxKind.END_FIND;
			case "end-for" -> SyntaxKind.END_FOR;
			case "end-function" -> SyntaxKind.END_FUNCTION;
			case "end-histogram" -> SyntaxKind.END_HISTOGRAM;
			case "endhoc" -> SyntaxKind.ENDHOC;
			case "end-if" -> SyntaxKind.END_IF;
			case "end-interface" -> SyntaxKind.END_INTERFACE;
			case "end-loop" -> SyntaxKind.END_LOOP;
			case "end-method" -> SyntaxKind.END_METHOD;
			case "end-norec" -> SyntaxKind.END_NOREC;
			case "end-parameters" -> SyntaxKind.END_PARAMETERS;
			case "end-parse" -> SyntaxKind.END_PARSE;
			case "end-process" -> SyntaxKind.END_PROCESS;
			case "end-property" -> SyntaxKind.END_PROPERTY;
			case "end-prototype" -> SyntaxKind.END_PROTOTYPE;
			case "end-read" -> SyntaxKind.END_READ;
			case "end-repeat" -> SyntaxKind.END_REPEAT;
			case "end-result" -> SyntaxKind.END_RESULT;
			case "end-select" -> SyntaxKind.END_SELECT;
			case "end-sort" -> SyntaxKind.END_SORT;
			case "end-start" -> SyntaxKind.END_START;
			case "end-subroutine" -> SyntaxKind.END_SUBROUTINE;
			case "end-toppage" -> SyntaxKind.END_TOPPAGE;
			case "end-work" -> SyntaxKind.END_WORK;
			case "ending" -> SyntaxKind.ENDING;
			case "enter" -> SyntaxKind.ENTER;
			case "entire" -> SyntaxKind.ENTIRE;
			case "eq" -> SyntaxKind.EQ;
			case "equal" -> SyntaxKind.EQUAL;
			case "erase" -> SyntaxKind.ERASE;
			case "error" -> SyntaxKind.ERROR;
			case "errors" -> SyntaxKind.ERRORS;
			case "es" -> SyntaxKind.ES;
			case "escape" -> SyntaxKind.ESCAPE;
			case "even" -> SyntaxKind.EVEN;
			case "event" -> SyntaxKind.EVENT;
			case "every" -> SyntaxKind.EVERY;
			case "examine" -> SyntaxKind.EXAMINE;
			case "except" -> SyntaxKind.EXCEPT;
			case "exists" -> SyntaxKind.EXISTS;
			case "exit" -> SyntaxKind.EXIT;
			case "exp" -> SyntaxKind.EXP;
			case "expand" -> SyntaxKind.EXPAND;
			case "export" -> SyntaxKind.EXPORT;
			case "external" -> SyntaxKind.EXTERNAL;
			case "extracting" -> SyntaxKind.EXTRACTING;
			case "false" -> SyntaxKind.FALSE;
			case "fc" -> SyntaxKind.FC;
			case "fetch" -> SyntaxKind.FETCH;
			case "field" -> SyntaxKind.FIELD;
			case "fields" -> SyntaxKind.FIELDS;
			case "file" -> SyntaxKind.FILE;
			case "fill" -> SyntaxKind.FILL;
			case "filler" -> SyntaxKind.FILLER;
			case "final" -> SyntaxKind.FINAL;
			case "find" -> SyntaxKind.FIND;
			case "first" -> SyntaxKind.FIRST;
			case "fl" -> SyntaxKind.FL;
			case "float" -> SyntaxKind.FLOAT;
			case "for" -> SyntaxKind.FOR;
			case "form" -> SyntaxKind.FORM;
			case "format" -> SyntaxKind.FORMAT;
			case "formatted" -> SyntaxKind.FORMATTED;
			case "formatting" -> SyntaxKind.FORMATTING;
			case "forms" -> SyntaxKind.FORMS;
			case "forward" -> SyntaxKind.FORWARD;
			case "found" -> SyntaxKind.FOUND;
			case "frac" -> SyntaxKind.FRAC;
			case "framed" -> SyntaxKind.FRAMED;
			case "from" -> SyntaxKind.FROM;
			case "fs" -> SyntaxKind.FS;
			case "full" -> SyntaxKind.FULL;
			case "function" -> SyntaxKind.FUNCTION;
			case "functions" -> SyntaxKind.FUNCTIONS;
			case "gc" -> SyntaxKind.GC;
			case "ge" -> SyntaxKind.GE;
			case "gen" -> SyntaxKind.GEN;
			case "generated" -> SyntaxKind.GENERATED;
			case "get" -> SyntaxKind.GET;
			case "gfid" -> SyntaxKind.GFID;
			case "give" -> SyntaxKind.GIVE;
			case "giving" -> SyntaxKind.GIVING;
			case "global" -> SyntaxKind.GLOBAL;
			case "globals" -> SyntaxKind.GLOBALS;
			case "greater" -> SyntaxKind.GREATER;
			case "gt" -> SyntaxKind.GT;
			case "gui" -> SyntaxKind.GUI;
			case "handle" -> SyntaxKind.HANDLE;
			case "having" -> SyntaxKind.HAVING;
			case "hc" -> SyntaxKind.HC;
			case "hd" -> SyntaxKind.HD;
			case "he" -> SyntaxKind.HE;
			case "header" -> SyntaxKind.HEADER;
			case "help" -> SyntaxKind.HELP;
			case "hex" -> SyntaxKind.HEX;
			case "histogram" -> SyntaxKind.HISTOGRAM;
			case "hold" -> SyntaxKind.HOLD;
			case "horiz" -> SyntaxKind.HORIZ;
			case "horizontally" -> SyntaxKind.HORIZONTALLY;
			case "hour" -> SyntaxKind.HOUR;
			case "hours" -> SyntaxKind.HOURS;
			case "hw" -> SyntaxKind.HW;
			case "ia" -> SyntaxKind.IA;
			case "ic" -> SyntaxKind.IC;
			case "icu" -> SyntaxKind.ICU;
			case "id" -> SyntaxKind.ID;
			case "identical" -> SyntaxKind.IDENTICAL;
			case "if" -> SyntaxKind.IF;
			case "ignore" -> SyntaxKind.IGNORE;
			case "im" -> SyntaxKind.IM;
			case "immediate" -> SyntaxKind.IMMEDIATE;
			case "import" -> SyntaxKind.IMPORT;
			case "in" -> SyntaxKind.IN;
			case "inc" -> SyntaxKind.INC;
			case "inccont" -> SyntaxKind.INCCONT;
			case "incdic" -> SyntaxKind.INCDIC;
			case "incdir" -> SyntaxKind.INCDIR;
			case "include" -> SyntaxKind.INCLUDE;
			case "included" -> SyntaxKind.INCLUDED;
			case "including" -> SyntaxKind.INCLUDING;
			case "incmac" -> SyntaxKind.INCMAC;
			case "independent" -> SyntaxKind.INDEPENDENT;
			case "index" -> SyntaxKind.INDEX;
			case "indexed" -> SyntaxKind.INDEXED;
			case "indicator" -> SyntaxKind.INDICATOR;
			case "init" -> SyntaxKind.INIT;
			case "initial" -> SyntaxKind.INITIAL;
			case "inner" -> SyntaxKind.INNER;
			case "input" -> SyntaxKind.INPUT;
			case "insensitive" -> SyntaxKind.INSENSITIVE;
			case "insert" -> SyntaxKind.INSERT;
			case "int" -> SyntaxKind.INT;
			case "integer" -> SyntaxKind.INTEGER;
			case "intercepted" -> SyntaxKind.INTERCEPTED;
			case "interface" -> SyntaxKind.INTERFACE;
			case "interface4" -> SyntaxKind.INTERFACE4;
			case "intermediate" -> SyntaxKind.INTERMEDIATE;
			case "intersect" -> SyntaxKind.INTERSECT;
			case "into" -> SyntaxKind.INTO;
			case "inverted" -> SyntaxKind.INVERTED;
			case "investigate" -> SyntaxKind.INVESTIGATE;
			case "ip" -> SyntaxKind.IP;
			case "is" -> SyntaxKind.IS;
			case "isn" -> SyntaxKind.KW_ISN;
			case "join" -> SyntaxKind.JOIN;
			case "just" -> SyntaxKind.JUST;
			case "justified" -> SyntaxKind.JUSTIFIED;
			case "kd" -> SyntaxKind.KD;
			case "keep" -> SyntaxKind.KEEP;
			case "key" -> SyntaxKind.KEY;
			case "keys" -> SyntaxKind.KEYS;
			case "language" -> SyntaxKind.LANGUAGE;
			case "last" -> SyntaxKind.LAST;
			case "lc" -> SyntaxKind.LC;
			case "lcu" -> SyntaxKind.LCU;
			case "le" -> SyntaxKind.LE;
			case "leading" -> SyntaxKind.LEADING;
			case "leave" -> SyntaxKind.LEAVE;
			case "leaving" -> SyntaxKind.LEAVING;
			case "left" -> SyntaxKind.LEFT;
			case "length" -> SyntaxKind.LENGTH;
			case "less" -> SyntaxKind.LESS;
			case "level" -> SyntaxKind.LEVEL;
			case "lib" -> SyntaxKind.LIB;
			case "libpw" -> SyntaxKind.LIBPW;
			case "library" -> SyntaxKind.LIBRARY;
			case "library-password" -> SyntaxKind.LIBRARY_PASSWORD;
			case "like" -> SyntaxKind.LIKE;
			case "limit" -> SyntaxKind.LIMIT;
			case "lindicator" -> SyntaxKind.LINDICATOR;
			case "lines" -> SyntaxKind.LINES;
			case "listed" -> SyntaxKind.LISTED;
			case "local" -> SyntaxKind.LOCAL;
			case "locks" -> SyntaxKind.LOCKS;
			case "log" -> SyntaxKind.LOG;
			case "log-ls" -> SyntaxKind.LOG_LS;
			case "log-ps" -> SyntaxKind.LOG_PS;
			case "logical" -> SyntaxKind.LOGICAL;
			case "loop" -> SyntaxKind.LOOP;
			case "lower" -> SyntaxKind.LOWER;
			case "ls" -> SyntaxKind.LS;
			case "lt" -> SyntaxKind.LT;
			case "macroarea" -> SyntaxKind.MACROAREA;
			case "map" -> SyntaxKind.MAP;
			case "mark" -> SyntaxKind.MARK;
			case "mask" -> SyntaxKind.MASK;
			case "max" -> SyntaxKind.MAX;
			case "mc" -> SyntaxKind.MC;
			case "mcg" -> SyntaxKind.MCG;
			case "messages" -> SyntaxKind.MESSAGES;
			case "method" -> SyntaxKind.METHOD;
			case "mgid" -> SyntaxKind.MGID;
			case "microsecond" -> SyntaxKind.MICROSECOND;
			case "min" -> SyntaxKind.MIN;
			case "minute" -> SyntaxKind.MINUTE;
			case "modal" -> SyntaxKind.MODAL;
			case "mode" -> SyntaxKind.MODE;
			case "modified" -> SyntaxKind.MODIFIED;
			case "module" -> SyntaxKind.MODULE;
			case "month" -> SyntaxKind.MONTH;
			case "more" -> SyntaxKind.MORE;
			case "move" -> SyntaxKind.MOVE;
			case "moving" -> SyntaxKind.MOVING;
			case "mp" -> SyntaxKind.MP;
			case "ms" -> SyntaxKind.MS;
			case "mt" -> SyntaxKind.MT;
			case "multi-fetch" -> SyntaxKind.MULTI_FETCH;
			case "multiply" -> SyntaxKind.MULTIPLY;
			case "name" -> SyntaxKind.NAME;
			case "named" -> SyntaxKind.NAMED;
			case "namespace" -> SyntaxKind.NAMESPACE;
			case "native" -> SyntaxKind.NATIVE;
			case "naver" -> SyntaxKind.NAVER;
			case "nc" -> SyntaxKind.NC;
			case "ncount" -> SyntaxKind.NCOUNT;
			case "ne" -> SyntaxKind.NE;
			case "newpage" -> SyntaxKind.NEWPAGE;
			case "nl" -> SyntaxKind.NL;
			case "nmin" -> SyntaxKind.NMIN;
			case "no" -> SyntaxKind.NO;
			case "node" -> SyntaxKind.NODE;
			case "nohdr" -> SyntaxKind.NOHDR;
			case "none" -> SyntaxKind.NONE;
			case "normalize" -> SyntaxKind.NORMALIZE;
			case "normalized" -> SyntaxKind.NORMALIZED;
			case "not" -> SyntaxKind.NOT;
			case "notequal" -> SyntaxKind.NOTEQUAL;
			case "notit" -> SyntaxKind.NOTIT;
			case "notitle" -> SyntaxKind.NOTITLE;
			case "null" -> SyntaxKind.NULL;
			case "null-handle" -> SyntaxKind.NULL_HANDLE;
			case "number" -> SyntaxKind.KW_NUMBER;
			case "numeric" -> SyntaxKind.NUMERIC;
			case "object" -> SyntaxKind.OBJECT;
			case "obtain" -> SyntaxKind.OBTAIN;
			case "occurrences" -> SyntaxKind.OCCURRENCES;
			case "of" -> SyntaxKind.OF;
			case "off" -> SyntaxKind.OFF;
			case "offset" -> SyntaxKind.OFFSET;
			case "old" -> SyntaxKind.OLD;
			case "on" -> SyntaxKind.ON;
			case "once" -> SyntaxKind.ONCE;
			case "only" -> SyntaxKind.ONLY;
			case "open" -> SyntaxKind.OPEN;
			case "optimize" -> SyntaxKind.OPTIMIZE;
			case "optional" -> SyntaxKind.OPTIONAL;
			case "options" -> SyntaxKind.OPTIONS;
			case "or" -> SyntaxKind.OR;
			case "order" -> SyntaxKind.ORDER;
			case "outer" -> SyntaxKind.OUTER;
			case "output" -> SyntaxKind.OUTPUT;
			case "packageset" -> SyntaxKind.PACKAGESET;
			case "page" -> SyntaxKind.PAGE;
			case "pages" -> SyntaxKind.PAGES;
			case "parameter" -> SyntaxKind.PARAMETER;
			case "parameters" -> SyntaxKind.PARAMETERS;
			case "parent" -> SyntaxKind.PARENT;
			case "parse" -> SyntaxKind.PARSE;
			case "pass" -> SyntaxKind.PASS;
			case "passw" -> SyntaxKind.PASSW;
			case "password" -> SyntaxKind.PASSWORD;
			case "path" -> SyntaxKind.PATH;
			case "pattern" -> SyntaxKind.PATTERN;
			case "pc" -> SyntaxKind.PC;
			case "pd" -> SyntaxKind.PD;
			case "pen" -> SyntaxKind.PEN;
			case "perform" -> SyntaxKind.PERFORM;
			case "pgdn" -> SyntaxKind.PGDN;
			case "pgup" -> SyntaxKind.PGUP;
			case "pgm" -> SyntaxKind.PGM;
			case "physical" -> SyntaxKind.PHYSICAL;
			case "pm" -> SyntaxKind.PM;
			case "policy" -> SyntaxKind.POLICY;
			case "pos" -> SyntaxKind.POS;
			case "position" -> SyntaxKind.POSITION;
			case "prefix" -> SyntaxKind.PREFIX;
			case "print" -> SyntaxKind.PRINT;
			case "printer" -> SyntaxKind.PRINTER;
			case "process" -> SyntaxKind.PROCESS;
			case "processing" -> SyntaxKind.PROCESSING;
			case "profile" -> SyntaxKind.PROFILE;
			case "program" -> SyntaxKind.PROGRAM;
			case "property" -> SyntaxKind.PROPERTY;
			case "prototype" -> SyntaxKind.PROTOTYPE;
			case "prty" -> SyntaxKind.PRTY;
			case "ps" -> SyntaxKind.PS;
			case "pt" -> SyntaxKind.PT;
			case "pw" -> SyntaxKind.PW;
			case "quarter" -> SyntaxKind.QUARTER;
			case "queryno" -> SyntaxKind.QUERYNO;
			case "rd" -> SyntaxKind.RD;
			case "read" -> SyntaxKind.READ;
			case "readonly" -> SyntaxKind.READONLY;
			case "rec" -> SyntaxKind.REC;
			case "record" -> SyntaxKind.RECORD;
			case "records" -> SyntaxKind.RECORDS;
			case "recursively" -> SyntaxKind.RECURSIVELY;
			case "redefine" -> SyntaxKind.REDEFINE;
			case "reduce" -> SyntaxKind.REDUCE;
			case "referenced" -> SyntaxKind.REFERENCED;
			case "referencing" -> SyntaxKind.REFERENCING;
			case "reinput" -> SyntaxKind.REINPUT;
			case "reject" -> SyntaxKind.REJECT;
			case "rel" -> SyntaxKind.REL;
			case "relation" -> SyntaxKind.RELATION;
			case "relationship" -> SyntaxKind.RELATIONSHIP;
			case "release" -> SyntaxKind.RELEASE;
			case "remainder" -> SyntaxKind.REMAINDER;
			case "repeat" -> SyntaxKind.REPEAT;
			case "replace" -> SyntaxKind.REPLACE;
			case "report" -> SyntaxKind.REPORT;
			case "reporter" -> SyntaxKind.REPORTER;
			case "reposition" -> SyntaxKind.REPOSITION;
			case "request" -> SyntaxKind.REQUEST;
			case "required" -> SyntaxKind.REQUIRED;
			case "reset" -> SyntaxKind.RESET;
			case "resetting" -> SyntaxKind.RESETTING;
			case "resize" -> SyntaxKind.RESIZE;
			case "response" -> SyntaxKind.RESPONSE;
			case "restore" -> SyntaxKind.RESTORE;
			case "result" -> SyntaxKind.RESULT;
			case "ret" -> SyntaxKind.RET;
			case "retain" -> SyntaxKind.RETAIN;
			case "retained" -> SyntaxKind.RETAINED;
			case "retry" -> SyntaxKind.RETRY;
			case "return" -> SyntaxKind.RETURN;
			case "returns" -> SyntaxKind.RETURNS;
			case "reversed" -> SyntaxKind.REVERSED;
			case "rg" -> SyntaxKind.RG;
			case "right" -> SyntaxKind.RIGHT;
			case "rollback" -> SyntaxKind.ROLLBACK;
			case "rounded" -> SyntaxKind.ROUNDED;
			case "routine" -> SyntaxKind.ROUTINE;
			case "row" -> SyntaxKind.ROW;
			case "rows" -> SyntaxKind.ROWS;
			case "rr" -> SyntaxKind.RR;
			case "rs" -> SyntaxKind.RS;
			case "rulevar" -> SyntaxKind.RULEVAR;
			case "run" -> SyntaxKind.RUN;
			case "sa" -> SyntaxKind.SA;
			case "same" -> SyntaxKind.SAME;
			case "scan" -> SyntaxKind.SCAN;
			case "screen" -> SyntaxKind.SCREEN;
			case "scroll" -> SyntaxKind.SCROLL;
			case "second" -> SyntaxKind.SECOND;
			case "select" -> SyntaxKind.SELECT;
			case "selection" -> SyntaxKind.SELECTION;
			case "send" -> SyntaxKind.SEND;
			case "sensitive" -> SyntaxKind.SENSITIVE;
			case "separate" -> SyntaxKind.SEPARATE;
			case "sequence" -> SyntaxKind.SEQUENCE;
			case "server" -> SyntaxKind.SERVER;
			case "set" -> SyntaxKind.SET;
			case "sets" -> SyntaxKind.SETS;
			case "settime" -> SyntaxKind.SETTIME;
			case "sf" -> SyntaxKind.SF;
			case "sg" -> SyntaxKind.SG;
			case "sgn" -> SyntaxKind.SGN;
			case "shared" -> SyntaxKind.SHARED;
			case "short" -> SyntaxKind.SHORT;
			case "show" -> SyntaxKind.SHOW;
			case "sin" -> SyntaxKind.SIN;
			case "single" -> SyntaxKind.SINGLE;
			case "size" -> SyntaxKind.SIZE;
			case "skip" -> SyntaxKind.SKIP;
			case "sl" -> SyntaxKind.SL;
			case "sm" -> SyntaxKind.SM;
			case "some" -> SyntaxKind.SOME;
			case "sort" -> SyntaxKind.SORT;
			case "sorted" -> SyntaxKind.SORTED;
			case "sortkey" -> SyntaxKind.SORTKEY;
			case "sound" -> SyntaxKind.SOUND;
			case "space" -> SyntaxKind.SPACE;
			case "specified" -> SyntaxKind.SPECIFIED;
			case "sql" -> SyntaxKind.SQL;
			case "sqlid" -> SyntaxKind.SQLID;
			case "sqrt" -> SyntaxKind.SQRT;
			case "stack" -> SyntaxKind.STACK;
			case "start" -> SyntaxKind.START;
			case "starting" -> SyntaxKind.STARTING;
			case "statement" -> SyntaxKind.STATEMENT;
			case "static" -> SyntaxKind.STATIC;
			case "status" -> SyntaxKind.STATUS;
			case "step" -> SyntaxKind.STEP;
			case "stop" -> SyntaxKind.STOP;
			case "store" -> SyntaxKind.STORE;
			case "subprogram" -> SyntaxKind.SUBPROGRAM;
			case "subprograms" -> SyntaxKind.SUBPROGRAMS;
			case "subroutine" -> SyntaxKind.SUBROUTINE;
			case "substr" -> SyntaxKind.SUBSTR;
			case "substring" -> SyntaxKind.SUBSTRING;
			case "subtract" -> SyntaxKind.SUBTRACT;
			case "sum" -> SyntaxKind.SUM;
			case "suppress" -> SyntaxKind.SUPPRESS;
			case "suppressed" -> SyntaxKind.SUPPRESSED;
			case "suspend" -> SyntaxKind.SUSPEND;
			case "symbol" -> SyntaxKind.SYMBOL;
			case "sync" -> SyntaxKind.SYNC;
			case "system" -> SyntaxKind.SYSTEM;
			case "tan" -> SyntaxKind.TAN;
			case "tc" -> SyntaxKind.TC;
			case "tcu" -> SyntaxKind.TCU;
			case "terminate" -> SyntaxKind.TERMINATE;
			case "text" -> SyntaxKind.TEXT;
			case "textarea" -> SyntaxKind.TEXTAREA;
			case "textvariable" -> SyntaxKind.TEXTVARIABLE;
			case "than" -> SyntaxKind.THAN;
			case "them" -> SyntaxKind.THEM;
			case "then" -> SyntaxKind.THEN;
			case "thru" -> SyntaxKind.THRU;
			case "time" -> SyntaxKind.TIME;
			case "timestamp" -> SyntaxKind.TIMESTAMP;
			case "timezone" -> SyntaxKind.TIMEZONE;
			case "title" -> SyntaxKind.TITLE;
			case "to" -> SyntaxKind.TO;
			case "top" -> SyntaxKind.TOP;
			case "total" -> SyntaxKind.TOTAL;
			case "tp" -> SyntaxKind.TP;
			case "tr" -> SyntaxKind.TR;
			case "trailer" -> SyntaxKind.TRAILER;
			case "trailing" -> SyntaxKind.TRAILING;
			case "transaction" -> SyntaxKind.TRANSACTION;
			case "transfer" -> SyntaxKind.TRANSFER;
			case "translate" -> SyntaxKind.TRANSLATE;
			case "treq" -> SyntaxKind.TREQ;
			case "true" -> SyntaxKind.TRUE;
			case "ts" -> SyntaxKind.TS;
			case "type" -> SyntaxKind.TYPE;
			case "types" -> SyntaxKind.TYPES;
			case "uc" -> SyntaxKind.UC;
			case "underlined" -> SyntaxKind.UNDERLINED;
			case "union" -> SyntaxKind.UNION;
			case "unique" -> SyntaxKind.UNIQUE;
			case "unknown" -> SyntaxKind.UNKNOWN;
			case "until" -> SyntaxKind.UNTIL;
			case "update" -> SyntaxKind.UPDATE;
			case "upload" -> SyntaxKind.UPLOAD;
			case "upper" -> SyntaxKind.UPPER;
			case "ur" -> SyntaxKind.UR;
			case "used" -> SyntaxKind.USED;
			case "user" -> SyntaxKind.USER;
			case "using" -> SyntaxKind.USING;
			case "val" -> SyntaxKind.VAL;
			case "value" -> SyntaxKind.VALUE;
			case "values" -> SyntaxKind.VALUES;
			case "vargraphic" -> SyntaxKind.VARGRAPHIC;
			case "variable" -> SyntaxKind.VARIABLE;
			case "variables" -> SyntaxKind.VARIABLES;
			case "vert" -> SyntaxKind.VERT;
			case "vertically" -> SyntaxKind.VERTICALLY;
			case "via" -> SyntaxKind.VIA;
			case "view" -> SyntaxKind.VIEW;
			case "wh" -> SyntaxKind.WH;
			case "when" -> SyntaxKind.WHEN;
			case "where" -> SyntaxKind.WHERE;
			case "while" -> SyntaxKind.WHILE;
			case "window" -> SyntaxKind.WINDOW;
			case "with" -> SyntaxKind.WITH;
			case "work" -> SyntaxKind.WORK;
			case "write" -> SyntaxKind.WRITE;
			case "with_cte" -> SyntaxKind.WITH_CTE;
			case "xml" -> SyntaxKind.XML;
			case "year" -> SyntaxKind.YEAR;
			case "zd" -> SyntaxKind.ZD;
			case "zp" -> SyntaxKind.ZP;
			default -> null;
		};
	}
}
