package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IFunction;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NaturalParserShould extends ParserIntegrationTest
{
	@Test
	void notReportDiagnosticsForUnresolvedCopyCodeVariables(@ProjectName("copycodetests") NaturalProject project)
	{
		assertParsesWithoutAnyDiagnostics(project.findModule("LIBONE", "SUBPROG3"));
	}

	@Test
	void notReportDiagnosticsForReferencesToTheFunctionName(@ProjectName("variablereferencetests") NaturalProject project)
	{
		assertParsesWithoutAnyDiagnostics(project.findModule("LIBONE", "FUNC"));
	}

	@Test
	void parseTheReturnTypesOfFunctions(@ProjectName("naturalParserTests") NaturalProject project)
	{
		var module = parse(project.findModule("TEST", "FUNC"));
		assertThat(module).isInstanceOf(IFunction.class);
		var function = (IFunction) module;
		assertThat(function.returnType().format()).isEqualTo(DataFormat.LOGIC);
	}

	@Test
	void parseTheReturnTypesOfFunctionsWithDynamicLength(@ProjectName("naturalParserTests") NaturalProject project)
	{
		var module = parse(project.findModule("TEST", "FUNCDYN"));
		assertThat(module).isInstanceOf(IFunction.class);
		var function = (IFunction) module;
		assertThat(function.returnType().format()).isEqualTo(DataFormat.ALPHANUMERIC);
		assertThat(function.returnType().hasDynamicLength()).isTrue();
	}

	@Test
	void stillParseTheDefineDataWhenParsingTheFunctionReturnType(@ProjectName("naturalParserTests") NaturalProject project)
	{
		var module = parse(project.findModule("TEST", "FUNC2"));
		assertThat(module).isInstanceOf(IFunction.class);
		var function = (IFunction) module;
		assertThat(function.returnType().format()).isEqualTo(DataFormat.ALPHANUMERIC);
		assertThat(function.returnType().hasDynamicLength()).isTrue();

		var defineData = ((IFunction) module).defineData();
		assertThat(defineData).isNotNull();
		assertThat(defineData.parameterInOrder()).hasSize(1);
		assertThat(defineData.findVariable("#VAR1")).isNotNull();
	}

	@Test
	void parseTheReturnTypesOfFunctionsWithFixedLength(@ProjectName("naturalParserTests") NaturalProject project)
	{
		var module = parse(project.findModule("TEST", "FUNCSET"));
		assertThat(module).isInstanceOf(IFunction.class);
		var function = (IFunction) module;
		assertThat(function.returnType().format()).isEqualTo(DataFormat.NUMERIC);
		assertThat(function.returnType().length()).isEqualTo(12.7);
	}

	@Test
	void reportADiagnosticsForUnreferencedVariablesInFunctions(@ProjectName("variablereferencetests") NaturalProject project)
	{
		var module = parse(project.findModule("LIBONE", "FUNC2"));
		assertThat(module.diagnostics()).anyMatch(d -> d.id().equals("NPP016") && d.message().equals("Unresolved reference: FUNC"));
	}

	@Test
	void raiseADiagnosticForModulesHavingReportingMode(@ProjectName("reporting") NaturalProject project)
	{
		var module = parse(project.findModule("LIBONE", "REPORT"));
		assertThat(module.diagnostics()).anyMatch(d -> d.id().equals(ParserError.UNSUPPORTED_PROGRAMMING_MODE.id()));
	}
}
