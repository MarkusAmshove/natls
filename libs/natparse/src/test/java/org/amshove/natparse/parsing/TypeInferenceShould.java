package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.testhelpers.ResourceHelper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TypeInferenceShould
{

	@ParameterizedTest
	@CsvSource(
		{
			"*OCC(#ARR),(I4)",
			"*ISN,(P10)",
			"*LENGTH(#STR),(I4)",
			"*TIMX,(T)",
			"*TIMESTMP,(B8)",
			"*TIMN,(N7)",
			"*DAT4J,(A7)",
			"*NET-USER,(A253)"
		}
	)
	void inferTheTypeOfSystemFunctions(String rhs, String expectedType)
	{
		assertInferredType(inferRhs(rhs), expectedType);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"\"Hello\",(A5)",
			"5,(I1)"
		}
	)
	void inferTheTypeOfLiterals(String rhs, String expectedType)
	{
		assertInferredType(inferRhs(rhs), expectedType);
	}

	@Test
	void inferTheTypeOfAnArithmeticExpressionWithLiterals()
	{
		assertInferredType(inferRhs("10 + 10"), "(I1)");
	}

	@Test
	void inferTheTypeOfAnArithmeticExpressionWithMoreMath()
	{
		assertInferredType(inferRhs("10 + 10 * 5000 / 240001"), "(I4)");
	}

	@Test
	void inferTheTypeOfAnArithmeticExpressionWithSystemVarsMixedIn()
	{
		assertInferredType(inferRhs("*OCC(#ARR) + 1"), "(I4)");
	}

	@TestFactory
	Stream<DynamicContainer> testInferenceFiles()
	{
		var files = ResourceHelper.findRelativeResourceFiles("typeinference", getClass());
		var provider = createModuleProvider();
		return files.stream().map(f ->
		{
			var path = Paths.get(f);
			var containerName = path.getFileName().toString();
			var testsInFile = new ArrayList<DynamicTest>();

			var content = ResourceHelper.readResourceFile(f, getClass());

			var lexer = new Lexer();
			var tokens = lexer.lex(content, path);
			if (tokens.diagnostics().hasItems())
			{
				throw new RuntimeException("Expected the source to lex without diagnostics");
			}
			var parser = new NaturalParser(provider);
			var module = parser.parse(new NaturalFile(containerName, path, NaturalFileType.SUBPROGRAM), tokens);

			var lines = content.split("\\r?\\n");
			var lineNo = -1;
			for (var line : lines)
			{
				lineNo++;
				if (!line.contains("TYPE:"))
				{
					continue;
				}

				var expectedType = line.substring(line.indexOf("TYPE:") + "TYPE:".length()).trim();
				var statement = NodeUtil.findStatementInLine(path, lineNo, ((IModuleWithBody) module).body()).orElseThrow();
				testsInFile.add(dynamicTest("Line %d: %s".formatted(lineNo, expectedType), () ->
				{
					if (!(statement instanceof IAssignmentStatementNode assign))
					{
						throw new RuntimeException("Tested statement must be an assignment");
					}

					var inferedType = TypeInference.inferType(assign.operand()).orElseThrow();
					assertInferredType(inferedType, expectedType);
				}));
			}

			if (testsInFile.isEmpty())
			{
				throw new RuntimeException("No tests found in %s".formatted(f));
			}
			return dynamicContainer(containerName, testsInFile);
		});
	}

	private IModuleProvider createModuleProvider()
	{
		var provider = mock(IModuleProvider.class);
		var lReturnModule = new Function(new NaturalFile("LRETURN", Path.of("LRETURN.NS7"), NaturalFileType.FUNCTION));
		lReturnModule.setReturnType(new DataType(DataFormat.LOGIC, 0));
		when(provider.findNaturalModule("LRETURN", NaturalFileType.FUNCTION)).thenReturn(lReturnModule);
		var iReturnModule = new Function(new NaturalFile("IRETURN", Path.of("IRETURN.NS7"), NaturalFileType.FUNCTION));
		iReturnModule.setReturnType(new DataType(DataFormat.INTEGER, 4));
		when(provider.findNaturalModule("IRETURN", NaturalFileType.FUNCTION)).thenReturn(iReturnModule);
		return provider;
	}

	private void assertInferredType(IDataType type, String expectedType)
	{
		assertThat(type.toShortString()).isEqualTo(expectedType);
	}

	private IDataType inferRhs(String rhs)
	{
		var source = "#TARGET := %s".formatted(rhs);
		var lexer = new Lexer();
		var tokens = lexer.lex(source, Path.of("SUB.NSN"));
		assertThat(tokens.diagnostics())
			.as("Expected the source to lex without diagnostics but got: %s".formatted(tokens.diagnostics().stream().map(IDiagnostic::message).collect(Collectors.joining(", "))))
			.isEmpty();
		var parseResult = new StatementListParser(new ModuleProviderStub()).parse(tokens);
		assertThat(parseResult.diagnostics())
			.as("Expected the source to parse without diagnostics but got: %s".formatted(parseResult.diagnostics().stream().map(IDiagnostic::message).collect(Collectors.joining(", "))))
			.isEmpty();

		var statement = (IAssignmentStatementNode) parseResult.result().statements().first();
		return TypeInference.inferType(statement.operand()).orElseThrow();
	}
}
