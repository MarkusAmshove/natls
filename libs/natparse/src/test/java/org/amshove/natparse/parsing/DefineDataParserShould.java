package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

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

		assertThat(defineData.position().line()).isEqualTo(0);
		var firstTokenNode = assertNodeType(defineData.descendants().first(), ITokenNode.class);
		assertThat(firstTokenNode.token().kind()).isEqualTo(SyntaxKind.DEFINE);
		assertThat(defineData.descendants().last().position().line()).isEqualTo(2);
		var lastTokenNode = assertNodeType(defineData.descendants().last(), ITokenNode.class);
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

		assertThat(defineData.descendants()).isNotNull();
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
			   LOCAL
			   1 #MYVAR (A5)
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		assertAll(
			() -> assertTokenNode(defineData.descendants().get(0), n -> n.token().kind())
				.isEqualTo(SyntaxKind.DEFINE),
			() -> assertTokenNode(defineData.descendants().get(1), n -> n.token().kind())
				.isEqualTo(SyntaxKind.DATA),
			() -> assertNodeType(defineData.descendants().get(2), IUsingNode.class),
			() -> assertNodeType(defineData.descendants().get(3), IUsingNode.class),
			() -> assertNodeType(defineData.descendants().get(4), IUsingNode.class),
			() -> assertNodeType(defineData.descendants().get(5), IUsingNode.class),
			() -> assertNodeType(defineData.descendants().get(6), IScopeNode.class),
			() -> assertTokenNode(defineData.descendants().get(7), n -> n.token().kind())
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

		var variable = assertNodeType(defineData.variables().first(), ITypedVariableNode.class);
		assertThat(variable.name()).isEqualTo("#MYVAR");
		assertThat(variable.level()).isEqualTo(1);
		assertThat(variable.type().format()).isEqualTo(DataFormat.ALPHANUMERIC);
		assertThat(variable.type().length()).isEqualTo(10.0);
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

	@TestFactory
	List<DynamicTest> parseCorrectDataTypes()
	{
		return List.of(
			createTypeTest("(A10)", DataFormat.ALPHANUMERIC, 10.0, false),
			createTypeTest("(A) dynamic", DataFormat.ALPHANUMERIC, 0.0, true),
			createTypeTest("(U7)", DataFormat.UNICODE, 7, false),
			createTypeTest("(U) DYNAMIC", DataFormat.UNICODE, 0.0, true),
			createTypeTest("(B2)", DataFormat.BINARY, 2.0, false),
			createTypeTest("(B) dynamic", DataFormat.BINARY, 0.0, true),
			createTypeTest("(C)", DataFormat.CONTROL, 0.0, false),
			createTypeTest("(D)", DataFormat.DATE, 0.0, false),
			createTypeTest("(F4)", DataFormat.FLOAT, 4.0, false),
			createTypeTest("(I4)", DataFormat.INTEGER, 4.0, false),
			createTypeTest("(L)", DataFormat.LOGIC, 0.0, false),
			createTypeTest("(N8)", DataFormat.NUMERIC, 8.0, false),
			createTypeTest("(N12,7)", DataFormat.NUMERIC, 12.7, false),
			createTypeTest("(N12.7)", DataFormat.NUMERIC, 12.7, false),
			createTypeTest("(P02)", DataFormat.PACKED, 2.0, false),
			createTypeTest("(T)", DataFormat.TIME, 0.0, false)
		);
	}

	@ParameterizedTest
	@CsvSource({ "A,true", "B,true", "C,false", "D,false", "F4,false", "I4,false", "L,false", "N4,false", "P4,false", "T,false", "U,true" })
	void addDiagnosticsForTypesIfTheyDoNotAllowDynamicLength(String type, boolean canHaveDynamicLength)
	{
		if (canHaveDynamicLength)
		{
			assertParsesWithoutDiagnostics("DEFINE DATA LOCAL 1 #AVAR (%s) DYNAMIC END-DEFINE".formatted(type));
		}
		else
		{
			assertDiagnostic("DEFINE DATA LOCAL 1 #AVAR (%s) DYNAMIC END-DEFINE".formatted(type), ParserError.INVALID_DATA_TYPE_FOR_DYNAMIC_LENGTH);
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "F", "I", "N", "P", "A", "B", "U" })
	void addDiagnosticsForTypesMissingALength(String type)
	{
		assertDiagnostic("define data local 1 #m (%s) end-define".formatted(type), ParserError.VARIABLE_LENGTH_MISSING);
	}

	@Test
	void supportInitialValues()
	{
		var defineData = assertParsesWithoutDiagnostics("""
			define data local
			1 #myvar (A10) init <'hello'>
			end-define
			""");

		var variable = assertNodeType(defineData.variables().first(), ITypedVariableNode.class);
		assertThat(variable.type().initialValue().source()).isEqualTo("'hello'");
	}

	@Test
	void supportSystemVariablesAsInitializer()
	{
		var defineData = assertParsesWithoutDiagnostics("""
			define data local
			1 #myvar (N8) init <*DATN>
			end-define
			""");

		var variable = assertNodeType(defineData.variables().first(), ITypedVariableNode.class);
		assertThat(variable.type().initialValue().source()).isEqualTo("*DATN");
		var systemVar = variable.findDescendantOfType(ISystemVariableNode.class);
		assertThat(systemVar).isNotNull();
		assertThat(systemVar.systemVariable()).isEqualTo(SyntaxKind.DATN);
	}

	@Test
	void supportConstantValues()
	{
		var defineData = assertParsesWithoutDiagnostics("""
			define data local
			1 #myvar (A10) const <'hello'>
			end-define
			""");

		var variable = assertNodeType(defineData.variables().first(), ITypedVariableNode.class);
		assertThat(variable.type().isConstant()).isTrue();
		assertThat(variable.type().initialValue().source()).isEqualTo("'hello'");
	}

	@ParameterizedTest
	@CsvSource({ "A,5", "N,\"Hi\"", "I,\"Hello\"", "P,TRUE", "F,FALSE" })
	void addADiagnosticForTypeMismatchesInInitialValues(String type, String literal)
	{
		assertDiagnostic("define data local 1 #var (%s4) init <%s> end-define".formatted(type, literal), ParserError.INITIAL_VALUE_TYPE_MISMATCH);
	}

	@ParameterizedTest
	@CsvSource({ "A,5", "N,\"Hi\"", "I,\"Hello\"", "P,TRUE", "F,FALSE" })
	void addADiagnosticForTypeMismatchesInConstValues(String type, String literal)
	{
		assertDiagnostic("define data local 1 #var (%s4) const <%s> end-define".formatted(type, literal), ParserError.INITIAL_VALUE_TYPE_MISMATCH);
	}

	@Test
	void parseMultipleVariables()
	{
		var defineData = assertParsesWithoutDiagnostics("""
			define data
			local
			1 #FIRSTVAR (A10)
			1 #SECONDVAR (A5)
			end-define
			""");

		assertThat(defineData.variables().size()).isEqualTo(2);
		assertThat(defineData.variables().first().name()).isEqualTo("#FIRSTVAR");
		assertThat(defineData.variables().first().level()).isEqualTo(1);
		assertThat(defineData.variables().get(1).name()).isEqualTo("#SECONDVAR");
		assertThat(defineData.variables().get(1).level()).isEqualTo(1);
	}

	@Test
	void parseGroupVariables()
	{
		var defineData = assertParsesWithoutDiagnostics("""
			define data
			local
			1 #A-GROUP
			  2 #WITHINGROUP1 (A5)
			  2 #WITHINGROUP2 (N2)
			  2 #ANOTHERGROUP
			  	3 #SUPERIN (L)
			  2 #TWOAGAIN (C)
			1 #ONEAGAIN (T)
			end-define
			""");

		var scopeNode = defineData.findDescendantOfType(IScopeNode.class);
		assertThat(scopeNode).isNotNull();
		assertThat(scopeNode.descendants().size()).isEqualTo(3); // LOCAL + Group + Typed

		var group = assertNodeType(defineData.variables().first(), IGroupNode.class);

		assertThat(group.level()).isEqualTo(1);
		assertThat(group.name()).isEqualTo("#A-GROUP");
		assertThat(group.qualifiedName()).isEqualTo("#A-GROUP");
		assertThat(group.descendants().size()).isEqualTo(6); // 1 + #A-GROUP + 2 #WI..2 + 2 #WITH..2 + 2 #ANOTH.. + 2 #TWOAGAIN
		{
			var firstChild = assertNodeType(group.variables().first(), ITypedVariableNode.class);
			assertThat(firstChild.level()).isEqualTo(2);
			assertThat(firstChild.name()).isEqualTo("#WITHINGROUP1");
			assertThat(firstChild.qualifiedName()).isEqualTo("#A-GROUP.#WITHINGROUP1");
			assertThat(firstChild.type().format()).isEqualTo(DataFormat.ALPHANUMERIC);
			assertThat(firstChild.type().length()).isEqualTo(5.0);

			var secondChild = assertNodeType(group.variables().get(1), ITypedVariableNode.class);
			assertThat(secondChild.level()).isEqualTo(2);
			assertThat(secondChild.name()).isEqualTo("#WITHINGROUP2");
			assertThat(secondChild.qualifiedName()).isEqualTo("#A-GROUP.#WITHINGROUP2");
			assertThat(secondChild.type().format()).isEqualTo(DataFormat.NUMERIC);
			assertThat(secondChild.type().length()).isEqualTo(2.0);

			var thirdChild = assertNodeType(group.variables().get(2), IGroupNode.class);
			assertThat(thirdChild.level()).isEqualTo(2);
			assertThat(thirdChild.name()).isEqualTo("#ANOTHERGROUP");
			assertThat(thirdChild.qualifiedName()).isEqualTo("#A-GROUP.#ANOTHERGROUP");
			{
				var superIn = assertNodeType(thirdChild.variables().first(), ITypedVariableNode.class);
				assertThat(superIn.level()).isEqualTo(3);
				assertThat(superIn.name()).isEqualTo("#SUPERIN");
				assertThat(superIn.type().format()).isEqualTo(DataFormat.LOGIC);
				assertThat(superIn.qualifiedName()).isEqualTo("#A-GROUP.#SUPERIN");
			}

			var fourthChild = assertNodeType(group.variables().get(3), ITypedVariableNode.class);
			assertThat(fourthChild.level()).isEqualTo(2);
			assertThat(fourthChild.name()).isEqualTo("#TWOAGAIN");
			assertThat(fourthChild.qualifiedName()).isEqualTo("#A-GROUP.#TWOAGAIN");
			assertThat(fourthChild.type().format()).isEqualTo(DataFormat.CONTROL);
		}

		var afterGroup = assertNodeType(defineData.variables().last(), ITypedVariableNode.class);
		assertThat(afterGroup.level()).isEqualTo(1);
		assertThat(afterGroup.name()).isEqualTo("#ONEAGAIN");
		assertThat(afterGroup.qualifiedName()).isEqualTo("#ONEAGAIN");
		assertThat(afterGroup.type().format()).isEqualTo(DataFormat.TIME);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"(T/2)",
		"(T/1:10)",
		"(T/1:*)",
		"(T/*,1:5)",
		"(T/*:10)",
		"(A10/1:10)",
		"(T/1:10,50:*,*:20)",
		"(A20/1:10,50:*,*:20)",
	})
	void parseArrayDefinitions(String variable)
	{
		assertParsesWithoutDiagnostics("""
			define data local
			1 AN-ARRAY %s
			end-define
			""".formatted(variable));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"(1:10,20:*)",
		"(1:10)",
		"(1:*)",
		"(*:5)",
		"(*)",
		"(5)"
	})
	void parseArrayDefinitionsForGroups(String variable)
	{
		assertParsesWithoutDiagnostics("""
			define data local
			1 AN-ARRAY %s
			2 INSIDE-GROUP (A5)
			end-define
			""".formatted(variable));
	}

	@Test
	void parseAnArrayWithWhitespaceBeforeTheSlash()
	{
		assertParsesWithoutDiagnostics("""
			define data
			local
			01 #DATN (N8 /1:5)
			end-define
			""");
	}

	@Test
	void parseAnArrayWithMultipleCommasAndReferences()
	{
		assertParsesWithoutDiagnostics("""
			define data
			local
			01 #C1 (N2) CONST<1>
			01 #C2 (N2) CONST<2>
			01 #ARR(N12,7/1:#C1,1:#C2)
			end-define
			""");
	}

	@Test
	void parseAnArrayThatHasAConstReferenceAsDimension()
	{
		var defineData = assertParsesWithoutDiagnostics("""
			define data
			local
			1 #myconst (N5) const<2>
			1 #mygroup (#myconst)
			2 #ingroup (a1)
			end-define
			""");

		var myGroup = assertNodeType(defineData.variables().get(1), IGroupNode.class);
		assertThat(myGroup.dimensions().first().upperBound()).isEqualTo(2);
	}

	@Test
	void parseAnArrayThatHasAConstReferenceAsDimensionWithType()
	{
		var defineData = assertParsesWithoutDiagnostics("""
			define data local
			1 #length (N2) const <5>
			1 #myarray (A10/#length)
			end-define
			""");

		var myArray = assertNodeType(defineData.variables().last(), ITypedVariableNode.class);
		assertThat(myArray.dimensions().first().upperBound()).isEqualTo(5);
	}

	@Test
	void parseAnArrayThatHasAConstReferenceBoundInSecondDimensionWithoutWhitespace()
	{
		var defineData = assertParsesWithoutDiagnostics("""
				define data local
				1 c-my-const (N2) CONST <56>
				1 #my-arr (A8/1:2,1:c-my-const)
				end-define
			""");

		var myConst = assertNodeType(defineData.variables().first(), IReferencableNode.class);
		assertThat(myConst.references().size()).isEqualTo(1);
		var myArr = assertNodeType(defineData.variables().last(), ITypedVariableNode.class);
		assertThat(myArr.dimensions().last().lowerBound()).isEqualTo(1);
		assertThat(myArr.dimensions().last().upperBound()).isEqualTo(56);

		assertThat(myArr.dimensions().first().lowerBound()).isEqualTo(1);
		assertThat(myArr.dimensions().first().upperBound()).isEqualTo(2);
	}

	@Test
	void parseAnArrayThatHasAConstReferenceBoundInSecondDimensionWithWhitespace()
	{
		var defineData = assertParsesWithoutDiagnostics("""
				define data local
				1 c-my-const (N2) CONST <56>
				1 #my-arr (A8/1:2, 1:c-my-const)
				end-define
			""");

		var myConst = assertNodeType(defineData.variables().first(), IReferencableNode.class);
		assertThat(myConst.references().size()).isEqualTo(1);
		var myArr = assertNodeType(defineData.variables().last(), ITypedVariableNode.class);
		assertThat(myArr.dimensions().last().lowerBound()).isEqualTo(1);
		assertThat(myArr.dimensions().last().upperBound()).isEqualTo(56);

		assertThat(myArr.dimensions().first().lowerBound()).isEqualTo(1);
		assertThat(myArr.dimensions().first().upperBound()).isEqualTo(2);
	}

	@Test
	void parseAnArrayWithLowerAndUpperBoundBeingReferences()
	{
		var defineData = assertParsesWithoutDiagnostics("""
			define data local
			1 c-min (N1) const<1>
			1 c-max (n1) const<9>
			1 #thegroup (c-min : c-max)
			2 #inside (a1)
			end-define
			""");

		var theGroup = assertNodeType(defineData.variables().get(2), IGroupNode.class);
		assertThat(theGroup.dimensions().first().lowerBound()).isEqualTo(1);
		assertThat(theGroup.dimensions().first().upperBound()).isEqualTo(9);
	}

	@Test
	void addAReferenceToTheConstantForConstArrayDimension()
	{
		var defineData = assertParsesWithoutDiagnostics("""
			define data local
			1 #length (N2) const <5>
			1 #myarray (A10/#length)
			end-define
			""");

		var length = assertNodeType(defineData.variables().first(), IReferencableNode.class);
		var myArray = assertNodeType(defineData.variables().last(), ITypedVariableNode.class);
		var referenceNode = myArray.dimensions().first().findDescendantOfType(ISymbolReferenceNode.class);

		assertThat(length.references().first()).isEqualTo(referenceNode);
		assertThat(referenceNode.reference()).isEqualTo(length);
	}

	@Test
	void parseIndependentVariables()
	{
		var source = """
			   DEFINE DATA
			   INDEPENDENT
			   1 +MY-AIV (A10)
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var independent = defineData.variables().first();
		assertThat(independent.parent()).isInstanceOf(IScopeNode.class);
		assertThat(independent.level()).isEqualTo(1);
		assertThat(independent.name()).isEqualTo("+MY-AIV");
		assertThat(independent.scope().isIndependent()).isTrue();
	}

	@Test
	void parseRedefines()
	{
		var source = """
			   DEFINE DATA
			   LOCAL
			   1 #DATE (N8)
			   1 REDEFINE #DATE
			   	2 #YEAR (N4)
			   	2 #MONTH (N2)
			   	2 #DAY (N2)
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var date = defineData.variables().first();
		var redefinition = assertNodeType(defineData.variables().get(1), IRedefinitionNode.class);
		assertThat(redefinition.target()).isEqualTo(date);
		assertThat(redefinition.variables().first().qualifiedName()).isEqualTo("#DATE.#YEAR");
	}

	@Test
	void notReportALengthDiagnosticForNestedRedefineVariables()
	{
		assertParsesWithoutDiagnostics("""
			define data
			local
			1 A-VAR (A8)
			1 REDEFINE A-VAR
			   2 INSIDE (A4)
			   2 REDEFINE INSIDE
				   3 INSIDE-INSIDE (A2) /* these should not be counted to the total length
				   3 INSIDE-INSIDE-2 (A2)
			   2 ALSO-INSIDE (A4)
			end-define
			""");
	}

	@Test
	void redefineGroups()
	{
		var source = """
			   DEFINE DATA
			   LOCAL
			   01 #FIRSTVAR
			     02 #FIRSTVAR-A (N2) INIT <5>
			     02 #FIRSTVAR-B (P8) INIT <10>
			  01 REDEFINE #FIRSTVAR
			     02 #FIRSTVAR-ALPHA (A10)
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var firstVar = defineData.variables().first();
		var redefinition = assertNodeType(defineData.variables().get(3), IRedefinitionNode.class);
		assertThat(redefinition.target()).isEqualTo(firstVar);
		assertThat(redefinition.variables().first().qualifiedName()).isEqualTo("#FIRSTVAR.#FIRSTVAR-ALPHA");
	}

	@Test
	void redefineIndependentVariables()
	{
		var source = """
			   DEFINE DATA
			   INDEPENDENT
			   1 +MY-AIV (A10)
			   1 REDEFINE +MY-AIV
			   2 #INSIDE (A2)
			   END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var myAiv = defineData.variables().first();
		var redefinition = assertNodeType(defineData.variables().get(1), IRedefinitionNode.class);
		assertThat(redefinition.target()).isEqualTo(myAiv);
		assertThat(redefinition.variables().first().qualifiedName()).isEqualTo("+MY-AIV.#INSIDE");
	}

	@Test
	void notRaiseADiagnosticIfRedefineHasSmallerLength()
	{
		var source = """
			   DEFINE DATA
			   LOCAL
			   1 #DATE (N8)
			   1 REDEFINE #DATE
			    2 #YEAR (N4)
			    2 #MONTH (N2)
			   END-DEFINE
			""";

		assertParsesWithoutDiagnostics(source);
	}

	@Test
	void parseViews()
	{
		var source = """
			DEFINE DATA
			LOCAL
			1 MY-VIEW VIEW MY-DDM
			2 DDM-FIELD (A15)
			2 THE-SUPERDESCRIPTOR (N8)
			2 REDEFINE THE-SUPERDESCRIPTOR
			3 YEAR (N4)
			3 MONTH (N2)
			3 DAY (N2)
			END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var view = assertNodeType(defineData.variables().first(), IViewNode.class);
		assertThat(view.declaration().source()).isEqualTo("MY-VIEW");
		assertThat(view.ddmNameToken().source()).isEqualTo("MY-DDM");
		assertThat(view.variables().size()).isEqualTo(3);
		assertThat(view.variables().last()).isInstanceOf(IRedefinitionNode.class);
		assertThat(((IRedefinitionNode) view.variables().last()).variables().size()).isEqualTo(3);
	}

	@Test
	void parseViewsWithArrays()
	{
		var source = """
			DEFINE DATA
			LOCAL
			1 MY-VIEW VIEW MY-DDM
			2 DDM-FIELD
			2 AN-ARRAY (N8/1:9)
			END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var view = assertNodeType(defineData.variables().first(), IViewNode.class);
		assertThat(view.declaration().source()).isEqualTo("MY-VIEW");
		assertThat(view.ddmNameToken().source()).isEqualTo("MY-DDM");
		assertThat(view.variables().size()).isEqualTo(2);
		var theArray = assertNodeType(view.variables().last(), ITypedVariableNode.class);
		assertThat(theArray.dimensions().first().lowerBound()).isEqualTo(1);
		assertThat(theArray.dimensions().first().upperBound()).isEqualTo(9);
	}

	@Test
	void parseViewsWithGroupArrays()
	{
		var source = """
			DEFINE DATA
			LOCAL
			1 MY-VIEW VIEW MY-DDM
			2 DDM-FIELD
			2 AN-GROUP-ARRAY (1:9)
			3 ANOTHER-FIELD
			3 ANOTHER-TYPED-FIELD (N2)
			END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var view = assertNodeType(defineData.variables().first(), IViewNode.class);
		assertThat(view.variables().size()).isEqualTo(2);
		var theArray = assertNodeType(view.variables().last(), IGroupNode.class);
		assertThat(theArray.name()).isEqualTo("AN-GROUP-ARRAY");
		assertThat(theArray.dimensions().first().lowerBound()).isEqualTo(1);
		assertThat(theArray.dimensions().first().upperBound()).isEqualTo(9);
	}

	@Test
	void parseViewsWithArraysReferencingVariables()
	{
		var source = """
			DEFINE DATA
			LOCAL
			1 #MY-VAR (N1) INIT <*LANGUAGE>
			1 MY-VIEW VIEW MY-DDM
			2 ARRAY-INSIDE (#MY-VAR)
			END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var view = findVariable(defineData, "MY-VIEW", IViewNode.class);
		assertThat(view.variables().size()).isEqualTo(1);
		var theArray = assertNodeType(view.variables().first(), ITypedVariableNode.class);
		assertThat(theArray.name()).isEqualTo("ARRAY-INSIDE");
		assertThat(theArray.dimensions().first().lowerBound()).isEqualTo(1);
		assertThat(theArray.dimensions().first().upperBound()).isEqualTo(8);
	}

	@Test
	void parseAViewVariableWithoutTypeNotationButWithArrayNotation()
	{
		var source = """
			DEFINE DATA
			LOCAL
			1 MY-VIEW VIEW MY-DDM
			2 DDM-FIELD (1:10)
			END-DEFINE
			""";

		var defineData = assertParsesWithoutDiagnostics(source);

		var view = assertNodeType(defineData.variables().first(), IViewNode.class);
		assertThat(view.variables().size()).isEqualTo(1);
		var theArray = assertNodeType(view.variables().first(), TypedVariableNode.class);
		assertThat(theArray.name()).isEqualTo("DDM-FIELD");
		assertThat(theArray.dimensions().first().lowerBound()).isEqualTo(1);
		assertThat(theArray.dimensions().first().upperBound()).isEqualTo(10);
	}

	@Test
	void parseViewWithOptionalOf()
	{
		assertParsesWithoutDiagnostics("""
			define data
			local
			1 my-view view of my-ddm
			end-define
			""");
	}

	@Test
	void parseArrayInitialValues()
	{
		assertParsesWithoutDiagnostics("""
						define data
						local
						1 #initialized-array (A3/1:2) INIT
							(1) <'abc'>
							(2) <'def'>
						end-define
			""");
		// TODO(array-initializer): Check values
	}

	@Test
	void parseArrayCommaSeparatedInitialValues()
	{
		assertParsesWithoutDiagnostics("""
						define data
						local
						1 #initialized-array (A3/1:2) INIT <'abc','def'>
						end-define
			""");
		// TODO(array-initializer): Check values
	}

	@Test
	void parseArrayCommaSeparatedMultipleInitialValues()
	{
		assertParsesWithoutDiagnostics("""
						define data
						local
						1 #initialized-array (A3/1:3) INIT <'abc',,'def'>
						end-define
			""");
		// TODO(array-initializer): Check values
	}

	@Test
	void parseArraySemicolonSeparatedMultipleInitialValues()
	{
		assertParsesWithoutDiagnostics("""
						define data
						local
						1 #initialized-array (A3/1:3) INIT <1;2;3>
						end-define
			""");
		// TODO(array-initializer): Check values
	}

	@Test
	void parseArrayAsteriskInitialValues()
	{
		assertParsesWithoutDiagnostics("""
						define data
						local
						1 #initialized-array (A3/1:2) INIT (*) <'abc'>
						end-define
			""");
		// TODO(array-initializer): Check values
	}

	@Test
	void parseArrayBoundInitialValues()
	{
		assertParsesWithoutDiagnostics("""
						define data
						local
						1 #initialized-array (A3/1:5) INIT (1:5) <'abc'>
						end-define
			""");
		// TODO(array-initializer): Check values
	}

	@Test
	void parseArrayDifferentIndizesInitialValues()
	{
		assertParsesWithoutDiagnostics("""
						define data
						local
						1 #initialized-array (A3/1:5) INIT (1,3) <'abc'>
						end-define
			""");
		// TODO(array-initializer): Check values
	}

	@Test
	void parseArrayIndexNotationInitialValues()
	{
		assertParsesWithoutDiagnostics("""
						define data
						local
						1 #initialized-array (A3/1:5, 1:3) INIT (1,V) <'C','D'>
						end-define
			""");
		// TODO(array-initializer): Check values
	}

	@Test
	void parseInitializerOfVariablesInGroupArray()
	{
		var data = assertParsesWithoutDiagnostics("""
			define data
			local
			1 #myarraygroup (1:10)
			2 #inside (A5) INIT <'abc', 'def', 'ghi'>
			end-define
			""");
		// TODO(array-initializer): Check values

		var inside = data.variables().last();
		assertThat(inside.name()).isEqualTo("#INSIDE");
		assertThat(inside.dimensions().first().lowerBound()).isEqualTo(1);
		assertThat(inside.dimensions().first().upperBound()).isEqualTo(10);
	}

	@Test
	void addMultipleDimensionsForGroupArraysContainingArrays()
	{
		var data = assertParsesWithoutDiagnostics("""
			define data
			local
			1 #myarraygroup (1:10)
			2 #inside (A5/1:5) /* This is considered a second dimension, so (1:10,1:5)
			end-define
			""");
		// TODO(array-initializer): Check values

		var inside = data.variables().last();
		assertThat(inside.name()).isEqualTo("#INSIDE");
		assertThat(inside.dimensions().size()).isEqualTo(2);
		assertThat(inside.dimensions().first().lowerBound()).isEqualTo(1);
		assertThat(inside.dimensions().first().upperBound()).isEqualTo(10);
		assertThat(inside.dimensions().last().lowerBound()).isEqualTo(1);
		assertThat(inside.dimensions().last().upperBound()).isEqualTo(5);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"EM=9999",
		"EMU=9999",
		"HD='header'",
		"PM=952"
	})
	void parseEditMasks(String mask)
	{
		assertParsesWithoutDiagnostics("""
			define data local
			1 #VAR (N10) (%s)
			end-define
			""".formatted(mask));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"AD=D",
		"AD=B",
		"AD=I",
		"AD=N",
		"AD=V",
		"AD=U",
		"AD=C",
		"AD=Y",
		"AD=P",
		"CD=BL",
		"CD=GR",
		"CD=NE",
		"CD=PI",
		"CD=RE",
		"CD=TU",
		"CD=YE",
		"AD=I CD=BL"
	})
	void parseAttributeConstantsAsInit(String attribute)
	{
		assertParsesWithoutDiagnostics("""
			define data local
			1 #VAR (C) INIT <(%s)>
			end-define
			""".formatted(attribute));
	}

	@Test
	void parseInitAll()
	{
		assertParsesWithoutDiagnostics("""
			define data
			local
			1 #initialized-array (A3/1:2) INIT ALL <'A'>
			end-define
			""");
	}

	@Test
	void parseInitAllLength()
	{
		assertParsesWithoutDiagnostics("""
			define data
			local
			1 #initialized-array (A3/1:2) INIT ALL LENGTH <'A'>
			end-define
			""");
	}

	@Test
	void parseInitAllLengthN()
	{
		assertParsesWithoutDiagnostics("""
			define data
			local
			1 #initialized-array (A3/1:10) INIT ALL LENGTH 7 <'A'>
			end-define
			""");
	}

	@Test
	void parseInitAllFullLength()
	{
		assertParsesWithoutDiagnostics("""
			define data
			local
			1 #initialized-array (A3/1:10) INIT ALL FULL LENGTH <'A'>
			end-define
			""");
	}

	@Test
	void parseAParameterByValue()
	{
		assertParsesWithoutDiagnostics("""
			define data
			parameter
			1 #p-para (A3) BY VALUE
			end-define
			""");
	}

	@Test
	void parseAParameterByValueResult()
	{
		assertParsesWithoutDiagnostics("""
			define data
			parameter
			1 #p-para (A3) BY VALUE RESULT
			end-define
			""");
	}

	@Test
	void parseAParameterByValueResultOptional()
	{
		assertParsesWithoutDiagnostics("""
			define data
			parameter
			1 #p-para (A3) BY VALUE RESULT OPTIONAL
			end-define
			""");
	}

	@Test
	void parseAParameterOptional()
	{
		assertParsesWithoutDiagnostics("""
			define data
			parameter
			1 #p-para (A3) OPTIONAL
			end-define
			""");
	}

	private IDefineData assertParsesWithoutDiagnostics(String source)
	{
		var lexer = new Lexer();
		var lexResult = lexer.lex(source);
		assertThat(lexResult.diagnostics().size())
			.as(
				"Expected the source to lex without diagnostics%n%s"
					.formatted(lexResult.diagnostics().stream().map(IDiagnostic::message).collect(Collectors.joining("\n"))))
			.isZero();
		var parser = new DefineDataParser();
		var parseResult = parser.parse(lexResult);
		assertThat(parseResult.diagnostics().size())
			.as(
				"Expected the source to parse without diagnostics%n%s"
					.formatted(parseResult.diagnostics().stream().map(IDiagnostic::message).collect(Collectors.joining("\n"))))
			.isZero();

		return parseResult.result();
	}

	private DynamicTest createTypeTest(String source, DataFormat expectedFormat, double expectedLength, boolean hasDynamicLength)
	{
		return dynamicTest(
			source,
			() -> {
				var defineDataSource = """
					define data
					local
					1 #myvar %s
					end-define
					""".formatted(source);
				var defineData = assertParsesWithoutDiagnostics(defineDataSource);
				var variable = assertNodeType(defineData.variables().first(), ITypedVariableNode.class);
				assertThat(variable.type().format()).isEqualTo(expectedFormat);
				assertThat(variable.type().length()).isEqualTo(expectedLength);
				assertThat(variable.type().hasDynamicLength()).isEqualTo(hasDynamicLength);
			}
		);
	}

	private <T extends ISyntaxNode> T findVariable(IDefineData defineData, String variableName, Class<T> expectedType)
	{
		var variable = defineData.variables().stream().filter(v -> v.name().equals(variableName)).findFirst();
		if(variable.isEmpty())
		{
			fail("Could not find variable %s in %s".formatted(variable, defineData.variables().stream().map(IVariableNode::toString).collect(Collectors.joining(", "))));
		}

		return assertNodeType(variable.get(), expectedType);
	}
}
