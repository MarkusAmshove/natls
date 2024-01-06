package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.assertj.core.api.ObjectAssert;

import java.nio.file.Path;
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

	protected NodeType assertParsesWithoutDiagnostics(String source, NaturalFileType fileType)
	{
		var lexer = new Lexer();
		var lexResult = lexer.lex(source, Paths.get("TEST.%s".formatted(fileType.getExtension())));
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

	protected NodeType assertParsesWithoutDiagnostics(String source)
	{
		return assertParsesWithoutDiagnostics(source, NaturalFileType.SUBPROGRAM);
	}

	protected NodeType assertDiagnostic(String source, NaturalFileType fileType, ParserError expectedError)
	{
		var lexer = new Lexer();
		var tokens = lexer.lex(source, Paths.get("TESTMODULE.%s".formatted(fileType.getExtension())));
		var result = sut.parse(tokens);

		assertDiagnosticsContain(result.diagnostics(), expectedError);

		return result.result();
	}

	protected NodeType assertDiagnostic(String source, ParserError expectedError)
	{
		return assertDiagnostic(source, NaturalFileType.SUBPROGRAM, expectedError);
	}

	protected void assertDiagnosticsContain(ReadOnlyList<IDiagnostic> diagnostics, ParserError expectedError)
	{
		assertThat(diagnostics.size())
			.as("Expected to get at least one diagnostic of type <%s>, but found none".formatted(expectedError.name()))
			.isGreaterThan(0);
		assertThat(diagnostics)
			.as("Diagnostic %s(%s) not found".formatted(expectedError.name(), expectedError.id()))
			.anyMatch(d -> d.id().equals(expectedError.id()));
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
		var file = new NaturalFile("MYLDA", Path.of(""), NaturalFileType.LDA);
		var module = new NaturalModule(file);
		module.setDefineData(new DefineDataNode());
		return module;
	}

	protected NaturalModule newEmptySubprogram()
	{
		var file = new NaturalFile("SUBPROG", Path.of(""), NaturalFileType.SUBPROGRAM);
		var module = new NaturalModule(file);
		module.setDefineData(new DefineDataNode());
		return module;
	}

	protected NaturalModule newEmptyCopyCode()
	{
		var file = new NaturalFile("THECC", Path.of(""), NaturalFileType.COPYCODE);
		var module = new NaturalModule(file);
		return module;
	}

	protected IVariableReferenceNode assertIsVariableReference(IOperandNode operand, String name)
	{
		assertThat(operand).as("Expected a variable reference, but operand is null").isNotNull();
		var variable = assertNodeType(operand, IVariableReferenceNode.class);
		assertThat(variable.referencingToken().symbolName()).isEqualTo(name);
		return variable;
	}

	protected void assertValueAttribute(IAttributeNode attribute, SyntaxKind kind, String value)
	{
		var valueAttribute = assertNodeType(attribute, IValueAttributeNode.class);
		assertThat(valueAttribute.kind()).isEqualTo(kind);
		assertThat(valueAttribute.value()).isEqualTo(value);
	}

	protected ILiteralNode assertLiteral(IOperandNode operand, SyntaxKind literalType)
	{
		var literal = assertNodeType(operand, ILiteralNode.class);
		assertThat(literal.token().kind()).isEqualTo(literalType);
		return literal;
	}
}
