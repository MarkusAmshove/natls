package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;
import org.assertj.core.api.ObjectAssert;

import java.util.function.Function;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class AbstractParserTest
{
	private final DefineDataParser sut = new DefineDataParser();

	protected void assertDiagnostic(String source, ParserError expectedError)
	{
		var lexer = new Lexer();
		var tokens = lexer.lex(source);
		var result = sut.parse(tokens);
		assertThat(result.diagnostics().size())
			.as("Expected to get at least one diagnostic, but found none")
			.isGreaterThan(0);
		assertThat(result.diagnostics())
			.anyMatch(d -> d.id().equals(expectedError.id()));
	}

	protected <T> T assertNodeType(ISyntaxNode node, Class<? extends T> expectedType)
	{
		assertThat(node).isInstanceOf(expectedType);
		return (T)node;
	}

	protected <T, R> ObjectAssert<R> assertWithType(ISyntaxNode node, Class<? extends T> expectedType, Function<T, R> assertion)
	{
		T typedNode = assertNodeType(node, expectedType);
		return assertThat(assertion.apply(typedNode));
	}

	protected <T> ObjectAssert<T> assertTokenNode(ISyntaxNode node, Function<ITokenNode, T> extractor)
	{
		var typedNode = assertNodeType(node, ITokenNode.class);
		return assertThat(extractor.apply(typedNode));
	}
}
