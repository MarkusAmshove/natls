package org.amshove.natparse.lexing;

import java.util.Locale;

public class KeywordTable
{
	public static SyntaxKind getKeyword(String possibleKeyword)
	{
		var key = possibleKeyword.toLowerCase(Locale.ENGLISH);
		return switch (key)
			{
				// operators
				case "eq" -> SyntaxKind.EQ;
				case "ne" -> SyntaxKind.NE;
				case "gt" -> SyntaxKind.GT;
				case "ge" -> SyntaxKind.GE;
				case "lt" -> SyntaxKind.LT;
				case "le" -> SyntaxKind.LE;

				case "abs" -> SyntaxKind.ABS;
				case "accept" -> SyntaxKind.ACCEPT;
				case "add" -> SyntaxKind.ADD;
				case "all" -> SyntaxKind.ALL;
				case "any" -> SyntaxKind.ANY;
				case "assign" -> SyntaxKind.ASSIGN;
				case "at" -> SyntaxKind.AT;
				case "atn" -> SyntaxKind.ATN;
				case "aver" -> SyntaxKind.AVER;
				case "backout" -> SyntaxKind.BACKOUT;
				case "before" -> SyntaxKind.BEFORE;
				case "break" -> SyntaxKind.BREAK;
				case "browse" -> SyntaxKind.BROWSE;
				case "call" -> SyntaxKind.CALL;
				case "calldbproc" -> SyntaxKind.CALLDBPROC;
				case "callnat" -> SyntaxKind.CALLNAT;
				case "close" -> SyntaxKind.CLOSE;
				case "commit" -> SyntaxKind.COMMIT;
				case "compose" -> SyntaxKind.COMPOSE;
				case "compress" -> SyntaxKind.COMPRESS;
				case "compute" -> SyntaxKind.COMPUTE;
				case "const" -> SyntaxKind.CONST;
				case "copy" -> SyntaxKind.COPY;
				case "cos" -> SyntaxKind.COS;
				case "count" -> SyntaxKind.COUNT;
				case "create" -> SyntaxKind.CREATE;
				case "data" -> SyntaxKind.DATA;
				case "decide" -> SyntaxKind.DECIDE;
				case "define" -> SyntaxKind.DEFINE;
				case "delete" -> SyntaxKind.DELETE;
				case "display" -> SyntaxKind.DISPLAY;
				case "divide" -> SyntaxKind.DIVIDE;
				case "dlogoff" -> SyntaxKind.DLOGOFF;
				case "dlogon" -> SyntaxKind.DLOGON;
				case "dnative" -> SyntaxKind.DNATIVE;
				case "do" -> SyntaxKind.DO;
				case "doend" -> SyntaxKind.DOEND;
				case "download" -> SyntaxKind.DOWNLOAD;
				case "dynamic" -> SyntaxKind.DYNAMIC;
				case "eject" -> SyntaxKind.EJECT;
				case "else" -> SyntaxKind.ELSE;
				case "end" -> SyntaxKind.END;
				case "end-all" -> SyntaxKind.END_ALL;
				case "end-before" -> SyntaxKind.END_BEFORE;
				case "end-break" -> SyntaxKind.END_BREAK;
				case "end-browse" -> SyntaxKind.END_BROWSE;
				case "end-data" -> SyntaxKind.END_DATA;
				case "end-decide" -> SyntaxKind.END_DECIDE;
				case "end-define" -> SyntaxKind.END_DEFINE;
				case "end-enddata" -> SyntaxKind.END_ENDDATA;
				case "end-endfile" -> SyntaxKind.END_ENDFILE;
				case "end-endpage" -> SyntaxKind.END_ENDPAGE;
				case "end-error" -> SyntaxKind.END_ERROR;
				case "end-file" -> SyntaxKind.END_FILE;
				case "end-find" -> SyntaxKind.END_FIND;
				case "end-for" -> SyntaxKind.END_FOR;
				case "end-histogram" -> SyntaxKind.END_HISTOGRAM;
				case "endhoc" -> SyntaxKind.ENDHOC;
				case "end-if" -> SyntaxKind.END_IF;
				case "end-loop" -> SyntaxKind.END_LOOP;
				case "end-norec" -> SyntaxKind.END_NOREC;
				case "end-parse" -> SyntaxKind.END_PARSE;
				case "end-process" -> SyntaxKind.END_PROCESS;
				case "end-read" -> SyntaxKind.END_READ;
				case "end-repeat" -> SyntaxKind.END_REPEAT;
				case "end-result" -> SyntaxKind.END_RESULT;
				case "end-select" -> SyntaxKind.END_SELECT;
				case "end-sort" -> SyntaxKind.END_SORT;
				case "end-start" -> SyntaxKind.END_START;
				case "end-subroutine" -> SyntaxKind.END_SUBROUTINE;
				case "end-toppage" -> SyntaxKind.END_TOPPAGE;
				case "end-work" -> SyntaxKind.END_WORK;
				case "entire" -> SyntaxKind.ENTIRE;
				case "escape" -> SyntaxKind.ESCAPE;
				case "examine" -> SyntaxKind.EXAMINE;
				case "exp" -> SyntaxKind.EXP;
				case "expand" -> SyntaxKind.EXPAND;
				case "export" -> SyntaxKind.EXPORT;
				case "false" -> SyntaxKind.FALSE;
				case "fetch" -> SyntaxKind.FETCH;
				case "find" -> SyntaxKind.FIND;
				case "for" -> SyntaxKind.FOR;
				case "format" -> SyntaxKind.FORMAT;
				case "frac" -> SyntaxKind.FRAC;
				case "get" -> SyntaxKind.GET;
				case "global" -> SyntaxKind.GLOBAL;
				case "histogram" -> SyntaxKind.HISTOGRAM;
				case "if" -> SyntaxKind.IF;
				case "ignore" -> SyntaxKind.IGNORE;
				case "import" -> SyntaxKind.IMPORT;
				case "inccont" -> SyntaxKind.INCCONT;
				case "incdic" -> SyntaxKind.INCDIC;
				case "incdir" -> SyntaxKind.INCDIR;
				case "include" -> SyntaxKind.INCLUDE;
				case "incmac" -> SyntaxKind.INCMAC;
				case "independent" -> SyntaxKind.INDEPENDENT;
				case "init" -> SyntaxKind.INIT;
				case "input" -> SyntaxKind.INPUT;
				case "insert" -> SyntaxKind.INSERT;
				case "int" -> SyntaxKind.INT;
				case "investigate" -> SyntaxKind.INVESTIGATE;
				case "limit" -> SyntaxKind.LIMIT;
				case "local" -> SyntaxKind.LOCAL;
				case "log" -> SyntaxKind.LOG;
				case "loop" -> SyntaxKind.LOOP;
				case "map" -> SyntaxKind.MAP;
				case "max" -> SyntaxKind.MAX;
				case "min" -> SyntaxKind.MIN;
				case "move" -> SyntaxKind.MOVE;
				case "multiply" -> SyntaxKind.MULTIPLY;
				case "naver" -> SyntaxKind.NAVER;
				case "ncount" -> SyntaxKind.NCOUNT;
				case "newpage" -> SyntaxKind.NEWPAGE;
				case "nmin" -> SyntaxKind.NMIN;
				case "none" -> SyntaxKind.NONE;
				case "null-handle" -> SyntaxKind.NULL_HANDLE;
				case "obtain" -> SyntaxKind.OBTAIN;
				case "old" -> SyntaxKind.OLD;
				case "on" -> SyntaxKind.ON;
				case "open" -> SyntaxKind.OPEN;
				case "options" -> SyntaxKind.OPTIONS;
				case "parameter" -> SyntaxKind.PARAMETER;
				case "parse" -> SyntaxKind.PARSE;
				case "passw" -> SyntaxKind.PASSW;
				case "perform" -> SyntaxKind.PERFORM;
				case "pos" -> SyntaxKind.POS;
				case "print" -> SyntaxKind.PRINT;
				case "process" -> SyntaxKind.PROCESS;
				case "read" -> SyntaxKind.READ;
				case "redefine" -> SyntaxKind.REDEFINE;
				case "reduce" -> SyntaxKind.REDUCE;
				case "reinput" -> SyntaxKind.REINPUT;
				case "reject" -> SyntaxKind.REJECT;
				case "release" -> SyntaxKind.RELEASE;
				case "repeat" -> SyntaxKind.REPEAT;
				case "request" -> SyntaxKind.REQUEST;
				case "reset" -> SyntaxKind.RESET;
				case "resize" -> SyntaxKind.RESIZE;
				case "restore" -> SyntaxKind.RESTORE;
				case "ret" -> SyntaxKind.RET;
				case "retry" -> SyntaxKind.RETRY;
				case "return" -> SyntaxKind.RETURN;
				case "rollback" -> SyntaxKind.ROLLBACK;
				case "rulevar" -> SyntaxKind.RULEVAR;
				case "run" -> SyntaxKind.RUN;
				case "select" -> SyntaxKind.SELECT;
				case "send" -> SyntaxKind.SEND;
				case "separate" -> SyntaxKind.SEPARATE;
				case "set" -> SyntaxKind.SET;
				case "settime" -> SyntaxKind.SETTIME;
				case "sgn" -> SyntaxKind.SGN;
				case "show" -> SyntaxKind.SHOW;
				case "sin" -> SyntaxKind.SIN;
				case "skip" -> SyntaxKind.SKIP;
				case "sort" -> SyntaxKind.SORT;
				case "sortkey" -> SyntaxKind.SORTKEY;
				case "sqrt" -> SyntaxKind.SQRT;
				case "stack" -> SyntaxKind.STACK;
				case "start" -> SyntaxKind.START;
				case "stop" -> SyntaxKind.STOP;
				case "store" -> SyntaxKind.STORE;
				case "substr" -> SyntaxKind.SUBSTR;
				case "substring" -> SyntaxKind.SUBSTRING;
				case "subtract" -> SyntaxKind.SUBTRACT;
				case "sum" -> SyntaxKind.SUM;
				case "suspend" -> SyntaxKind.SUSPEND;
				case "tan" -> SyntaxKind.TAN;
				case "terminate" -> SyntaxKind.TERMINATE;
				case "top" -> SyntaxKind.TOP;
				case "total" -> SyntaxKind.TOTAL;
				case "transfer" -> SyntaxKind.TRANSFER;
				case "true" -> SyntaxKind.TRUE;
				case "until" -> SyntaxKind.UNTIL;
				case "update" -> SyntaxKind.UPDATE;
				case "using" -> SyntaxKind.USING;
				case "upload" -> SyntaxKind.UPLOAD;
				case "val" -> SyntaxKind.VAL;
				case "value" -> SyntaxKind.VALUE;
				case "values" -> SyntaxKind.VALUES;
				case "waste" -> SyntaxKind.WASTE;
				case "when" -> SyntaxKind.WHEN;
				case "while" -> SyntaxKind.WHILE;
				case "with_cte" -> SyntaxKind.WITH_CTE;
				case "write" -> SyntaxKind.WRITE;
				default -> null;
			};
	}
}
