package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalHeader;
import org.amshove.natparse.natural.project.NaturalProgrammingMode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class NaturalModule
{
	protected final NaturalFile file;
	private IDefineData defineData;
	private final List<IDiagnostic> diagnostics = new ArrayList<>();
	private final List<IModuleReferencingNode> callers = new ArrayList<>();
	private final List<IReferencableNode> referencableNodes = new ArrayList<>();
	private IStatementListNode body;
	private ISyntaxTree tree;
	private ReadOnlyList<SyntaxToken> comments;
	private ReadOnlyList<SyntaxToken> tokens;
	private NaturalHeader sourceHeader;
	private IDataType returnType;
	private SyntaxToken functionName;

	public NaturalModule(NaturalFile file)
	{
		this.file = file;
	}

	void setHeader(NaturalHeader header)
	{
		sourceHeader = header;
	}

	public String name()
	{
		if (functionName != null)
		{
			return functionName.symbolName();
		}
		return file.getReferableName();
	}

	public NaturalFile file()
	{
		return file;
	}

	public ReadOnlyList<IDiagnostic> diagnostics()
	{
		return ReadOnlyList.from(diagnostics);
	}

	public ReadOnlyList<IModuleReferencingNode> callers()
	{
		return ReadOnlyList.from(callers);
	}

	public ReadOnlyList<SyntaxToken> tokens()
	{
		return tokens;
	}

	public ISyntaxTree syntaxTree()
	{
		return tree;
	}

	public IDefineData defineData()
	{
		return defineData;
	}

	// TODO(cyclic-dependencies): temporary?
	public void setDefineData(IDefineData defineData)
	{
		this.defineData = defineData;
	}

	void addDiagnostics(List<IDiagnostic> diagnostics)
	{
		this.diagnostics.addAll(diagnostics);
	}

	public IStatementListNode body()
	{
		return body;
	}

	void setBody(IStatementListNode body)
	{
		this.body = body;
	}

	public void removeCaller(IModuleReferencingNode callerNode)
	{
		callers.remove(callerNode);
	}

	public void addCaller(IModuleReferencingNode caller)
	{
		callers.add(caller);
	}

	void setTokens(ReadOnlyList<SyntaxToken> tokens)
	{
		this.tokens = tokens;
	}

	void setSyntaxTree(ISyntaxTree tree)
	{
		this.tree = tree;
	}

	void addReferencableNodes(List<IReferencableNode> nodes)
	{
		referencableNodes.addAll(nodes);
	}

	public void setComments(ReadOnlyList<SyntaxToken> comments)
	{
		this.comments = comments;
	}

	public ReadOnlyList<SyntaxToken> comments()
	{
		return comments;
	}

	public ReadOnlyList<IReferencableNode> referencableNodes()
	{
		return ReadOnlyList.from(referencableNodes);
	}

	public String moduleDocumentation()
	{
		if (comments == null || comments.isEmpty())
		{
			return "";
		}

		var firstLineOfCode = syntaxTree() != null
			? syntaxTree().descendants().first().diagnosticPosition().line()
			: defineData.descendants().first().diagnosticPosition().line();

		return comments.stream()
			.takeWhile(t -> t.line() < firstLineOfCode)
			.map(SyntaxToken::source)
			.filter(l -> !l.startsWith("* >") && !l.startsWith("* <") && !l.startsWith("* :"))
			.filter(l -> !l.trim().endsWith("*"))
			.collect(Collectors.joining(System.lineSeparator()));
	}

	public String extractLineComment(int line)
	{
		if (comments == null)
		{
			return "";
		}

		return comments.stream()
			.filter(t -> t.line() == line)
			.map(SyntaxToken::source)
			.findFirst()
			.orElse("");
	}

	public NaturalHeader header()
	{
		return sourceHeader;
	}

	public NaturalProgrammingMode programmingMode()
	{
		return sourceHeader != null ? sourceHeader.getProgrammingMode() : NaturalProgrammingMode.UNKNOWN;
	}

	@Nullable
	public IDataType returnType()
	{
		return returnType;
	}

	void setReturnType(IDataType type)
	{
		returnType = type;
	}

	void setFunctionName(SyntaxToken name)
	{
		this.functionName = name;
	}

	@Nullable
	public SyntaxToken functionName()
	{
		return functionName;
	}
}
