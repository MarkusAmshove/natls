package org.amshove.natparse.lexing;

public enum SyntaxKind
{
	EOF(false, false, false), // end of file
	LBRACKET(false, false, false),
	RBRACKET(false, false, false),
	LPAREN(false, false, false),
	RPAREN(false, false, false),
	EQUALS_SIGN(false, false, false),
	COLON(false, false, false),
	COLON_EQUALS_SIGN(false, false, false),
	DOT(false, false, false),
	CARET(false, false, false),
	COMMA(false, false, false),
	PLUS(false, false, false),
	EXPONENT_OPERATOR(false, false, false),
	MINUS(false, false, false),
	ASTERISK(false, false, false),
	SLASH(false, false, false),
	BACKSLASH(false, false, false),
	UNDERSCORE(false, false, false),
	SECTION_SYMBOL(false, false, false),
	SEMICOLON(false, false, false),
	GREATER_SIGN(false, false, false),
	GREATER_EQUALS_SIGN(false, false, false),
	LESSER_SIGN(false, false, false),
	LESSER_EQUALS_SIGN(false, false, false),
	CIRCUMFLEX_EQUAL(false, false, false),
	NUMBER_LITERAL(false, false, false),
	LESSER_GREATER(false, false, false),
	STRING_LITERAL(false, false, false),
	IDENTIFIER(true, false, false),
	LABEL_IDENTIFIER(false, false, false),
	COMMENT(false, false, false),
	PERCENT(false, false, false),
	QUESTIONMARK(false, false, false),
	OPERAND_SKIP(false, false, false),
	TAB_SETTING(false, false, false),
	COLOR_ATTRIBUTE(false, false, false),

	// System variables and functions
	APPLIC_ID(false, true, false),
	INIT_ID(false, true, false),
	SV_TIME(false, true, false),
	TIMX(false, true, false),
	TIMD(false, false, true),
	TIMN(false, true, false),
	TIME_OUT(false, true, false),
	DATD(false, true, false),
	DATG(false, true, false),
	DATE(true, false, false),
	SV_DATE(false, true, false),
	DAT4E(false, true, false),
	DAT4I(false, true, false),
	DATI(false, true, false),
	DAT4J(false, true, false),
	DATJ(false, true, false),
	DAT4D(false, true, false),
	LANGUAGE(true, true, false),
	DATX(false, true, false),
	DATN(false, true, false),
	DATU(false, true, false),
	DAT4U(false, true, false),
	DATV(false, true, false),
	DATVS(false, true, false),
	STARTUP(false, true, false),
	STEPLIB(false, true, false),
	PAGE_NUMBER(false, true, true),
	WINDOW_LS(false, true, false),
	WINDOW_PS(false, true, false),
	LIBRARY_ID(false, true, false),
	LINEX(false, true, false),
	LINE_COUNT(false, true, true),
	LINESIZE(false, true, false),
	MACHINE_CLASS(false, true, false),
	PAGESIZE(false, true, false),
	SV_ISN(false, true, true),
	CURRENT_UNIT(false, true, false),
	OCC(false, false, true),
	OCCURRENCE(false, false, true),
	ERROR_NR(false, true, false),
	SV_ERROR(false, true, false), // *ERROR is equivalent of *ERROR-NR
	ERROR_LINE(false, true, false),
	ERROR_TA(false, true, false),
	LINE(false, true, false),
	TRIM(false, false, true),
	MAXVAL(false, false, true),
	MINVAL(false, false, true),
	CURS_LINE(false, true, false),
	CURS_COL(false, true, false),
	CURS_FIELD(false, true, false),
	SV_DATA(false, true, false),
	SV_LEVEL(false, true, false),
	SV_NUMBER(false, true, true),
	SV_LENGTH(false, false, true),
	TRANSLATE(true, false, true),
	TIMESTMP(false, true, false),
	PF_KEY(false, true, false),
	INIT_PROGRAM(false, true, false),
	INIT_USER(false, true, false),
	SV_USER(false, true, false),
	COUNTER(false, true, true),
	COM(false, true, false),
	DEVICE(false, true, false),
	OPSYS(false, true, false),
	TPSYS(false, true, false),
	PROGRAM(true, true, false),
	ETID(false, true, false),
	CPU_TIME(false, true, false),
	LBOUND(false, false, true),
	UBOUND(false, false, true),
	SERVER_TYPE(false, true, false),

