# Implemented statements

This document tracks the implementation status of Natural statements.

Legend:


:x: - not implemented (22)

:white_check_mark: - implemented or reporting (95)

| Statement | Status               |
| --- |----------------------|
| ACCEPT/REJECT | :white_check_mark:   |
| ADD | :white_check_mark:   |
| ASSIGN | :white_check_mark:   |
| AT BREAK | :white_check_mark:   |
| AT END OF DATA | :white_check_mark:   |
| AT END OF PAGE | :white_check_mark:   |
| AT START OF DATA | :white_check_mark:   |
| AT TOP OF PAGE | :white_check_mark:   |
| BACKOUT TRANSACTION | :white_check_mark:   |
| BEFORE BREAK PROCESSING | :white_check_mark:   |
| CALL | :white_check_mark:   |
| CALL FILE | :white_check_mark:   |
| CALL LOOP | :white_check_mark:   |
| CALLDBPROC (SQL) | :x:                  |
| CALLNAT | :white_check_mark:   |
| CLOSE CONVERSATION | :white_check_mark:   |
| CLOSE PC FILE | :white_check_mark:   |
| CLOSE PRINTER | :white_check_mark:   |
| CLOSE WORK FILE | :white_check_mark:   |
| COMMIT (SQL) | :white_check_mark:   |
| COMPRESS | :white_check_mark:   |
| COMPOSE | :white_check_mark:   |
| COMPUTE | :white_check_mark:   |
| CREATE OBJECT | :x:                  |
| DECIDE FOR | :white_check_mark:   |
| DECIDE ON | :white_check_mark:   |
| DEFINE CLASS | :x:                  |
| DEFINE DATA | :white_check_mark:   |
| DEFINE FUNCTION | :white_check_mark:   |
| DEFINE PRINTER | :white_check_mark:   |
| DEFINE PROTOTYPE | :white_check_mark:   |
| DEFINE SUBROUTINE | :white_check_mark:   |
| DEFINE WINDOW | :white_check_mark:   |
| DEFINE WORK FILE | :white_check_mark:   |
| DELETE | :white_check_mark:   |
| DELETE (SQL) | :white_check_mark:   |
| DISPLAY | :white_check_mark:   |
| DIVIDE | :white_check_mark:   |
| DOWNLOAD PC FILE | :white_check_mark:   |
| EJECT | :white_check_mark:   |
| END | :white_check_mark:   |
| END TRANSACTION | :white_check_mark:   |
| ESCAPE | :white_check_mark:   |
| EXAMINE | :white_check_mark:   |
| EXPAND | :white_check_mark:   |
| FETCH | :white_check_mark:   |
| FIND | :white_check_mark:   |
| FOR | :white_check_mark:   |
| FORMAT | :white_check_mark:   |
| GET | :white_check_mark:   |
| GET SAME | :white_check_mark:   |
| GET TRANSACTION DATA | :white_check_mark:   |
| HISTOGRAM | ::white_check_mark:: |
| IF | :white_check_mark:   |
| IF SELECTION | :white_check_mark:   |
| IGNORE | :white_check_mark:   |
| INCLUDE | :white_check_mark:   |
| INPUT | :white_check_mark:   |
| INSERT (SQL) | :white_check_mark:   |
| INTERFACE | :x:                  |
| LIMIT | :white_check_mark:   |
| METHOD | :x:                  |
| MOVE | :white_check_mark:   |
| MULTIPLY | :white_check_mark:   |
| NEWPAGE | :white_check_mark:   |
| ON ERROR | :white_check_mark:   |
| OPEN CONVERSATION | :white_check_mark:   |
| OPTIONS | :white_check_mark:   |
| PARSE XML | :white_check_mark:   |
| PARSE JSON | :white_check_mark:   |
| PASSW | :white_check_mark:   |
| PERFORM | :white_check_mark:   |
| PERFORM BREAK PROCESSING | :white_check_mark:   |
| PRINT | :white_check_mark:   |
| PROCESS | :x:                  |
| PROCESS COMMAND | :x:                  |
| PROCESS PAGE | :x:                  |
| PROCESS SQL (SQL) | :white_check_mark:   |
| PROPERTY | :x:                  |
| READ | :white_check_mark:   |
| READ RESULT SET (SQL) | :x:                  |
| READ WORK FILE | :white_check_mark:   |
| READLOB | :x:                  |
| REDUCE | :white_check_mark:   |
| REINPUT | :x:                  |
| RELEASE | :white_check_mark:   |
| REPEAT | :white_check_mark:   |
| REQUEST DOCUMENT | :x:                  |
| RESET | :white_check_mark:   |
| RESIZE | :white_check_mark:   |
| RETRY | :x:                  |
| ROLLBACK (SQL) | :white_check_mark:   |
| RUN | :white_check_mark:   |
| SELECT (SQL) | :white_check_mark:   |
| SEND METHOD | :x:                  |
| SEPARATE | :white_check_mark:   |
| SET CONTROL | :x:                  |
| SET GLOBALS | :x:                  |
| SET KEY | :white_check_mark:   |
| SET TIME | :white_check_mark:   |
| SET WINDOW | :white_check_mark:   |
| SKIP | :white_check_mark:   |
| SORT | :x:                  |
| STACK | :white_check_mark:   |
| STOP | :white_check_mark:   |
| STORE | :x:                  |
| SUBTRACT | :white_check_mark:   |
| SUSPEND IDENTICAL SUPPRESS | :x:                  |
| TERMINATE | :white_check_mark:   |
| UPDATE | :white_check_mark:   |
| UPDATE (SQL) | :white_check_mark:   |
| UPDATELOB | :x:                  |
| UPLOAD PC FILE | :x:                  |
| WRITE | white_check_mark     |
| WRITE TITLE | :white_check_mark:   |
| WRITE TRAILER | :white_check_mark:   |
| WRITE WORK FILE | :white_check_mark:   |

## Reporting mode only statements

These are not planned to be implemented, because they're reporting mode only.

- `DO/DOEND`
- `LOOP`
- `MOVE INDEXED`
- `OBTAIN`
- `REDEFINE`
