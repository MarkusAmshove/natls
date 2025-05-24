package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;

import java.util.ArrayList;
import java.util.List;

public class NaturalParser
{
	private final IModuleProvider moduleProvider;

	public NaturalParser()
	{
		this(null);
	}

	public NaturalParser(IModuleProvider moduleProvider)
	{
		this.moduleProvider = moduleProvider;
	}

	public INaturalModule parse(NaturalFile file, TokenList tokens)
	{
		var moduleProviderToUse = moduleProvider;
		if (moduleProviderToUse == null)
		{
			moduleProviderToUse = new DefaultModuleProvider(file);
		}
		return parseModule(file, moduleProviderToUse, tokens);
	}

	private INaturalModule parseModule(NaturalFile file, IModuleProvider moduleProvider, TokenList tokens)
	{
		var moduleBuilder = new NaturalModuleBuilder(file)
			.addDiagnostics(tokens.diagnostics())
			.setComments(tokens.comments())
			.setTokens(tokens.allTokens());
		var topLevelNodes = new ArrayList<ISyntaxNode>();

		var sourceHeader = tokens.sourceHeader();
		moduleBuilder.setHeader(sourceHeader);
		if (sourceHeader != null && sourceHeader.isReportingMode())
		{
			moduleBuilder.addDiagnostic(ParserErrors.unsupportedProgrammingMode(sourceHeader.getProgrammingMode(), file.getPath()));
			// REPORTING mode is not supported. If we can't deduce the mode, assume STRUCTURED.
			return moduleBuilder.build();
		}

		VariableNode functionReturnVariable = null;
		if (file.getFiletype() == NaturalFileType.FUNCTION) // skip over DEFINE FUNCTION
		{
			functionReturnVariable = consumeDefineFunction(tokens, moduleBuilder);
		}

		// Try to advance to DEFINE DATA.
		// If the module contains a DEFINE DATA, the TokenLists offset will be set to the start of DEFINE DATA.
		// This was introduced to temporarily skip over INCLUDE and OPTION before DEFINE DATA
		if (advanceToDefineData(tokens))
		{
			topLevelNodes.add(parseDefineData(tokens, moduleProvider, moduleBuilder));
			if (file.getFiletype() == NaturalFileType.FUNCTION && moduleBuilder.getDefineData() != null && functionReturnVariable != null)
			{
				var defineData = (DefineDataNode) moduleBuilder.getDefineData();
				defineData.addVariable(functionReturnVariable);
				moduleBuilder.addReferencableNodes(List.of(functionReturnVariable));
			}
		}

		if (file.getFiletype().canHaveBody())
		{
			var bodyParseResult = parseBody(tokens, moduleProvider, moduleBuilder);
			topLevelNodes.add(bodyParseResult.body());
			if (moduleBuilder.fileType() != NaturalFileType.COPYCODE)
			{
				// Copycodes will be analyzed in context of their including module.
				// Analyzing them doesn't make sense, because we can't check parameter
				// types etc.
				ExternalParameterCheck.performParameterCheck(moduleBuilder, bodyParseResult.moduleRefs());
			}
		}

		moduleBuilder.setSyntaxTree(SyntaxTree.create(ReadOnlyList.from(topLevelNodes)));

		return moduleBuilder.build();
	}

	private boolean advanceToDefineData(TokenList tokens)
	{
		SyntaxToken current;
		SyntaxToken next;
		for (var offset = 0; offset < tokens.size(); offset++)
		{
			current = tokens.peek(offset);
			next = tokens.peek(offset + 1);
			if (current != null && next != null && current.kind() == SyntaxKind.DEFINE && next.kind() == SyntaxKind.DATA)
			{
				tokens.advanceBy(offset);
				return true;
			}
		}

		return false;
	}

