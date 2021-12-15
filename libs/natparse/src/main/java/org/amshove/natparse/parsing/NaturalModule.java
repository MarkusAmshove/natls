package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;

import java.util.ArrayList;
import java.util.List;

public class NaturalModule
	// TODO: Clean up once new subclasses happen. Remove public then
	implements INaturalModule, IExternalSubroutine, IGlobalDataArea, ILocalDataArea, IParameterDataArea, IProgram, ISubprogram
{
	private final NaturalFile file;
	private IDefineData defineData;
	private final List<IDiagnostic> diagnostics = new ArrayList<>();

	NaturalModule(NaturalFile file)
	{
		this.file = file;
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
	public IDefineData defineData()
	{
		return defineData;
	}

	void setDefineData(IDefineData defineData)
	{
		this.defineData = defineData;
	}

	void addDiagnostics(ReadOnlyList<IDiagnostic> diagnostics)
	{
		for (var diagnostic : diagnostics)
		{
			this.diagnostics.add(diagnostic);
		}
	}
}
