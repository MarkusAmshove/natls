package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ExternalParameterCheckShould
{

	@Test
	void reportADiagnosticWhenPassingTooManyParameters()
	{
		parse("CALLED.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CALLED' 'Parameter'
			END
			""");

		assertDiagnostic("Trailing parameter number 1. Module only expects 0 parameter");
	}

	@Test
	void reportADiagnosticWhenMissingAParameter()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER 1 #PARM (A10)
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CALLED'
			END
			""");

		assertDiagnostic("Expected parameter #PARM (A10) not provided");
	}

	@Test
	void notReportADiagnosticWhenMissingAnOptionalParameter()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER 1 #PARM (A10) OPTIONAL
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CALLED'
			END
			""");

		assertNoDiagnostic();
	}

	@Test
	void notReportADiagnosticWhenPassingSkipOperandForOptionalParameter()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER 1 #PARM (A10) OPTIONAL
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CALLED' 1X
			END
			""");

		assertNoDiagnostic();
	}

	@Test
	void notReportADiagnosticWhenPassingSkipOperandForOptionalParameterInBetween()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #PARM (A10) OPTIONAL
			1 #PARM2 (N2) BY VALUE
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CALLED' 1X 10
			END
			""");

		assertNoDiagnostic();
	}

	@Test
	void reportADiagnosticWhenPassingASkipOperandForNonOptionalParameter()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #PARM (A10)
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CALLED' 1X
			END
			""");

		assertDiagnostic("Parameter #PARM (A10) can not be skipped");
	}

	@Test
	void notMistakeRedefineChildrenAsParameter()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #PARM (A10)
			1 REDEFINE #PARM
			2 #P-1 (A5)
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE
			#VAR := 'Hi'
			CALLNAT 'CALLED' #VAR
			END
			""");

		assertNoDiagnostic();
	}

	@Test
	void notCountPassedRedefineChildMember()
	{
		addDataArea("MYPDA.NSA", """
			DEFINE DATA PARAMETER
			1 MYPDA
			2 #PARM-1 (A20)
			2 REDEFINE #PARM-1
			3 #PARM-1-1 (A5)
			END-DEFINE
		""");

		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER USING MYPDA
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA
			LOCAL USING MYPDA
			END-DEFINE
			CALLNAT 'CALLED' MYPDA
			END
			""");

		assertNoDiagnostic();
	}

	@Test
	void notCheckParameterInCopycodes()
	{
		// Copycodes will be analyzed in context of their includer.
		// Analyzing them doesn't make sense, because we can't check parameter
		// types etc.

		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #DEF-ONE-PARAM (A10)
			END-DEFINE
			END
			""");

		parse("COPYC.NSC", """
			CALLNAT 'CALLED' /* Nothing provided */
			""");

		assertNoDiagnostic();
	}

	@Test
	void raiseADiagnosticWhenPassingAParameterByReferenceButTheReceiverTypeIsBigger()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (A15)
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			1 #PASSED (A10)
			END-DEFINE
			CALLNAT 'CALLED' #PASSED
			END
			""");

		assertDiagnostic("Parameter is passed BY REFERENCE but type of parameter (A15) does not fit into passed type (A10)");
	}

	private void assertNoDiagnostic()
	{
		var messages = lastParsedModule.diagnostics().stream().map(IDiagnostic::message).toList();
		assertThat(messages).isEmpty();
	}

	private void assertDiagnostic(String message)
	{
		var messages = lastParsedModule.diagnostics().stream().map(IDiagnostic::message).toList();
		assertThat(messages).contains(message);
	}

	// TODO: Heavily Refactor test setup please, thanks.
	@BeforeEach
	void setup()
	{
		moduleProvider = new ModuleProviderStub();
	}

	private ModuleProviderStub moduleProvider;
	private INaturalModule lastParsedModule; // Conenvience

	private void addDataArea(String name, String source)
	{
		var path = Paths.get(name);
		var file = new NaturalFile(name, path, NaturalFileType.fromPath(path));
		var module = new NaturalModule(file);
		module.setDefineData(new DefineDataParser(moduleProvider).parse(new Lexer().lex(source, path)).result());
		moduleProvider.addModule(IFilesystem.filenameWithoutExtension(path), module);
	}

	private INaturalModule parse(String filename, String source)
	{
		var path = Paths.get(filename);
		var tokens = new Lexer().lex(source, path);
		var file = new NaturalFile(
			IFilesystem.filenameWithoutExtension(path),
			path, NaturalFileType.fromPath(path)
		);
		var module = new NaturalParser(moduleProvider).parse(file, tokens);
		moduleProvider.addModule(IFilesystem.filenameWithoutExtension(path), module);
		lastParsedModule = module;
		return module;
	}
}
