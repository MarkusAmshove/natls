# Implemented statements

This document tracks the implementation status of Natural statements.

Legend:


:x: - not implemented (55)

:white_check_mark: - implemented or reporting (55)

partial - partially implemented to prevent false positives (12)


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
| COMPOSE | :white_check_mark: |
| COMPUTE | :white_check_mark: |
| CREATE OBJECT | :x:                |
| DECIDE FOR | :white_check_mark: |
| DECIDE ON | :white_check_mark: |
| DEFINE CLASS | :x:                |
| DEFINE DATA | :white_check_mark: |
| DEFINE FUNCTION | partial            |
| DEFINE PRINTER | :white_check_mark: |
| DEFINE PROTOTYPE | partial            |
| DEFINE SUBROUTINE | :white_check_mark: |
| DEFINE WINDOW | :white_check_mark: |
| DEFINE WORK FILE | :white_check_mark: |
| DELETE | :x:                |
| DELETE (SQL) | partial            |
| DISPLAY | :x:                |
| DIVIDE | :white_check_mark: |
| DOWNLOAD PC FILE | :white_check_mark: |
| EJECT | :white_check_mark: |
| END | :white_check_mark: |
| END TRANSACTION | :x:                |
| ESCAPE | :white_check_mark: |
| EXAMINE | :white_check_mark: |
| EXPAND | :white_check_mark: |
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
| INPUT | partial          |
| INSERT (SQL) | partial            |
| INTERFACE | :x:                |
| LIMIT | :white_check_mark: |
| METHOD | :x:                |
| MOVE | :x:                |
| MOVE INDEXED | :x:                |
| MULTIPLY | :white_check_mark: |
| NEWPAGE | :white_check_mark: |
| ON ERROR | :white_check_mark: |
| OPEN CONVERSATION | :x:                |
| OPTIONS | :x:                |
| PARSE XML | :x:                |
| PASSW | :x:                |
| PERFORM | :white_check_mark: |
| PERFORM BREAK PROCESSING | :white_check_mark: |
| PRINT | partial            |
| PROCESS | :x:                |
| PROCESS COMMAND | :x:                |
| PROCESS PAGE | :x:                |
| PROCESS SQL (SQL) | partial            |
| PROPERTY | :x:                |
| READ | :x:                |
| READ RESULT SET (SQL) | :x:                |
| READ WORK FILE | :x:                |
| READLOB | :x:                |
| REDUCE | :white_check_mark: |
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
| SELECT (SQL) | partial            |
| SEND METHOD | :x:                |
| SEPARATE | :white_check_mark: |
| SET CONTROL | :x:                |
| SET GLOBALS | :x:                |
| SET KEY | :white_check_mark: |
| SET TIME | :x:                |
| SET WINDOW | :white_check_mark: |
| SKIP | :white_check_mark: |
| SORT | :x:                |
| STACK | :white_check_mark: |
| STOP | :x:                |
| STORE | :x:                |
| SUBTRACT | :white_check_mark: |
| SUSPEND IDENTICAL SUPPRESS | :x:                |
| TERMINATE | :white_check_mark: |
| UPDATE | :x:                |
| UPDATE (SQL) | partial            |
| UPDATELOB | :x:                |
| UPLOAD PC FILE | :x:                |
| WRITE | partial            |
| WRITE TITLE | :x:                |
| WRITE TRAILER | :x:                |
| WRITE WORK FILE | :white_check_mark: |

## Reporting mode only statements

These are not planned to be implemented, because they're reporting mode only.

- `LOOP`
- `DO/DOEND`
- `OBTAIN`
- `REDEFINE`