	private VariableNode consumeDefineFunction(TokenList tokens, NaturalModuleBuilder moduleBuilder)
	{
		VariableNode functionReturnVariable = null;
		while (!tokens.isAtEnd())
		{
			if (tokens.peek().kind() == SyntaxKind.DEFINE && tokens.peek(1).kind() == SyntaxKind.DATA)
			{
				break;
			}

			if (tokens.peek(1).kind() == SyntaxKind.RETURNS)
			{
				var functionName = tokens.advance();
				moduleBuilder.setFunctionName(functionName);
				functionReturnVariable = new VariableNode();
				functionReturnVariable.setLevel(1);
				functionReturnVariable.setScope(VariableScope.LOCAL);
				functionReturnVariable.setDeclaration(new TokenNode(functionName));

				tokens.advance(); // RETURNS
				if (tokens.peek().kind() == SyntaxKind.LPAREN)
				{
					tokens.advance(); // (
					var typeTokenSource = tokens.advance().source();
					if (tokens.peek().kind() == SyntaxKind.COMMA || tokens.peek().kind() == SyntaxKind.DOT)
					{
						typeTokenSource += tokens.advance().source(); // decimal
						typeTokenSource += tokens.advance().source(); // next number
					}
					var type = DataType.fromString(typeTokenSource);
					var typedReturnVariable = new TypedVariableNode(functionReturnVariable);

					if (typeTokenSource.contains("/") || tokens.peek().kind() == SyntaxKind.SLASH)
					{
						var firstDimension = new ArrayDimension();
						// Parsing array dimensions is currently too tightly coupled into DefineDataParser,
						// so we do a rudimentary implementation to revisit later.
						firstDimension.setLowerBound(IArrayDimension.UNBOUND_VALUE);
						firstDimension.setUpperBound(IArrayDimension.UNBOUND_VALUE);
						typedReturnVariable.addDimension(firstDimension);
						while (tokens.peek().kind() != SyntaxKind.RPAREN && !tokens.isAtEnd())
						{
							if (tokens.peek().kind() == SyntaxKind.COMMA)
							{
								var nextDimension = new ArrayDimension();
								nextDimension.setLowerBound(IArrayDimension.UNBOUND_VALUE);
								nextDimension.setUpperBound(IArrayDimension.UNBOUND_VALUE);
								typedReturnVariable.addDimension(nextDimension);
							}
							tokens.advance();
						}
					}

					tokens.advance(); // )
					if (tokens.peek().kind() == SyntaxKind.DYNAMIC)
					{
						type = DataType.ofDynamicLength(type.format());
					}
					moduleBuilder.setReturnType(type);
					typedReturnVariable.setType(new VariableType(type));
					functionReturnVariable = typedReturnVariable;
				}
				advanceToDefineData(tokens);
				break;
			}

			tokens.advance();
		}

		return functionReturnVariable;
	}

	private IDefineData parseDefineData(TokenList tokens, IModuleProvider moduleProvider, NaturalModuleBuilder moduleBuilder)
	{

		var defineDataParser = new DefineDataParser(moduleProvider);
		var result = defineDataParser.parse(tokens);
		moduleBuilder.addDiagnostics(result.diagnostics());
		var defineData = result.result();
		if (defineData != null)
		{
			moduleBuilder.setDefineData(defineData);
			moduleBuilder.addReferencableNodes(defineData.variables().stream().map(n -> (IReferencableNode) n).toList());
		}

		return defineData;
	}

	private BodyParseResult parseBody(TokenList tokens, IModuleProvider moduleProvider, NaturalModuleBuilder moduleBuilder)
	{
		var statementParser = new StatementListParser(moduleProvider);
		var result = statementParser.parse(tokens);
		moduleBuilder.addReferencableNodes(statementParser.getReferencableNodes());
		addRelevantParserDiagnostics(moduleBuilder, result);
		moduleBuilder.setBody(result.result());
		resolveVariableReferences(statementParser, moduleBuilder);

		if (moduleBuilder.getDefineData() != null)
		{
			var typer = new TypeChecker();
			for (var diagnostic : typer.check(moduleBuilder.getDefineData()))
			{
				moduleBuilder.addDiagnostic(diagnostic);
			}
		}

		var theBody = moduleBuilder.body();
		if (theBody != null && moduleBuilder.fileType() != NaturalFileType.COPYCODE)
		{
			var endStatementFound = false;
			for (var statement : theBody.statements())
			{
				if (endStatementFound)
				{
					reportNoSourceCodeAfterEndStatementAllowed(moduleBuilder, statement);
					break;
				}
				endStatementFound = statement instanceof IEndNode;
			}
			if (!endStatementFound && theBody.statements().hasItems())
			{
				reportEndStatementMissing(moduleBuilder, theBody.statements().last());
			}

			var typer = new TypeChecker();
			for (var diagnostic : typer.check(theBody))
			{
				moduleBuilder.addDiagnostic(diagnostic);
			}
		}

		return new BodyParseResult(result.result(), statementParser.moduleReferencingNodes());
	}

