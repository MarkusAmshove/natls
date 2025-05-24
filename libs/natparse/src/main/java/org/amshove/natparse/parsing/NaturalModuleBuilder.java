package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.project.NaturalHeader;

import java.util.ArrayList;
import java.util.List;

public class NaturalModuleBuilder
{
	private final List<IDiagnostic> diagnostics = new ArrayList<>();
	private final List<IReferencableNode> referencableNodes = new ArrayList<>();
	private final NaturalFile correspondingFile;
	private ReadOnlyList<SyntaxToken> comments;
	private IDefineData defineData;
	private ReadOnlyList<SyntaxToken> tokens;
	private NaturalHeader sourceHeader;
	private SyntaxToken functionName;
	private DataType returnType;
	private IStatementListNode body;
	private ISyntaxTree syntaxTree;

	public NaturalModuleBuilder(NaturalFile file)
	{
		correspondingFile = file;
	}

	public INaturalModule build()
	{
		var theModule = create();

		theModule.setHeader(sourceHeader);
		theModule.setDefineData(defineData);
		theModule.setComments(comments);
		theModule.setTokens(tokens);
		theModule.setBody(body);
		theModule.setSyntaxTree(syntaxTree);
		theModule.addReferencableNodes(referencableNodes);
		theModule.setFunctionName(functionName);
		theModule.setReturnType(returnType);
		theModule.addDiagnostics(diagnostics);

		return (INaturalModule) theModule;
	}

	public NaturalModuleBuilder setSyntaxTree(ISyntaxTree tree)
	{
		syntaxTree = tree;
		return this;
	}

	NaturalFileType fileType()
	{
		return correspondingFile.getFiletype();
	}

	boolean isTestCase()
	{
		return fileType() == NaturalFileType.SUBPROGRAM
			&& (correspondingFile.getReferableName().startsWith("TC") || correspondingFile.getReferableName().startsWith("TS"));
	}

	IDefineData getDefineData()
	{
		return defineData;
	}

	IStatementListNode body()
	{
		return body;
	}

	NaturalFile file()
	{
		return correspondingFile;
	}

	String name()
	{
		if (functionName != null)
		{
			return functionName.symbolName();
		}
		return correspondingFile.getReferableName();
	}

	public NaturalModuleBuilder setComments(ReadOnlyList<SyntaxToken> comments)
	{
		this.comments = comments;
		return this;
	}

	public NaturalModuleBuilder setHeader(NaturalHeader header)
	{
		this.sourceHeader = header;
		return this;
	}

	public NaturalModuleBuilder setTokens(ReadOnlyList<SyntaxToken> tokens)
	{
		this.tokens = tokens;
		return this;
	}

	public NaturalModuleBuilder addDiagnostic(IDiagnostic diagnostic)
	{
		diagnostics.add(diagnostic);
		return this;
	}

	public NaturalModuleBuilder addDiagnostics(ReadOnlyList<IDiagnostic> diagnostics)
	{
		for (var diagnostic : diagnostics)
		{
			this.diagnostics.add(diagnostic);
		}
		return this;
	}

	public NaturalModuleBuilder addReferencableNodes(List<IReferencableNode> referencableNodes)
	{
		this.referencableNodes.addAll(referencableNodes);
		return this;
	}

	public NaturalModuleBuilder setReturnType(DataType type)
	{
		returnType = type;
		return this;
	}

	public NaturalModuleBuilder setFunctionName(SyntaxToken token)
	{
		this.functionName = token;
		return this;
	}

	public NaturalModuleBuilder addDiagnostics(List<IDiagnostic> diagnostics)
	{
		this.diagnostics.addAll(diagnostics);
		return this;
	}

	public NaturalModuleBuilder setDefineData(IDefineData defineData)
	{
		this.defineData = defineData;
		return this;
	}

	public NaturalModuleBuilder setBody(IStatementListNode body)
	{
		this.body = body;
		return this;
	}

	private NaturalModule create()
	{
		return switch (correspondingFile.getFiletype())
		{
			case DDM -> throw new IllegalStateException("Tried to create a DDM through NaturalModuleBuilder");
			case SUBPROGRAM -> new Subprogram(correspondingFile);
			case PROGRAM -> new Program(correspondingFile);
			case SUBROUTINE -> new ExternalSubroutine(correspondingFile);
			case HELPROUTINE -> new Helproutine(correspondingFile);
			case GDA -> new GlobalDataArea(correspondingFile);
			case LDA -> new LocalDataArea(correspondingFile);
			case PDA -> new ParameterDataArea(correspondingFile);
			case MAP -> new NaturalMap(correspondingFile);
			case COPYCODE -> new CopyCode(correspondingFile);
			case FUNCTION -> new Function(correspondingFile);
		};
	}
}
