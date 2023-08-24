package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.project.NaturalHeader;
import org.amshove.natparse.natural.project.NaturalProgrammingMode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NaturalModule
	// TODO: Clean up once new subclasses happen. Remove public then
	implements IExternalSubroutine, IGlobalDataArea, ILocalDataArea, IParameterDataArea, IProgram, ISubprogram, IFunction
{
	private final NaturalFile file;
	private IDefineData defineData;
	private final List<IDiagnostic> diagnostics = new ArrayList<>();
	private final List<IModuleReferencingNode> callers = new ArrayList<>();
	private final List<IReferencableNode> referencableNodes = new ArrayList<>();
	private IStatementListNode body;
	private ISyntaxTree tree;
	private ReadOnlyList<SyntaxToken> comments;
	private NaturalHeader sourceHeader;
	private IDataType returnType;

	public NaturalModule(NaturalFile file)
	{
		this.file = file;
	}

	void setHeader(NaturalHeader header)
	{
		sourceHeader = header;
	}

	@Override
	public String name()
	{
		return file.getReferableName();
	}

	@Override
	public NaturalFile file()
	{
		return file;
	}

	@Override
	public ReadOnlyList<IDiagnostic> diagnostics()
	{
		return ReadOnlyList.from(diagnostics);
	}

	@Override
	public ReadOnlyList<IModuleReferencingNode> callers()
	{
		return ReadOnlyList.from(callers);
	}

	@Override
	public boolean isTestCase()
	{
		return file.getFiletype() == NaturalFileType.SUBPROGRAM &&
			(file.getReferableName().startsWith("TC") || file.getReferableName().startsWith("TS"));
	}

	@Override
	public ISyntaxTree syntaxTree()
	{
		return tree;
	}

	@Override
	public IDefineData defineData()
	{
		return defineData;
	}

	// TODO(cyclic-dependencies): temporary?
	public void setDefineData(IDefineData defineData)
	{
		this.defineData = defineData;
	}

	void addDiagnostic(IDiagnostic diagnostic)
	{
		this.diagnostics.add(diagnostic);
	}

	void addDiagnostics(ReadOnlyList<IDiagnostic> diagnostics)
	{
		for (var diagnostic : diagnostics)
		{
			addDiagnostic(diagnostic);
		}
	}

	void addReference(IModuleReferencingNode referencingNode)
	{
		callers.add(referencingNode);
	}

	@Override
	public IStatementListNode body()
	{
		return body;
	}

	void setBody(IStatementListNode body)
	{
		this.body = body;
	}

	@Override
	public void removeCaller(IModuleReferencingNode callerNode)
	{
		callers.remove(callerNode);
	}

	@Override
	public void addCaller(IModuleReferencingNode caller)
	{
		callers.add(caller);
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

	@Override
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

	@Override
	public NaturalHeader header()
	{
		return sourceHeader;
	}

	@Override
	public NaturalProgrammingMode programmingMode()
	{
		return sourceHeader != null ? sourceHeader.getProgrammingMode() : NaturalProgrammingMode.UNKNOWN;
	}

	@Nullable
	@Override
	public IDataType returnType()
	{
		return returnType;
	}

	void setReturnType(IDataType type)
	{
		returnType = type;
	}
}