	private void addRelevantParserDiagnostics(NaturalModuleBuilder moduleBuilder, ParseResult<IStatementListNode> result)
	{
		for (var diagnostic : result.diagnostics())
		{
			if (diagnostic.id().equals(ParserError.UNRESOLVED_MODULE.id()))
			{
				if (moduleBuilder.isTestCase() && diagnostic.message().contains("module TEARDOWN") || diagnostic.message().contains("module SETUP"))
				{
					// Skip these unresolved subroutines.
					// These are special cases for NatUnit, because it doesn't force you to implement them.
					// It however calls them if they're present.
					continue;
				}
			}

			if (moduleBuilder.fileType() == NaturalFileType.COPYCODE)
			{
				if (ParserError.isUnresolvedError(diagnostic.id()))
				{
					// When parsing a copycode we don't want to report any unresolved references, because we simply don't know
					// if they are declared where the copycode is used.
					// They do however get reported in the module including the copycode.
					continue;
				}
			}

			moduleBuilder.addDiagnostic(diagnostic);
		}
	}

	private void resolveVariableReferences(StatementListParser statementParser, NaturalModuleBuilder moduleBuilder)
	{
		// This could actually be done in the StatementListParser when encountering
		// a possible reference. But that would need changes in the architecture, since
		// it does not know about declared variables.

		var defineData = moduleBuilder.getDefineData();
		if (defineData == null)
		{
			return;
		}

		var unresolvedAdabasArrayAccess = new ArrayList<ISymbolReferenceNode>();
		for (var unresolvedReference : statementParser.unresolvedSymbols())
		{
			if (unresolvedReference.parent() instanceof IAdabasIndexAccess)
			{
				unresolvedAdabasArrayAccess.add(unresolvedReference); // needs to be re-evaluated after, because it's parents need to be resolved
				continue;
			}

			if (unresolvedReference.referencingToken().symbolName().startsWith("&")
				|| (unresolvedReference.referencingToken().symbolName().contains(".")
					&& unresolvedReference.referencingToken().symbolName().split("\\.")[1].startsWith("&")))
			{
				// Copycode parameter
				continue;
			}

			if (tryFindAndReference(unresolvedReference.token().symbolName(), unresolvedReference, defineData, moduleBuilder))
			{
				continue;
			}

			if (unresolvedReference.token().symbolName().startsWith("+")
				&& tryFindAndReference(unresolvedReference.token().symbolName().substring(1), unresolvedReference, defineData, moduleBuilder))
			{
				// TODO(hack, expressions): This should be handled when parsing expressions.
				continue;
			}

			if (unresolvedReference.token().symbolName().startsWith("C*")
				&& tryFindAndReference(unresolvedReference.token().symbolName().substring(2), unresolvedReference, defineData, moduleBuilder))
			{
				continue;
			}

			if (unresolvedReference.token().symbolName().startsWith("T*")
				&& tryFindAndReference(unresolvedReference.token().symbolName().substring(2), unresolvedReference, defineData, moduleBuilder))
			{
				// TODO(hack, write-statement): This will be obsolete when the WRITE statement is parsed
				continue;
			}

			if (unresolvedReference.token().symbolName().startsWith("P*")
				&& tryFindAndReference(unresolvedReference.token().symbolName().substring(2), unresolvedReference, defineData, moduleBuilder))
			{
				// TODO(hack, write-statement): This will be obsolete when the WRITE statement is parsed
				continue;
			}

			if (moduleBuilder.file().getFiletype() == NaturalFileType.FUNCTION
				&& unresolvedReference.referencingToken().symbolName().equals(moduleBuilder.name()))
			{
				continue;
			}

			if (unresolvedReference.token().kind() == SyntaxKind.IDENTIFIER)
			{
				reportUnresolvedReference(moduleBuilder, unresolvedReference);
			}
		}

		for (var unresolvedReference : unresolvedAdabasArrayAccess)
		{
			if (unresolvedReference.parent()instanceof IAdabasIndexAccess adabasIndexAccess
				&& adabasIndexAccess.parent()instanceof IVariableReferenceNode arrayRef
				&& arrayRef.reference()instanceof IVariableNode resolvedArray
				&& !resolvedArray.isInView())
			{
				var diagnostic = ParserErrors.variableQualificationNotAllowedHere(
					"Variable qualification is not allowed when not referring to a database array",
					adabasIndexAccess.diagnosticPosition()
				);

				if (!diagnostic.filePath().equals(moduleBuilder.file().getPath()))
				{
					diagnostic = diagnostic.relocate(unresolvedReference.diagnosticPosition());
				}
				moduleBuilder.addDiagnostic(diagnostic);
			}
			else
			{
				if (!tryFindAndReference(unresolvedReference.token().symbolName(), unresolvedReference, defineData, moduleBuilder))
				{
					reportUnresolvedReference(moduleBuilder, unresolvedReference);
				}
			}
		}
	}