	// Kcheck reserved keywords
	ABS(false, false, false),
	ACCEPT(false, false, false),
	ADD(false, false, false),
	ALL(false, false, false),
	ANY(false, false, false),
	ASSIGN(false, false, false),
	AT(false, false, false),
	ATN(false, false, false),
	AVER(false, false, false),
	BACKOUT(false, false, false),
	BEFORE(true, false, false), // TODO: Should be false, but is currently used by NatUnit
	BREAK(false, false, false),
	BROWSE(false, false, false),
	CALL(false, false, false),
	CALLDBPROC(false, false, false),
	CALLNAT(false, false, false),
	CLOSE(false, false, false),
	COMMIT(false, false, false),
	COMPOSE(false, false, false),
	COMPRESS(false, false, false),
	COMPUTE(false, false, false),
	COPY(false, false, false),
	COS(false, false, false),
	COUNT(true, false, false),
	CREATE(false, false, false),
	DECIDE(false, false, false),
	DEFINE(false, false, false),
	DELETE(false, false, false),
	DISPLAY(false, false, false),
	DIVIDE(false, false, false),
	DLOGOFF(false, false, false),
	DLOGON(false, false, false),
	DNATIVE(false, false, false),
	DO(false, false, false),
	DOEND(false, false, false),
	DOWNLOAD(false, false, false),
	EJECT(false, false, false),
	ELSE(false, false, false),
	END(false, false, false),
	END_ALL(false, false, false),
	END_BEFORE(false, false, false),
	END_BREAK(false, false, false),
	END_BROWSE(false, false, false),
	END_DECIDE(false, false, false),
	END_ENDDATA(false, false, false),
	END_ENDFILE(false, false, false),
	END_ENDPAGE(false, false, false),
	END_ERROR(false, false, false),
	END_FILE(false, false, false),
	END_FIND(false, false, false),
	END_FOR(false, false, false),
	END_FUNCTION(false, false, false),
	END_HISTOGRAM(false, false, false),
	ENDHOC(false, false, false),
	END_IF(false, false, false),
	END_LOOP(false, false, false),
	END_NOREC(false, false, false),
	END_PARSE(false, false, false),
	END_PROCESS(false, false, false),
	END_READ(false, false, false),
	END_REPEAT(false, false, false),
	END_RESULT(false, false, false),
	END_SELECT(false, false, false),
	END_SORT(false, false, false),
	END_START(false, false, false),
	END_SUBROUTINE(false, false, false),
	END_TOPPAGE(false, false, false),
	END_WORK(false, false, false),
	ENTIRE(false, false, false),
	ESCAPE(false, false, false),
	EXAMINE(false, false, false),
	EXP(false, false, false),
	EXPAND(false, false, false),
	EXPORT(false, false, false),
	FALSE(false, false, false),
	FETCH(false, false, false),
	FIND(false, false, false),
	FOR(false, false, false),
	FORMAT(false, false, false),
	FRAC(false, false, false),
	GET(false, false, false),
	HISTOGRAM(false, false, false),
	IF(false, false, false),
	IGNORE(false, false, false),
	IMPORT(false, false, false),
	INCCONT(false, false, false),
	INCDIC(false, false, false),
	INCDIR(false, false, false),
	INCLUDE(false, false, false),
	INCMAC(false, false, false),
	INPUT(false, false, false),
	INSERT(false, false, false),
	INT(false, false, false),
	INVESTIGATE(false, false, false),
	LIMIT(false, false, false),
	LOG(true, false, false),
	LOOP(false, false, false),
	MAP(true, false, false),
	MAX(false, false, false),
	MIN(false, false, false),
	MOVE(false, false, false),
	MULTIPLY(false, false, false),
	NAVER(false, false, false),
	NCOUNT(false, false, false),
	NEWPAGE(false, false, false),
	NMIN(false, false, false),
	NONE(false, false, false),
	NULL_HANDLE(false, false, false),
	OBTAIN(false, false, false),
	OLD(false, false, false),
	ON(false, false, false),
	OPEN(false, false, false),
	OPTIONS(false, false, false),
	PARSE(false, false, false),
	PASSW(false, false, false),
	PERFORM(false, false, false),
	POS(false, false, false),
	PRINT(false, false, false),
	PROCESS(false, false, false),
	READ(false, false, false),
	REDEFINE(false, false, false),
	REDUCE(false, false, false),
	REINPUT(false, false, false),
	REJECT(false, false, false),
	RELEASE(false, false, false),
	REPEAT(false, false, false),
	REQUEST(false, false, false),
	RESET(false, false, false),
	RESIZE(false, false, false),
	RESTORE(false, false, false),
	RET(false, false, false),
	RETRY(false, false, false),
	RETURN(true, false, false),
	ROLLBACK(false, false, false),
	ROUNDED(false, false, false),
	RULEVAR(false, false, false),
	RUN(false, false, false),
	SELECT(false, false, false),
	SEND(false, false, false),
	SEPARATE(false, false, false),
	SET(false, false, false),
	SETTIME(false, false, false),
	SGN(false, false, false),
	SHOW(false, false, false),
	SIN(false, false, false),
	SKIP(false, false, false),
	SORT(false, false, false),
	SORTKEY(false, false, false),
	SQRT(false, false, false),
	STACK(false, false, false),
	START(false, false, false),
	STOP(false, false, false),
	STORE(false, false, false),
	SUBSTR(false, false, false),
	SUBSTRING(false, false, false),
	SUBTRACT(false, false, false),
	SUM(false, false, false),
	SUSPEND(false, false, false),
	TAN(false, false, false),
	TERMINATE(false, false, false),
	TOP(false, false, false),
	TOTAL(false, false, false),
	TRANSFER(false, false, false),
	TRUE(false, false, false),
	UNTIL(false, false, false),
	UPDATE(false, false, false),
	UPLOAD(false, false, false),
	VAL(false, false, false),
	VALUE(false, false, false),
	VALUES(false, false, false),
	WASTE(false, false, false),
	WHEN(false, false, false),
	WHILE(false, false, false),
	WITH_CTE(false, false, false),
	WRITE(false, false, false),

