package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.ITokenNode;
import org.amshove.natparse.natural.IUsingNode;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class DefineDataParserShould extends AbstractParserTest
{

	@Test
	void returnADiagnosticWhenNoDefineDataIsFound()
	{
		assertDiagnostic("/* DEFINE DATA", ParserError.NO_DEFINE_DATA_FOUND);
	}

	@Test
	void returnADiagnosticWhenEndDefineIsNotFound()
	{
		assertDiagnostic("DEFINE DATA\nLOCAL USING SOMELDA\n", ParserError.MISSING_END_DEFINE);
	}

	@Test
	void setTheCorrectStartAndEndNodes()
	{
		var source = """
			   DEFINE DATA
			   LOCAL USING SOMELDA
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		assertThat(defineData.line()).isEqualTo(0);
		var firstTokenNode = assertNodeType(defineData.nodes().first(), ITokenNode.class);
		assertThat(firstTokenNode.token().kind()).isEqualTo(SyntaxKind.DEFINE);
		assertThat(defineData.nodes().last().line()).isEqualTo(2);
		var lastTokenNode = assertNodeType(defineData.nodes().last(), ITokenNode.class);
		assertThat(lastTokenNode.token().kind()).isEqualTo(SyntaxKind.END_DEFINE);
	}

	@Test
	void parseASimpleLocalImport()
	{
		var source = """
			   DEFINE DATA
			   LOCAL USING SOMELDA
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		assertThat(defineData.nodes()).isNotNull();
		assertThat(defineData.localUsings().size()).isEqualTo(1);
		assertThat(defineData.localUsings().first().target().source()).isEqualTo("SOMELDA");
	}

	@Test
	void setTheCorrectParentForNodes()
	{
		var source = """
			   DEFINE DATA
			   LOCAL USING SOMELDA
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		assertThat(defineData.localUsings().first().parent()).isEqualTo(defineData);
	}

	@Test
	void parseAParameterUsing()
	{
		var source = """
			   DEFINE DATA
			   PARAMETER USING SOMEPDA
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var parameterUsing = defineData.parameterUsings().first();
		assertThat(parameterUsing.parent()).isEqualTo(defineData);
		assertThat(parameterUsing.isParameterUsing()).isTrue();
		assertThat(parameterUsing.target().source()).isEqualTo("SOMEPDA");
	}

	@Test
	void parseAGlobalUsing()
	{
		var source = """
			   DEFINE DATA
			   GLOBAL USING SOMEGDA
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var parameterUsing = defineData.globalUsings().first();
		assertThat(parameterUsing.parent()).isEqualTo(defineData);
		assertThat(parameterUsing.isGlobalUsing()).isTrue();
		assertThat(parameterUsing.target().source()).isEqualTo("SOMEGDA");
	}

	@Test
	void mixDifferentUsings()
	{
		var source = """
			   DEFINE DATA
			   GLOBAL USING SOMEGDA
			   LOCAL USING SOMELDA
			   PARAMETER USING SOMEPDA
			   LOCAL USING ALDA
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var usings = defineData.usings();
		assertThat(usings.size()).isEqualTo(4);

		assertAll(
			() -> assertThat(usings.get(0).isGlobalUsing()).isTrue(),
			() -> assertThat(usings.get(0).target().source()).isEqualTo("SOMEGDA"),
			() -> assertThat(usings.get(1).isLocalUsing()).isTrue(),
			() -> assertThat(usings.get(1).target().source()).isEqualTo("SOMELDA"),
			() -> assertThat(usings.get(2).isParameterUsing()).isTrue(),
			() -> assertThat(usings.get(2).target().source()).isEqualTo("SOMEPDA"),
			() -> assertThat(usings.get(3).isLocalUsing()).isTrue(),
			() -> assertThat(usings.get(3).target().source()).isEqualTo("ALDA")
		);
	}

	@Test
	void setTheCorrectChildNodes()
	{
		var source = """
			   DEFINE DATA
			   GLOBAL USING SOMEGDA
			   LOCAL USING SOMELDA
			   PARAMETER USING SOMEPDA
			   LOCAL USING ALDA
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		assertAll(
			() -> assertTokenNode(defineData.nodes().get(0), n -> n.token().kind())
				.isEqualTo(SyntaxKind.DEFINE),
			() -> assertTokenNode(defineData.nodes().get(1), n -> n.token().kind())
				.isEqualTo(SyntaxKind.DATA),
			() -> assertNodeType(defineData.nodes().get(2), IUsingNode.class),
			() -> assertNodeType(defineData.nodes().get(3), IUsingNode.class),
			() -> assertNodeType(defineData.nodes().get(4), IUsingNode.class),
			() -> assertNodeType(defineData.nodes().get(5), IUsingNode.class),
			() -> assertTokenNode(defineData.nodes().get(6), n -> n.token().kind())
				.isEqualTo(SyntaxKind.END_DEFINE)
		);
	}

	@Test
	void parseALocalVariable()
	{
		var source = """
			define data
			local
			1 #MYVAR (A10)
			end-define
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var variable = defineData.variables().first();
		assertThat(variable.name()).isEqualTo("#MYVAR");
		assertThat(variable.level()).isEqualTo(1);
		assertThat(variable.dataFormat()).isEqualTo(DataFormat.ALPHANUMERIC);
		assertThat(variable.dataLength()).isEqualTo(10.0);
	}

	@Test
	void addADiagnosticForMissingDataFormats()
	{
		var source = """
			define data
			local
			1 #MYVAR ()
			end-define
			""";

		assertDiagnostic(source, ParserError.UNEXPECTED_TOKEN);
	}

	private IDefineData assertParsesWithoutDiagnostics(String source)
	{
		var lexer = new Lexer();
		var lexResult = lexer.lex(source);
		assertThat(lexResult.diagnostics().size())
			.as(
				"Expected the source to lex without diagnostics%n%s"
					.formatted(lexResult.diagnostics().stream().map(d -> d.message()).collect(Collectors.joining("\n"))))
			.isZero();
		var parser = new DefineDataParser();
		var parseResult = parser.parse(lexResult);
		assertThat(parseResult.diagnostics().size())
			.as(
				"Expected the source to parse without diagnostics%n%s"
					.formatted(parseResult.diagnostics().stream().map(d -> d.message()).collect(Collectors.joining("\n"))))
			.isZero();

		return parseResult.result();
	}
}
