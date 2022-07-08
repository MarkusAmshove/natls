package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.IExternalPerformNode;
import org.amshove.natparse.natural.IInternalPerformNode;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SubroutineReferenceTests extends AbstractParserTest<IStatementListNode>
{
	protected SubroutineReferenceTests()
	{
		super(StatementListParser::new);
	}

	@Override
	protected IStatementListNode assertParsesWithoutDiagnostics(String source)
	{
		var lexer = new Lexer();
		var modulePath = Paths.get("SUB.NSN");
		var lexResult = lexer.lex(source, modulePath);
		var parseResult = sut.parse(lexResult);
		var module = new NaturalModule(new NaturalFile("SUB", modulePath, NaturalFileType.SUBPROGRAM));
		module.setBody(parseResult.result());
		new ReferenceResolver(moduleProvider).resolveReferences(module);
		return parseResult.result();
	}

	@Test
	void resolveInternalSubroutinePerforms()
	{
		var statements = assertParsesWithoutDiagnostics("""
			PERFORM MY-SUBROUTINE

			DEFINE SUBROUTINE MY-SUBROUTINE
				IGNORE
			END-SUBROUTINE
			""");

		assertThat(statements.statements()).hasSize(2);
		var subroutine = statements.statements().get(1);
		var perform = assertNodeType(statements.statements().get(0), IInternalPerformNode.class);

		AssertionsForClassTypes.assertThat(perform.token().symbolName()).isEqualTo("MY-SUBROUTINE");
		assertThat(perform.reference()).isEqualTo(subroutine);
	}

	@Test
	void resolveExternalPerformCalls()
	{
		var calledSubroutine = new NaturalModule(null);
		moduleProvider.addModule("EXTERNAL-SUB", calledSubroutine);

		var statements = assertParsesWithoutDiagnostics("PERFORM EXTERNAL-SUB");
		var perform = ((IExternalPerformNode) statements.statements().first());
		AssertionsForClassTypes.assertThat(perform.reference()).isEqualTo(calledSubroutine);
		assertThat(calledSubroutine.callers()).contains(perform);
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

		AssertionsForClassTypes.assertThat(perform.token().symbolName()).isEqualTo("THIS-HAS-MORE-THAN-THIRTY-TWO-CHARACTERS-BUT-IT-WORKS-I-SHOULD-NEVER-DO-THAT");
		AssertionsForClassTypes.assertThat(perform.token().trimmedSymbolName(32)).isEqualTo("THIS-HAS-MORE-THAN-THIRTY-TWO-CH");
		assertThat(perform.reference()).isEqualTo(subroutine);
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

}
