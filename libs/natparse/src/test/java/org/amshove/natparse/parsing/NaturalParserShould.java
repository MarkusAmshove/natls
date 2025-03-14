package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IFunction;
import org.amshove.natparse.natural.ITypedVariableNode;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("DataFlowIssue")
class NaturalParserShould extends ParserIntegrationTest
{
	@Test
	void reportDiagnosticsForUnresolvedCopyCodeVariables(@ProjectName("copycodetests") NaturalProject project)
	{
		var result = parse(project.findModule("LIBONE", "SUBPROG3"));
		assertThat(result.diagnostics()).hasSize(1);
		assertThat(result.diagnostics().first().id()).isEqualTo(ParserError.UNRESOLVED_REFERENCE.id());
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
		assertThat(defineData.declaredParameterInOrder()).hasSize(1);
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
	void addTheFunctionAsVariableToItsDefineData(@ProjectName("naturalParserTests") NaturalProject project)
	{
		var module = parse(project.findModule("TEST", "FUNCSET"));
		assertThat(module).isInstanceOf(IFunction.class);
		var function = (IFunction) module;
		assertThat(function.returnType().format()).isEqualTo(DataFormat.NUMERIC);
		assertThat(function.returnType().length()).isEqualTo(12.7);
		var variable = (ITypedVariableNode) function.defineData().findVariable("FUNCSET");
		assertThat(variable).as("Function name as variable not found").isNotNull();
		assertThat(variable.type().format()).isEqualTo(DataFormat.NUMERIC);
		assertThat(variable.type().length()).isEqualTo(12.7);
	}

	@Test
	void parseTheFunctionReturnDimensions(@ProjectName("naturalParserTests") NaturalProject project)
	{
		var module = parse(project.findModule("TEST", "FUNC1DIM"));
		assertThat(module).isInstanceOf(IFunction.class);
		var function = (IFunction) module;
		var variable = (ITypedVariableNode) function.defineData().findVariable("FUNC1DIM");
		assertThat(variable).as("Function name as variable not found").isNotNull();
		assertThat(variable.dimensions()).hasSize(1);
	}

	@Test
	void parseTheFunctionReturnDimensionsForMultipleDimensions(@ProjectName("naturalParserTests") NaturalProject project)
	{
		var module = parse(project.findModule("TEST", "FUNC2DIM"));
		assertThat(module).isInstanceOf(IFunction.class);
		var function = (IFunction) module;
		var variable = (ITypedVariableNode) function.defineData().findVariable("FUNC2DIM");
		assertThat(variable).as("Function name as variable not found").isNotNull();
		assertThat(variable.dimensions()).hasSize(2);
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
