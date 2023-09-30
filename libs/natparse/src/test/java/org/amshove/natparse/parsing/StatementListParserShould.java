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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
		assertIsVariableReference(callnat.providedParameter().get(0), "#VAR");
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
		assertDiagnostic("INCLUDE 1", ParserError.UNEXPECTED_TOKEN_EXPECTED_IDENTIFIER);
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
	void reportADiagnosticIfASubroutineHasAnEmptyBody()
	{
		assertDiagnostic("""
			   DEFINE SUBROUTINE MY-SUBROUTINE
			   END-SUBROUTINE
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
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
		assertIsVariableReference(perform.providedParameter().get(0), "PDA1");
		assertThat(assertNodeType(perform.providedParameter().get(1), ILiteralNode.class).token().stringValue()).isEqualTo("Literal");
		assertThat(assertNodeType(perform.providedParameter().get(2), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertIsVariableReference(perform.providedParameter().get(3), "#VAR");
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
		assertIsVariableReference(perform.providedParameter().last(), "#VARNEWLINE");
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

	@ParameterizedTest
	@ValueSource(strings =
	{
		"ACCEPT IF #TEST = 3",
		"ACCEPT #TEST = 3",
		"REJECT IF #TEST = 3",
		"REJECT #TEST = 3",
	})
	void parseAcceptRejectIfStatements(String statement)
	{
		var acceptReject = assertParsesSingleStatement("""
			%s
			""".formatted(statement), IAcceptRejectNode.class);

		assertThat(acceptReject.condition()).isNotNull();
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
	void parseAnIfWithElse()
	{
		var ifStatement = assertParsesSingleStatement("""
            IF TRUE
            WRITE 'Hi'
            ELSE
            IGNORE
            END-IF
            """, IIfStatementNode.class);

		assertThat(ifStatement.body().statements()).hasSize(1);
		assertNodeType(ifStatement.body().statements().first(), IWriteNode.class);
		assertThat(ifStatement.elseBranch().statements()).hasSize(1);
		assertNodeType(ifStatement.elseBranch().statements().first(), IIgnoreNode.class);
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
	void reportADiagnosticIfAnIfStatementHasNoBody()
	{
		assertDiagnostic("""
			IF #TEST = 5 THEN
			END-IF
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
	}

	@Test
	void reportADiagnosticIfAnElseBranchHasNoBody()
	{
		assertDiagnostic("""
			IF #TEST = 5
			IGNORE
			ELSE
			END-IF
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
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
	void parseRepeatLoopConditionFirst()
	{
		var repeatLoop = assertParsesSingleStatement("""
			REPEAT UNTIL A = B OR B > C OR (X = 10)
				IGNORE
			END-REPEAT
			""", IRepeatLoopNode.class);

		assertThat(repeatLoop.body().statements()).hasSize(1);
		assertNodeType(repeatLoop.body().statements().first(), IIgnoreNode.class);
		assertThat(repeatLoop.descendants()).hasSize(5);
	}

	@Test
	void parseRepeatLoopConditionLast()
	{
		var repeatLoop = assertParsesSingleStatement("""
			REPEAT
				WRITE 'HEY!'
			WHILE A = B OR B > C OR (X = 10)
			END-REPEAT
			""", IRepeatLoopNode.class);

		assertThat(repeatLoop.body().statements()).hasSize(1);
		assertNodeType(repeatLoop.body().statements().first(), IWriteNode.class);
		assertThat(repeatLoop.condition()).isNotNull();
	}

	@Test
	void parseRepeatLoopNoCondition()
	{
		var repeatLoop = assertParsesSingleStatement("""
			REPEAT
				IF X > Y
				  ESCAPE BOTTOM
				END-IF
			END-REPEAT
			""", IRepeatLoopNode.class);

		assertThat(repeatLoop.body().statements()).hasSize(1);
		assertNodeType(repeatLoop.body().statements().first(), IIfStatementNode.class);
	}

	@Test
	void raiseADiagnosticIfARepeatLoopHasNoBody()
	{
		assertDiagnostic("""
			REPEAT
			END-REPEAT
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SORT #VAR1",
		"SORT THEM #VAR1",
		"SORT RECORD #VAR1",
		"SORT RECORDS BY #VAR1",
		"AND SORT THEM BY #VAR1 #VAR2",
		"#LABEL. SORT THEM BY #VAR1 #VAR2",
		"AND #LABEL. SORT THEM BY #VAR1 #VAR2",
		"AND #LABEL. SORT #VAR1 #VAR2",
		"SORT BY #VAR1 USING KEY",
		"SORT BY #VAR1 USING KEYS",
		"SORT BY #VAR1 USING #KEY1",
		"SORT BY #VAR1 USING #KEY1 #KEY2",
		"SORT BY #VAR1 USING #KEY1 #KEY2",
		"SORT BY #VAR1 ASC #VAR2 DESC USING #KEY1 #KEY2",
		"SORT BY #VAR1 ASCENDING #VAR2 DESCENDING USING #KEY1 #KEY2",
		"SORT BY #VAR1 USING #KEY1 #KEY2 GIVE MIN MAX AVER #GIVE",
		"SORT BY #VAR1 USING KEYS GIVE MIN MAX AVER OF #GIVE",
		"SORT BY #VAR1 USING KEYS GIVE MIN MAX AVER (#GIVE1) SUM TOTAL OF (#GIVE2)",
		"AND SORT THEM BY #VAR1 #VAR2 USING #KEY1 #KEY2 GIVING MIN MAX AVER (#GIVE1) SUM TOTAL OF (#GIVE2) (NL=10)",
	})
	void parseSortStatements(String statement)
	{
		var sort = assertParsesSingleStatement("""
			END-ALL
			%s
			END-SORT
			""".formatted(statement), ISortStatementNode.class);
		assertThat(sort.body().statements()).isEmpty();
	}

	@Test
	void recognizeBeforeBreakAsStatementInsteadOfOperandToSort()
	{
		assertParsesSingleStatement("""
			END-ALL
			SORT BY #VAR1
			BEFORE BREAK PROCESSING
			IGNORE
			END-BEFORE
			END-SORT
			""", ISortStatementNode.class);
	}

	@Test
	void parseSortWithSortDirection()
	{
		var sort = assertParsesSingleStatement("""
			END-ALL
			SORT BY #VAR1 ASC #VAR2 DESC #VAR3 ASCENDING #VAR4 DESCENDING #VAR5 USING #USING
			WRITE 'Hey!'
			END-SORT
			""", ISortStatementNode.class);
		assertThat(sort.body().statements()).hasSize(1);
		assertThat(sort.usings().isEmpty());
		assertIsVariableReference(sort.operands().get(0).operand(), "#VAR1");
		assertThat(sort.operands().get(0).direction()).isEqualTo(SortDirection.ASCENDING);
		assertIsVariableReference(sort.operands().get(1).operand(), "#VAR2");
		assertThat(sort.operands().get(1).direction()).isEqualTo(SortDirection.DESCENDING);
		assertIsVariableReference(sort.operands().get(2).operand(), "#VAR3");
		assertThat(sort.operands().get(2).direction()).isEqualTo(SortDirection.ASCENDING);
		assertIsVariableReference(sort.operands().get(3).operand(), "#VAR4");
		assertThat(sort.operands().get(3).direction()).isEqualTo(SortDirection.DESCENDING);
		assertIsVariableReference(sort.operands().get(4).operand(), "#VAR5");
		assertThat(sort.operands().get(4).direction()).isEqualTo(SortDirection.ASCENDING);
		assertIsVariableReference(sort.mutations().first(), "#VAR1");
		assertIsVariableReference(sort.mutations().last(), "#USING");
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
		assertIsVariableReference(forLoopNode.loopControl(), "#I");
		assertIsVariableReference(forLoopNode.mutations().first(), "#I");
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
	void raiseADiagnosticIfAForLoopHasNoBody()
	{
		assertDiagnostic("""
			FOR #I 1 10
			END-FOR
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
	}

	@Test
	void notComplainAboutMissingEndForWhenSortIsFollowing()
	{
		assertParsesWithoutDiagnostics("""
			FOR #I := 1 TO 10
			    FOR #J := 1 TO 20
			        WRITE #I #J
			END-ALL
			AND
			SORT THEM BY #I #J
			END-SORT
			""");
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
		assertThat(findStatement.descendants()).anyMatch(n -> n instanceof IConditionNode);
	}

	@Test
	void parseFindWithSetName()
	{
		var findStatement = assertParsesSingleStatement("""
			FIND THE-VIEW WITH 'COMPLETE-SET'
				IF TRUE
			    IGNORE
			    END-IF
			END-FIND
			""", IFindNode.class);

		assertThat(findStatement.viewReference()).isNotNull();
	}

	@Test
	void parseFindWithNumberLimitAndNoWith()
	{
		var findStatement = assertParsesSingleStatement("""
			FIND (5) THE-VIEW THE-DESCRIPTOR = 'Asd'
			IGNORE
			END-FIND
			""", IFindNode.class);

		assertThat(findStatement.viewReference()).isNotNull();
		assertThat(findStatement.descendants()).anyMatch(n -> n instanceof IConditionNode);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"FIRST", "NUMBER", "UNIQUE"
	})
	void parseFindWithoutBody(String findOption)
	{
		var findStatement = assertParsesSingleStatement("""
			FIND %s THE-VIEW WITH LIMIT 5 THE-DESCRIPTOR = 'Asd'
			""".formatted(findOption), IFindNode.class);

		assertThat(findStatement.viewReference()).isNotNull();
		assertThat(findStatement.descendants()).anyMatch(n -> n instanceof IConditionNode);
		assertThat(findStatement.descendants()).hasSize(7);
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

	@ParameterizedTest
	@ValueSource(strings =
	{
		"OR COUPLED TO FILE ANOTHER-VIEW VIA DESC2 = DESC1 DESC = 1",
		"AND COUPLED TO FILE ANOTHER-VIEW VIA DESC2 EQUAL TO DESC1 DESC = 1",
		"AND COUPLED TO FILE ANOTHER-VIEW WITH DESC = 1",
		"SORTED BY DESC2 DESC3 DESC4 DESCENDING",
		"RETAIN AS 'RetainedSet'",
		"PASSWORD='psw' CIPHER=123",
		"STARTING WITH ISN = 1 SORTED BY DESC2 DESC3 DESC4 DESCENDING WHERE X > Y",
		"SHARED HOLD SKIP RECORD IN HOLD",
		"IN SHARED HOLD SKIP IN HOLD",
	})
	void parseAdvancedFinds(String statement)
	{
		var findStatement = assertParsesSingleStatement("""
			FIND THE-VIEW DESC1 = 'Asd' %s
				IGNORE
			END-FIND
			""".formatted(statement), IFindNode.class);

		assertThat(findStatement.viewReference()).isNotNull();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"IN PHYSICAL ASCENDING SEQUENCE",
		"IN PHYSICAL VARIABLE 'A' SEQUENCE",
		"PHYSICAL DYNAMIC #DIRECTION",
		"ASC",
		"DESC",
		"VARIABLE 'A'",
		"DYNAMIC #DIRECTION",
		"VARIABLE 'A' SEQUENCE",
		"DYNAMIC #DIRECTION SEQUENCE",
		"",
	})
	void parseReadPhysical(String statement)
	{
		var read = assertParsesSingleStatement("""
			READ THE-VIEW %s
			END-READ
			""".formatted(statement), IReadNode.class);

		assertThat(read.viewReference()).isNotNull();
		assertThat(read.readSequence().isPhysicalSequence()).isTrue();
		assertThat(read.readSequence().isIsnSequence()).isFalse();
		assertThat(read.readSequence().isLogicalSequence()).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"BY ISN",
		"BY ISN FROM 123",
		"BY ISN STARTING FROM 123",
		"BY ISN STARTING FROM 123 ENDING AT 234",
		"BY ISN EQUAL 123",
		"BY ISN >= 123",
		"BY ISN WHERE *ISN > 1000",
	})
	void parseReadByIsn(String statement)
	{
		var read = assertParsesSingleStatement("""
			READ THE-VIEW %s
			END-READ
			""".formatted(statement), IReadNode.class);

		assertThat(read.viewReference()).isNotNull();
		assertThat(read.readSequence().isPhysicalSequence()).isFalse();
		assertThat(read.readSequence().isIsnSequence()).isTrue();
		assertThat(read.readSequence().isLogicalSequence()).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"BY DESC1 = 'Asd' SHARED HOLD SKIP RECORD IN HOLD",
		"BY DESC1 = 'Asd' IN SHARED HOLD SKIP IN HOLD",
		"BY DESC1 = 'Asd' SHARED HOLD MODE='Q' SKIP RECORDS IN HOLD",
		"BY DESC1",
		"BY DESC1 STARTING FROM 'Asd' THRU 'def'",
		"BY DESC1 STARTING FROM 'Asd' ENDING AT 'def'",
		"BY DESC1 FROM 'Asd' TO 'def'",
		"BY DESC1 FROM 'Asd' THRU 'def'",
		"LOGICAL DYNAMIC #DIRECTION SEQUENCE BY DESC1",
		"LOGICAL DYNAMIC #DIRECTION SEQUENCE BY DESC1 FROM 'A' TO 'Z'",
		"IN LOGICAL DYNAMIC #DIRECTION SEQUENCE BY DESC1 STARTING FROM 'A' TO 'Z'",
		"WITH DESC1 EQUAL TO 'Asd'",
		"WITH DESC1 GT 'Asd'",
		"WITH DESC1 LESS THAN 'Asd'",
		"WITH DESC1 <= 'Asd'",
	})
	void parseReadLogical(String statement)
	{
		var read = assertParsesSingleStatement("""
			READ THE-VIEW %s
			END-READ
			""".formatted(statement), IReadNode.class);

		assertThat(read.viewReference()).isNotNull();
		assertThat(read.readSequence().isPhysicalSequence()).isFalse();
		assertThat(read.readSequence().isIsnSequence()).isFalse();
		assertThat(read.readSequence().isLogicalSequence()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"PASSWORD='psw' CIPHER=123 WITH REPOSITION BY DESC1 = 'Asd'",
		"WITH REPOSITION BY DESC1 = 'Asd' WHERE X > Y",
	})
	void parseAdvancedReads(String statement)
	{
		var readStatement = assertParsesSingleStatement("""
			BROWSE (100) THE-VIEW %s
				IGNORE
			END-READ
			""".formatted(statement), IReadNode.class);

		assertThat(readStatement.viewReference()).isNotNull();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"1 #VAR1 VAR2 VAR3 VAR4",
		"FILE 1 #VAR1 VAR2 VAR3 VAR4",
		"FILE 1 RECORD #RECORD",
		"FILE 1 AND SELECT OFFSET 1 #VAR1 OFFSET 2 #VAR2 FILLER 10X #VAR3",
		"FILE 1 AND SELECT OFFSET 1 #VAR1 FILLER 10X #VAR3(*) AND ADJUST",
	})
	void parseReadWorkFileWithBody(String statement)
	{
		var work = assertParsesSingleStatement("""
			READ WORK %s
			END-WORK
			""".formatted(statement), IReadWorkNode.class);
		assertThat(work.workFileNumber().token().intValue())
			.isEqualTo(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"1 ONCE #VAR1 VAR2 VAR3 VAR4",
		"FILE 1 ONCE #VAR1 VAR2 VAR3 VAR4",
		"FILE 1 ONCE RECORD #RECORD",
		"FILE 1 ONCE AND SELECT OFFSET 1 #VAR1 OFFSET 2 #VAR2 FILLER 10X #VAR3",
		"FILE 1 ONCE AND SELECT OFFSET 1 #VAR1 FILLER 10X #VAR3(*) AND ADJUST",
		"FILE 1 ONCE #VAR1 VAR2 VAR3 VAR4 AT END OF FILE IGNORE END-ENDFILE",
	})
	void parseReadWorkFileWithNoBody(String statement)
	{
		var work = assertParsesSingleStatement("""
			READ WORK %s
			""".formatted(statement), IReadWorkNode.class);
		assertThat(work.workFileNumber().token().intValue()).isEqualTo(1);
	}

	@Test
	void parseReadWorkWithAdjustWithoutAnyKeywordBeforeOperand4()
	{
		// There was a bug where AND, ADJUST and OCCURRENCES where treated as variable references (operands)
		// instead of keywords.
		var readWork = assertParsesSingleStatement("READ WORK FILE 1 ONCE #VAR(*) AND ADJUST OCCURRENCES", IReadWorkNode.class);
		assertThat(readWork.directDescendantsOfType(IVariableReferenceNode.class))
			.hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"#VAR1 #VAR2 #VAR3",
		"#VAR1 #VAR2 #VAR3(1)",
		"DATA #VAR1 #VAR2 #VAR3",
		"DATA #VAR1 #VAR2 #VAR3(1)",
	})
	void parseGetTransactionStatements(String statement)
	{
		var get = assertParsesSingleStatement("GET TRANSACTION %s".formatted(statement), IGetTransactionNode.class);
		assertIsVariableReference(get.operands().get(0), "#VAR1");
		assertIsVariableReference(get.operands().get(1), "#VAR2");
		assertIsVariableReference(get.operands().get(2), "#VAR3");
		assertIsVariableReference(get.mutations().get(0), "#VAR1");
		assertIsVariableReference(get.mutations().get(1), "#VAR2");
		assertIsVariableReference(get.mutations().get(2), "#VAR3");
	}

	@Test
	void parseGetSameStatement()
	{
		var get = assertParsesSingleStatement("GET SAME", IGetSameNode.class);
		assertThat(get.label()).isEmpty();
	}

	@Test
	void reportADiagnosticIfGetSameHasAnInvalidLabel()
	{
		assertDiagnostic("GET SAME (LABELNODOT)", ParserError.UNEXPECTED_TOKEN_EXPECTED_OPERAND);
	}

	@Test
	void parseGetSameStatementWithLabel()
	{
		var get = assertParsesSingleStatement("GET SAME (LABEL.)", IGetSameNode.class);
		assertThat(get.label()).isNotEmpty();
		assertThat(get.label()).map(SyntaxToken::symbolName).hasValue("LABEL.");
	}

	@Test
	void parseGetSameStatementWithNumberLabel()
	{
		var get = assertParsesSingleStatement("GET SAME (0123)", IGetSameNode.class);
		assertThat(get.label()).isNotEmpty();
		assertThat(get.label()).map(SyntaxToken::symbolName).hasValue("0123");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"THE-VIEW *ISN",
		"THE-VIEW #ISN",
		"IN THE-VIEW *ISN",
		"IN THE-VIEW #ISN",
		"FILE THE-VIEW *ISN",
		"FILE THE-VIEW #ISN",
		"IN FILE THE-VIEW *ISN",
		"IN FILE THE-VIEW #ISN",
		"IN FILE THE-VIEW RECORD *ISN",
		"IN FILE THE-VIEW RECORD #ISN",
		"IN FILE THE-VIEW RECORDS *ISN",
		"IN FILE THE-VIEW RECORDS #ISN",
		"IN FILE THE-VIEW PASSWORD='pwd' CIPHER=123 RECORDS *ISN",
		"IN FILE THE-VIEW PASSWORD='pwd' CIPHER=123 RECORDS #ISN",
	})

	void parseGetStatements(String statement)
	{
		var get = assertParsesSingleStatement("GET %s".formatted(statement), IGetNode.class);
		assertThat(get.viewReference().token().symbolName()).isEqualTo("THE-VIEW");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"THE-VIEW *ISN(LABELNODOT)",
		"THE-VIEW *ISN(LABEL.)",
		"THE-VIEW *ISN (LABEL.)",
		"THE-VIEW *ISN (0123)",
		"IN THE-VIEW *ISN(LABEL.)",
		"IN THE-VIEW *ISN (LABEL.)",
		"IN THE-VIEW *ISN (0123)",
		"FILE THE-VIEW *ISN(LABEL.)",
		"FILE THE-VIEW *ISN (LABEL.)",
		"FILE THE-VIEW *ISN (0123)",
		"IN FILE THE-VIEW *ISN(LABEL.)",
		"IN FILE THE-VIEW *ISN (LABEL.)",
		"IN FILE THE-VIEW *ISN (0123)",
		"IN FILE THE-VIEW RECORD *ISN(LABEL.)",
		"IN FILE THE-VIEW RECORD *ISN (LABEL.)",
		"IN FILE THE-VIEW RECORD *ISN (0123)",
		"IN FILE THE-VIEW RECORDS *ISN(LABEL.)",
		"IN FILE THE-VIEW RECORDS *ISN (LABEL.)",
		"IN FILE THE-VIEW RECORDS *ISN (0123)",
		"IN FILE THE-VIEW PASSWORD='pwd' CIPHER=123 RECORDS *ISN(LABEL.)",
		"IN FILE THE-VIEW PASSWORD='pwd' CIPHER=123 RECORDS *ISN (LABEL.)",
		"IN FILE THE-VIEW PASSWORD='pwd' CIPHER=123 RECORDS *ISN (0123)",
	})
	void parseGetStatementsWithLabel(String statement)
	{
		var get = assertParsesSingleStatement("GET %s".formatted(statement), IGetNode.class);
		assertThat(get.viewReference().token().symbolName()).isEqualTo("THE-VIEW");
	}

	@Test
	void reportADiagnosticIfGetHasNoView()
	{
		assertDiagnostic("GET *ISN", ParserError.UNEXPECTED_TOKEN_EXPECTED_IDENTIFIER);
	}

	@Test
	void parseCallFileStatement()
	{
		var call = assertParsesSingleStatement("""
			CALL FILE 'PGM' #CF #RA
				IGNORE
			END-FILE""", ICallFileNode.class);
		assertThat(call.calling().token().symbolName()).isEqualTo("'PGM'");
		assertIsVariableReference(call.controlField(), "#CF");
		assertIsVariableReference(call.recordArea(), "#RA");
		assertThat(call.body().statements()).hasSize(1);
	}

	@Test
	void reportADiagnosticIfNoOperandsFollowProgram()
	{
		assertDiagnostic("""
			CALL FILE 'PGM'
				IGNORE
			END-FILE
			""", ParserError.UNEXPECTED_TOKEN_EXPECTED_OPERAND);
	}

	@Test
	void parseCallLoopStatement()
	{
		var call = assertParsesSingleStatement("""
			CALL LOOP 'PGM'
				IGNORE
			END-LOOP""", ICallLoopNode.class);
		assertThat(assertNodeType(call.calling(), ILiteralNode.class).token().stringValue()).isEqualTo("PGM");
		assertThat(call.body().statements()).hasSize(1);
	}

	@Test
	void parseCallLoopStatementWithOperands()
	{
		var call = assertParsesSingleStatement("""
			CALL LOOP 'PGM' #VAR1 #VAR2 #VAR3
				IGNORE
			END-LOOP""", ICallLoopNode.class);
		assertThat(assertNodeType(call.calling(), ILiteralNode.class).token().stringValue()).isEqualTo("PGM");
		assertThat(call.body().statements()).hasSize(1);
		assertIsVariableReference(call.operands().get(1), "#VAR2");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"'CMULT'",
		"'CMULT' #VAR1 #VAR2 #VAR3",
		"'CMULT' USING #VAR1 #VAR2 #VAR3",
		"INTERFACE4 'CMULT'",
		"INTERFACE4 'CMULT' #VAR1 #VAR2 #VAR3",
		"INTERFACE4 'CMULT' USING #VAR1 #VAR2 #VAR3",
	})
	void parseCall(String statement)
	{
		var call = assertParsesSingleStatement("CALL %s".formatted(statement), ICallNode.class);
		assertThat(assertNodeType(call.calling(), ILiteralNode.class).token().stringValue()).isEqualTo("CMULT");
	}

	@Test
	void parseCallStatementWithOperands()
	{
		var call = assertParsesSingleStatement("CALL 'PGM' USING #VAR1 #VAR2 #VAR3", ICallNode.class);
		assertThat(assertNodeType(call.calling(), ILiteralNode.class).token().stringValue()).isEqualTo("PGM");
		assertIsVariableReference(call.operands().get(1), "#VAR2");
	}

	@Test
	void reportADiagnosticIfNoOperandsFollowingUsing()
	{
		assertDiagnostic("CALL 'PGM' USING IGNORE", ParserError.UNEXPECTED_TOKEN_EXPECTED_OPERAND);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"#VAR1 TO #VAR2",
		"#VAR1 (PM=I) TO #VAR2",
		"#VAR1 (DF=I) TO #VAR2",
		"#VAR1 (DF=I PM=I) TO #VAR2",
	})
	void parseMove(String statement)
	{
		var move = assertParsesSingleStatement("MOVE %s".formatted(statement), IMoveStatementNode.class);
		assertIsVariableReference(move.operand(), "#VAR1");
		assertThat(move.targets()).hasSize(1);
		assertIsVariableReference(move.targets().first(), "#VAR2");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"OLD(#VAR1) INTO #VAR2",
		"OLD(*ISN) TO #VAR2",
		"SUM(#VAR1) INTO #VAR2",
	})
	void parseMoveWithSystemFunctions(String statement)
	{
		var move = assertParsesSingleStatement("MOVE %s".formatted(statement), IMoveStatementNode.class);
		assertThat(move.targets()).hasSize(1);
		assertIsVariableReference(move.targets().first(), "#VAR2");
	}

	@Test
	void parseMoveAttributeDefinition()
	{
		var move = assertParsesSingleStatement("MOVE (AD=I CD=RE) TO #CV", IMoveStatementNode.class);
		assertThat(move.targets()).hasSize(1);
		assertIsVariableReference(move.targets().first(), "#CV");
	}

	@Test
	void parseMoveSubstring()
	{
		var move = assertParsesSingleStatement("MOVE SUBSTRING(#VAR1,1,2) TO #VAR2", IMoveStatementNode.class);
		var substringOperand = assertNodeType(move.operand(), ISubstringOperandNode.class);
		assertIsVariableReference(substringOperand.operand(), "#VAR1");
		assertThat(move.targets()).hasSize(1);
		assertIsVariableReference(move.targets().first(), "#VAR2");
	}

	@Test
	void parseMoveSubstringToSubstring()
	{
		var move = assertParsesSingleStatement("MOVE SUBSTRING(#VAR1,1,2) TO SUBSTRING(#VAR2,5)", IMoveStatementNode.class);
		var substringOperand = assertNodeType(move.operand(), ISubstringOperandNode.class);
		assertIsVariableReference(substringOperand.operand(), "#VAR1");
		assertThat(move.targets()).hasSize(1);
		substringOperand = assertNodeType(move.targets().first(), ISubstringOperandNode.class);
		assertIsVariableReference(substringOperand.operand(), "#VAR2");
	}

	@Test
	void parseMoveSubstringToSubstringMultiTargets()
	{
		var move = assertParsesSingleStatement("MOVE SUBSTRING(#VAR1,1,2) (PM=I) TO SUBSTRING(#VAR2,5) SUBSTRING(#VAR3,5) #VAR4", IMoveStatementNode.class);
		var substringOperand = assertNodeType(move.operand(), ISubstringOperandNode.class);
		assertIsVariableReference(substringOperand.operand(), "#VAR1");
		assertThat(move.targets()).hasSize(3);
		substringOperand = assertNodeType(move.targets().first(), ISubstringOperandNode.class);
		assertIsVariableReference(substringOperand.operand(), "#VAR2");
		assertIsVariableReference(move.targets().last(), "#VAR4");
	}

	@Test
	void parseMoveRounded()
	{
		var move = assertParsesSingleStatement("MOVE ROUNDED #VAR1 TO #VAR2", IMoveStatementNode.class);
		assertThat(move.isRounded()).isTrue();
	}

	@Test
	void parseMoveRoundedMulti()
	{
		var move = assertParsesSingleStatement("MOVE ROUNDED #VAR1 TO #VAR2 #VAR3 #VAR4", IMoveStatementNode.class);
		assertThat(move.isRounded()).isTrue();
		assertThat(move.targets()).hasSize(3);
	}

	@Test
	void parseMoveByName()
	{
		var move = assertParsesSingleStatement("MOVE BY NAME #VAR1 TO #VAR2", IMoveStatementNode.class);
		assertThat(move.byKind()).isEqualTo(SyntaxKind.NAME);
	}

	@Test
	void parseMoveByNameDefault()
	{
		var move = assertParsesSingleStatement("MOVE BY #VAR1 TO #VAR2", IMoveStatementNode.class);
		assertThat(move.byKind()).isEqualTo(SyntaxKind.NAME);
	}

	@Test
	void parseMoveByPosition()
	{
		var move = assertParsesSingleStatement("MOVE BY POSITION #VAR1 TO #VAR2", IMoveStatementNode.class);
		assertThat(move.byKind()).isEqualTo(SyntaxKind.POSITION);
	}

	@Test
	void parseMoveEditedApplyingMask()
	{
		var move = assertParsesSingleStatement("MOVE EDITED #VAR1 (EM=XX) TO #VAR2", IMoveStatementNode.class);
		assertThat(move.isEdited()).isTrue();
	}

	@Test
	void parseMoveEditedWithEditorMaskContainingRParenInString()
	{
		var result = assertParsesWithoutDiagnostics("""
			MOVE EDITED #V1 (EM=D')') TO #V2
			IF #V1 = #V2
			IGNORE
			END-IF
			""");

		assertThat(result.statements()).hasSize(2);
		assertNodeType(result.statements().first(), IMoveStatementNode.class);
		assertNodeType(result.statements().last(), IIfStatementNode.class);
	}

	@Test
	void parseMoveEditedUsingMask()
	{
		var move = assertParsesSingleStatement("MOVE EDITED #VAR1 TO #VAR2 (EM=XX)", IMoveStatementNode.class);
		assertThat(move.isEdited()).isTrue();
	}

	@Test
	void parseMoveLeft()
	{
		var move = assertParsesSingleStatement("MOVE LEFT #VAR1 TO #VAR2", IMoveStatementNode.class);
		assertThat(move.direction()).isEqualTo(SyntaxKind.LEFT);
	}

	@Test
	void parseMoveRight()
	{
		var move = assertParsesSingleStatement("MOVE RIGHT JUSTIFIED #VAR1 TO #VAR2", IMoveStatementNode.class);
		assertThat(move.direction()).isEqualTo(SyntaxKind.RIGHT);
	}

	@Test
	void parseMoveNormalized()
	{
		var move = assertParsesSingleStatement("MOVE NORMALIZED #VAR1 TO #VAR2", IMoveStatementNode.class);
		assertThat(move.isNormalized()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"A TO B",
		"A CODEPAGE #CP TO B",
		"A CODEPAGE #CP TO B CODEPAGE #CP",
		"A CODEPAGE #CP TO B IN CODEPAGE #CP",
		"A IN CODEPAGE #CP TO B",
		"A IN CODEPAGE #CP TO B CODEPAGE #CP",
		"A IN CODEPAGE #CP TO B IN CODEPAGE #CP",
		"A IN CODEPAGE #CP TO B IN CODEPAGE #CP GIVING #RC",
		"A IN CODEPAGE 'CP1' TO B IN CODEPAGE 'CP2'",
		"A IN CODEPAGE 'CP1' TO B IN CODEPAGE 'CP2' GIVING #RC",
	})
	void parseMoveEncoded(String statement)
	{
		var move = assertParsesSingleStatement("MOVE ENCODED %s".formatted(statement), IMoveStatementNode.class);
		assertThat(move.isEncoded()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"'-' TO B",
		"'-' TO B UNTIL 10",
		"'-' TO SUBSTRING(B,10)",
		"A TO B",
		"A TO B UNTIL 10",
		"A TO B UNTIL #UNTIL",
	})
	void parseMoveAll(String statement)
	{
		var move = assertParsesSingleStatement("MOVE ALL %s".formatted(statement), IMoveStatementNode.class);
		assertThat(move.isAll()).isTrue();
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
		assertIsVariableReference(reset.mutations().first(), "#THEVAR");
		assertIsVariableReference(reset.mutations().last(), "#THEOTHERVAR");
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
	void parseADefinePrinterWithPrinterNameAsAttribute()
	{
		var printer = assertParsesSingleStatement("DEFINE PRINTER (CC=2)", IDefinePrinterNode.class);
		assertThat(printer.printerNumber()).isEqualTo(2);
		assertThat(printer.printerName()).map(SyntaxToken::symbolName).hasValue("CC");
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
	void parseClosePrinterWithPrinterNameAsAttribute()
	{
		var closePrinter = assertParsesSingleStatement("CLOSE PRINTER (CC)", IClosePrinterNode.class);
		assertThat(closePrinter.printer().symbolName()).isEqualTo("CC");
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
	void parseWriteWithReportSpecificationAsAttribute()
	{
		var write = assertParsesSingleStatement("WRITE (CC)", IWriteNode.class);
		assertThat(write.reportSpecification()).map(SyntaxToken::source).hasValue("CC");
	}

	@Test
	void parseWriteWithLineAdvancement()
	{
		assertParsesSingleStatement("WRITE (1) // 10X 'literal' (I)", IWriteNode.class);
	}

	@Test
	void parseWriteWithAttributeDefinition()
	{
		var write = assertParsesSingleStatement("WRITE (AD=UL AL=17 NL=8)", IWriteNode.class);
		assertThat(write.descendants()).hasSize(6);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"WRITE (01) TITLE LEFT JUSTIFIED UNDERLINED 58T W-PRODUKT-TEKST / 1T TITLE-POL / 'Prisspecifikation' /",
		"WRITE (01) TRAILER LEFT JUSTIFIED / *TIMX (EM=DD.MM.YYYY' 'HH:II) 69T  'Side' *PAGE-NUMBER (01)",
	})
	void parseOtherFormsOfWrite(String writeSource)
	{
		assertParsesSingleStatement(writeSource, IWriteNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"WRITE NOHDR ' literal ' (I)",
		"WRITE NOHDR 25T '******' 'End of Data'(I) '******'"
	})
	void treatWriteIntensifiedAttributeToStringLiteralAsAttributeAndNotIdentifier(String writeSource)
	{
		var write = assertParsesSingleStatement(writeSource, IWriteNode.class);
		assertThat(write.descendants())
			.as("Write should not contain any variable reference")
			.noneMatch(n -> n instanceof IVariableReferenceNode);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"PRINT (SV12) NOHDR ' literal ' (I)",
		"PRINT (SV12) NOHDR 25T '******' 'End of Data'(I) '******'"
	})
	void treatPrintIntensifiedAttributeToStringLiteralAsAttributeAndNotIdentifier(String printSource)
	{
		var write = assertParsesSingleStatement(printSource, IPrintNode.class);
		assertThat(write.descendants())
			.as("Print should not contain any variable reference")
			.noneMatch(n -> n instanceof IVariableReferenceNode);
	}

	@Test
	void notParseAttributeAsIsnParameter()
	{
		var write = assertParsesSingleStatement("WRITE *ISN(NL=8)", IWriteNode.class);
		assertThat(write.descendants()).anyMatch(n -> n instanceof ITokenNode tNode && tNode.token().kind() == SyntaxKind.NL);
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
	void parseDisplayWithReportSpecificationAsAttribute()
	{
		var display = assertParsesSingleStatement("DISPLAY (CC)", IDisplayNode.class);
		assertThat(display.reportSpecification()).isPresent();
		assertThat(display.reportSpecification().get().symbolName()).isEqualTo("CC");
		assertThat(display.descendants()).hasSize(4);
	}

	@Test
	void parseASimpleExamineReplace()
	{
		var examine = assertParsesSingleStatement("EXAMINE #VAR 'a' REPLACE 'b'", IExamineNode.class);
		assertThat(examine.examined()).isNotNull();
		assertIsVariableReference(examine.examined(), "#VAR");
	}

	@Test
	void parseAnExamineWithSubstring()
	{
		var examine = assertParsesSingleStatement("EXAMINE SUBSTR(#VAR, 1, 5) FOR 'a'", IExamineNode.class);
		assertThat(examine.examined()).isNotNull();
		var substringOperand = assertNodeType(examine.examined(), ISubstringOperandNode.class);
		assertIsVariableReference(substringOperand.operand(), "#VAR");
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
		var examine = assertParsesSingleStatement("EXAMINE DIRECTION #FWD FULL VALUE OF #DOC STARTING FROM POSITION 7 ENDING AT POSITION 10 FOR FULL VALUE OF PATTERN #HTML(*) WITH DELIMITERS ',' AND DELETE FIRST GIVING INDEX IN #ASD #EFG #HIJ", IExamineNode.class);
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
	void parseASimpleSeparate()
	{
		var separate = assertParsesSingleStatement("SEPARATE #VAR INTO #ARR(*)", ISeparateStatementNode.class);
		assertThat(separate.separated()).isNotNull();
		assertIsVariableReference(separate.separated(), "#VAR");
		assertThat(separate.targets()).hasSize(1);
		var reference = assertNodeType(separate.targets().first(), VariableReferenceNode.class);
		assertThat(reference.token().source()).isEqualTo("#ARR");
		var rangedAccess = assertNodeType(reference.dimensions().first(), IRangedArrayAccessNode.class);
		assertThat(assertNodeType(rangedAccess.lowerBound(), ITokenNode.class).token().kind()).isEqualTo(SyntaxKind.ASTERISK);
		assertThat(assertNodeType(rangedAccess.upperBound(), ITokenNode.class).token().kind()).isEqualTo(SyntaxKind.ASTERISK);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"STARTING", "FROM", "FROM POSITION", "STARTING FROM", "STARTING FROM POSITION"
	})
	void parseASeparateWithStartingFrom(String from)
	{
		var separate = assertParsesSingleStatement("SEPARATE #VAR1 %s #POS INTO #VAR2 #VAR3 #VAR4".formatted(from), ISeparateStatementNode.class);
		assertThat(separate.separated()).isNotNull();
		assertIsVariableReference(separate.separated(), "#VAR1");
		assertThat(separate.targets()).hasSize(3);
		var reference = assertNodeType(separate.targets().first(), VariableReferenceNode.class);
		assertThat(reference.dimensions().isEmpty());
		assertThat(reference.token().source()).isEqualTo("#VAR2");
		assertIsVariableReference(separate.targets().get(1), "#VAR3");
		assertIsVariableReference(separate.targets().get(2), "#VAR4");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"IGNORE", "REMAINDER #REM", "REMAINDER POSITION #POS"
	})
	void parseASeparateWithIgnoreOrRemainder(String rem)
	{
		var separate = assertParsesSingleStatement("SEPARATE #VAR INTO #VAR2 #VAR3 #VAR4 %s".formatted(rem), ISeparateStatementNode.class);
		assertThat(separate.separated()).isNotNull();
		assertIsVariableReference(separate.separated(), "#VAR");
	}

	@Test
	void parseASeparateWithSubstring()
	{
		var separate = assertParsesSingleStatement("SEPARATE SUBSTR(#VAR, 1, 5) LEFT INTO #ARR(*)", ISeparateStatementNode.class);
		assertThat(separate.separated()).isNotNull();
		var substringOperand = assertNodeType(separate.separated(), ISubstringOperandNode.class);
		assertIsVariableReference(substringOperand.operand(), "#VAR");
		assertThat(assertNodeType(substringOperand.startPosition().orElseThrow(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(substringOperand.length().orElseThrow(), ILiteralNode.class).token().intValue()).isEqualTo(5);
		assertThat(separate.descendants().size()).isEqualTo(10);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"WITH RETAINED ANY DELIMITER", "WITH RETAINED ANY DELIMITERS", "WITH ANY DELIMITER", "WITH ANY DELIMITERS",
		"WITH RETAINED INPUT DELIMITER", "WITH RETAINED INPUT DELIMITERS", "WITH INPUT DELIMITER", "WITH INPUT DELIMITERS",
		"WITH RETAINED DELIMITER ' '", "WITH RETAINED DELIMITERS ' '", "WITH DELIMITER ' '", "WITH DELIMITERS ' '",
		"WITH RETAINED DELIMITER #DEL", "WITH RETAINED DELIMITERS #DEL", "WITH DELIMITER #DEL", "WITH DELIMITERS #DEL",
		"WITH RETAINED DELIMITER", "WITH RETAINED DELIMITERS", "WITH DELIMITER", "WITH DELIMITERS",
	})
	void parseAnSeparateWithDelimiters(String delimiter)
	{
		assertParsesSingleStatement("SEPARATE #VAR LEFT JUSTIFIED INTO #ARR(*) %s".formatted(delimiter), ISeparateStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"GIVING NUMBER IN", "GIVING NUMBER", "NUMBER IN"
	})
	void parseAnSeparateWithGiving(String delimiter)
	{
		assertParsesSingleStatement("SEPARATE #VAR INTO #ARR(*) %s #NUM".formatted(delimiter), ISeparateStatementNode.class);
	}

	@Test
	void parseAComplexSeparate()
	{
		var separate = assertParsesSingleStatement("SEPARATE #VAR1 STARTING FROM POSITION 1 LEFT INTO #VAR2 #ARR(*) REMAINDER POSITION #POS WITH RETAINED DELIMITER '#' GIVING NUMBER #NUM", ISeparateStatementNode.class);
		assertThat(separate.descendants().size()).isEqualTo(20);
	}

	@Test
	void reportADiagnosticIfNoSeparateField()
	{
		assertDiagnostic("SEPARATE INTO #ARR", ParserError.UNEXPECTED_TOKEN);
	}

	@Test
	void reportADiagnosticIfIntoIsMissing()
	{
		assertDiagnostic("SEPARATE #VAR #ARR IGNORE NUMBER #NUM", ParserError.UNEXPECTED_TOKEN);
	}

	@Test
	void reportADiagnosticIfRemainderFieldIdLiteral()
	{
		assertDiagnostic("SEPARATE #VAR INTO #ARR REMAINDER ' '", ParserError.INVALID_OPERAND);
	}

	@Test
	void parseNewPage()
	{
		var newPage = assertParsesSingleStatement("NEWPAGE EVEN IF TOP OF PAGE WITH TITLE LEFT JUSTIFIED 'The Title'", INewPageNode.class);
		assertThat(newPage.descendants()).hasSize(11);
	}

	@Test
	void parseNewPageWithoutTitle()
	{
		var newPage = assertParsesSingleStatement("NEWPAGE WHEN LESS THAN 10 LINES", INewPageNode.class);
		assertThat(newPage.descendants()).hasSize(6);
	}

	@Test
	void parseNewPageWithNumericReportSpecification()
	{
		var newPage = assertParsesSingleStatement("NEWPAGE(5) WHEN LESS 10 TITLE UNDERLINED 'The Title'", INewPageNode.class);
		assertThat(newPage.reportSpecification()).map(SyntaxToken::intValue).hasValue(5);
		assertThat(newPage.descendants()).hasSize(10);
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

	@Test
	void parseEscapeNumberedLabel()
	{
		var escape = assertParsesSingleStatement("ESCAPE BOTTOM (0123)", IEscapeNode.class);
		assertThat(escape.label()).map(SyntaxToken::symbolName).hasValue("0123");
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
			HISTOGRAM THE-VIEW PASSWORD='password' THE-DESC STARTING FROM 'M'
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

	@ParameterizedTest
	@ValueSource(strings =
	{
		"WHERE FIELD > 0",
		"WHERE FIELD > 0 AND FIELD < 100",
		"WHERE FIELD = 0 OR FIELD > 100",
		"GT 'XXX' WHERE FIELD > 0",
		"LESS THAN 'XXX' WHERE FIELD > 0",
		"GREATER EQUAL 'XXX' WHERE FIELD > 0",
		"STARTING FROM 'M' ENDING AT 'Q' WHERE FIELD < 100",
		"STARTING FROM 'M' TO 'Q' WHERE FIELD > 0 AND FIELD < 100",
	})
	void parseHistogramWithWhere(String where)
	{
		var histogram = assertParsesSingleStatement("""
			HISTOGRAM THE-VIEW FOR THE-DESC %s
			IGNORE
			END-HISTOGRAM""".formatted(where), IHistogramNode.class);
		assertThat(histogram.condition()).isNotNull();
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
		"UNION", "UNION ALL", "UNION DISTINCT", "INTERSECT", "EXCEPT"
	})
	void parseSelectWithUnion(String operation)
	{
		assertParsesSingleStatement("""
			SELECT * FROM DB2_TABLE WHERE COLUMN = 'search'
			%s
			SELECT * FROM ANOTHER_TABLE WHERE COLUMN = 'search'
			END-SELECT""".formatted(operation), ISelectNode.class);
	}

	@Test
	void parseDb2Insert()
	{
		assertParsesSingleStatement("""
			INSERT INTO DB2-TABLE
			  (COL1, COL2, COL3)
			  VALUES
			  ('A', 'B', 'C')
			""", IInsertStatementNode.class);
	}

	@Test
	void parseDb2InsertWithSelect()
	{
		assertParsesSingleStatement("""
			INSERT INTO DB2-TABLE
			  (SELECT * FROM ANOTHER-TABLE WHERE COL1 = 'somevalue')
			""", IInsertStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"UPDATE RECORD IN STATEMENT (GET.)",
		"UPDATE RECORD IN (GET.)",
		"UPDATE IN STATEMENT (GET.)",
		"UPDATE RECORD STATEMENT (GET.)",
		"UPDATE RECORD (GET.)",
		"UPDATE IN (GET.)",
		"UPDATE STATEMENT (GET.)",
		"UPDATE (GET.)",
		"UPDATE RECORD IN STATEMENT (0120)",
		"UPDATE RECORD IN (0120)",
		"UPDATE IN STATEMENT (0120)",
		"UPDATE RECORD STATEMENT (0120)",
		"UPDATE RECORD (0120)",
		"UPDATE IN (0120)",
		"UPDATE STATEMENT (0120)",
		"UPDATE (0120)",
		"UPDATE RECORD IN STATEMENT",
		"UPDATE RECORD IN",
		"UPDATE IN STATEMENT",
		"UPDATE RECORD STATEMENT",
		"UPDATE RECORD",
		"UPDATE IN",
		"UPDATE STATEMENT",
		"UPDATE",
	})
	void parseAdabasUpdate(String statement)
	{
		assertParsesSingleStatement("""
			%s
			""".formatted(statement), IUpdateStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"DELETE RECORD IN STATEMENT (GET.)",
		"DELETE RECORD IN (GET.)",
		"DELETE IN STATEMENT (GET.)",
		"DELETE RECORD STATEMENT (GET.)",
		"DELETE RECORD (GET.)",
		"DELETE IN (GET.)",
		"DELETE STATEMENT (GET.)",
		"DELETE (GET.)",
		"DELETE RECORD IN STATEMENT (0120)",
		"DELETE RECORD IN (0120)",
		"DELETE IN STATEMENT (0120)",
		"DELETE RECORD STATEMENT (0120)",
		"DELETE RECORD (0120)",
		"DELETE IN (0120)",
		"DELETE STATEMENT (0120)",
		"DELETE (0120)",
		"DELETE RECORD IN STATEMENT",
		"DELETE RECORD IN",
		"DELETE IN STATEMENT",
		"DELETE RECORD STATEMENT",
		"DELETE RECORD",
		"DELETE IN",
		"DELETE STATEMENT",
		"DELETE",
	})
	void parseAdabasDelete(String statement)
	{
		assertParsesSingleStatement("""
			%s
			""".formatted(statement), IDeleteStatementNode.class);
	}

	@Test
	void parseDb2Update()
	{
		assertParsesSingleStatement("""
			UPDATE DB2-TABLE
			  SET COL1 = 'xyz',
			  COL2 = 'abc',
			  COL3 = 123
			  WHERE F1 = F2
			""", IUpdateStatementNode.class);
	}

	@Test
	void parseDb2Delete()
	{
		assertParsesSingleStatement("""
			DELETE FROM DB2-TABLE
			  WHERE F1 = F2
			""", IDeleteStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"PROCESS SQL DB2-TABLE <<SET :CURR-SERV  = CURRENT SERVER>>",
		"PROCESS SQL DB2-TABLE <<CONNECT TO :LOCATION>>",
	})
	void parseProcessSql(String statement)
	{
		assertParsesSingleStatement(statement, IProcessSqlNode.class);
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

	@ParameterizedTest
	@ValueSource(strings =
	{
		"RESETTING",
		"RESETTING DATAAREA",
		"RESETTING TEXTAREA",
		"RESETTING MACROAREA",
		"RESETTING ALL",
		"MOVING",
		"ASSIGNING #VAR1 = #VAR2",
		"FORMATTING",
		"EXTRACTING #VAR2 = #VAR1",
	})
	void parseSimpleComposeStatements(String statement)
	{
		assertParsesSingleStatement("COMPOSE %s".formatted(statement), IComposeStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"VAR1 VAR2 VAR3 TO DATAAREA LAST STATUS TO STAT1",
		"VAR1 VAR2 LAST STATUS TO STAT1 STAT2",
		"VAR1 VAR2 VAR3 STATUS TO STAT1 STAT2",
		"VAR1 TO DATAAREA OUTPUT TO VARIABLES VAR1 VAR2 VAR3 STATUS TO STAT1 STAT2",
		"LAST OUTPUT TO VARIABLES VAR1 VAR2 VAR3 STATUS TO STAT1 STAT2",
		"VAR1 TO VARIABLES VAR1 VAR2 VAR3 STATUS TO STAT1 STAT2",
		"VAR1 TO VARIABLES VAR1 VAR2 VAR3",
		"OUTPUT TO VARIABLES VAR1 VAR2 VAR3 STATUS TO STAT1",
	})
	void parseComposeMovingStatements(String statement)
	{
		assertParsesSingleStatement("COMPOSE MOVING %s".formatted(statement), IComposeStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"(01)",
		"SUPPRESSED",
		"CALLING 'string'",
		"CALLING VAR1",
		"TO VARIABLES CONTROL CNTL1 CNTL2 VAR1 VAR2 VAR3",
		"TO VARIABLES VAR1 VAR2 VAR3",
		"DOCUMENT",
		"DOCUMENT TO FINAL",
		"DOCUMENT TO INTERMEDIATE",
		"DOCUMENT TO FINAL CABINET #CAB GIVING VAR1",
		"DOCUMENT TO INTERMEDIATE CABINET 'CABINET' VAR1",
		"DOCUMENT TO INTERMEDIATE CABINET 'CABINET' PASSW='password' GIVING VAR1",
		"DOCUMENT INTO FINAL CABINET 'CABINET' PASSW='password' VAR1 VAR2",
		"DOCUMENT INTO CABINET 'CABINET' PASSW='password' GIVING VAR1 VAR2",
	})
	void parseComposeFormattingOutputStatements(String statement)
	{
		assertParsesSingleStatement("COMPOSE FORMATTING OUTPUT %s".formatted(statement), IComposeStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"DATAAREA",
		"DATAAREA FROM EXIT #VAR1",
		"DATAAREA FROM CABINET #CAB1",
		"DATAAREA FROM CABINET #CAB1 CABINET #CAB2",
		"DATAAREA FROM CABINET 'CAB' PASSW=#PSW1 CABINET #CAB2",
		"DATAAREA FROM CABINET 'CAB' PASSW=#PSW1  CABINET #CAB2 PASSW=#PSW2",
		"#VAR1 FROM EXIT #EXIT1",
		"#VAR1 FROM EXIT #EXIT1 EXIT #EXIT2",
		"#VAR1 FROM CABINET 'CAB' PASSW=#PSW1",
		"#VAR1 FROM CABINET 'CAB' PASSW=#PSW1  CABINET #CAB2 PASSW=#PSW2",
	})
	void parseComposeFormattingInputStatements(String statement)
	{
		assertParsesSingleStatement("COMPOSE FORMATTING INPUT %s".formatted(statement), IComposeStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"ASSIGNING TEXTVARIABLE #VAR1 = #VAR2",
		"ASSIGNING TEXTVARIABLE 'VARNAME1' = #VAR1",
		"EXTRACTING TEXTVARIABLE #VAR1 = #VAR2",
		"EXTRACTING TEXTVARIABLE #VAR1 = 'VARNAME'",
		"ASSIGNING TEXTVARIABLE #VAR1 = #VAR2, #VAR3 = #VAR4",
		"ASSIGNING TEXTVARIABLE 'VARNAME1' = #VAR1, #VAR3 = #VAR4",
		"EXTRACTING TEXTVARIABLE #VAR1 = #VAR2, #VAR3 = #VAR4",
		"EXTRACTING TEXTVARIABLE #VAR1 = 'VARNAME', #VAR3 = #VAR4",
		"ASSIGNING #VAR1 = #VAR2",
		"ASSIGNING 'VARNAME1' = #VAR1",
		"EXTRACTING #VAR1 = #VAR2",
		"EXTRACTING #VAR1 = 'VARNAME'",
		"ASSIGNING #VAR1 = #VAR2, #VAR3 = #VAR4",
		"ASSIGNING 'VARNAME1' = #VAR1, #VAR3 = #VAR4",
		"EXTRACTING #VAR1 = #VAR2, #VAR3 = #VAR4",
		"EXTRACTING #VAR1 = 'VARNAME', #VAR3 = #VAR4",
	})
	void parseComposeAssigningAndExtractingStatements(String statement)
	{
		assertParsesSingleStatement("COMPOSE %s".formatted(statement), IComposeStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"ENDING AT PAGE #VAR1",
		"ENDING AT #VAR1",
		"ENDING PAGE #VAR1",
		"ENDING #VAR1",
		"ENDING AT PAGE 11",
		"ENDING AT 11",
		"ENDING PAGE 11",
		"ENDING 11",
		"ENDING AFTER #VAR1 PAGES",
		"ENDING AFTER 12 PAGES",
		"ENDING AFTER 12",
		"STARTING FROM PAGE #VAR",
		"STARTING FROM #VAR",
		"STARTING PAGE #VAR",
		"STARTING #VAR",
		"STARTING FROM PAGE 13",
		"STARTING FROM 13",
		"STARTING PAGE 13",
		"STARTING 13",
	})
	void parseComposeFormattingStartingEndingStatements(String statement)
	{
		assertParsesSingleStatement("COMPOSE FORMATTING %s".formatted(statement), IComposeStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"STATUS #VAR1",
		"STATUS #VAR1 #VAR2 #VAR3 #VAR4",
		"PROFILE #VAR1",
		"MESSAGES SUPPRESSED",
		"MESSAGES LISTED ON (01)",
		"MESSAGES LISTED (01)",
		"MESSAGES ON (01)",
		"MESSAGES (01)",
		"ERRORS INTERCEPTED",
		"ERRORS LISTED ON (01)",
		"ERRORS LISTED (01)",
		"ERRORS ON (01)",
		"ERRORS (01)",
	})
	void parseComposeFormattingOtherStatements(String statement)
	{
		assertParsesSingleStatement("COMPOSE FORMATTING %s".formatted(statement), IComposeStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"RESETTING DATAAREA MOVING ASSIGNING A = B FORMATTING STATUS VAR MESSAGES SUPPRESSED",
	})
	void parseComposeMultiClausesStatements(String statement)
	{
		assertParsesSingleStatement("COMPOSE %s".formatted(statement), IComposeStatementNode.class);
	}

	@Test
	void parseComposeRealLifeStatement()
	{
		assertParsesSingleStatement("""
			COMPOSE RESETTING ALL
			MOVING '.EM AUTE-KONSULENTBREV'
			ASSIGNING
			'FORSIDENT'        = POLICE-FORS-IDENT,
			'SKADEDATO'        = ' ',
			'LBNR'             = ' ',
			'SAGSBEHANDLER'    = BEGAERING-SAGSBEHANDLER,
			'AKTIVITETSTYPE'   = AKTIVITET-SKADE.AKTIVITET-TYPE,
			'AKTIVITETSTEKST'  = 'Brev om ....',
			'FORSTAGER'        = FORS-TAGER,
			'FTAGKONTAKTADR'   = FORS-TAGER-KONTAKT,
			'FORSTYPE'         = KODE-TEKST,
			'OBJEKTTYPE'       = ' ',
			'UDLOEBSDATO'      = UDLQBS-DATO
			FORMATTING
			OUTPUT DOCUMENT GIVING ISN-B4
			INPUT 'CONNECT-ADVIS-UDLQB-FEJL' FROM CABINET 'SKAETEXT'
			STATUS STATUS-KODE
			""", IComposeStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"EXTRACTING 'VARNAME' = #VAR1",
	})
	void reportADiagnosticForInvalidOperandForCompose(String statement)
	{
		assertDiagnostic("""
			COMPOSE %s
            """.formatted(statement), ParserError.INVALID_OPERAND);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"ASSIGNING #VAR2 = H'FF'",
		"ASSIGNING #VAR2 = TRUE",
		"EXTRACTING #VAR3 = 10",
		"EXTRACTING #VAR3 = FALSE",
		"FORMATTING OUTPUT CALLING 10",
		"FORMATTING OUTPUT DOCUMENT CABINET 'CAB' PASSW=10",
	})
	void reportADiagnosticForTypeMismatchForCompose(String statement)
	{
		assertDiagnostic(
			"""
			COMPOSE %s
            """.formatted(statement), ParserError.TYPE_MISMATCH
		);
	}

	@Test
	void reportADiagnosticIfDecideForMissesNone()
	{
		assertDiagnostic("""
            DECIDE FOR CONDITION
            WHEN 5 < 2
            IGNORE
            END-DECIDE
            """, ParserError.DECIDE_MISSES_NONE_BRANCH);
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
	void raiseADiagnosticIfADecideForBranchHasNoBody()
	{
		assertDiagnostic("""
				DECIDE FOR FIRST CONDITION
				WHEN 5 < 2
				END-DECIDE
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
	}

	@Test
	void raiseADiagnosticIfADecideForWhenAllBranchHasNoBody()
	{
		assertDiagnostic("""
				DECIDE FOR FIRST CONDITION
				WHEN ALL
				END-DECIDE
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
	}

	@Test
	void raiseADiagnosticIfADecideForWhenAnyBranchHasNoBody()
	{
		assertDiagnostic("""
				DECIDE FOR FIRST CONDITION
				WHEN ANY
				END-DECIDE
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
	}

	@Test
	void raiseADiagnosticIfADecideForWhenNoneBranchHasNoBody()
	{
		assertDiagnostic("""
				DECIDE FOR FIRST CONDITION
				WHEN NONE
				END-DECIDE
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
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
		assertIsVariableReference(resize.variableToResize(), "#VAR");
		assertThat(assertNodeType(resize.sizeToResizeTo(), ILiteralNode.class).token().intValue()).isEqualTo(20);
		assertIsVariableReference(resize.mutations().first(), "#VAR");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SIZE OF DYNAMIC VARIABLE", "DYNAMIC", "DYNAMIC VARIABLE", "SIZE OF DYNAMIC",
	})
	void parseResizeDynamicWithVariableSize(String combination)
	{
		var resize = assertParsesSingleStatement("RESIZE %s #VAR TO #SIZE".formatted(combination), IResizeDynamicNode.class);
		assertIsVariableReference(resize.variableToResize(), "#VAR");
		assertThat(assertNodeType(resize.sizeToResizeTo(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#SIZE");
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
		assertIsVariableReference(resize.arrayToResize(), "#VAR");
		// TODO(lexer-mode): Actually parse array dimensions
		assertThat(resize.findDescendantToken(SyntaxKind.LPAREN)).isNotNull();
		assertThat(resize.findDescendantToken(SyntaxKind.RPAREN)).isNotNull();
	}

	@Test
	void parseResizeArrayToDimensionWithVariableReferences()
	{
		var resize = assertParsesSingleStatement("RESIZE ARRAY ARR TO (1:#K)", IResizeArrayNode.class);
		var variableRef = resize.dimensions().first().findDescendantOfType(IVariableReferenceNode.class);
		assertThat(variableRef).isNotNull();
		assertIsVariableReference(variableRef, "#K");
	}

	@Test
	void parseResizeArrayToMultipleDimensionsWithVariableReferences()
	{
		var resize = assertParsesSingleStatement("RESIZE ARRAY ARR TO (1:#K,#L:5)", IResizeArrayNode.class);
		assertIsVariableReference(resize.dimensions().get(0).findDescendantOfType(IVariableReferenceNode.class), "#K");
		assertIsVariableReference(resize.dimensions().get(1).findDescendantOfType(IVariableReferenceNode.class), "#L");
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
		assertIsVariableReference(reduce.arrayToReduce(), "#ARR");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "OCCURRENCES OF"
	})
	void parseReduceArrayToDimension(String source)
	{
		var reduce = assertParsesSingleStatement("REDUCE %s ARRAY #ARR TO (1:10,*:*,5:*)".formatted(source), IReduceArrayNode.class);
		assertIsVariableReference(reduce.arrayToReduce(), "#ARR");
	}

	@Test
	void parseReduceArrayToDimensionWithVariableReferences()
	{
		var reduce = assertParsesSingleStatement("REDUCE ARRAY ARR TO (1:#K)", IReduceArrayNode.class);
		var variableRef = reduce.dimensions().first().findDescendantOfType(IVariableReferenceNode.class);
		assertThat(variableRef).isNotNull();
		assertIsVariableReference(variableRef, "#K");
	}

	@Test
	void parseReduceArrayToMultipleDimensionsWithVariableReferences()
	{
		var reduce = assertParsesSingleStatement("REDUCE ARRAY ARR TO (1:#K,#L:5)", IReduceArrayNode.class);
		assertIsVariableReference(reduce.dimensions().get(0).findDescendantOfType(IVariableReferenceNode.class), "#K");
		assertIsVariableReference(reduce.dimensions().get(1).findDescendantOfType(IVariableReferenceNode.class), "#L");
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
		assertIsVariableReference(reduce.variableToReduce(), "#VAR");
		assertThat(assertNodeType(reduce.sizeToReduceTo(), ILiteralNode.class).token().intValue()).isEqualTo(20);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SIZE OF DYNAMIC VARIABLE", "DYNAMIC", "DYNAMIC VARIABLE", "SIZE OF DYNAMIC",
	})
	void parseReduceDynamicWithVariableSize(String combination)
	{
		// TODO(type-check): Has to be dynamic typed
		var reduce = assertParsesSingleStatement("REDUCE %s #VAR TO #SIZE".formatted(combination), IReduceDynamicNode.class);
		assertIsVariableReference(reduce.variableToReduce(), "#VAR");
		assertThat(assertNodeType(reduce.sizeToReduceTo(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#SIZE");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "OCCURRENCES OF"
	})
	void parseExpandArrayToDimension(String source)
	{
		var expand = assertParsesSingleStatement("EXPAND %s ARRAY #ARR TO (1:10,*:*,5:*)".formatted(source), IExpandArrayNode.class);
		assertIsVariableReference(expand.arrayToExpand(), "#ARR");
		assertIsVariableReference(expand.mutations().first(), "#ARR");
	}

	@Test
	void parseExpandArrayToDimensionWithVariableReferences()
	{
		var expand = assertParsesSingleStatement("EXPAND ARRAY ARR TO (1:#K)", IExpandArrayNode.class);
		var variableRef = expand.dimensions().first().findDescendantOfType(IVariableReferenceNode.class);
		assertThat(variableRef).isNotNull();
		assertIsVariableReference(variableRef, "#K");
	}

	@Test
	void parseExpandArrayToMultipleDimensionsWithVariableReferences()
	{
		var expand = assertParsesSingleStatement("EXPAND ARRAY ARR TO (1:#K,#L:5)", IExpandArrayNode.class);
		assertIsVariableReference(expand.dimensions().get(0).findDescendantOfType(IVariableReferenceNode.class), "#K");
		assertIsVariableReference(expand.dimensions().get(1).findDescendantOfType(IVariableReferenceNode.class), "#L");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SIZE OF DYNAMIC VARIABLE", "DYNAMIC", "DYNAMIC VARIABLE", "SIZE OF DYNAMIC",
	})
	void parseExpandDynamic(String combination)
	{
		var expand = assertParsesSingleStatement("EXPAND %s #VAR TO 20".formatted(combination), IExpandDynamicNode.class);
		assertIsVariableReference(expand.variableToExpand(), "#VAR");
		assertThat(assertNodeType(expand.sizeToExpandTo(), ILiteralNode.class).token().intValue()).isEqualTo(20);
		assertIsVariableReference(expand.mutations().first(), "#VAR");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"SIZE OF DYNAMIC VARIABLE", "DYNAMIC", "DYNAMIC VARIABLE", "SIZE OF DYNAMIC",
	})
	void parseExpandDynamicWithVariableSize(String combination)
	{
		// TODO(type-check): Has to be dynamic typed
		var expand = assertParsesSingleStatement("EXPAND %s #VAR TO #SIZE".formatted(combination), IExpandDynamicNode.class);
		assertIsVariableReference(expand.variableToExpand(), "#VAR");
		assertThat(assertNodeType(expand.sizeToExpandTo(), IVariableReferenceNode.class).token().symbolName()).isEqualTo("#SIZE");
		assertIsVariableReference(expand.mutations().first(), "#VAR");
	}

	@Test
	void parseResizeArrayWithErrorNr()
	{
		var stmt = assertParsesSingleStatement("RESIZE ARRAY #ARR TO (*) GIVING #ERR", IResizeArrayNode.class);
		assertIsVariableReference(stmt.errorVariable(), "#ERR");
		assertIsVariableReference(stmt.mutations().first(), "#ARR");
		assertIsVariableReference(stmt.mutations().last(), "#ERR");
	}

	@Test
	void parseResizeDynamicWithErrorNr()
	{
		var stmt = assertParsesSingleStatement("RESIZE DYNAMIC #VAR TO 20 GIVING #ERR", IResizeDynamicNode.class);
		assertIsVariableReference(stmt.errorVariable(), "#ERR");
		assertIsVariableReference(stmt.mutations().first(), "#VAR");
		assertIsVariableReference(stmt.mutations().last(), "#ERR");
	}

	@Test
	void parseExpandArrayWithErrorNr()
	{
		var stmt = assertParsesSingleStatement("EXPAND ARRAY #ARR TO (*) GIVING #ERR", IExpandArrayNode.class);
		assertIsVariableReference(stmt.errorVariable(), "#ERR");
		assertIsVariableReference(stmt.mutations().first(), "#ARR");
		assertIsVariableReference(stmt.mutations().last(), "#ERR");
	}

	@Test
	void parseExpandDynamicWithErrorNr()
	{
		var stmt = assertParsesSingleStatement("EXPAND DYNAMIC #VAR TO 20 GIVING #ERR", IExpandDynamicNode.class);
		assertIsVariableReference(stmt.errorVariable(), "#ERR");
		assertIsVariableReference(stmt.mutations().first(), "#VAR");
		assertIsVariableReference(stmt.mutations().last(), "#ERR");
	}

	@Test
	void parseReduceArrayWithErrorNr()
	{
		var stmt = assertParsesSingleStatement("REDUCE ARRAY #ARR TO 0 GIVING #ERR", IReduceArrayNode.class);
		assertIsVariableReference(stmt.errorVariable(), "#ERR");
		assertIsVariableReference(stmt.mutations().first(), "#ARR");
		assertIsVariableReference(stmt.mutations().last(), "#ERR");
	}

	@Test
	void parseReduceDynamicWithErrorNr()
	{
		var stmt = assertParsesSingleStatement("REDUCE DYNAMIC #VAR TO 20 GIVING #ERR", IReduceDynamicNode.class);
		assertIsVariableReference(stmt.errorVariable(), "#ERR");
		assertIsVariableReference(stmt.mutations().first(), "#VAR");
		assertIsVariableReference(stmt.mutations().last(), "#ERR");
	}

	@Test
	void parseDefinePrototype()
	{
		var prototype = assertParsesSingleStatement("""
			DEFINE PROTOTYPE HI RETURNS (L)
			END-PROTOTYPE
			""", IDefinePrototypeNode.class);

		assertThat(prototype.nameToken().symbolName()).isEqualTo("HI");
		assertThat(prototype.isVariable()).isFalse();
		assertThat(prototype.variableReference()).isNull();
	}

	@Test
	void parseDefinePrototypeVariable()
	{
		var prototype = assertParsesSingleStatement("""
			DEFINE PROTOTYPE VARIABLE HI RETURNS (L)
			END-PROTOTYPE
			""", IDefinePrototypeNode.class);

		assertThat(prototype.nameToken().symbolName()).isEqualTo("HI");
		assertThat(prototype.isVariable()).isTrue();
		assertThat(prototype.variableReference()).isNotNull();
	}

	@Test
	void parseWritePcWithVariable()
	{
		var write = assertParsesSingleStatement("WRITE PC FILE 1 VARIABLE 'Hi'", IWritePcNode.class);
		assertThat(write.isVariable()).isTrue();
		assertLiteral(write.number(), SyntaxKind.NUMBER_LITERAL);
	}

	@Test
	void parseWritePcWithoutVariable()
	{
		var write = assertParsesSingleStatement("WRITE PC FILE 1 'Hi'", IWritePcNode.class);
		assertThat(write.isVariable()).isFalse();
		assertLiteral(write.operand(), SyntaxKind.STRING_LITERAL);
	}

	@Test
	void parseWritePcCommandSync()
	{
		assertParsesSingleStatement("WRITE PC 5 COMMAND 'Hi' SYNC", IWritePcNode.class);
	}

	@Test
	void parseWritePcCommandAsync()
	{
		assertParsesSingleStatement("WRITE PC 5 COMMAND 'Hi' ASYNC", IWritePcNode.class);
	}

	@Test
	void parseDownloadPcWithVariable()
	{
		var download = assertParsesSingleStatement("DOWNLOAD PC FILE 1 VARIABLE 'Hi'", IWritePcNode.class);
		assertThat(download.isVariable()).isTrue();
		assertLiteral(download.number(), SyntaxKind.NUMBER_LITERAL);
	}

	@Test
	void parseDownloadPcWithoutVariable()
	{
		var download = assertParsesSingleStatement("DOWNLOAD PC FILE 1 'Hi'", IWritePcNode.class);
		assertThat(download.isVariable()).isFalse();
		assertLiteral(download.operand(), SyntaxKind.STRING_LITERAL);
	}

	@Test
	void parseDownloadPcCommandSync()
	{
		assertParsesSingleStatement("DOWNLOAD PC 5 COMMAND 'Hi' SYNC", IWritePcNode.class);
	}

	@Test
	void parseDownloadPcCommandAsync()
	{
		assertParsesSingleStatement("DOWNLOAD PC 5 COMMAND 'Hi' ASYNC", IWritePcNode.class);
	}

	@Test
	void allowLabelIdentifierAsVariableOperand()
	{
		var assignment = assertParsesSingleStatement("#VAR(R1.) := 5", IAssignmentStatementNode.class);
		assertIsVariableReference(assignment.target(), "#VAR");
	}

	@Test
	void parsePerformBreak()
	{
		var perform = assertParsesSingleStatement("""
			PERFORM BREAK PROCESSING
			AT BREAK OF #INDEX
			IGNORE
			END-BREAK
			""", IPerformBreakNode.class);

		assertThat(perform.statementIdentifier()).isNull();
		assertIsVariableReference(perform.breakOf().operand(), "#INDEX");
		assertThat(perform.breakOf().body().statements()).hasSize(1);
		assertThat(perform.breakOf().parent()).isEqualTo(perform);
	}

	@Test
	void parsePerformBreakWithLabelIdentifier()
	{
		var perform = assertParsesSingleStatement("""
			PERFORM BREAK (S1.)
			AT BREAK OF #INDEX
			IGNORE
			END-BREAK
			""", IPerformBreakNode.class);

		assertThat(perform.statementIdentifier()).isNotNull();
		assertThat(perform.statementIdentifier().symbolName()).isEqualTo("S1.");
		assertIsVariableReference(perform.breakOf().operand(), "#INDEX");
		assertThat(perform.breakOf().body().statements()).hasSize(1);
	}

	@Test
	void parseLimit()
	{
		var limit = assertParsesSingleStatement("LIMIT 5", ILimitNode.class);
		assertLiteral(limit.limit(), SyntaxKind.NUMBER_LITERAL);
		assertThat(limit.limit().token().intValue()).isEqualTo(5);
	}

	@Test
	void parseLimitAsOperand()
	{
		var assign = assertParsesSingleStatement("LIMIT := 5", IAssignmentStatementNode.class);
		assertIsVariableReference(assign.target(), "LIMIT");
	}

	@Test
	void parseAnOnError()
	{
		var error = assertParsesSingleStatement("""
			ON ERROR
			IGNORE
			END-ERROR
			""", IOnErrorNode.class);

		assertThat(error.body().statements()).hasSize(1);
	}

	@Test
	void reportADiagnosticIfOnErrorHasEmptyBody()
	{
		assertDiagnostic("""
			ON ERROR
			END-ERROR
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"FIRST", "FIRST VALUE OF", "FIRST VALUE", "FIRST OF",
		"EVERY", "EVERY VALUE OF", "EVERY VALUE", "EVERY OF"
	})
	void parseDecideOn(String permutation)
	{
		var decideOn = assertParsesSingleStatement("""
			DECIDE ON %s #VAR
			NONE
			IGNORE
			END-DECIDE
			""".formatted(permutation), IDecideOnNode.class);
		assertIsVariableReference(decideOn.operand(), "#VAR");
		assertThat(decideOn.noneValue().statements()).hasSize(1);
	}

	@Test
	void reportADiagnosticIfDecideOnMissesNone()
	{
		assertDiagnostic("""
            DECIDE ON FIRST #VAR
            VALUE 'Hi'
            IGNORE
            END-DECIDE
            """, ParserError.DECIDE_MISSES_NONE_BRANCH);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "VALUE", "VALUES"
	})
	void parseDecideOnNoneBranch(String permutation)
	{
		var decideOn = assertParsesSingleStatement("""
			DECIDE ON FIRST #VAR
			NONE %s
			IGNORE
			END-DECIDE
			""".formatted(permutation), IDecideOnNode.class);
		assertThat(decideOn.noneValue().statements()).hasSize(1);
	}

	@Test
	void parseDecideOnSubstring()
	{
		var decideOn = assertParsesSingleStatement("""
			DECIDE ON FIRST SUBSTRING(#VAR, 1, 2)
			NONE
			IGNORE
			END-DECIDE
			""", IDecideOnNode.class);
		assertNodeType(decideOn.operand(), ISubstringOperandNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "VALUE", "VALUES"
	})
	void parseAnyValueBranch(String permutation)
	{
		var decideOn = assertParsesSingleStatement("""
			DECIDE ON FIRST #VAR
			ANY %s
			IGNORE
			NONE
			IGNORE
			END-DECIDE
			""".formatted(permutation), IDecideOnNode.class);

		assertThat(decideOn.anyValue()).isNotNull();
		assertThat(decideOn.anyValue().statements()).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "VALUE", "VALUES"
	})
	void parseAllValueBranch(String permutation)
	{
		var decideOn = assertParsesSingleStatement("""
			DECIDE ON FIRST #VAR
			ALL %s
			IGNORE
			NONE
			IGNORE
			END-DECIDE
			""".formatted(permutation), IDecideOnNode.class);

		assertThat(decideOn.allValues()).isNotNull();
		assertThat(decideOn.allValues().statements()).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"VALUE", "VALUES"
	})
	void parseDecideOnWithBranches(String valueKeyword)
	{
		var decideOn = assertParsesSingleStatement("""
			DECIDE ON FIRST #VAR
			%s SUBSTRING(#VAR2, 1)
			IGNORE
			%s 'Hi'
			IGNORE
			%s #VAR2
			IGNORE
			NONE
			IGNORE
			END-DECIDE
			""".formatted(valueKeyword, valueKeyword, valueKeyword), IDecideOnNode.class);

		assertThat(decideOn.branches()).hasSize(3);
		assertNodeType(decideOn.branches().get(0).values().first(), ISubstringOperandNode.class);
		assertLiteral(decideOn.branches().get(1).values().first(), SyntaxKind.STRING_LITERAL);
		assertIsVariableReference(decideOn.branches().get(2).values().first(), "#VAR2");
	}

	@Test
	void parseDecideOnWithValueRange()
	{
		var decideOn = assertParsesSingleStatement("""
			DECIDE ON FIRST #VAR
			VALUES 1:10
			IGNORE
			VALUES #VAR:#VAR2
			IGNORE
			NONE
			IGNORE
			END-DECIDE
			""", IDecideOnNode.class);

		assertThat(decideOn.branches()).hasSize(2);
		assertThat(decideOn.branches()).allMatch(IDecideOnBranchNode::hasValueRange);
		var firstBranch = decideOn.branches().first();
		assertThat(firstBranch.values()).hasSize(2);
		assertLiteral(firstBranch.values().first(), SyntaxKind.NUMBER_LITERAL);
		assertLiteral(firstBranch.values().last(), SyntaxKind.NUMBER_LITERAL);

		var secondBranch = decideOn.branches().last();
		assertThat(secondBranch.values()).hasSize(2);
		assertIsVariableReference(secondBranch.values().first(), "#VAR");
		assertIsVariableReference(secondBranch.values().last(), "#VAR2");
	}

	@Test
	void parseADecideOnBranchWithMultipleValues()
	{
		var decide = assertParsesSingleStatement("""
			DECIDE ON FIRST VALUE OF #VAR
			VALUE #VAR2,#VAR3,#VAR4
			IGNORE
			NONE
			IGNORE
			END-DECIDE
			""", IDecideOnNode.class);

		assertThat(decide.branches()).hasSize(1);
		var values = decide.branches().first().values();
		assertIsVariableReference(values.get(0), "#VAR2");
		assertIsVariableReference(values.get(1), "#VAR3");
		assertIsVariableReference(values.get(2), "#VAR4");
	}

	@Test
	void reportADiagnosticWhenDecideOnNoneBodyIsEmpty()
	{
		assertDiagnostic("""
			DECIDE ON FIRST #VAR
			NONE
			END-DECIDE
			""", ParserError.STATEMENT_HAS_EMPTY_BODY);
	}

	@Test
	void parseOptions()
	{
		assertParsesWithoutDiagnostics("OPTIONS TQMARK=OFF");
	}

	@Test
	void parseMultipleOptions()
	{
		assertParsesWithoutDiagnostics("OPTIONS TQMARK=OFF TQMARK=ON SOME=THING");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"ANY", "ALL", "VALUE 5", "VALUE #VAR", "VALUE #VAR(*)"
	})
	void reportADiagnosticWhenDecideOnBranchBodyIsEmpty(String branch)
	{
		assertDiagnostic("""
			DECIDE ON FIRST #VAR
			%s
			NONE
			IGNORE
			END-DECIDE
			""".formatted(branch), ParserError.STATEMENT_HAS_EMPTY_BODY);
	}
}
