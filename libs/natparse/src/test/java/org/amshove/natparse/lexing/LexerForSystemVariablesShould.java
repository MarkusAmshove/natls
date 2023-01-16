package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LexerForSystemVariablesShould extends AbstractLexerTest
{
	@Test
	void lexTimX()
	{
		assertTokens("*TIMX", token(SyntaxKind.TIMX, "*TIMX"));
	}

	@Test
	void lexSystemVariablesAndFunctionsCaseInsensitive()
	{
		assertTokens("*timX", token(SyntaxKind.TIMX, "*timX"));
	}

	@Test
	void lexDatE()
	{
		assertTokens("*DATE", token(SyntaxKind.DATE, "*DATE"));
	}

	@Test
	void lexDat4E()
	{
		assertTokens("*DAT4E", token(SyntaxKind.DAT4E, "*DAT4E"));
	}

	@Test
	void lexDatX()
	{
		assertTokens("*DATX", token(SyntaxKind.DATX, "*DATX"));
	}

	@Test
	void lexDatN()
	{
		assertTokens("*DATN", token(SyntaxKind.DATN, "*DATN"));
	}

	@Test
	void lexLanguage()
	{
		assertTokens("*LANGUAGE", token(SyntaxKind.LANGUAGE, "*LANGUAGE"));
	}

	@Test
	void lexProgram()
	{
		assertTokens("*PROGRAM", token(SyntaxKind.PROGRAM, "*PROGRAM"));
	}

	@Test
	void lexUser()
	{
		assertTokens("*USER", token(SyntaxKind.SV_USER, "*USER"));
	}

	@Test
	void lexLibraryId()
	{
		assertTokens("*LIBRARY-ID", token(SyntaxKind.LIBRARY_ID, "*LIBRARY-ID"));
	}

	@Test
	void lexLineX()
	{
		assertTokens("*LINEX", token(SyntaxKind.LINEX, "*LINEX"));
	}

	@Test
	void lexCurrentUnit()
	{
		assertTokens("*CURRENT-UNIT", token(SyntaxKind.CURRENT_UNIT, "*CURRENT-UNIT"));
	}

	@Test
	void lexOcc()
	{
		assertTokens("*OCC", token(SyntaxKind.OCC, "*OCC"));
	}

	@Test
	void lexOccurence()
	{
		assertTokens("*OCCURRENCE", token(SyntaxKind.OCCURRENCE, "*OCCURRENCE"));
	}

	@Test
	void lexErrorNr()
	{
		assertTokens("*ERROR-NR", token(SyntaxKind.ERROR_NR, "*ERROR-NR"));
	}

	@Test
	void lexErrorNoNr()
	{
		assertTokens("*ERROR", token(SyntaxKind.SV_ERROR, "*ERROR"));
	}

	@Test
	void lexErrorLine()
	{
		assertTokens("*ERROR-LINE", token(SyntaxKind.ERROR_LINE, "*ERROR-LINE"));
	}

	@Test
	void lexErrorTa()
	{
		assertTokens("*ERROR-TA", token(SyntaxKind.ERROR_TA, "*ERROR-TA"));
	}

	@Test
	void lexInitUser()
	{
		assertTokens("*INIT-USER", token(SyntaxKind.INIT_USER, "*INIT-USER"));
	}

	@Test
	void lexCounter()
	{
		assertTokens("*COUNTER", token(SyntaxKind.COUNTER, "*COUNTER"));
	}

	@Test
	void lexLine()
	{
		assertTokens("*LINE", token(SyntaxKind.LINE, "*LINE"));
	}

	@Test
	void lexTrim()
	{
		assertTokens("*TRIM", token(SyntaxKind.TRIM, "*TRIM"));
	}

	@Test
	void lexMinval()
	{
		assertTokens("*MINVAL", token(SyntaxKind.MINVAL, "*MINVAL"));
	}

	@Test
	void lexMaxval()
	{
		assertTokens("*MAXVAL", token(SyntaxKind.MAXVAL, "*MAXVAL"));
	}

	@Test
	void lexCursLine()
	{
		assertTokens("*CURS-LINE", token(SyntaxKind.CURS_LINE, "*CURS-LINE"));
	}

	@Test
	void lexCursCol()
	{
		assertTokens("*CURS-COL", token(SyntaxKind.CURS_COL, "*CURS-COL"));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"PF1", "PF2", "PF3", "PF9", "PF12", "PF15"
	})
	void lexPfKey(String pfKey)
	{
		assertTokens(pfKey, token(SyntaxKind.PF, pfKey));
	}

	@Test
	void lexPfKey()
	{
		assertTokens("*PF-KEY", token(SyntaxKind.PF_KEY, "*PF-KEY"));
	}

	@Test
	void lexDevice()
	{
		assertTokens("*DEVICE", token(SyntaxKind.DEVICE, "*DEVICE"));
	}

	@Test
	void lexDatD()
	{
		assertTokens("*DATD", token(SyntaxKind.DATD, "*DATD"));
	}

	@Test
	void lexTimN()
	{
		assertTokens("*TIMN", token(SyntaxKind.TIMN, "*TIMN"));
	}

	@Test
	void lexTimeOut()
	{
		assertTokens("*TIME-OUT", token(SyntaxKind.TIME_OUT, "*TIME-OUT"));
	}

	@Test
	void lexOpSys()
	{
		assertTokens("*OPSYS", token(SyntaxKind.OPSYS, "*OPSYS"));
	}

	@Test
	void lexTpSys()
	{
		assertTokens("*TPSYS", token(SyntaxKind.TPSYS, "*TPSYS"));
	}

	@Test
	void lexApplicId()
	{
		assertTokens("*APPLIC-ID", token(SyntaxKind.APPLIC_ID, "*APPLIC-ID"));
	}

	@Test
	void lexStartup()
	{
		assertTokens("*STARTUP", token(SyntaxKind.STARTUP, "*STARTUP"));
	}

	@Test
	void lexSteplib()
	{
		assertTokens("*STEPLIB", token(SyntaxKind.STEPLIB, "*STEPLIB"));
	}

	@Test
	void lexPageNumber()
	{
		assertTokens("*PAGE-NUMBER", token(SyntaxKind.PAGE_NUMBER, "*PAGE-NUMBER"));
	}

	@Test
	void lexWindowPs()
	{
		assertTokens("*WINDOW-PS", token(SyntaxKind.WINDOW_PS, "*WINDOW-PS"));
	}

	@Test
	void lexInitId()
	{
		assertTokens("*INIT-ID", token(SyntaxKind.INIT_ID, "*INIT-ID"));
	}

	@Test
	void lexCom()
	{
		assertTokens("*COM", token(SyntaxKind.COM, "*COM"));
	}

	@Test
	void lexDat4D()
	{
		assertTokens("*DAT4D", token(SyntaxKind.DAT4D, "*DAT4D"));
	}

	@Test
	void lexCursField()
	{
		assertTokens("*CURS-FIELD", token(SyntaxKind.CURS_FIELD, "*CURS-FIELD"));
	}

	@Test
	void lexTimestmp()
	{
		assertTokens("*TIMESTMP", token(SyntaxKind.TIMESTMP, "*TIMESTMP"));
	}

	@Test
	void lexData()
	{
		assertTokens("*DATA", token(SyntaxKind.SV_DATA, "*DATA"));
	}

	@Test
	void lexLevel()
	{
		assertTokens("*LEVEL", token(SyntaxKind.SV_LEVEL, "*LEVEL"));
	}

	@Test
	void lexNumber()
	{
		assertTokens("*NUMBER", token(SyntaxKind.SV_NUMBER, "*NUMBER"));
	}

	@Test
	void lexTranslate()
	{
		assertTokens("*TRANSLATE", token(SyntaxKind.TRANSLATE, "*TRANSLATE"));
	}

	@Test
	void lexIsn()
	{
		assertTokens("*ISN", token(SyntaxKind.SV_ISN, "*ISN"));
	}

	@Test
	void lexDatG()
	{
		assertTokens("*DATG", token(SyntaxKind.DATG, "*DATG"));
	}

	@Test
	void lexLineCount()
	{
		assertTokens("*LINE-COUNT", token(SyntaxKind.LINE_COUNT, "*LINE-COUNT"));
	}

	@Test
	void lexLineSize()
	{
		assertTokens("*LINESIZE", token(SyntaxKind.LINESIZE, "*LINESIZE"));
	}

	@Test
	void lexMachineClass()
	{
		assertTokens("*MACHINE-CLASS", token(SyntaxKind.MACHINE_CLASS, "*MACHINE-CLASS"));
	}

	@Test
	void lexPageSize()
	{
		assertTokens("*PAGESIZE", token(SyntaxKind.PAGESIZE, "*PAGESIZE"));
	}

	@Test
	void lexDat4I()
	{
		assertTokens("*DAT4I", token(SyntaxKind.DAT4I, "*DAT4I"));
	}

	@Test
	void lexDatI()
	{
		assertTokens("*DATI", token(SyntaxKind.DATI, "*DATI"));
	}

	@Test
	void lexTimD()
	{
		assertTokens("*TIMD", token(SyntaxKind.TIMD, "*TIMD"));
	}

	@Test
	void lexCpuTime()
	{
		assertTokens("*CPU-TIME", token(SyntaxKind.CPU_TIME, "*CPU-TIME"));
	}

	@Test
	void lexEtid()
	{
		assertTokens("*ETID", token(SyntaxKind.ETID, "*ETID"));
	}

	@Test
	void lexInitProgram()
	{
		assertTokens("*INIT-PROGRAM", token(SyntaxKind.INIT_PROGRAM, "*INIT-PROGRAM"));
	}

	@Test
	void lexLbound()
	{
		assertTokens("*LBOUND", token(SyntaxKind.LBOUND, "*LBOUND"));
	}

	@Test
	void lexUbound()
	{
		assertTokens("*UBOUND", token(SyntaxKind.UBOUND, "*UBOUND"));
	}

	@Test
	void lexServerType()
	{
		assertTokens("*SERVER-TYPE", token(SyntaxKind.SERVER_TYPE, "*SERVER-TYPE"));
	}

	@Test
	void lexDat4J()
	{
		assertTokens("*DAT4J", token(SyntaxKind.DAT4J, "*DAT4J"));
	}

	@Test
	void lexDatJ()
	{
		assertTokens("*DATJ", token(SyntaxKind.DATJ, "*DATJ"));
	}

	@Test
	void lexDat4U()
	{
		assertTokens("*DAT4U", token(SyntaxKind.DAT4U, "*DAT4U"));
	}

	@Test
	void lexDatU()
	{
		assertTokens("*DATU", token(SyntaxKind.DATU, "*DATU"));
	}

	@Test
	void lexDatVS()
	{
		assertTokens("*DATVS", token(SyntaxKind.DATVS, "*DATVS"));
	}

	@Test
	void lexDatV()
	{
		assertTokens("*DATV", token(SyntaxKind.DATV, "*DATV"));
	}

}
