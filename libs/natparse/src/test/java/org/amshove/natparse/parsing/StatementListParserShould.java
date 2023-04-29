package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.conditionals.IConditionNode;
import org.amshove.natparse.natural.conditionals.IRelationalCriteriaNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class StatementListParserShould extends StatementParseTest
{

	@Test
	void parseASimpleCallnat()
	{
		ignoreModuleProvider();
		var callnat = assertParsesSingleStatement("CALLNAT 'MODULE'", ICallnatNode.class);
		assertThat(callnat.referencingToken().kind()).isEqualTo(SyntaxKind.STRING_LITERAL);
		assertThat(callnat.referencingToken().stringValue()).isEqualTo("MODULE");
	}

	@Test
	void raiseADiagnosticWhenNoModuleIsPassed()
	{
		ignoreModuleProvider();
		assertDiagnostic("CALLNAT 1", ParserError.UNEXPECTED_TOKEN);
	}

	@Test
	void allowVariablesAsModuleReferences()
	{
		ignoreModuleProvider();
		var callnat = assertParsesSingleStatement("CALLNAT #THE-SUBPROGRAM", ICallnatNode.class);
		assertThat(callnat.referencingToken().kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(callnat.referencingToken().symbolName()).isEqualTo("#THE-SUBPROGRAM");
		assertThat(callnat.reference()).isNull();
	}

	@Test
	void addBidirectionalReferencesForCallnats()
	{
		var calledSubprogram = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubprogram);

		var callnat = assertParsesSingleStatement("CALLNAT 'A-MODULE'", ICallnatNode.class);
		assertThat(callnat.reference()).isEqualTo(calledSubprogram);
		assertThat(calledSubprogram.callers()).contains(callnat);
	}

	@Test
	void allowTrailingSpacesInModuleNamesThatAreInStrings()
	{
		var calledSubprogram = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubprogram);

		var callnat = assertParsesSingleStatement("CALLNAT 'A-MODULE ' ", ICallnatNode.class);
		assertThat(callnat.reference()).isEqualTo(calledSubprogram);
		assertThat(calledSubprogram.callers()).contains(callnat);
	}

	@Test
	void findCalledSubprogramsWhenSourceContainsLowerCaseCharacters()
	{
		var calledSubprogram = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubprogram);

		var callnat = assertParsesSingleStatement("CALLNAT 'A-module'", ICallnatNode.class);
		assertThat(callnat.reference()).isEqualTo(calledSubprogram);
		assertThat(calledSubprogram.callers()).contains(callnat);
	}

	@Test
	void parseParameterForCallnats()
	{
		var calledSubprogram = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubprogram);

		var callnat = assertParsesSingleStatement("CALLNAT 'A-module' #VAR 10 'String' TRUE 1X", ICallnatNode.class);
		assertThat(callnat.providedParameter()).hasSize(5);
		assertNodeType(callnat.providedParameter().get(0), IVariableReferenceNode.class);
		assertNodeType(callnat.providedParameter().get(1), ILiteralNode.class);
		assertNodeType(callnat.providedParameter().get(2), ILiteralNode.class);
		assertNodeType(callnat.providedParameter().get(3), ILiteralNode.class);
		assertNodeType(callnat.providedParameter().get(4), ISkipOperandNode.class);
	}

	@Test
	void parseParameterForCallnatsWithAttributeDefinition()
	{
		var calledSubprogram = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubprogram);

		var callnat = assertParsesSingleStatement("CALLNAT 'A-module' #VAR (AD=O) #VAR2 (AD=M) #VAR3 (AD=A)", ICallnatNode.class);
		assertThat(callnat.providedParameter()).hasSize(3);

		var first = assertNodeType(callnat.providedParameter().get(0), IVariableReferenceNode.class);
		assertThat(first.findDescendantToken(SyntaxKind.AD)).isNotNull();

		var second = assertNodeType(callnat.providedParameter().get(1), IVariableReferenceNode.class);
		assertThat(second.findDescendantToken(SyntaxKind.AD)).isNotNull();

		var third = assertNodeType(callnat.providedParameter().get(2), IVariableReferenceNode.class);
		assertThat(third.findDescendantToken(SyntaxKind.AD)).isNotNull();
	}

	@Test
	void parseCallnatWithUsing()
	{
		var calledSubprogram = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubprogram);

		var callnat = assertParsesSingleStatement("CALLNAT 'A-module' USING #VAR", ICallnatNode.class);
		assertThat(callnat.providedParameter()).hasSize(1);
		assertThat(assertNodeType(callnat.providedParameter().get(0), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"#VAR5 := 1", "#VAR5(2) := 10", "*ERROR-NR := 5"
	})
	void distinguishBetweenCallnatParameterAndVariableAssignment(String nextLine)
	{

		var calledSubprogram = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubprogram);

		var statements = assertParsesWithoutDiagnostics("""
			CALLNAT 'A-module' #VAR #VAR
				#VARNEWLINE
			%s
			""".formatted(nextLine)).statements();

		assertThat(statements.size()).isGreaterThan(1); // Assignment not parsed yet. Change this to two when this test breaks from implementing assignments
		var callnat = assertNodeType(statements.first(), ICallnatNode.class);
		assertThat(assertNodeType(callnat.providedParameter().last(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VARNEWLINE");
	}

	@Test
	void parseASimpleInclude()
	{
		ignoreModuleProvider();
		var include = assertParsesSingleStatement("INCLUDE L4NLOGIT", IIncludeNode.class);
		assertThat(include.referencingToken().kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(include.referencingToken().symbolName()).isEqualTo("L4NLOGIT");
	}

	@Test
	void parseAnIncludeWithParameter()
	{
		ignoreModuleProvider();
		var include = assertParsesSingleStatement("INCLUDE THECC '''Literal''' '#VAR' '5' '*OCC(#ARR)'", IIncludeNode.class);
		assertThat(include.providedParameter()).hasSize(4);
		assertThat(assertNodeType(include.providedParameter().get(0), ILiteralNode.class).token().stringValue()).isEqualTo("'Literal'");
		assertThat(assertNodeType(include.providedParameter().get(1), ILiteralNode.class).token().stringValue()).isEqualTo("#VAR");
		assertThat(assertNodeType(include.providedParameter().get(2), ILiteralNode.class).token().stringValue()).isEqualTo("5");
		assertThat(assertNodeType(include.providedParameter().get(3), ILiteralNode.class).token().stringValue()).isEqualTo("*OCC(#ARR)");
	}

	@Test
	void raiseADiagnosticWhenNoCopycodeIsPassed()
	{
		assertDiagnostic("INCLUDE 1", ParserError.UNEXPECTED_TOKEN);
	}

	@Test
	void parseASimpleFetch()
	{
		ignoreModuleProvider();
		var fetch = assertParsesSingleStatement("FETCH 'PROG'", IFetchNode.class);
		assertThat(fetch.referencingToken().kind()).isEqualTo(SyntaxKind.STRING_LITERAL);
		assertThat(fetch.referencingToken().stringValue()).isEqualTo("PROG");
	}

	@Test
	void parseASimpleFetchReturn()
	{
		ignoreModuleProvider();
		var fetch = assertParsesSingleStatement("FETCH RETURN 'PROG'", IFetchNode.class);
		assertThat(fetch.referencingToken().kind()).isEqualTo(SyntaxKind.STRING_LITERAL);
		assertThat(fetch.referencingToken().stringValue()).isEqualTo("PROG");
	}

	@Test
	void parseASimpleFetchRepeat()
	{
		ignoreModuleProvider();
		var fetch = assertParsesSingleStatement("FETCH REPEAT 'PROG'", IFetchNode.class);
		assertThat(fetch.referencingToken().kind()).isEqualTo(SyntaxKind.STRING_LITERAL);
		assertThat(fetch.referencingToken().stringValue()).isEqualTo("PROG");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "REPEAT", "RETURN"
	})
	void parseAFetchWithVariables(String fetchType)
	{
		ignoreModuleProvider();
		var fetch = assertParsesSingleStatement("FETCH %s #MYVAR".formatted(fetchType), IFetchNode.class);
		assertThat(fetch.referencingToken().kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(fetch.referencingToken().symbolName()).isEqualTo("#MYVAR");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "REPEAT", "RETURN"
	})
	void parseAFetchWithQualifiedVariables(String fetchType)
	{
		ignoreModuleProvider();
		var fetch = assertParsesSingleStatement("FETCH %s #MYGROUP.#MYVAR".formatted(fetchType), IFetchNode.class);
		assertThat(fetch.referencingToken().kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(fetch.referencingToken().symbolName()).isEqualTo("#MYGROUP.#MYVAR");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "RETURN", "REPEAT"
	})
	void resolveExternalModulesForAFetchStatement(String fetchSource)
	{
		var program = new NaturalModule(null);
		moduleProvider.addModule("PROG", program);

		var fetch = assertParsesSingleStatement("FETCH %s 'PROG'".formatted(fetchSource), IFetchNode.class);
		assertThat(fetch.reference()).isEqualTo(program);
	}

	@Test
	void parseAnEndNode()
	{
		var endNode = assertParsesSingleStatement("END", IEndNode.class);
		assertThat(endNode.descendants()).isNotEmpty();
	}

	@Test
	void parseIgnore()
	{
		assertParsesSingleStatement("IGNORE", IIgnoreNode.class);
	}

	@Test
	void parseASubroutine()
	{
		var subroutine = assertParsesSingleStatement("""
			   DEFINE SUBROUTINE MY-SUBROUTINE
			       IGNORE
			   END-SUBROUTINE
			""", ISubroutineNode.class);

		assertThat(subroutine.declaration().symbolName()).isEqualTo("MY-SUBROUTINE");
		assertThat(subroutine.references()).isEmpty();
		assertThat(subroutine.body().statements()).hasSize(1);
	}

	@Test
	void parseASubroutineWithoutSubroutineKeyword()
	{
		var subroutine = assertParsesSingleStatement("""
			   DEFINE #MY-SUBROUTINE
			       IGNORE
			   END-SUBROUTINE
			""", ISubroutineNode.class);

		assertThat(subroutine.declaration().symbolName()).isEqualTo("#MY-SUBROUTINE");
	}

	@Test
	void parseASubroutineWithoutSubroutineKeywordButKeywordAsName()
	{
		var subroutine = assertParsesSingleStatement(
			"""
				 DEFINE RESULT
				 IGNORE
				 END-SUBROUTINE
				""",
			ISubroutineNode.class
		);

		assertThat(subroutine.declaration().symbolName()).isEqualTo("RESULT");
	}

	@Test
	void parseInternalPerformNodes()
	{
		ignoreModuleProvider();
		var perform = assertParsesSingleStatement("PERFORM MY-SUBROUTINE", IInternalPerformNode.class);
		assertThat(perform.token().symbolName()).isEqualTo("MY-SUBROUTINE");
		assertThat(perform.reference()).isNull();
	}

	@Test
	void parseInternalPerformNodesWithReference()
	{
		var statements = assertParsesWithoutDiagnostics("""
			DEFINE SUBROUTINE MY-SUBROUTINE
				IGNORE
			END-SUBROUTINE

			PERFORM MY-SUBROUTINE
			""");

		assertThat(statements.statements()).hasSize(2);
		var subroutine = statements.statements().get(0);
		var perform = assertNodeType(statements.statements().get(1), IInternalPerformNode.class);

		assertThat(perform.token().symbolName()).isEqualTo("MY-SUBROUTINE");
		assertThat(perform.reference()).isEqualTo(subroutine);
	}

	@Test
	void resolveInternalSubroutinesWithLongNames()
	{
		var statements = assertParsesWithoutDiagnostics("""
			DEFINE SUBROUTINE THIS-HAS-MORE-THAN-THIRTY-TWO-CHARACTERS
				IGNORE
			END-SUBROUTINE

			PERFORM THIS-HAS-MORE-THAN-THIRTY-TWO-CHARACTERS-BUT-IT-WORKS-I-SHOULD-NEVER-DO-THAT
			""");

		assertThat(statements.statements()).hasSize(2);
		var subroutine = statements.statements().get(0);
		var perform = assertNodeType(statements.statements().get(1), IInternalPerformNode.class);

		assertThat(perform.token().symbolName()).isEqualTo("THIS-HAS-MORE-THAN-THIRTY-TWO-CHARACTERS-BUT-IT-WORKS-I-SHOULD-NEVER-DO-THAT");
		assertThat(perform.token().trimmedSymbolName(32)).isEqualTo("THIS-HAS-MORE-THAN-THIRTY-TWO-CH");
		assertThat(perform.reference()).isEqualTo(subroutine);
	}

	@Test
	void parseInternalPerformNodesWithReferenceWhenSubroutineIsDefinedAfter()
	{
		var statements = assertParsesWithoutDiagnostics("""
			PERFORM MY-SUBROUTINE

			DEFINE SUBROUTINE MY-SUBROUTINE
				IGNORE
			END-SUBROUTINE
			""");

		assertThat(statements.statements()).hasSize(2);
		var perform = assertNodeType(statements.statements().get(0), IInternalPerformNode.class);
		var subroutine = statements.statements().get(1);

		assertThat(perform.token().symbolName()).isEqualTo("MY-SUBROUTINE");
		assertThat(perform.reference()).isEqualTo(subroutine);
	}

	@Test
	void notExportResolvedPerformCallsAsUnresolved()
	{
		var statements = assertParsesWithoutDiagnostics("""
			PERFORM MY-SUBROUTINE

			DEFINE SUBROUTINE MY-SUBROUTINE
				IGNORE
			END-SUBROUTINE
			""");

		assertThat(statements.statements()).hasSize(2);
		assertThat(((StatementListParser) sut).getUnresolvedReferences()).isEmpty();
	}

	@Test
	void parseExternalPerformCalls()
	{
		var calledSubroutine = new NaturalModule(null);
		moduleProvider.addModule("EXTERNAL-SUB", calledSubroutine);

		var perform = assertParsesSingleStatement("PERFORM EXTERNAL-SUB", IExternalPerformNode.class);
		assertThat(perform.reference()).isEqualTo(calledSubroutine);
		assertThat(calledSubroutine.callers()).contains(perform);
	}

	@Test
	void parseAndResolveExternalPerformCallsWithParameter()
	{
		var calledSubroutine = new NaturalModule(null);
		moduleProvider.addModule("EXTERNAL-SUB", calledSubroutine);

		var perform = assertParsesSingleStatement("PERFORM EXTERNAL-SUB PDA1 'Literal' 5 #VAR", IExternalPerformNode.class);
		assertThat(perform.reference()).isEqualTo(calledSubroutine);
		assertThat(calledSubroutine.callers()).contains(perform);
		assertThat(perform.providedParameter()).hasSize(4);
		assertThat(assertNodeType(perform.providedParameter().get(0), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("PDA1");
		assertThat(assertNodeType(perform.providedParameter().get(1), ILiteralNode.class).token().stringValue()).isEqualTo("Literal");
		assertThat(assertNodeType(perform.providedParameter().get(2), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(assertNodeType(perform.providedParameter().get(3), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
	}

	@Test
	void distinguishBetweenPerformParameterAndVariableAssignment()
	{

		var calledSubroutine = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubroutine);

		var statements = assertParsesWithoutDiagnostics("""
			PERFORM A-MODULE #VAR #VAR
				#VARNEWLINE
			#VAR5 := 1
			""").statements();

		assertThat(statements.size()).isEqualTo(2);
		var perform = assertNodeType(statements.first(), IExternalPerformNode.class);
		assertThat(assertNodeType(perform.providedParameter().last(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VARNEWLINE");
	}

	@Test
	void parseAFunctionCallWithoutParameter()
	{
		var calledFunction = new NaturalModule(null);
		moduleProvider.addModule("ISSTH", calledFunction);

		var call = assertParsesSingleStatement("ISSTH(<>)", IFunctionCallNode.class);
		assertThat(call.reference()).isEqualTo(calledFunction);
		assertThat(calledFunction.callers()).contains(call);
	}

	@Test
	void parseAFunctionCallWithParameter()
	{
		var calledFunction = new NaturalModule(null);
		moduleProvider.addModule("ISSTH", calledFunction);

		var call = assertParsesSingleStatement("ISSTH(<5>)", IFunctionCallNode.class);
		assertThat(call.reference()).isEqualTo(calledFunction);
		assertThat(calledFunction.callers()).contains(call);
		assertThat(call.position().offsetInLine()).isZero();
		assertThat(call.providedParameter()).hasSize(1);
		assertThat(assertNodeType(call.providedParameter().first(), ILiteralNode.class).token().intValue()).isEqualTo(5);
	}

	@Test
	void reportADiagnosticForFunctionCallsWithTrailingCommas()
	{
		var calledFunction = new NaturalModule(null);
		moduleProvider.addModule("ISSTH", calledFunction);

		assertDiagnostic("ISSTH(<5,>)", ParserError.TRAILING_TOKEN);
	}

	@Test
	void distinguishBetweenArrayAccessAndFunctionCallInIfCondition()
	{
		var statementList = assertParsesWithoutDiagnostics("""
			   IF #THE-ARRAY(#THE-VARIABLE) <> 5
			   IGNORE
			   END-IF
			""");

		assertThat(statementList.statements()).noneMatch(s -> s instanceof IFunctionCallNode);
	}

	@Test
	void parseIfStatements()
	{
		var ifStatement = assertParsesSingleStatement("""
			IF #TEST = 5
			    IGNORE
			END-IF
			""", IIfStatementNode.class);

		assertThat(ifStatement.condition()).isNotNull();
		assertThat(ifStatement.body().statements()).hasSize(1);
		assertThat(ifStatement.descendants()).hasSize(4);
	}

	@Test
	void allowIfStatementsToContainTheThenKeyword()
	{
		var ifStatement = assertParsesSingleStatement("""
			IF #TEST = 5 THEN
			    IGNORE
			END-IF
			""", IIfStatementNode.class);

		assertThat(ifStatement.condition().findDescendantToken(SyntaxKind.THEN)).isNull(); // should not be part of the condition
		assertThat(ifStatement.findDescendantToken(SyntaxKind.THEN)).isNotNull(); // but be part of the if statement itself
		assertThat(ifStatement.body().statements()).hasSize(1);
	}

	@Test
	void allowThenAfterMaskInIf()
	{
		assertParsesSingleStatement("""
			IF #TEST = MASK(A...) THEN
			    IGNORE
			END-IF
			""", IIfStatementNode.class);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"BREAK, #TEST", "BREAK OF, #TEST", "BREAK #TEST, THEN", "BREAK OF #TEST, THEN", "BREAK OF #TEST /3/, THEN"
		}
	)

	void parseIfBreakStatements(String keywords, String variables)
	{
		var source = """
			IF %s %s
				IGNORE
			END-IF
			""".formatted(keywords, variables);

		var ifStatement = assertParsesSingleStatement(source, IIfBreakNode.class);
		assertThat(ifStatement.body().statements()).hasSize(1);
		var i = source.split("[ |\\/]").length + 2;
		assertThat(ifStatement.descendants()).hasSize(i);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"SELECTION, #A #B", "SELECTION NOT, #A #B", "SELECTION NOT UNIQUE, #A #B", "SELECTION NOT UNIQUE IN FIELDS #A #B, THEN",
		}
	)

	void parseIfSeelctionNotUniqueStatements(String keywords, String variables)
	{
		var source = """
			IF %s %s
				IGNORE
			END-IF
			""".formatted(keywords, variables);

		assertParsesSingleStatement(source, IIfSelectionNode.class);
	}

	@Test
	void parseForColonEqualsToStatements()
	{
		var forLoopNode = assertParsesSingleStatement("""
			FOR #I := 1 TO 10
			    IGNORE
			END-FOR
			""", IForLoopNode.class);

		assertThat(forLoopNode.body().statements()).hasSize(1);
		assertThat(forLoopNode.descendants()).hasSize(8);
	}

	@Test
	void parseAMinimalForLoop()
	{
		var forLoopNode = assertParsesSingleStatement("""
			FOR #I 1 10
			    IGNORE
			END-FOR
			""", IForLoopNode.class);

		assertThat(forLoopNode.body().statements()).hasSize(1);
	}

	@Test
	void notComplainAboutMissingEndForWhenSortIsFollowing()
	{
		assertParsesWithoutDiagnostics("""
			FOR #I := 1 TO 10
			    FOR #J := 1 TO 20
			        WRITE #I #J
			END-ALL""");
	}

	@Test
	void parseForEqToStatements()
	{
		var forLoopNode = assertParsesSingleStatement("""
			FOR #I EQ 1 TO 10
			    IGNORE
			END-FOR
			""", IForLoopNode.class);

		assertThat(forLoopNode.body().statements()).hasSize(1);
		assertThat(forLoopNode.descendants()).hasSize(8);
	}

	@Test
	void parseForFromToStatementsStep()
	{
		var forLoopNode = assertParsesSingleStatement("""
			FOR #I FROM 5 TO 10 STEP 2
			    IGNORE
			END-FOR
			""", IForLoopNode.class);

		assertThat(forLoopNode.body().statements()).hasSize(1);
		assertThat(forLoopNode.descendants()).hasSize(10);
	}

	@Test
	void parseForWithoutFromOrEqOrColonEqualsToStatementsStep()
	{
		var forLoopNode = assertParsesSingleStatement("""
			FOR #I 5 TO 10 STEP 2
			    IGNORE
			END-FOR
			""", IForLoopNode.class);

		assertThat(forLoopNode.body().statements()).hasSize(1);
		assertThat(forLoopNode.descendants()).hasSize(9);
	}

	@Test
	void allowSystemFunctionsAsUpperBound()
	{
		var forLoopNode = assertParsesSingleStatement("""
			FOR #I FROM 5 TO *OCC(#ARR)
			    IGNORE
			END-FOR
			""", IForLoopNode.class);

		var upperBound = assertNodeType(forLoopNode.upperBound(), ISystemFunctionNode.class);
		assertThat(upperBound.systemFunction()).isEqualTo(SyntaxKind.OCC);
		assertThat(upperBound.parameter().first()).isInstanceOf(IVariableReferenceNode.class);
		assertThat(forLoopNode.body().statements()).hasSize(1);
		assertThat(forLoopNode.descendants()).hasSize(8);
	}

	@Test
	void rudimentaryParseForFromThruStatementsStep()
	{
		var forLoopNode = assertParsesSingleStatement("""
			FOR #I FROM 5 THRU 10 STEP 5
			    IGNORE
			END-FOR
			""", IForLoopNode.class);

		assertThat(forLoopNode.body().statements()).hasSize(1);
		assertThat(forLoopNode.descendants()).hasSize(10);
	}

	@Test
	void reportADiagnosticForNotClosedIfStatements()
	{
		assertDiagnostic("""
			IF 5 > 2
			    IGNORE
			""", ParserError.UNCLOSED_STATEMENT);
	}

	@Test
	void parseIfNoRecord()
	{
		var noRecNode = assertParsesSingleStatement("""
			IF NO RECORDS FOUND
			    IGNORE
			END-NOREC
			""", IIfNoRecordNode.class);

		assertThat(noRecNode.body().statements()).hasSize(1);
		assertThat(noRecNode.descendants()).hasSize(6);
	}

	@Test
	void parseIfNoRecordWithoutOptionalTokens()
	{
		var noRecNode = assertParsesSingleStatement("""
			IF NO
			    IGNORE
			END-NOREC
			""", IIfNoRecordNode.class);

		assertThat(noRecNode.body().statements()).hasSize(1);
		assertThat(noRecNode.descendants()).hasSize(4);
	}

	@Test
	void parseIfNoRecordWithoutFoundToken()
	{
		var noRecNode = assertParsesSingleStatement("""
			IF NO RECORDS
			    IGNORE
			END-NOREC
			""", IIfNoRecordNode.class);

		assertThat(noRecNode.body().statements()).hasSize(1);
		assertThat(noRecNode.descendants()).hasSize(5);
	}

	@Test
	void parseSetKey()
	{
		var setKey = assertParsesSingleStatement("""
			SET KEY PF1=HELP PF2=PROGRAM
			""", ISetKeyNode.class);

		assertThat(setKey.descendants()).hasSize(8);
	}

	@Test
	void parseSetKeyNamedOff()
	{
		assertParsesSingleStatement("""
			SET KEY NAMED OFF
			""", ISetKeyNode.class);
	}

	@Test
	void parseSetKeyNamed()
	{
		assertParsesSingleStatement("""
			SET KEY PF1 NAMED '-'
			""", ISetKeyNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SET KEY ALL", "SET KEY PF2", "SET KEY PF2=PGM", "SET KEY OFF", "SET KEY ON", "SET KEY PF2=OFF", "SET KEY PF2=ON", "SET KEY PA2=ON", "SET KEY PF4='SAVE'", "SET KEY PF4=#XYX", "SET KEY PF6='LIST MAP *'", "SET KEY PF2='%%'", "SET KEY PF9=' '", "SET KEY PF12=DATA 'YES'", "SET KEY PF4=COMMAND OFF", "SET KEY PF4=COMMAND ON", "SET KEY COMMAND OFF", "SET KEY COMMAND ON", "SET KEY PF1=HELP",
		"SET KEY PF10=DISABLED", "SET KEY ENTR NAMED 'EXEC'", "SET KEY PF3 NAMED 'EXIT'", "SET KEY PF3 NAMED OFF", "SET KEY NAMED OFF", "SET KEY PF4='AP1' NAMED 'APPL1'"
	})
	void parseSetKeyExamples(String statement)
	{
		assertParsesSingleStatement(statement, ISetKeyNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SET KEY CLR", "SET KEY CLR=OFF", "SET KEY CLR=COMMAND ON", "SET KEY CLR=DISABLED", "SET KEY CLR=ON NAMED 'Clear'", "SET KEY CLR=PGM NAMED OFF",
		"SET KEY DYNAMIC #XYZ", "SET KEY DYNAMIC #XYZ = HELP", "SET KEY DYNAMIC #XYZ = DATA 'Hello' NAMED 'Dynam'"
	})
	void parseSetKeyClrAndDynamicExamples(String statement)
	{
		assertParsesSingleStatement(statement, ISetKeyNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SET KEY ENTR NAMED OFF CLR=DATA 'CLR' NAMED OFF",
		"SET KEY PF1=HELP NAMED 'Help' PF3 NAMED 'Exit' PF5=PGM NAMED 'Update' PF12 NAMED 'Return'",
		"SET KEY ENTR NAMED 'Exec' PF1=HELP NAMED 'Help' PF3 NAMED 'Exit' PF5=PGM NAMED 'Update' PF12 NAMED 'Return'",
	})
	void parseSetKeyWithMultipleKeys(String statement)
	{
		assertParsesSingleStatement(statement, ISetKeyNode.class);
	}

	@Test
	void notMistakeAnAssignmentAsSetKeyOperand()
	{
		var statementList = assertParsesWithoutDiagnostics("""
			SET KEY PF5 NAMED 'PHON'
			#VAR := 'Hi'
			""");

		assertThat(statementList).hasSize(2);
		assertNodeType(statementList.statements().get(0), ISetKeyNode.class);
		assertNodeType(statementList.statements().get(1), IAssignmentStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"PF100", "PA4", "CLRS", "PF011", "PA01", "PF25", "DYNAMIC #XYZ = COMMAND ENTR NAMED OFF",
	})
	void showADiagnosticForInvalidTokensInSetKey(String length)
	{
		assertDiagnostic(
			"""
				SET KEY %s
				""".formatted(length),
			ParserError.UNEXPECTED_TOKEN
		);
	}

	@Test
	void parseFind()
	{
		var findStatement = assertParsesSingleStatement("""
			FIND THE-VIEW WITH THE-DESCRIPTOR = 'Asd'
			    IGNORE
			END-FIND
			""", IFindNode.class);

		assertThat(findStatement.viewReference()).isNotNull();
		assertThat(findStatement.descendants()).anyMatch(n -> n instanceof IDescriptorNode);
		assertThat(findStatement.descendants()).hasSize(8);
	}

	@Test
	void parseFindWithNumberLimit()
	{
		var findStatement = assertParsesSingleStatement("""
			FIND (5) THE-VIEW WITH THE-DESCRIPTOR = 'Asd'
			IGNORE
			END-FIND
			""", IFindNode.class);

		assertThat(findStatement.viewReference()).isNotNull();
		assertThat(findStatement.descendants()).anyMatch(n -> n instanceof IDescriptorNode);
		assertThat(findStatement.descendants()).hasSize(11);
	}

	@Test
	void parseFindWithoutBody()
	{
		var findStatement = assertParsesSingleStatement("""
			FIND FIRST THE-VIEW WITH THE-DESCRIPTOR = 'Asd'
			""", IFindNode.class);

		assertThat(findStatement.viewReference()).isNotNull();
		assertThat(findStatement.descendants()).anyMatch(n -> n instanceof IDescriptorNode);
		assertThat(findStatement.descendants()).hasSize(5);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"ON", "OFF", "OF 1000", "2000", "OF N", "NUMBER"
	})
	void parseFindWithMultiFetch(String multifetch)
	{
		assertParsesSingleStatement("""
			FIND MULTI-FETCH %s THE-VIEW WITH THE-DESCRIPTOR = 'Asd'
			IGNORE
			END-FIND""".formatted(multifetch), IFindNode.class);
	}

	@Test
	void parseResetStatements()
	{
		var reset = assertParsesSingleStatement("RESET #THEVAR", IResetStatementNode.class);
		assertThat(reset.operands()).hasSize(1);
	}

	@Test
	void parseResetInitialStatements()
	{
		var reset = assertParsesSingleStatement("RESET INITIAL #THEVAR #THEOTHERVAR", IResetStatementNode.class);
		assertThat(reset.operands()).hasSize(2);
	}

	@Test
	void rudimentaryParseMasks()
	{
		// TODO(expressions): Implement proper expressions
		var mask = assertParsesSingleStatement("MASK (DDMMYYYY)", SyntheticTokenStatementNode.class);
		assertThat(mask).isNotNull();
	}

	@Test
	void parseASimpleDefinePrinter()
	{
		var printer = assertParsesSingleStatement("DEFINE PRINTER(2)", IDefinePrinterNode.class);
		assertThat(printer.printerNumber()).isEqualTo(2);
		assertThat(printer.printerName()).isEmpty();
	}

	@Test
	void parseADefinePrinterWithPrinterName()
	{
		var printer = assertParsesSingleStatement("DEFINE PRINTER(MYPRINTER=5)", IDefinePrinterNode.class);
		assertThat(printer.printerNumber()).isEqualTo(5);
		assertThat(printer.printerName()).map(SyntaxToken::symbolName).hasValue("MYPRINTER");
	}

	@Test
	void parseADefinePrinterWithOutputString()
	{
		var printer = assertParsesSingleStatement("DEFINE PRINTER(5) OUTPUT 'LPT1'", IDefinePrinterNode.class);
		assertThat(printer.output()).hasValueSatisfying(n -> assertThat(n).isInstanceOf(ILiteralNode.class));
		assertThat(printer.output()).map(ILiteralNode.class::cast).map(ILiteralNode::token).map(SyntaxToken::stringValue).hasValue("LPT1");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"DUMMY", "INFOLINE", "SOURCE", "NOM", "ANYTHING GOES!"
	})
	void parseADefinePrinterWithAllowedBuiltInOutputNames(String output)
	{
		var printer = assertParsesSingleStatement("DEFINE PRINTER (4) OUTPUT '%s'".formatted(output), IDefinePrinterNode.class);
		assertThat(printer.output()).hasValueSatisfying(n -> assertThat(n).isInstanceOf(ILiteralNode.class));
		assertThat(printer.output()).map(ILiteralNode.class::cast).map(ILiteralNode::token).map(SyntaxToken::stringValue).hasValue(output);
	}

	@Test
	void reportADiagnosticIfDefinePrinterHasAnInvalidOutputFormat()
	{
		assertDiagnostic("DEFINE PRINTER (2) OUTPUT 10", ParserError.UNEXPECTED_TOKEN);
	}

	@Test
	void reportADiagnosticIfDefinePrinterHasAnInvalidTokenKind()
	{
		assertDiagnostic("DEFINE PRINTER (2) OUTPUT 5", ParserError.UNEXPECTED_TOKEN);
	}

	@Test
	void parseADefinePrinterWithOutputVariable()
	{
		var printer = assertParsesSingleStatement("DEFINE PRINTER(5) OUTPUT #MYPRINTER", IDefinePrinterNode.class);
		assertThat(printer.output()).hasValueSatisfying(n -> assertThat(n).isInstanceOf(ISymbolReferenceNode.class));
		assertThat(printer.output()).map(ISymbolReferenceNode.class::cast).map(ISymbolReferenceNode::referencingToken).map(SyntaxToken::symbolName).hasValue("#MYPRINTER");
	}

	@Test
	void parseADefinePrinterWithProfile()
	{
		assertParsesWithoutDiagnostics("DEFINE PRINTER(8) PROFILE 'MYPR'");
	}

	@Test
	void parseADefinePrinterWithCopies()
	{
		assertParsesWithoutDiagnostics("DEFINE PRINTER(8) COPIES 10");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"HOLD", "KEEP", "DEL"
	})
	void parseADefinePrinterWithDisp(String disp)
	{
		assertParsesWithoutDiagnostics("DEFINE PRINTER(8) DISP %s".formatted(disp));
	}

	@Test
	void reportADiagnosticIfThePrinterProfileIsLongerThan8()
	{
		assertDiagnostic("DEFINE PRINTER(8) PROFILE 'MYLONGPROFILE'", ParserError.INVALID_LENGTH_FOR_LITERAL);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"PROFILE 'PROF' DISP KEEP COPIES 5", "COPIES 3 PROFILE 'PROF' DISP DEL", "DISP HOLD COPES 2 DISP HOLD"
	})
	void parseADefinePrinterWithAnyOrderOfDispProfileAndCopies(String order)
	{
		assertParsesWithoutDiagnostics("DEFINE PRINTER (2) %s".formatted(order));
	}

	@Test
	void parseClosePrinterWithPrinterNumber()
	{
		var closePrinter = assertParsesSingleStatement("CLOSE PRINTER(5)", IClosePrinterNode.class);
		assertThat(closePrinter.printer().kind()).isEqualTo(SyntaxKind.NUMBER_LITERAL);
		assertThat(closePrinter.printer().intValue()).isEqualTo(5);
	}

	@Test
	void parseClosePrinterWithPrinterName()
	{
		var closePrinter = assertParsesSingleStatement("CLOSE PRINTER (PR5)", IClosePrinterNode.class);
		assertThat(closePrinter.printer().kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(closePrinter.printer().symbolName()).isEqualTo("PR5");
	}

	@Test
	void rudimentaryParseDefineWindow()
	{
		var window = assertParsesSingleStatement("DEFINE WINDOW MAIN", IDefineWindowNode.class);
		assertThat(window.name().symbolName()).isEqualTo("MAIN");
	}

	@Test
	void parseFormat()
	{
		var statementList = assertParsesWithoutDiagnostics("""
			FORMAT (PR15) AD=IO AL=5 CD=BL DF=S DL=29 EM=YYYY-MM-DD ES=ON FC= FL=2 GC=a HC=L HW=OFF IC= IP=ON IS=OFF LC=- LS=5 MC=3 MP=2 MS=ON NL=20 PC=3 PM=I PS=40 SF=3 SG=0 TC= UC=
			ZP=ON""");
		assertThat(statementList.statements().size()).isEqualTo(1);
	}

	@Test
	void parseFormatWithPrinterNumber()
	{
		var statementList = assertParsesWithoutDiagnostics("FORMAT (5) LS=5 ZP=ON");
		assertThat(statementList.statements().size()).isEqualTo(1);
	}

	@Test
	void parseFormatIfNextLineStartsWithStatement()
	{
		// If a format thingy is empty, the next line should still properly be identified as the next statement
		var statementList = assertParsesWithoutDiagnostics("""
			FORMAT (PR15) AD=IO AL=5 CD=BL DF=S DL=29 EM=YYYY-MM-DD ES=ON FC= FL=2 GC=a HC=L HW=OFF IC= IP=ON IS=OFF LC=- LS=5 MC=3 MP=2 MS=ON NL=20 PC=3 PM=I PS=40 SF=3 SG=0 TC= UC=
			ZP=
			DEFINE PRINTER (5)""");

		assertThat(statementList.statements().size()).isEqualTo(2);
		assertThat(statementList.statements().get(0)).isInstanceOf(IFormatNode.class);
		assertThat(statementList.statements().get(1)).isInstanceOf(IDefinePrinterNode.class);
	}

	@Test
	void parseWriteWithReportSpecification()
	{
		var write = assertParsesSingleStatement("WRITE (REP1)", IWriteNode.class);
		assertThat(write.reportSpecification()).map(SyntaxToken::source).hasValue("REP1");
	}

	@Test
	void parseWriteWithAttributeDefinition()
	{
		var write = assertParsesSingleStatement("WRITE (AD=UL AL=17 NL=8)", IWriteNode.class);
		assertThat(write.descendants()).hasSize(10);
	}

	@Test
	void parseWriteWithNoTitleAndNoHdr()
	{
		var write = assertParsesSingleStatement("WRITE NOTITLE NOHDR", IWriteNode.class);
		assertThat(write.findDescendantToken(SyntaxKind.NOTITLE)).isNotNull();
		assertThat(write.findDescendantToken(SyntaxKind.NOHDR)).isNotNull();
	}

	@Test
	void parseDisplay()
	{
		var display = assertParsesSingleStatement("DISPLAY", IDisplayNode.class);
		assertThat(display.descendants()).hasSize(1);
	}

	@Test
	void parseDisplayWithReportSpecification()
	{
		var display = assertParsesSingleStatement("DISPLAY (PR2)", IDisplayNode.class);
		assertThat(display.reportSpecification()).isPresent();
		assertThat(display.reportSpecification().get().symbolName()).isEqualTo("PR2");
		assertThat(display.descendants()).hasSize(4);
	}

	@Test
	void parseASimpleExamineReplace()
	{
		var examine = assertParsesSingleStatement("EXAMINE #VAR 'a' REPLACE 'b'", IExamineNode.class);
		assertThat(examine.examined()).isNotNull();
		assertThat(assertNodeType(examine.examined(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
	}

	@Test
	void parseAnExamineWithSubstring()
	{
		var examine = assertParsesSingleStatement("EXAMINE SUBSTR(#VAR, 1, 5) FOR 'a'", IExamineNode.class);
		assertThat(examine.examined()).isNotNull();
		var substringOperand = assertNodeType(examine.examined(), ISubstringOperandNode.class);
		assertThat(assertNodeType(substringOperand.operand(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");
		assertThat(assertNodeType(substringOperand.startPosition().orElseThrow(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(substringOperand.length().orElseThrow(), ILiteralNode.class).token().intValue()).isEqualTo(5);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SUBSTRING(#VAR, 1)", "SUBSTRING(#VAR, ,10)",
	})
	void parseSubstringWithOmittedParameter(String substring)
	{
		assertParsesWithoutDiagnostics("EXAMINE %s FOR 'Hi'".formatted(substring));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"DELIMITER", "DELIMITERS", "DELIMITER ' '", "DELIMITERS ' '", "DELIMITER #DEL", "DELIMITERS #DEL",
	})
	void parseAnExamineWithDelimiters(String delimiter)
	{
		assertParsesSingleStatement("EXAMINE #VAR FOR #VAR2 WITH %s GIVING INDEX #INDEX".formatted(delimiter), IExamineNode.class);
	}

	@Test
	void parseAComplexExamineReplace()
	{
		var examine = assertParsesSingleStatement("EXAMINE DIRECTION FORWARD FULL VALUE OF #DOC STARTING FROM POSITION 7 ENDING AT POSITION 10 FOR FULL VALUE OF PATTERN #HTML(*) WITH DELIMITERS ',' AND REPLACE FIRST WITH FULL VALUE OF #TAB(*) ", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(32);
	}

	@Test
	void parseAComplexExamineDelete()
	{
		var examine = assertParsesSingleStatement("EXAMINE DIRECTION FORWARD FULL VALUE OF #DOC STARTING FROM POSITION 7 ENDING AT POSITION 10 FOR FULL VALUE OF PATTERN #HTML(*) WITH DELIMITERS ',' AND DELETE FIRST", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(27);
	}

	@Test
	void parseAComplexExamineDeleteGiving()
	{
		var examine = assertParsesSingleStatement("EXAMINE DIRECTION FORWARD FULL VALUE OF #DOC STARTING FROM POSITION 7 ENDING AT POSITION 10 FOR FULL VALUE OF PATTERN #HTML(*) WITH DELIMITERS ',' AND DELETE FIRST GIVING INDEX IN #ASD #EFG #HIJ", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(33);
	}

	@Test
	void parseAExamineWithMultipleGivings()
	{
		var examine = assertParsesSingleStatement("EXAMINE #DOC FOR FULL VALUE OF 'a' GIVING NUMBER #NUM GIVING POSITION #POS GIVING LENGTH #LEN GIVING INDEX #INDEX", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(19);
	}

	@Test
	void parseAnExamineTranslateStatement()
	{
		var examine = assertParsesSingleStatement("EXAMINE #ASD AND TRANSLATE INTO UPPER CASE", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(7);
	}

	@Test
	void parseAnExamineTranslateUsingStatement()
	{
		var examine = assertParsesSingleStatement("EXAMINE #ASD AND TRANSLATE USING INVERTED #EFG", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(7);
	}

	@Test
	void parseNewPage()
	{
		var newPage = assertParsesSingleStatement("NEWPAGE EVEN IF TOP OF PAGE WITH TITLE 'The Title'", INewPageNode.class);
		assertThat(newPage.descendants()).hasSize(9);
	}

	@Test
	void parseNewPageWithoutTitle()
	{
		var newPage = assertParsesSingleStatement("NEWPAGE WHEN LESS THAN 10 LINES LEFT", INewPageNode.class);
		assertThat(newPage.descendants()).hasSize(7);
	}

	@Test
	void parseNewPageWithNumericReportSpecification()
	{
		var newPage = assertParsesSingleStatement("NEWPAGE(5) WHEN LESS 10 TITLE 'The Title'", INewPageNode.class);
		assertThat(newPage.reportSpecification()).map(SyntaxToken::intValue).hasValue(5);
		assertThat(newPage.descendants()).hasSize(9);
	}

	@Test
	void parseNewPageWithLogicalNameInReportSpecification()
	{
		var newPage = assertParsesSingleStatement("NEWPAGE(THEPRINT) IF LESS THAN #VAR LINES LEFT", INewPageNode.class);
		assertThat(newPage.reportSpecification()).map(SyntaxToken::symbolName).hasValue("THEPRINT");
		assertThat(newPage.descendants()).hasSize(10);
	}

	@Test
	void parseNewPageWithFollowingIfStatementNotThinkingTheIfBelongsToTheNewPage()
	{
		var list = assertParsesWithoutDiagnostics("""
			NEWPAGE (PR5)
			IF TRUE
			IGNORE
			END-IF""");
		assertThat(list.statements()).hasSize(2);
	}

	@Test
	void parseAtEndOfPage()
	{
		var endOfPage = assertParsesSingleStatement("""
			AT END OF PAGE (PRNT)
			IGNORE
			END-ENDPAGE
			""", IEndOfPageNode.class);

		assertThat(endOfPage.reportSpecification()).map(SyntaxToken::symbolName).hasValue("PRNT");
		assertThat(endOfPage.body().statements()).hasSize(1);
	}

	@Test
	void parseEndPage()
	{
		var endOfPage = assertParsesSingleStatement("""
			END PAGE (5)
			IGNORE
			END-ENDPAGE
			""", IEndOfPageNode.class);

		assertThat(endOfPage.reportSpecification()).map(SyntaxToken::intValue).hasValue(5);
		assertThat(endOfPage.body().statements()).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"AT END OF PAGE", "END PAGE", "END OF PAGE", "AT END PAGE"
	})
	void parseMultipleHeaderOptionsForEndOfPage(String header)
	{
		assertParsesWithoutDiagnostics("""
				%s
				IGNORE
				END-ENDPAGE
			""".formatted(header));
	}

	@Test
	void parseAtTopOfPage()
	{
		var topOfPage = assertParsesSingleStatement("""
			AT TOP OF PAGE (PRNT)
			IGNORE
			END-TOPPAGE
			""", ITopOfPageNode.class);

		assertThat(topOfPage.reportSpecification()).map(SyntaxToken::symbolName).hasValue("PRNT");
		assertThat(topOfPage.body().statements()).hasSize(1);
	}

	@Test
	void parseTopPage()
	{
		var topOfPage = assertParsesSingleStatement("""
			TOP PAGE (5)
			IGNORE
			END-TOPPAGE
			""", ITopOfPageNode.class);

		assertThat(topOfPage.reportSpecification()).map(SyntaxToken::intValue).hasValue(5);
		assertThat(topOfPage.body().statements()).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"AT TOP OF PAGE", "TOP PAGE", "TOP OF PAGE", "AT TOP PAGE"
	})
	void parseMultipleHeaderOptionsForTopOfPage(String header)
	{
		assertParsesWithoutDiagnostics("""
			%s
			IGNORE
			END-TOPPAGE
			""".formatted(header));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"AT START OF DATA (R1.)", "START DATA", "START OF DATA", "AT START DATA (R5.)"
	})
	void parseAtStartOfData(String header)
	{
		var startOfData = assertParsesSingleStatement("""
			%s
			IGNORE
			END-START
			""".formatted(header), IStartOfDataNode.class);

		assertThat(startOfData.body().statements()).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"AT END OF DATA (R1.)", "END DATA", "END OF DATA", "AT END DATA (R5.)"
	})
	void parseAtEndOfData(String header)
	{
		var endOfData = assertParsesSingleStatement("""
			%s
			IGNORE
			END-ENDDATA
			""".formatted(header), IEndOfDataNode.class);

		assertThat(endOfData.body().statements()).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"AT BREAK (RD.) OF #VAR /5/", "BREAK #VARIABLE", "AT BREAK #VAR", "BREAK (R1.) OF #VAR /10/"
	})
	void parseAtBreakOf(String header)
	{
		var breakOf = assertParsesSingleStatement("""
			%s
			IGNORE
			END-BREAK
			""".formatted(header), IBreakOfNode.class);

		assertThat(breakOf.body().statements()).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"EJECT ON (0)", "EJECT OFF (PRNT)", "EJECT OFF", "EJECT ON", "EJECT (PRNT)", "EJECT (5)", "EJECT", "EJECT IF LESS THAN 10 LINES LEFT", "EJECT WHEN LESS THAN #VAR LINES LEFT", "EJECT (10) LESS #VAR", "EJECT (10) WHEN LESS #VAR", "EJECT (10) WHEN LESS THAN #VAR", "EJECT (10) WHEN LESS THAN #VAR LEFT", "EJECT (PRNT) IF LESS THAN 10 LINES LEFT",
	})
	void parseEject(String eject)
	{
		var statements = assertParsesWithoutDiagnostics(eject);
		assertThat(statements.statements()).hasSize(1);
		assertThat(statements.statements().get(0)).isInstanceOf(IEjectNode.class);
	}

	@Test
	void parseEjectWithPrinterReference()
	{
		var eject = assertParsesSingleStatement("EJECT (PRNT)", IEjectNode.class);
		assertThat(eject.reportSpecification()).map(SyntaxToken::symbolName).hasValue("PRNT");
	}

	@Test
	void parseEjectWithNumericPrinterReference()
	{
		var eject = assertParsesSingleStatement("EJECT (5)", IEjectNode.class);
		assertThat(eject.reportSpecification()).map(SyntaxToken::intValue).hasValue(5);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"ESCAPE TOP REPOSITION", "ESCAPE TOP", "ESCAPE BOTTOM IMMEDIATE", "ESCAPE BOTTOM (RD.) IMMEDIATE", "ESCAPE BOTTOM", "ESCAPE BOTTOM (R1.)", "ESCAPE ROUTINE IMMEDIATE", "ESCAPE ROUTINE", "ESCAPE MODULE IMMEDIATE", "ESCAPE MODULE"
	})
	void parseEscapes(String escape)
	{
		assertParsesSingleStatement(escape, IEscapeNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"TOP", "BOTTOM", "ROUTINE", "MODULE"
	})
	void parseEscapeDirectionOfEscapeNode(String direction)
	{
		var escape = assertParsesSingleStatement("ESCAPE %s".formatted(direction), IEscapeNode.class);
		assertThat(escape.escapeDirection().name()).isEqualTo(direction);
	}

	@Test
	void parseEscapeImmediate()
	{
		var escape = assertParsesSingleStatement("ESCAPE ROUTINE IMMEDIATE", IEscapeNode.class);
		assertThat(escape.isImmediate()).isTrue();
	}

	@Test
	void parseEscapeReposition()
	{
		var escape = assertParsesSingleStatement("ESCAPE TOP REPOSITION", IEscapeNode.class);
		assertThat(escape.isReposition()).isTrue();
	}

	@Test
	void parseEscapeLabel()
	{
		var escape = assertParsesSingleStatement("ESCAPE BOTTOM (RD.)", IEscapeNode.class);
		assertThat(escape.label()).map(SyntaxToken::symbolName).hasValue("RD.");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"STACK TOP COMMAND 'ASD' #VAR 'ASDF'", "STACK 'MOD'", "STACK DATA FORMATTED #DATA1 #DATA2 #DATA3", "STACK TOP DATA #VAR1 #VAR2", "STACK TOP FORMATTED #VAR1 #VAR2"
	})
	void parseStack(String stack)
	{
		var statementList = assertParsesWithoutDiagnostics(stack);
		assertThat(statementList.statements()).hasSize(1);
		assertThat(statementList.statements().get(0)).isInstanceOf(IStackNode.class);
	}

	@Test
	void parseStackWithManyOperands()
	{
		var statementList = assertParsesWithoutDiagnostics("""
			STACK TOP COMMAND #ASD #ASDF 'ASD' #ASDFG
			IGNORE
			""");
		assertThat(statementList.statements()).hasSize(2);
		assertThat(statementList.statements().get(0)).isInstanceOf(IStackNode.class);
		assertThat(statementList.statements().get(1)).isInstanceOf(IIgnoreNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"BEFORE BREAK PROCESSING", "BEFORE BREAK", "BEFORE", "BEFORE PROCESSING"
	})
	void parseBeforeBreakProcessing(String header)
	{
		var beforeBreak = assertParsesSingleStatement("""
			%s
			IGNORE
			END-BEFORE
			""".formatted(header), IBeforeBreakNode.class);

		assertThat(beforeBreak.body().statements()).hasSize(1);
		assertThat(beforeBreak.body().statements().first()).isInstanceOf(IIgnoreNode.class);
	}

	@Test
	void parseASimpleHistogram()
	{
		var histogram = assertParsesSingleStatement("""
			HISTOGRAM THE-VIEW THE-DESC STARTING FROM 'M'
			IGNORE
			END-HISTOGRAM""", IHistogramNode.class);
		assertThat(histogram.view().token().symbolName()).isEqualTo("THE-VIEW");
		assertThat(histogram.descriptor().symbolName()).isEqualTo("THE-DESC");
	}

	@Test
	void parseAHistogramWithNumber()
	{
		assertParsesSingleStatement("""
			HISTOGRAM(1) THE-VIEW THE-DESC
			IGNORE
			END-HISTOGRAM""", IHistogramNode.class);
	}

	@Test
	void parseHistogramWithAll()
	{
		assertParsesSingleStatement("""
			HISTOGRAM ALL THE-VIEW THE-DESC
			IGNORE
			END-HISTOGRAM""", IHistogramNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"ON", "OFF", "OF 1000", "2000", "OF N", "NUMBER"
	})
	void parseHistogramWithMultiFetch(String multifetch)
	{
		assertParsesSingleStatement("""
			HISTOGRAM ALL MULTI-FETCH %s THE-VIEW THE-DESC
			IGNORE
			END-HISTOGRAM""".formatted(multifetch), IHistogramNode.class);
	}

	@Test
	void parseHistogramWithStartingFrom()
	{
		assertParsesSingleStatement("""
			HISTOGRAM IN FILE THE-VIEW VALUE FOR FIELD THE-DESC STARTING FROM VALUES #ABC ENDING AT #DEF
			IGNORE
			END-HISTOGRAM""", IHistogramNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"IN ASCENDING SEQUENCE", "IN DESCENDING SEQUENCE", "IN ASC", "IN DESC", "ASC", "DESC", "IN VARIABLE #VAR2", "DYNAMIC #VAR2"
	})
	void parseHistogramWithSorting(String sorting)
	{
		assertParsesSingleStatement("""
			HISTOGRAM THE-VIEW %s THE-DESC
			IGNORE
			END-HISTOGRAM""".formatted(sorting), IHistogramNode.class);
	}

	@Test
	void parseSelectWithNoBody()
	{
		assertParsesSingleStatement("""
			SELECT * FROM DB2_TABLE WHERE COLUMN = 'search'
			END-SELECT""", ISelectNode.class);
	}

	@Test
	void parseSelectWithBody()
	{
		var select = assertParsesSingleStatement("""
			SELECT * FROM DB2_TABLE WHERE COLUMN = 'search'
			ADD 1 TO #INDEX
			END-SELECT""", ISelectNode.class);
		assertThat(select.body().statements()).hasSize(1);
		assertThat(select.body().statements().first()).isInstanceOf(IAddStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"10", "5 LINES", "#VAR", "#VAR LINES"
	})
	void parseSkipStatement(String skipOperands)
	{
		var skip = assertParsesSingleStatement("SKIP %s".formatted(skipOperands), ISkipStatementNode.class);
		assertThat(skip.descendants()).hasSize(1 + skipOperands.split(" ").length);
	}

	@Test
	void parseNumberOfLinesOfSkipStatement()
	{
		var skip = assertParsesSingleStatement("SKIP 5 LINES", ISkipStatementNode.class);
		assertThat(assertNodeType(skip.toSkip(), ILiteralNode.class).token().intValue()).isEqualTo(5);
	}

	@Test
	void parseVariableAsNumberOfLinesOfSkipStatement()
	{
		var skip = assertParsesSingleStatement("SKIP #VAR LINES", ISkipStatementNode.class);
		assertThat(assertNodeType(skip.toSkip(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#VAR");
	}

	@Test
	void parseSkipStatementWithReportSpecification()
	{
		var skip = assertParsesSingleStatement("SKIP (PR2) 5 LINES", ISkipStatementNode.class);
		assertThat(skip.reportSpecification()).isPresent();
		assertThat(skip.reportSpecification().get().reportSpecification().symbolName()).isEqualTo("PR2");
	}

	@Test
	void parseDecideForCondition()
	{
		var decide = assertParsesSingleStatement("""
				DECIDE FOR CONDITION
				WHEN 5 < 2
					IGNORE
				WHEN ANY
					IGNORE
				WHEN ALL
					IGNORE
				WHEN NONE
					IGNORE
				END-DECIDE
			""", IDecideForConditionNode.class);

		assertThat(decide.whenNone().statements()).hasSize(1);
		assertNodeType(decide.whenNone().statements().first(), IIgnoreNode.class);

		assertThat(decide.whenAny()).isPresent();
		assertThat(decide.whenAny().get().statements()).hasSize(1);
		assertNodeType(decide.whenNone().statements().first(), IIgnoreNode.class);

		assertThat(decide.whenAll()).isPresent();
		assertThat(decide.whenAll().get().statements()).hasSize(1);
		assertNodeType(decide.whenNone().statements().first(), IIgnoreNode.class);

		assertThat(decide.branches()).hasSize(1);
		var condition = assertNodeType(decide.branches().first().criteria(), IConditionNode.class);
		var criteria = assertNodeType(condition.criteria(), IRelationalCriteriaNode.class);
		assertThat(assertNodeType(criteria.left(), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(assertNodeType(criteria.right(), ILiteralNode.class).token().intValue()).isEqualTo(2);
		assertThat(decide.branches().first().body().statements()).hasSize(1);
		assertThat(decide.branches().first().body().statements().first()).isInstanceOf(IIgnoreNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"EVERY", "FIRST"
	})
	void parseDecideForConditionWithEveryAndFirst(String permutation)
	{
		assertParsesSingleStatement("""
				DECIDE FOR %s CONDITION
				WHEN 5 < 2
					IGNORE
				WHEN NONE
					IGNORE
				END-DECIDE
			""".formatted(permutation), IDecideForConditionNode.class);
	}

	@Test
	void parseDecideForWithComplexConditions()
	{
		var decide = assertParsesSingleStatement("""
				DECIDE FOR CONDITION
				WHEN (#VAR IS (N4))
					IGNORE
				WHEN #VAR = 'Hello' AND #VAR2 = 2
					IGNORE
				WHEN (#VAR = 'Hello' OR *OCC(#ARR) = 5)
					IGNORE
				WHEN NONE
					IGNORE
				END-DECIDE
			""", IDecideForConditionNode.class);

		assertThat(decide.branches()).hasSize(3);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SIZE OF DYNAMIC VARIABLE", "DYNAMIC", "DYNAMIC VARIABLE", "SIZE OF DYNAMIC",
	})
	void parseResizeDynamic(String combination)
	{
		// TODO(type-check): Has to be dynamic typed
		var resize = assertParsesSingleStatement("RESIZE %s #VAR TO 20".formatted(combination), IResizeDynamicNode.class);
		assertThat(resize.variableToResize().referencingToken().symbolName()).isEqualTo("#VAR");
		assertThat(resize.sizeToResizeTo()).isEqualTo(20);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"AND RESET OCCURRENCES OF", "AND RESET", "OCCURRENCES OF",
	})
	void parseResizeArray(String combination)
	{
		// TODO(type-check): Has to be an x-array
		var resize = assertParsesSingleStatement("RESIZE %s ARRAY #VAR TO (10)".formatted(combination), IResizeArrayNode.class);
		assertThat(resize.arrayToResize().referencingToken().symbolName()).isEqualTo("#VAR");
		// TODO(lexer-mode): Actually parse array dimensions
		assertThat(resize.findDescendantToken(SyntaxKind.LPAREN)).isNotNull();
		assertThat(resize.findDescendantToken(SyntaxKind.RPAREN)).isNotNull();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"MODIFIED", "NOT MODIFIED"
	})
	void raiseADiagnosticForModifiedConditionIfTargetIsNotAVariable(String modified)
	{
		assertParsesSingleStatementWithDiagnostic(
			"""
						IF 'Hi' %s
						IGNORE
						END-IF
			""".formatted(modified),
			IfStatementNode.class,
			ParserError.INVALID_OPERAND
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"MODIFIED", "NOT MODIFIED"
	})
	void raiseNoDiagnosticForModifiedConditionIfTargetAVariable(String modified)
	{
		assertParsesWithoutDiagnostics("""
						IF #VAR %s
						IGNORE
						END-IF
			""".formatted(modified));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SPECIFIED", "NOT SPECIFIED"
	})
	void raiseADiagnosticForSpecifiedConditionIfTargetIsNotAVariable(String specified)
	{
		assertParsesSingleStatementWithDiagnostic(
			"""
						IF 'Hi' %s
						IGNORE
						END-IF
			""".formatted(specified),
			IfStatementNode.class,
			ParserError.INVALID_OPERAND
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SPECIFIED", "NOT SPECIFIED"
	})
	void raiseNoDiagnosticForSpecifiedConditionIfTargetAVariable(String specified)
	{
		assertParsesWithoutDiagnostics("""
						IF #VAR %s
						IGNORE
						END-IF
			""".formatted(specified));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"BACKOUT", "BACKOUT TRANSACTION"
	})
	void parseBackoutTransaction(String statement)
	{
		assertParsesSingleStatement(statement, IBackoutNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "FILE"
	})
	void parseCloseWork(String file)
	{
		var close = assertParsesSingleStatement("CLOSE WORK %s 1".formatted(file), ICloseWorkNode.class);
		assertThat(close.number().token().intValue()).isEqualTo(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "FILE"
	})
	void parseClosePc(String file)
	{
		var close = assertParsesSingleStatement("CLOSE PC %s 5".formatted(file), IClosePcNode.class);
		assertThat(close.number().token().intValue()).isEqualTo(5);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "FILE"
	})
	void parseSimpleWriteWork(String file)
	{
		var write = assertParsesSingleStatement("WRITE WORK %s 10 'Hi'".formatted(file), IWriteWorkNode.class);
		assertThat(write.isVariable()).isFalse();
		assertThat(write.number().token().intValue()).isEqualTo(10);
		assertThat(write.operands()).hasSize(1);
		assertNodeType(write.operands().first(), ILiteralNode.class);
	}

	@Test
	void parseWriteWorkWithMultipleOperands()
	{
		var write = assertParsesSingleStatement("WRITE WORK FILE 2 VARIABLE #VAR #ASD 'Hi'", IWriteWorkNode.class);
		assertThat(write.isVariable()).isTrue();
		assertThat(write.number().token().intValue()).isEqualTo(2);
		var operands = write.operands();
		assertThat(operands).hasSize(3);
		assertIsVariableReference(operands.first(), "#VAR");
		assertIsVariableReference(operands.get(1), "#ASD");
		assertLiteral(operands.get(2), SyntaxKind.STRING_LITERAL);
	}

	@Test
	void parseWriteWorkWhenStatementFollows()
	{
		var statementList = assertParsesWithoutDiagnostics("WRITE WORK FILE 2 #VAR\n#VAR2 := 5");
		assertThat(statementList.statements()).hasSize(2);
		assertThat(assertNodeType(statementList.statements().first(), IWriteWorkNode.class).operands()).hasSize(1);
	}

	@Test
	void parseSetWindowWithOff()
	{
		var setWindow = assertParsesSingleStatement("SET WINDOW OFF", ISetWindowNode.class);
		assertThat(setWindow.window().kind()).isEqualTo(SyntaxKind.OFF);
	}

	@Test
	void parseSetWindowWithStringLiteral()
	{
		var setWindow = assertParsesSingleStatement("SET WINDOW 'Fancy'", ISetWindowNode.class);
		assertThat(setWindow.window().kind()).isEqualTo(SyntaxKind.STRING_LITERAL);
		assertThat(setWindow.window().stringValue()).isEqualTo("Fancy");
	}

	@Test
	void parseTerminateWithoutExitCode()
	{
		var terminate = assertParsesSingleStatement("TERMINATE", ITerminateNode.class);
		assertThat(terminate.operands()).isEmpty();
	}

	@Test
	void parseTerminateWithSingleExitCode()
	{
		var terminate = assertParsesSingleStatement("TERMINATE 1", ITerminateNode.class);
		assertLiteral(terminate.operands().first(), SyntaxKind.NUMBER_LITERAL);
	}

	@Test
	void parseTerminateWithSingleExitCodeAsReference()
	{
		var terminate = assertParsesSingleStatement("TERMINATE #EXIT-CODE", ITerminateNode.class);
		assertIsVariableReference(terminate.operands().first(), "#EXIT-CODE");
	}

	@Test
	void parseTerminateWithAdditionalReturnOperand()
	{
		var terminate = assertParsesSingleStatement("TERMINATE #EXIT-CODE #VAR", ITerminateNode.class);
		assertIsVariableReference(terminate.operands().first(), "#EXIT-CODE");
		assertIsVariableReference(terminate.operands().get(1), "#VAR");
	}

	@Test
	void raiseADiagnosticIfTerminateIsCalledWithNonNumericLiteral()
	{
		assertDiagnostic("TERMINATE 'Hi'", ParserError.TYPE_MISMATCH);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "OCCURRENCES OF"
	})
	void parseReduceArrayToZero(String source)
	{
		var reduce = assertParsesSingleStatement("REDUCE %s ARRAY #ARR TO 0".formatted(source), IReduceArrayNode.class);
		assertThat(reduce.arrayToReduce().referencingToken().symbolName()).isEqualTo("#ARR");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "OCCURRENCES OF"
	})
	void parseReduceArrayToDimension(String source)
	{
		var reduce = assertParsesSingleStatement("REDUCE %s ARRAY #ARR TO (1:10,*:*,5:*)".formatted(source), IReduceArrayNode.class);
		assertThat(reduce.arrayToReduce().referencingToken().symbolName()).isEqualTo("#ARR");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SIZE OF DYNAMIC VARIABLE", "DYNAMIC", "DYNAMIC VARIABLE", "SIZE OF DYNAMIC",
	})
	void parseReduceDynamic(String combination)
	{
		// TODO(type-check): Has to be dynamic typed
		var reduce = assertParsesSingleStatement("REDUCE %s #VAR TO 20".formatted(combination), IReduceDynamicNode.class);
		assertThat(reduce.variableToReduce().referencingToken().symbolName()).isEqualTo("#VAR");
		assertThat(reduce.sizeToReduceTo()).isEqualTo(20);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "OCCURRENCES OF"
	})
	void parseExpandArrayToDimension(String source)
	{
		var reduce = assertParsesSingleStatement("EXPAND %s ARRAY #ARR TO (1:10,*:*,5:*)".formatted(source), IExpandArrayNode.class);
		assertThat(reduce.arrayToExpand().referencingToken().symbolName()).isEqualTo("#ARR");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SIZE OF DYNAMIC VARIABLE", "DYNAMIC", "DYNAMIC VARIABLE", "SIZE OF DYNAMIC",
	})
	void parseExpandDynamic(String combination)
	{
		// TODO(type-check): Has to be dynamic typed
		var reduce = assertParsesSingleStatement("EXPAND %s #VAR TO 20".formatted(combination), IExpandDynamicNode.class);
		assertThat(reduce.variableToExpand().referencingToken().symbolName()).isEqualTo("#VAR");
		assertThat(reduce.sizeToExpandTo()).isEqualTo(20);
	}
}
