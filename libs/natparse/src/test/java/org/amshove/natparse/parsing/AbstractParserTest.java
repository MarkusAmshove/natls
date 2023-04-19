package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.assertj.core.api.ObjectAssert;

import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class AbstractParserTest<NodeType>
{
	private final Function<IModuleProvider, AbstractParser<NodeType>> sutFactory;
	protected AbstractParser<NodeType> sut;

	protected ModuleProviderStub moduleProvider;

	protected AbstractParserTest(Function<IModuleProvider, AbstractParser<NodeType>> sutFactory)
	{
		this.sutFactory = sutFactory;
		moduleProvider = new ModuleProviderStub();
		this.sut = sutFactory.apply(moduleProvider);
	}

	protected void ignoreModuleProvider()
	{
		moduleProvider = null;
		sut = sutFactory.apply(null);
	}

	protected void useStubModuleProvider()
	{
		moduleProvider = new ModuleProviderStub();
		sut = sutFactory.apply(moduleProvider);
	}

	protected NodeType assertParsesWithoutDiagnostics(String source)
	{
		var lexer = new Lexer();
		var lexResult = lexer.lex(source, Paths.get("TEST.NSN"));
		assertThat(lexResult.diagnostics().size())
			.as(
				"Expected the source to lex without diagnostics%n%s"
					.formatted(lexResult.diagnostics().stream().map(IDiagnostic::message).collect(Collectors.joining("\n")))
			)
			.isZero();
		var parseResult = sut.parse(lexResult);
		assertThat(parseResult.diagnostics().size())
			.as(
				"Expected the source to parse without diagnostics%n%s"
					.formatted(parseResult.diagnostics().stream().map(IDiagnostic::message).collect(Collectors.joining("\n")))
			)
			.isZero();

		return parseResult.result();
	}

	protected NodeType assertDiagnostic(String source, ParserError expectedError)
	{
		var lexer = new Lexer();
		var tokens = lexer.lex(source, Paths.get("TESTMODULE.NSN"));
		var result = sut.parse(tokens);
		assertThat(result.diagnostics().size())
			.as("Expected to get at least one diagnostic, but found none")
			.isGreaterThan(0);
		assertThat(result.diagnostics())
			.anyMatch(d -> d.id().equals(expectedError.id()));

		return result.result();
	}

	@SuppressWarnings("unchecked")
	protected <T extends ISyntaxNode> T assertNodeType(ISyntaxNode node, Class<T> expectedType)
	{
		assertThat(node).isInstanceOf(expectedType);
		var castedNode = (T) node;
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
		assertThat(node.parent()).as("Parent for node %s was not set".formatted(node.getClass().getSimpleName())).isNotNull();
		assertThat(node.position()).as("Position for node was not set").isNotNull();

		if (!(node instanceof ITokenNode))
		{
			assertThat(node.descendants().size()).as("No child were added to node").isPositive();
		}

		return node;
	}

	protected NaturalModule newEmptyLda()
	{
		var module = new NaturalModule(null);
		module.setDefineData(new DefineDataNode());
		return module;
	}

	protected IVariableReferenceNode assertIsVariableReference(IOperandNode operand, String name)
	{
		var variable = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(variable.referencingToken().symbolName()).isEqualTo(name);
		return variable;
	}

	protected ILiteralNode assertLiteral(IOperandNode operand, SyntaxKind literalType)
	{
		var literal = assertNodeType(operand, ILiteralNode.class);
		assertThat(literal.token().kind()).isEqualTo(literalType);
		return literal;
	}
}