	// Other keywords
	ABSOLUTE(true, false, false),
	ACTION(true, false, false),
	ACTIVATION(true, false, false),
	AD(true, false, false), // Attribute Definition
	CD(true, false, false), // Color Definition
	AFTER(true, false, false),
	AL(true, false, false),
	ALARM(true, false, false),
	ALPHA(true, false, false),
	ALPHABETICALLY(true, false, false),
	AND(true, false, false),
	APPL(true, false, false),
	APPLICATION(true, false, false),
	ARRAY(true, false, false),
	AS(true, false, false),
	ASC(true, false, false),
	ASCENDING(true, false, false),
	ASSIGNING(true, false, false),
	ASYNC(true, false, false),
	ATT(true, false, false),
	ATTRIBUTES(true, false, false),
	AUTH(true, false, false),
	AUTHORIZATION(true, false, false),
	AUTO(true, false, false),
	AVG(true, false, false),
	BACKWARD(true, false, false),
	BASE(true, false, false),
	BETWEEN(true, false, false),
	BLOCK(true, false, false),
	BOT(true, false, false),
	BOTTOM(true, false, false),
	BUT(true, false, false),
	BX(true, false, false),
	BY(true, false, false),
	CABINET(true, false, false),
	CALLING(true, false, false),
	CAP(true, false, false),
	CAPTIONED(true, false, false),
	CASE(true, false, false),
	CC(true, false, false),
	CDID(true, false, false),
	CF(true, false, false),
	CHAR(true, false, false),
	CHARLENGTH(true, false, false),
	CHARPOSITION(true, false, false),
	CHILD(true, false, false),
	CIPH(true, false, false),
	CIPHER(true, false, false),
	CLASS(true, false, false),
	COALESCE(true, false, false),
	CODEPAGE(true, false, false),
	COMMAND(true, false, false),
	CONCAT(true, false, false),
	CONDITION(true, false, false),
	CONST(true, false, false),
	CONSTANT(true, false, false),
	CONTEXT(true, false, false),
	CONTROL(true, false, false),
	CONVERSATION(true, false, false),
	COPIES(true, false, false),
	COUPLED(true, false, false),
	CS(true, false, false),
	CURRENT(true, false, false),
	CURSOR(true, false, false),
	CV(true, false, false),
	DATA(true, false, false),
	DATAAREA(true, false, false),
	DAY(true, false, false),
	DAYS(true, false, false),
	DC(true, false, false),
	DECIMAL(true, false, false),
	DEFINITION(true, false, false),
	DELIMITED(true, false, false),
	DELIMITER(true, false, false),
	DELIMITERS(true, false, false),
	DESC(true, false, false),
	DESCENDING(true, false, false),
	DF(true, false, false),
	DIALOG(true, false, false),
	DIALOG_ID(true, false, false),
	DIGITS(true, false, false),
	DIRECTION(true, false, false),
	DISABLED(true, false, false),
	DISP(true, false, false),
	DISTINCT(true, false, false),
	DL(true, false, false),
	DNRET(true, false, false),
	DOCUMENT(true, false, false),
	DU(true, false, false),
	DY(true, false, false),
	DYNAMIC(true, false, false),
	EDITED(true, false, false),
	EJ(true, false, false),
	EM(true, false, false), // EDITOR MASK
	ENCODED(true, false, false),
	END_CLASS(true, false, false),
	END_DEFINE(true, false, false),
	END_INTERFACE(true, false, false),
	END_METHOD(true, false, false),
	END_PARAMETERS(true, false, false),
	END_PROPERTY(true, false, false),
	END_PROTOTYPE(true, false, false),
	ENDING(true, false, false),
	ENTER(true, false, false),
	EQ(true, false, false),
	EQUAL(true, false, false),
	ERASE(true, false, false),
	ERROR(true, false, false),
	ERRORS(true, false, false),
	ES(true, false, false),
	EVEN(true, false, false),
	EVENT(true, false, false),
	EVERY(true, false, false),
	EXCEPT(true, false, false),
	EXISTS(true, false, false),
	EXIT(true, false, false),
	EXTERNAL(true, false, false),
	EXTRACTING(true, false, false),
	FC(true, false, false),
	FIELD(true, false, false),
	FIELDS(true, false, false),
	FILE(true, false, false),
	FILL(true, false, false),
	FILLER(true, false, false),
	FINAL(true, false, false),
	FIRST(true, false, false),
	FL(true, false, false),
	FLOAT(true, false, false),
	FORM(true, false, false),
	FORMATTED(true, false, false),
	FORMATTING(true, false, false),
	FORMS(true, false, false),
	FORWARD(true, false, false),
	FOUND(true, false, false),
	FRAMED(true, false, false),
	FROM(true, false, false),
	FS(true, false, false),
	FULL(true, false, false),
	FUNCTION(true, false, false),
	FUNCTIONS(true, false, false),
	GC(true, false, false),
	GE(true, false, false),
	GEN(true, false, false),
	GENERATED(true, false, false),
	GFID(true, false, false),
	GIVE(true, false, false),
	GIVING(false, false, false),
	GLOBAL(true, false, false),
	GLOBALS(true, false, false),
	GREATER(true, false, false),
	GT(true, false, false),
	GUI(true, false, false),
	HANDLE(true, false, false),
	HAVING(true, false, false),
	HC(true, false, false),
	HD(true, false, false),
	HE(true, false, false),
	HEADER(true, false, false),
	HELP(true, false, false),
	HEX(true, false, false),
	HOLD(true, false, false),
	HORIZ(true, false, false),
	HORIZONTALLY(true, false, false),
	HOUR(true, false, false),
	HOURS(true, false, false),
	HW(true, false, false),
	IA(true, false, false),
	IC(true, false, false),
	ID(true, false, false),
	IDENTICAL(true, false, false),
	IM(true, false, false),
	IMMEDIATE(true, false, false),
	IN(true, false, false),
	INC(true, false, false),
	INCLUDED(true, false, false),
	INCLUDING(true, false, false),
	INDEPENDENT(true, false, false),
	INDEX(true, false, false),
	INDEXED(true, false, false),
	INDICATOR(true, false, false),
	INIT(true, false, false),
	INITIAL(true, false, false),
	INNER(true, false, false),
	INSENSITIVE(true, false, false),
	INTEGER(true, false, false),
	INTERCEPTED(true, false, false),
	INTERFACE(true, false, false),
	INTERFACE4(true, false, false),
	INTERMEDIATE(true, false, false),
	INTERSECT(true, false, false),
	INTO(true, false, false),
	INVERTED(true, false, false),
	IP(true, false, false),
	IS(true, false, false),
	KW_ISN(true, false, false),
	JOIN(true, false, false),
	JUST(true, false, false),
	JUSTIFIED(true, false, false),
	KD(true, false, false),
	DEL(true, false, false),
	KEEP(true, false, false),
	KEY(true, false, false),
	KEYS(true, false, false),
	LAST(true, false, false),
	LC(true, false, false),
	LE(true, false, false),
	LEAVE(true, false, false),
	LEAVING(true, false, false),
	LEFT(true, false, false),
	LENGTH(true, false, false),
	LESS(true, false, false),
	LEVEL(true, false, false),
	LIB(true, false, false),
	LIBPW(true, false, false),
	LIBRARY(true, false, false),
	LIBRARY_PASSWORD(true, false, false),
	LIKE(true, false, false),
	LINDICATOR(true, false, false),
	LINES(true, false, false),
	LISTED(true, false, false),
	LOCAL(true, false, false),
	LOCKS(true, false, false),
	// LOG(true,false, false), sadly this is used my natunit as a subroutine name :(
	LOG_LS(true, false, false),
	LOG_PS(true, false, false),
	LOGICAL(true, false, false),
	LOWER(true, false, false),
	LS(true, false, false),
	LT(true, false, false),
	MACROAREA(true, false, false),
	MARK(true, false, false),
	MASK(true, false, false),
	MC(true, false, false),
	MCG(true, false, false),
	MESSAGES(true, false, false),
	METHOD(true, false, false),
	MGID(true, false, false),
	MICROSECOND(true, false, false),
	MINUTE(true, false, false),
	MODAL(true, false, false),
	MODIFIED(true, false, false),
	MODULE(true, false, false),
	MONTH(true, false, false),
	MORE(true, false, false),
	MOVING(true, false, false),
	MP(true, false, false),
	MS(true, false, false),
	MT(true, false, false),
	MULTI_FETCH(true, false, false),
	NAME(true, false, false),
	NAMED(true, false, false),
	NAMESPACE(true, false, false),
	NATIVE(true, false, false),
	NC(true, false, false),
	NE(true, false, false),
	NL(true, false, false),
	NO(true, false, false),
	NODE(true, false, false),
	NOHDR(true, false, false),
	NORMALIZE(true, false, false),
	NORMALIZED(true, false, false),
	NOT(true, false, false),
	NOTEQUAL(true, false, false),
	NOTIT(true, false, false),
	NOTITLE(true, false, false),
	NULL(true, false, false),
	KW_NUMBER(true, false, false),
	NUMERIC(true, false, false),
	OBJECT(true, false, false),
	OCCURRENCES(true, false, false),
	OF(true, false, false),
	OFF(true, false, false),
	OFFSET(true, false, false),
	ONCE(true, false, false),
	ONLY(true, false, false),
	OPTIMIZE(true, false, false),
	OPTIONAL(true, false, false),
	OR(true, false, false),
	ORDER(true, false, false),
	OUTER(true, false, false),
	OUTPUT(true, false, false),
	PACKAGESET(true, false, false),
	PAGE(true, false, false),
	PARAMETER(true, false, false),
	PARAMETERS(true, false, false),
	PARENT(true, false, false),
	PASS(true, false, false),
	PASSWORD(true, false, false),
	PATH(true, false, false),
	PATTERN(true, false, false),
	PC(true, false, false),
	PD(true, false, false),
	PEN(true, false, false),
	PGDN(true, false, false),
	PGUP(true, false, false),
	PGM(true, false, false),
	PHYSICAL(true, false, false),
	PM(true, false, false),
	POLICY(true, false, false),
	POSITION(true, false, false),
	PREFIX(true, false, false),
	PRINTER(true, false, false),
	PROCESSING(true, false, false),
	PROFILE(true, false, false),
	PROPERTY(true, false, false),
	PROTOTYPE(true, false, false),
	PRTY(true, false, false),
	PS(true, false, false),
	PT(true, false, false),
	PW(true, false, false),
	QUARTER(true, false, false),
	QUERYNO(true, false, false),
	RD(true, false, false),
	READONLY(true, false, false),
	REC(true, false, false),
	RECORD(true, false, false),
	RECORDS(true, false, false),
	RECURSIVELY(true, false, false),
	REFERENCED(true, false, false),
	REFERENCING(true, false, false),
	REL(true, false, false),
	RELATION(true, false, false),
	RELATIONSHIP(true, false, false),
	REMAINDER(true, false, false),
	REPLACE(true, false, false),
	REPORT(true, false, false),
	REPORTER(true, false, false),
	REPOSITION(true, false, false),
	REQUIRED(true, false, false),
	RESETTING(true, false, false),
	RESPONSE(true, false, false),
	RESULT(true, false, false),
	RETAIN(true, false, false),
	RETAINED(true, false, false),
	RETURNS(true, false, false),
	REVERSED(true, false, false),
	RG(true, false, false),
	RIGHT(true, false, false),
	ROUTINE(true, false, false),
	ROW(true, false, false),
	ROWS(true, false, false),
	RR(true, false, false),
	RS(true, false, false),
	SA(true, false, false),
	SAME(true, false, false),
	SCAN(true, false, false),
	SCREEN(true, false, false),
	SCROLL(true, false, false),
	SECOND(true, false, false),
	SELECTION(true, false, false),
	SENSITIVE(true, false, false),
	SEQUENCE(true, false, false),
	SERVER(true, false, false),
	SETS(true, false, false),
	SF(true, false, false),
	SG(true, false, false),
	SHORT(true, false, false),
	SINGLE(true, false, false),
	SIZE(true, false, false),
	SL(true, false, false),
	SM(true, false, false),
	SOME(true, false, false),
	SORTED(true, false, false),
	SOUND(true, false, false),
	SPACE(true, false, false),
	SPECIFIED(true, false, false),
	SQL(true, false, false),
	SQLID(true, false, false),
	STARTING(true, false, false),
	STATEMENT(true, false, false),
	STATIC(true, false, false),
	STATUS(true, false, false),
	STEP(true, false, false),
	SUBPROGRAM(true, false, false),
	SUBPROGRAMS(true, false, false),
	SUBROUTINE(true, false, false),
	SUPPRESS(true, false, false),
	SUPPRESSED(true, false, false),
	SYMBOL(true, false, false),
	SYNC(true, false, false),
	SYSTEM(true, false, false),
	TC(true, false, false),
	TEXT(true, false, false),
	TEXTAREA(true, false, false),
	TEXTVARIABLE(true, false, false),
	THAN(true, false, false),
	THEM(true, false, false),
	THEN(true, false, false),
	THRU(true, false, false),
	TIME(true, false, false),
	TIMESTAMP(true, false, false),
	TIMEZONE(true, false, false),
	TITLE(true, false, false),
	TO(true, false, false),
	TP(true, false, false),
	TR(true, false, false),
	TRAILER(true, false, false),
	TRANSACTION(true, false, false),
	TREQ(true, false, false),
	TS(true, false, false),
	TYPE(true, false, false),
	TYPES(true, false, false),
	UC(true, false, false),
	UNDERLINED(true, false, false),
	UNION(true, false, false),
	UNIQUE(true, false, false),
	UNKNOWN(true, false, false),
	UPPER(true, false, false),
	UR(true, false, false),
	USED(true, false, false),
	USER(true, false, false),
	USING(true, false, false),
	VARGRAPHIC(true, false, false),
	VARIABLE(true, false, false),
	VARIABLES(true, false, false),
	VERT(true, false, false),
	VERTICALLY(true, false, false),
	VIA(true, false, false),
	VIEW(true, false, false),
	WH(true, false, false),
	WHERE(true, false, false),
	WINDOW(true, false, false),
	WITH(true, false, false),
	WORK(true, false, false),
	XML(true, false, false),
	YEAR(true, false, false),
	ZD(true, false, false),
	ZP(true, false, false);

	private final boolean canBeIdentifier;
	private final boolean isSystemVariable;
	private final boolean isSystemFunction;

	SyntaxKind(boolean canBeIdentifier, boolean isSystemVariable, boolean isSystemFunction)
	{
		this.canBeIdentifier = canBeIdentifier;
		this.isSystemVariable = isSystemVariable;
		this.isSystemFunction = isSystemFunction;
	}

	public boolean isIdentifier()
	{
		return this == IDENTIFIER;
	}

	public boolean isSystemVariable()
	{
		return this.isSystemVariable;
	}

	public boolean isSystemFunction()
	{
		return this.isSystemFunction;
	}

	public boolean isBoolean()
	{
		return this == TRUE || this == FALSE;
	}

	public boolean isLiteralOrConst()
	{
		return isBoolean() || this == NUMBER_LITERAL || this == STRING_LITERAL;
	}

	public boolean canBeIdentifier()
	{
		return canBeIdentifier;
	}

	public boolean isAttribute()
	{
		return this == AD || this == DY || this == CD || this == EM || this == NL || this == AL || this == DF || this == IP || this == IS || this == CV;
	}
}