	private void reportUnresolvedReference(NaturalModuleBuilder moduleBuilder, ISymbolReferenceNode unresolvedReference)
	{
		var diagnostic = ParserErrors.unresolvedReference(unresolvedReference);
		if (!diagnostic.filePath().equals(moduleBuilder.file().getPath()))
		{
			diagnostic = diagnostic.relocate(unresolvedReference.diagnosticPosition());
		}
		moduleBuilder.addDiagnostic(diagnostic);
	}

	private void reportNoSourceCodeAfterEndStatementAllowed(NaturalModuleBuilder moduleBuilder, IStatementNode statement)
	{
		var diagnostic = ParserErrors.noSourceCodeAllowedAfterEnd(statement);
		moduleBuilder.addDiagnostic(diagnostic);
	}

	private void reportEndStatementMissing(NaturalModuleBuilder moduleBuilder, IStatementNode statement)
	{
		var diagnostic = ParserErrors.endStatementMissing(statement);
		moduleBuilder.addDiagnostic(diagnostic);
	}

	private boolean tryFindAndReference(String symbolName, ISymbolReferenceNode referenceNode, IDefineData defineData, NaturalModuleBuilder moduleBuilder)
	{
		var foundVariables = ((DefineDataNode) defineData).findVariablesWithName(symbolName);

		if (foundVariables.size() > 1)
		{
			var possibleQualifications = new StringBuilder();
			for (var foundVariable : foundVariables)
			{
				possibleQualifications.append(foundVariable.qualifiedName()).append(" ");
			}

			if (defineData.findDdmField(symbolName) != null) // TODO: Currently only necessary here because we don't parse FIND or READ yet
			{
				return true;
			}

			if (areAllInAView(foundVariables) && tryFindAndReferenceInViewAccessibleByCurrentAdabasStatementNesting(referenceNode))
			{
				return true;
			}

			moduleBuilder.addDiagnostic(ParserErrors.ambiguousSymbolReference(referenceNode, possibleQualifications.toString()));
		}

		if (!foundVariables.isEmpty())
		{
			foundVariables.get(0).addReference(referenceNode);
			return true;
		}

		return defineData.findDdmField(symbolName) != null;
	}

	private boolean tryFindAndReferenceInViewAccessibleByCurrentAdabasStatementNesting(ISymbolReferenceNode referenceNode)
	{
		var adabasViewInAccess = getAdabasViewsInAccessAtNodePosition(referenceNode);
		if (adabasViewInAccess.isEmpty())
		{
			return false;
		}

		var variableCandidatesInViews = new ArrayList<IVariableNode>();
		for (var viewInAccess : adabasViewInAccess)
		{
			var maybeDeclaredVariable = viewInAccess.findVariable(referenceNode.referencingToken().symbolName());
			if (maybeDeclaredVariable != null)
			{
				variableCandidatesInViews.add(maybeDeclaredVariable);
			}
		}

		if (variableCandidatesInViews.size() == 1)
		{
			variableCandidatesInViews.get(0).addReference(referenceNode);
			return true;
		}

		return false;
	}

	private List<IViewNode> getAdabasViewsInAccessAtNodePosition(ISyntaxNode node)
	{
		var views = new ArrayList<IViewNode>();
		while (node.parent() != null)
		{
			var parent = node.parent();
			if (parent instanceof IAdabasAccessStatementNode adabasAccess)
			{
				views.add((IViewNode) adabasAccess.view().reference());
			}
			node = parent;
		}
		return views;
	}

	private boolean areAllInAView(List<IVariableNode> variables)
	{
		for (var foundVariable : variables)
		{
			if (!foundVariable.isInView())
			{
				return false;
			}
		}

		return true;
	}

	private record BodyParseResult(IStatementListNode body, List<IModuleReferencingNode> moduleRefs)
	{}
}
