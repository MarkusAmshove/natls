# Implemented statements

This document tracks the implementation status of Natural statements.

Legend:

:x: - not implemented (73)

:white_check_mark: - implemented (43)

partial - partially implemented to prevent false positives (6)

| Statement | Status             |
| --- |--------------------|
| ACCEPT/REJECT | :x:                |
| ADD | :white_check_mark: |
| ASSIGN | :white_check_mark: |
| AT BREAK | :white_check_mark: |
| AT END OF DATA | :white_check_mark: |
| AT END OF PAGE | :white_check_mark: |
| AT START OF DATA | :white_check_mark: |
| AT TOP OF PAGE | :white_check_mark: |
| BACKOUT TRANSACTION | :white_check_mark: |
| BEFORE BREAK PROCESSING | :white_check_mark: |
| CALL | :x:                |
| CALL FILE | :x:                |
| CALL LOOP | :x:                |
| CALLDBPROC (SQL) | :x:                |
| CALLNAT | :white_check_mark: |
| CLOSE CONVERSATION | :x:                |
| CLOSE PC FILE | :white_check_mark: |
| CLOSE PRINTER | :white_check_mark: |
| CLOSE WORK FILE | :white_check_mark: |
| COMMIT (SQL) | :x:                |
| COMPRESS | :white_check_mark: |
| COMPUTE | :white_check_mark: |
| CREATE OBJECT | :x:                |
| DECIDE FOR | :white_check_mark: |
| DECIDE ON | :x:                |
| DEFINE CLASS | :x:                |
| DEFINE DATA | :white_check_mark: |
| DEFINE FUNCTION | partial            |
| DEFINE PRINTER | :white_check_mark: |
| DEFINE PROTOTYPE | :x:                |
| DEFINE SUBROUTINE | :white_check_mark: |
| DEFINE WINDOW | partial            |
| DEFINE WORK FILE | :white_check_mark: |
| DELETE | :x:                |
| DELETE (SQL) | :x:                |
| DISPLAY | :x:                |
| DIVIDE | :white_check_mark: |
| DO/DOEND | :x:                |
| DOWNLOAD PC FILE | :x:                |
| EJECT | :white_check_mark: |
| END | :white_check_mark: |
| END TRANSACTION | :x:                |
| ESCAPE | :white_check_mark: |
| EXAMINE | :white_check_mark: |
| EXPAND | :x:                |
| FETCH | :white_check_mark: |
| FIND | :white_check_mark: |
| FOR | :white_check_mark: |
| FORMAT | partial            |
| GET | :x:                |
| GET SAME | :x:                |
| GET TRANSACTION DATA | :x:                |
| HISTOGRAM | :partial:          |
| IF | :white_check_mark: |
| IF SELECTION | :white_check_mark: |
| IGNORE | :white_check_mark: |
| INCLUDE | :white_check_mark: |
| INPUT | :x:                |
| INSERT (SQL) | :x:                |
| INTERFACE | :x:                |
| LIMIT | :x:                |
| LOOP | :x:                |
| METHOD | :x:                |
| MOVE | :x:                |
| MOVE INDEXED | :x:                |
| MULTIPLY | :white_check_mark: |
| NEWPAGE | :white_check_mark: |
| OBTAIN | :x:                |
| ON ERROR | :x:                |
| OPEN CONVERSATION | :x:                |
| OPTIONS | :x:                |
| PARSE XML | :x:                |
| PASSW | :x:                |
| PERFORM | :white_check_mark: |
| PERFORM BREAK PROCESSING | :x:                |
| PRINT | :x:                |
| PROCESS | :x:                |
| PROCESS COMMAND | :x:                |
| PROCESS PAGE | :x:                |
| PROCESS SQL (SQL) | :x:                |
| PROPERTY | :x:                |
| READ | :x:                |
| READ RESULT SET (SQL) | :x:                |
| READ WORK FILE | :x:                |
| READLOB | :x:                |
| REDEFINE | :white_check_mark: |
| REDUCE | :x:                |
| REINPUT | :x:                |
| REJECT | :x:                |
| RELEASE | :x:                |
| REPEAT | :x:                |
| REQUEST DOCUMENT | :x:                |
| RESET | :white_check_mark: |
| RESIZE | :white_check_mark: |
| RETRY | :x:                |
| ROLLBACK (SQL) | :x:                |
| RUN | :x:                |
| SELECT (SQL) | :white_check_mark: |
| SEND METHOD | :x:                |
| SEPARATE | :x:                |
| SET CONTROL | :x:                |
| SET GLOBALS | :x:                |
| SET KEY | :white_check_mark: |
| SET TIME | :x:                |
| SET WINDOW | :x:                |
| SKIP | :white_check_mark: |
| SORT | :x:                |
| STACK | :white_check_mark: |
| STOP | :x:                |
| STORE | :x:                |
| SUBTRACT | :white_check_mark: |
| SUSPEND IDENTICAL SUPPRESS | :x:                |
| TERMINATE | :x:                |
| UPDATE | :x:                |
| UPDATE (SQL) | :x:                |
| UPDATELOB | :x:                |
| UPLOAD PC FILE | :x:                |
| WRITE | partial            |
| WRITE TITLE | :x:                |
| WRITE TRAILER | :x:                |
| WRITE WORK FILE | :x:                |
