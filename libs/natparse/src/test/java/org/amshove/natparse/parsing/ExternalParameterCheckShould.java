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

		assertDiagnostic("Trailing parameter. Module only expects 0 parameter");
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
			1 #PARM2 (N2)
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
