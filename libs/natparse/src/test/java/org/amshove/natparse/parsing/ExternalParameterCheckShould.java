package org.amshove.natparse.parsing;

import org.amshove.natparse.AdditionalDiagnosticInfo;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

		assertDiagnostic(
			"Parameter type mismatch. Expected (A15) by reference but got (A10)",
			"Passed variable is declared here",
			"Received parameter is declared here"
		);
	}

	@Test
	void includeTheDeclarationAndReferencePositionsInAdditionalInfosWhenPassingAGroupToATypeMismatch()
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
			1 #GRP
				2 #PASSED (A10)
			END-DEFINE
			CALLNAT 'CALLED' #GRP
			END
			""");

		assertDiagnostic(
			"Parameter type mismatch. Expected (A15) by reference but got (A10)",
			"Passed variable is declared here",
			"Received parameter is declared here"
		);
	}

	@Test
	void useTheUntrimmedLengthOfPassedStringLiteralsForLengthChecks()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (A8) BY VALUE
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CALLED' 'A       '
			END
			""");

		assertNoDiagnostic();
	}

	@Test
	void notAllowToPassALiteralToAByReferenceParameterIfThePassedValueIsTooSmall()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (A6) /* This is not BY VALUE
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CALLED' 'Hello' /* Length doesn't match
			END
			""");

		assertDiagnostic(
			"Parameter type mismatch. Expected (A6) by reference but got (A5)"
		);
	}

	@Test
	void notAllowToPassALiteralToAByReferenceParameterIfThePassedValueIsTooBig()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (A6) /* This is not BY VALUE
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CALLED' 'Hello World' /* Length doesn't match
			END
			""");

		assertDiagnostic(
			"Parameter type mismatch. Expected (A6) by reference but got (A11)"
		);
	}

	@Test
	void allowToPassALiteralToAByReferenceParameterIfTheTypesMatch()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (A5) /* This is not BY VALUE
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'CALLED' 'Hello' /* Length matches the receiver, all good
			END
			""");

		assertNoDiagnostic();
	}

	@Test
	void allowToPassAVariableToAByReferenceParameterWhenTheTypesAreCompatible()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (A5)
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			1 #PASSER (A5)
			END-DEFINE
			CALLNAT 'CALLED' #PASSER
			END
			""");

		assertNoDiagnostic();
	}

	@ParameterizedTest
	@CsvSource(
		{
			"A5,A10",
			"A10,A5"
		}
	)
	void notAllowToPassAVariableToAByReferenceParameterWhenTheTypesAreIncompatible(String receiverType, String passerType)
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (%s)
			END-DEFINE
			END
			""".formatted(receiverType));

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			1 #PASSER (%s)
			END-DEFINE
			CALLNAT 'CALLED' #PASSER
			END
			""".formatted(passerType));

		assertDiagnostic(
			"Parameter type mismatch. Expected (%s) by reference but got (%s)"
				.formatted(receiverType, passerType)
		);
	}

	@Test
	void allowToPassBiggerTypesIfPassedByValue()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (A5) BY VALUE
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			1 #PASSER (A10)
			END-DEFINE
			CALLNAT 'CALLED' #PASSER
			END
			""");

		assertNoDiagnostic();
	}

	@Test
	void allowToPassSmallerTypesIfPassedByValue()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (A5) BY VALUE
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			1 #PASSER (A2)
			END-DEFINE
			CALLNAT 'CALLED' #PASSER
			END
			""");

		assertNoDiagnostic();
	}

	@Test
	void allowPassingArraysWithSameLength()
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (A5/1:10)
			END-DEFINE
			END
			""");

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			1 #PASSER (A5/1:10)
			END-DEFINE
			CALLNAT 'CALLED' #PASSER(*)
			END
			""");

		assertNoDiagnostic();
	}

	@ParameterizedTest
	@CsvSource(
		{
			"A10/1:10,A10/1:15",
			"A10/1:15,A10/1:10"
		}
	)
	void notAllowDifferentArrayLengths(String received, String passed)
	{
		parse("CALLED.NSN", """
			DEFINE DATA
			PARAMETER
			1 #RECEIVER (%s)
			END-DEFINE
			END
			""".formatted(received));

		parse("CALLER.NSN", """
			DEFINE DATA LOCAL
			1 #PASSER (%s)
			END-DEFINE
			CALLNAT 'CALLED' #PASSER(*)
			END
			""".formatted(passed));

		assertDiagnostic(
			"Parameter array length mismatch. Expected (%s) but got (%s)".formatted(received, passed)
		);
	}

	private void assertNoDiagnostic()
	{
		var messages = lastParsedModule.diagnostics().stream().map(IDiagnostic::message).toList();
		assertThat(messages).isEmpty();
	}

	private void assertDiagnostic(String message, String... additionalInfos)
	{
		var messages = lastParsedModule.diagnostics().stream().map(IDiagnostic::message).toList();
		assertThat(messages).contains(message);
		if (additionalInfos.length > 0)
		{
			var diagnostic = lastParsedModule.diagnostics().stream().filter(d -> d.message().equals(message)).findAny().orElseThrow();
			var gottenAdditionalInfos = diagnostic.additionalInfo().stream().map(AdditionalDiagnosticInfo::message).toList();
			for (var additionalInfo : additionalInfos)
			{
				assertThat(gottenAdditionalInfos).contains(additionalInfo);
			}

			assertThat(gottenAdditionalInfos)
				.as("Not all additional infos have been asserted")
				.hasSize(additionalInfos.length);
		}
	}

	// TODO: Heavily Refactor test setup please, thanks.
	@BeforeEach
	void setup()
	{
		moduleProvider = new ModuleProviderStub();
	}

	private ModuleProviderStub moduleProvider;
	private INaturalModule lastParsedModule; // Convenience

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
