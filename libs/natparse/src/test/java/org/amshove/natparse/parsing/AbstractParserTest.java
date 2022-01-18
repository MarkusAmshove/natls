package org.amshove.natparse.parsing;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;
import org.assertj.core.api.ObjectAssert;

public abstract class AbstractParserTest<NodeType>
{
	private final AbstractParser<NodeType> sut;

    protected AbstractParserTest(AbstractParser<NodeType> sut)
    {
        this.sut = sut;
    }

    protected NodeType assertParsesWithoutDiagnostics(String source)
    {
        var lexer = new Lexer();
		var lexResult = lexer.lex(source, Paths.get("TEST.NSA"));
		assertThat(lexResult.diagnostics().size())
			.as(
				"Expected the source to lex without diagnostics%n%s"
					.formatted(lexResult.diagnostics().stream().map(IDiagnostic::message).collect(Collectors.joining("\n"))))
			.isZero();
		var parseResult = sut.parse(lexResult);
		assertThat(parseResult.diagnostics().size())
			.as(
				"Expected the source to parse without diagnostics%n%s"
					.formatted(parseResult.diagnostics().stream().map(IDiagnostic::message).collect(Collectors.joining("\n"))))
			.isZero();

		return parseResult.result();
    }


	protected void assertDiagnostic(String source, ParserError expectedError)
	{
		var lexer = new Lexer();
		var tokens = lexer.lex(source, Paths.get("TESTMODULE.NSN"));
		var result = sut.parse(tokens);
		assertThat(result.diagnostics().size())
			.as("Expected to get at least one diagnostic, but found none")
			.isGreaterThan(0);
		assertThat(result.diagnostics())
			.anyMatch(d -> d.id().equals(expectedError.id()));
	}

	@SuppressWarnings("unchecked")
	protected <T extends ISyntaxNode> T assertNodeType(ISyntaxNode node, Class<T> expectedType)
	{
		assertThat(node).isInstanceOf(expectedType);
		var castedNode = (T)node;
		return assertValidNode(castedNode);
	}

	protected <T extends ISyntaxNode, R> ObjectAssert<R> assertWithType(ISyntaxNode node, Class<T> expectedType, Function<T, R> assertion)
	{
		T typedNode = assertNodeType(node, expectedType);
		return assertThat(assertion.apply(typedNode));
	}

	protected <T> ObjectAssert<T> assertTokenNode(ISyntaxNode node, Function<ITokenNode, T> extractor)
	{
		var typedNode = assertNodeType(node, ITokenNode.class);
		return assertThat(extractor.apply(typedNode));
	}

	protected <T extends ISyntaxNode> T assertValidNode(T node)
	{
		assertThat(node.parent()).as("Parent for node was not set").isNotNull();
		assertThat(node.position()).as("Position for node was not set").isNotNull();

		if(!(node instanceof ITokenNode))
		{
			assertThat(node.descendants().size()).as("No child were added to node").isPositive();
		}

		return node;
	}
}
