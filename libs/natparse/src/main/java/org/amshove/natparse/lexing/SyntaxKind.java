package org.amshove.natparse.lexing;

public enum SyntaxKind
{
	WHITESPACE,
	NEW_LINE,
	TAB,
	LBRACKET,
	RBRACKET,
	LPAREN,
	RPAREN,
	EQUALS,
	COLON,
	COLON_EQUALS,
	DOT,
	COMMA,
	PLUS,
	MINUS,
	ASTERISK,
	SLASH,
	BACKSLASH,
	SEMICOLON,
	GREATER,
	GREATER_EQUALS,
	LESSER_EQUALS,
	GT,
	LT,
	LE,
	GE,
	EQ,
	NE,
	LESSER,
	NUMBER,
	LESSER_GREATER,
	STRING,
	IDENTIFIER_OR_KEYWORD,
	IDENTIFIER,
	COMMENT,

	// Builtin Functions/Expressions
	TIMX,
	DATX,

	ABS,
	ACCEPT,
	ADD,
	ALL,
	ANY,
	ASSIGN,
	AT,
	ATN,
	AVER,
	BACKOUT,
	BEFORE,
	BREAK,
	BROWSE,
	CALL,
	CALLDBPROC,
	CALLNAT,
	CLOSE,
	COMMIT,
	COMPOSE,
	COMPRESS,
	COMPUTE,
	COPY,
	COS,
	COUNT,
	CREATE,
	DATA,
	DECIDE,
	DEFINE,
	DELETE,
	DISPLAY,
	DIVIDE,
	DLOGOFF,
	DLOGON,
	DNATIVE,
	DO,
	DOEND,
	DOWNLOAD,
	EJECT,
	ELSE,
	END,
	END_ALL,
	END_BEFORE,
	END_BREAK,
	END_BROWSE,
	END_DECIDE,
	END_ENDDATA,
	END_ENDFILE,
	END_ENDPAGE,
	END_ERROR,
	END_FILE,
	END_FIND,
	END_FOR,
	END_HISTOGRAM,
	ENDHOC,
	END_IF,
	END_LOOP,
	END_NOREC,
	END_PARSE,
	END_PROCESS,
	END_READ,
	END_REPEAT,
	END_RESULT,
	END_SELECT,
	END_SORT,
	END_START,
	END_DATA,
	END_DEFINE,
	END_SUBROUTINE,
	END_TOPPAGE,
	END_WORK,
	ENTIRE,
	ESCAPE,
	EXAMINE,
	EXP,
	EXPAND,
	EXPORT,
	FALSE,
	FETCH,
	FIND,
	FOR,
	FORMAT,
	FRAC,
	GET,
	HISTOGRAM,
	IF,
	IGNORE,
	IMPORT,
	INCCONT,
	INCDIC,
	INCDIR,
	INCLUDE,
	INCMAC,
	INPUT,
	INSERT,
	INT,
	INVESTIGATE,
	LIMIT,
	LOCAL,
	LOG,
	LOOP,
	MAP,
	MAX,
	MIN,
	MOVE,
	MULTIPLY,
	NAVER,
	NCOUNT,
	NEWPAGE,
	NMIN,
	NONE,
	NULL_HANDLE,
	OBTAIN,
	OLD,
	ON,
	OPEN,
	OPTIONS,
	PARSE,
	PASSW,
	PERFORM,
	POS,
	PRINT,
	PROCESS,
	READ,
	REDEFINE,
	REDUCE,
	REINPUT,
	REJECT,
	RELEASE,
	REPEAT,
	REQUEST,
	RESET,
	RESIZE,
	RESTORE,
	RET,
	RETRY,
	RETURN,
	ROLLBACK,
	RULEVAR,
	RUN,
	SELECT,
	SEND,
	SEPARATE,
	SET,
	SETTIME,
	SGN,
	SHOW,
	SIN,
	SKIP,
	SORT,
	SORTKEY,
	SQRT,
	STACK,
	START,
	STOP,
	STORE,
	SUBSTR,
	SUBSTRING,
	SUBTRACT,
	SUM,
	SUSPEND,
	TAN,
	TERMINATE,
	TOP,
	TOTAL,
	TRANSFER,
	TRUE,
	UNTIL,
	UPDATE,
	USING,
	UPLOAD,
	VAL,
	VALUE,
	VALUES,
	WASTE,
	WHEN,
	WHILE,
	WITH_CTE,
	WRITE;

	public boolean isWhitespace()
	{
		return this == WHITESPACE || this == TAB;
	}

	public boolean isIdentifier()
	{
		return this == IDENTIFIER || this == IDENTIFIER_OR_KEYWORD; // TODO: Keyword temporary
	}
}
